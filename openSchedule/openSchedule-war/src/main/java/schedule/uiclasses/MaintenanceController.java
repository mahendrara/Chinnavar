package schedule.uiclasses;

import eventlog.EventLogBean;
import eventlog.EventRecord;
import eventlog.TimerInfo;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import schedule.uiclasses.util.JsfUtil;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.ejb.TimerService;
import javax.ejb.Timer;

import schedule.entities.Schedule;
import schedule.entities.ScheduledDay;
import schedule.entities.ScheduledDuty;
import schedule.entities.ScheduledService;
import schedule.messages.MaintenanceItem;
import schedule.messages.XmlMessageSender;
import schedule.sessions.ScheduledDutyFacade;
import schedule.uiclasses.util.UiText;
import schedule.uiclasses.util.config.ConfigBean;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

// This does NOT inherit BaseController, as this dialog does not access to
// any entity
@Named("maintenanceController")
@SessionScoped
public class MaintenanceController implements Serializable {

    @Inject
    private UiText uiText;
    @Inject
    private ScheduleCreatorBean scheduleCreatorBean;
    @Inject
    private XmlMessageSender xmlMessageSender;
    @Inject
    private schedule.sessions.ScheduledServiceFacade ejbScheduledServiceFacade;
    @Inject
    private schedule.sessions.ScheduledTripFacade ejbScheduledTripFacade;
    @Inject
    private ScheduledDutyFacade ejbScheduledDutyFacade;

    @Inject
    private schedule.sessions.ScheduledDayFacade ejbScheduledDayFacade;
    
    @Inject
    private EventLogBean eventLog;
   
 
    @Resource
    protected UserTransaction userTransaction;
    @PersistenceContext(unitName = "openSchedule-warPU")
    protected EntityManager entityManager;

    private Date startTime;
    //user might set different values for these two fields.
    private Date endTime1;
    private Date endTime2;
    private boolean autoDeleteScheduledServices;
    private int autoDeleteDays;

    public MaintenanceController() {
    }

    @PostConstruct
    public void init() {
        autoDeleteScheduledServices = false;
        autoDeleteDays = 30;
    }

    public String prepareView() {
        return "View";
    }

    public String clearDrivingTimeStatistic() {

        eventLog.addEvent("MaintenanceController", "User command to clear statistics");
        // Send explicit positive message, negative is handled in XMLMessageSender
        if (xmlMessageSender.sendMaintenanceMsg(MaintenanceItem.Operation.CLEARDRIVINGTIMESTATISTIC)) {
            JsfUtil.addSuccessMessage(uiText.get("DrivingTimeStatisticCleared"));
        }

        return null;
    }

    public String saveDrivingTimeStatistic() {
        
        // Send explicit positive message, negative is handled in XMLMessageSender
        if (xmlMessageSender.sendMaintenanceMsg(MaintenanceItem.Operation.SAVEDRIVINGTIMESTATISTIC)) {
            JsfUtil.addSuccessMessage(uiText.get("DrivingTimeStatisticSaved"));
        }

        return null;
    }

    public String loadScheduledDays() {
        
        eventLog.addEvent("MaintenanceController", "User command to load scheduled days");
        try {
            userTransaction.begin();
            entityManager.joinTransaction();
            // Force timerBean to run and execute scheduleday loading
            scheduleCreatorBean.createScheduledDay(entityManager);
            userTransaction.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            try {
                userTransaction.rollback();
            } catch (IllegalStateException | SecurityException | SystemException ex1) {
                Logger.getLogger(MaintenanceController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(MaintenanceController.class.getName()).log(Level.SEVERE, null, ex);
            JsfUtil.addErrorMessage(ex, uiText.get("PersistenceErrorOccured"));
        }catch (Exception ex) {
            Logger.getLogger(MaintenanceController.class.getName()).log(Level.SEVERE, null, ex);
            JsfUtil.addErrorMessage(ex, uiText.get("PersistenceErrorOccured"));
        }
        // Explicit positive feedback
        JsfUtil.addSuccessMessage(uiText.get("ScheduledDaysLoadedManually"));

        return null;
    }

    private boolean canScheduledServiceBeDeleted(ScheduledService ss) {
        // Loop through ScheduledService's ScheduledDays and if there are any 
        // which are archived or active, ScheduledService is not allowed to be deleted
        //if (ss.getDay() != null && (/*ss.getDay().getArchived() ||*/ ss.getDay().isActive())) {
        //    //if (ss.getDay() != null && (ss.getDay().isActive())) {
        //    return false;
        //}

        // ScheduledService can be deleted
        return true;
    }

    private void deleteScheduledServices(int startScheduledDayCode, int endScheduledDayCode) {
        try {
            userTransaction.begin();
            entityManager.joinTransaction();
            long start = System.currentTimeMillis();

            List<ScheduledDuty> duties = null;
            //option3:
            if (ConfigBean.getConfig().getPage("duty").isVisible()) {
                //this.ejbScheduledDutyFacade.deleteScheduledDutyRange(startScheduledDayCode, endScheduledDayCode);
                duties = this.ejbScheduledDutyFacade.findByDayCodes(startScheduledDayCode, endScheduledDayCode);
                //this.ejbScheduledDutyFacade.delete(duties);
                if (duties != null) {
                    Iterator<ScheduledDuty> it = duties.iterator();
                    ScheduledDuty duty;
                    while (it.hasNext()) {
                        duty = it.next();
                        duty = entityManager.merge(duty);
                        entityManager.remove(duty);
                        duty.setRemoving(true);
                    }
                }
            }
            LinkedList<ScheduledService> scheduledServices = new LinkedList<>(ejbScheduledServiceFacade.findScheduledServiceRange(startScheduledDayCode, endScheduledDayCode));

            Iterator<ScheduledService> iterator = scheduledServices.iterator();
            Map<Integer, ScheduledDay> deletedScheduledDays = new HashMap<>();

            ScheduledService service = null;
            while (iterator.hasNext()) {
                service = iterator.next();
                //if (service.getDay().getScheduledDayCode() <= endScheduledDayCode && service.getDay().getScheduledDayCode() >= startScheduledDayCode) {
                if (canScheduledServiceBeDeleted(service)) {
                    //ejbScheduledServiceFacade.remove(service);
                    service = entityManager.merge(service);
                    entityManager.remove(service);
                    service.setRemoving(true);
                    
                    deletedScheduledDays.put(service.getDay().getScheduledDayCode(), service.getDay());
                } else {
                    service.setValid(false);
                    service.validateTimedTrips(false);
                    service.setEditing(true);
                    entityManager.merge(service);
                    //ejbScheduledServiceFacade.edit(service);
                }

                //}
            }

            userTransaction.commit();

            ScheduleCreatorBean.logger.log(Level.SEVERE, String.format("MaintenanceController.deleteScheduledServices: time in milliseconds " + (System.currentTimeMillis() - start)));
            if (!scheduledServices.isEmpty()) {
                xmlMessageSender.sendServiceDutyChangeMsg(scheduledServices, duties);
            }
            

            ejbScheduledDayFacade.evictAll();
            ejbScheduledServiceFacade.evictAll();
            ejbScheduledTripFacade.evictAll();
            ejbScheduledDutyFacade.evictAll();
            JsfUtil.addSuccessMessage(uiText.get("ViewMaintenanceServicesDeleted"));
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            try {
                userTransaction.rollback();
            } catch (IllegalStateException | SecurityException | SystemException ex1) {
                Logger.getLogger(MaintenanceController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(MaintenanceController.class.getName()).log(Level.SEVERE, null, ex);
            JsfUtil.addErrorMessage(ex, uiText.get("PersistenceErrorOccured"));
        }

    }

    public void deleteScheduledServices1() {
        int endScheduledDayCode = 0;

        if (endTime1 != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(endTime1);
            endScheduledDayCode = Schedule.convertToDayCode(calendar);
        } else {
            JsfUtil.addErrorMessage(uiText.get("RequiredMessage_enddate"));
            return;
        }
        deleteScheduledServices(0, endScheduledDayCode);
    }

    public void deleteScheduledServices2() {
        int startScheduledDayCode = 0;
        int endScheduledDayCode = 0;

        if (startTime != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startTime);
            startScheduledDayCode = Schedule.convertToDayCode(calendar);
        } else {
            JsfUtil.addErrorMessage(uiText.get("RequiredMessage_startdate"));
            return;
        }

        if (endTime2 != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(endTime2);
            endScheduledDayCode = Schedule.convertToDayCode(calendar);
        } else {
            JsfUtil.addErrorMessage(uiText.get("RequiredMessage_enddate"));
            return;
        }

        if (startScheduledDayCode > endScheduledDayCode) {
            JsfUtil.addErrorMessage(uiText.get("Error_invalidTimeSpan"));
            return;
        } else {
            deleteScheduledServices(startScheduledDayCode, endScheduledDayCode);
        }
    }

    public void deleteScheduledServices3() {
        int endScheduledDayCode = 0;
        Calendar calendar = Calendar.getInstance();
        endScheduledDayCode = Schedule.convertToDayCode(calendar) - this.autoDeleteDays;

        deleteScheduledServices(0, endScheduledDayCode);
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime1() {
        return endTime1;
    }

    public void setEndTime1(Date endTime1) {
        this.endTime1 = endTime1;
    }

    public Date getEndTime2() {
        return endTime2;
    }

    public void setEndTime2(Date endTime2) {
        this.endTime2 = endTime2;
    }

    public boolean isAutoDeleteScheduledServices() {
        return autoDeleteScheduledServices;
    }

    public void setAutoDeleteScheduledServices(boolean autoDeleteScheduledServices) {
        this.autoDeleteScheduledServices = autoDeleteScheduledServices;
    }

    public int getAutoDeleteDays() {
        return autoDeleteDays;
    }

    public void setAutoDeleteDays(int autoDeleteDays) {
        this.autoDeleteDays = autoDeleteDays;
    }
    public DataModel<EventRecord>  getScheduleEvents() {
        
        List<EventRecord> targetList = new ArrayList<EventRecord>();
        eventLog.getEvents(targetList, "");
         
        DataModel<EventRecord> currentSnapShot = new ListDataModel<EventRecord>(targetList);
        
        return currentSnapShot;        
    }
     public DataModel<TimerInfo> getTimers() {
        
        List<TimerInfo> timerList = eventLog.getEJBTimers();
         
        DataModel<TimerInfo> currentSnapShot = new ListDataModel<TimerInfo>(timerList);
        
        return currentSnapShot;        
    }
}

