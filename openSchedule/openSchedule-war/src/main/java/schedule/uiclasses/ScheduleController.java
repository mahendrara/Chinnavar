package schedule.uiclasses;

import schedule.entities.*;
import schedule.sessions.*;
import schedule.uiclasses.util.JsfUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
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

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.time.SimpleTimePeriod;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import schedule.messages.Operation;
import schedule.messages.XmlMessageSender;
import schedule.uiclasses.util.UiText;
import schedule.uiclasses.util.config.ConfigBean;

@Named("scheduleController")
@SessionScoped
public class ScheduleController extends BaseController<Schedule> implements Serializable {

    @Inject
    TripFacade ejbTripFacade;
    @Inject
    DayTypeFacade ejbDayTypeFacade;
    @Inject
    LongTermFilterController longTermFilterController;
    @Inject
    TimeBlockController timeBlockController;
    @Inject
    TimeBlockFacade timeBlockFacade;
    @Inject
    DayTypeFacade dayTypeFacade;
    @Inject
    PlannedServiceFacade ejbPlanndServiceFacade;
    @Inject
    ScheduledServiceFacade ejbScheduledServiceFacade;
    @Inject
    private PlannedServiceController plannedServiceController;
    @Inject
    private ScheduledDutyFacade ejbScheduledDutyFacade;
    @Inject
    private PlannedDutyFacade ejbPlannedDutyFacade;
    @Inject
    private PlannedTripFacade ejbPlannedTripFacade;
    @Inject
    private PlannedDutyController plannedDutyController;
    
        @Resource
    protected UserTransaction userTransaction;
    //This is important, we need to make the controler's method in a transaction for operations related to both service and duty.
    @PersistenceContext(unitName = "openSchedule-warPU")
    protected EntityManager entityManager;

    @Inject
    protected XmlMessageSender xmlMessageSender;
    @Inject
    LanguageBean languageBean;
    @Inject
    private UiText uiText;
    @Inject
    private schedule.sessions.ScheduleFacade ejbFacade;

    private StreamedContent chart = null;
    private StreamedContent ganttChart = null;
    private int addedValidSchedules = 0;

    public ScheduleController() {
        super(50); // Only 10 schedule items per page
    }

    public StreamedContent getChart(Schedule schedule) {
        //Chart
        if (schedule != null) {
            try {
                JFreeChart jfreechart = ChartFactory.createPieChart(
                        uiText.get("Schedule_PieChartTitle"),
                        createDataset(schedule),
                        true,
                        true,
                        false);
                languageBean.applyChartTheme(jfreechart);
                File chartFile = new File("daytypechart");
                ChartUtilities.saveChartAsPNG(chartFile, jfreechart, 300, 360);
                chart = new DefaultStreamedContent(new FileInputStream(chartFile), "image/png");
            } catch (Exception e) {
                //logger.severe(e.getMessage());
                System.out.println("getChart:" + e.getMessage());
            }
        }
        return chart;
    }

    public StreamedContent getSchedulesChart() {
        //Chart
        try {
            JFreeChart jfreechart = ChartFactory.createGanttChart(
                    uiText.get("Schedule_ScheduleChartTitle"),
                    uiText.get("Schedule_ScheduleChartYTitle"),
                    uiText.get("Schedule_ScheduleChartXTitle"),
                    createSchedulesDataset(),
                    true,
                    true,
                    false);
            languageBean.applyChartTheme(jfreechart);
            File chartFile = new File("schedulechart");
            ChartUtilities.saveChartAsPNG(chartFile, jfreechart, 750, this.addedValidSchedules * 50 + 100);
            ganttChart = new DefaultStreamedContent(new FileInputStream(chartFile), "image/png");
        } catch (Exception e) {
            System.out.println("getSchedulesChart:" + e.getMessage());
        }
        return ganttChart;
    }

    private IntervalCategoryDataset createSchedulesDataset() {

        this.addedValidSchedules = 0;

        TaskSeries s1 = new TaskSeries(uiText.get("ViewScheduleTitle_valid"));
        List<Schedule> all = getFacade().findAll();

        Iterator<Schedule> iteSchedules = all.iterator();

        while (iteSchedules.hasNext()) {
            Schedule cur = iteSchedules.next();
            if (cur.getValid()) {
                SimpleTimePeriod timespan = new SimpleTimePeriod(cur.getStartTime(), cur.getEndTime());
                s1.add(new Task(cur.getDescription(), timespan));
                this.addedValidSchedules++;
            }
        }
        TaskSeriesCollection collection = new TaskSeriesCollection();
        collection.add(s1);
        //collection.add(s2);

        return collection;
    }

    private PieDataset createDataset(Schedule schedule) {

        List<DayType> types = schedule.getDayTypes();

        Iterator<DayType> iteDaytypes = types.iterator();

        while (iteDaytypes.hasNext()) {
            DayType cur = iteDaytypes.next();
            cur.setHelperCounter(0);
        }
        List<DayInSchedule> days = schedule.getDayInSchedules();
        Iterator<DayInSchedule> iteDays = days.iterator();

        while (iteDays.hasNext()) {
            DayInSchedule cur = iteDays.next();
//            if (cur.getDayType() != null) {
//                cur.getDayType().incHelperCounter();
//            }
            for(DayType dt: cur.getDayTypeList()) {
                dt.incHelperCounter();
            }
        }

        DefaultPieDataset dataset = new DefaultPieDataset();

        iteDaytypes = types.iterator();

        while (iteDaytypes.hasNext()) {
            DayType cur = iteDaytypes.next();

            StringBuilder sb = new StringBuilder();
            sb.append(cur.getAbbr());
            sb.append(" (");
            sb.append(cur.getHelperCounter());
            sb.append(')');
            String legend = sb.toString();

            dataset.setValue(legend, new Double(cur.getHelperCounter()));

        }
        return dataset;
    }

    @Override
    protected ScheduleFacade getFacade() {
        return ejbFacade;
    }

    @Override
    public Schedule constructNewItem() {
        Schedule current = new Schedule();
        current.setEditing(true);
        current.setCreating(true);
        current.setLoadDayWidth(1);
        current.setValid(true);
        return current;
    }

    public String activateEditDay(Schedule schedule, DayInSchedule day) {
        day.setEditing(true);
        return null;
    }

    @Override
    public String save(Schedule schedule) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            schedule.setStartTime(dateFormat.parse(dateFormat.format(schedule.getStartTime())));
            schedule.setEndTime(dateFormat.parse(dateFormat.format(schedule.getEndTime())));
        } catch (ParseException ex) {
            Logger.getLogger(ScheduleController.class.getName()).log(Level.SEVERE, null, ex);
            JsfUtil.addErrorMessage(ex, ex.getMessage());
            return null;
        }

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        start.setTime(schedule.getStartTime());
        end.setTime(schedule.getEndTime());

        if (schedule.getStartTime().after(schedule.getEndTime())) {
            JsfUtil.addErrorMessage(uiText.get("Error_invalidTimeSpan"));
            return null;
        } else {
            if (schedule.isCreating()) {
                try {
                    schedule.setEditing(false);
                    schedule.setCreating(false);
                    getFacade().createFromTemplate(schedule, start, end);
                    xmlMessageSender.sendDayTypeMsg(schedule.getDayTypes(), Operation.CREATE);
                    JsfUtil.addSuccessMessage(uiText.get("ScheduleCreated"));

                } catch (Exception e) {
                    schedule.setEditing(true);
                    schedule.setCreating(true);
                    JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
                    return null;
                }
            } else {
                try {
                    schedule.setEditing(false);
                    getFacade().edit(schedule);
                    //recreateModel();
                    JsfUtil.addSuccessMessage(uiText.get("ScheduleUpdated"));
                } catch (Exception e) {
                    schedule.setEditing(true);
                    JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
                    return null;
                }
            }

            schedule = updateState(schedule);
            recreateModel();

            scheduleFilterChanged(schedule);
            return null;
        }
    }

    public void scheduleFilterChanged(Schedule modifiedSchedule) {
        Schedule scheduleFilter = null;

        if (modifiedSchedule != null) {
            if (modifiedSchedule.getValid()) {
                scheduleFilter = modifiedSchedule;
            } else if (plannedServiceController.getFilter().getScheduleFilter() != null
                    && (plannedServiceController.getFilter().getScheduleFilter().getScheduleId() == modifiedSchedule.getScheduleId())) {
                scheduleFilter = null;
            }
        }//else the schedule is deleted.

        if (scheduleFilter == null) {
            ScheduleFilter filter = new ScheduleFilter();
            filter.setValid(true);
            scheduleFilter = this.ejbFacade.findFirst(filter);
        }
        plannedServiceController.getFilter().setScheduleFilter(scheduleFilter);
        plannedServiceController.scheduleFilterChanged();
        longTermFilterController.setSelectedSchedule(scheduleFilter);
        longTermFilterController.selectedScheduleChanged();
        if (ConfigBean.getConfig().getPage("timeBlock").isVisible()) {
            timeBlockController.getFilter().setSchedule(scheduleFilter);
            timeBlockController.scheduleFilterChanged();
        }
        if (ConfigBean.getConfig().getPage("duty").isVisible()) {
            plannedDutyController.getFilter().setSchedule(scheduleFilter);
            plannedDutyController.scheduleFilterChanged();
        }
    }

    public String saveDay(Schedule schedule, DayInSchedule day) {
        if (day.getDayTypeList() != null && !day.getDayTypeList().isEmpty()) {
            try {
                for (int i = 0; i < schedule.getDayInSchedules().size(); i++) {

                    if (schedule.getDayInSchedules().get(i).getDayId() == day.getDayId()) {
                        //temp.setEditing(true);
                        schedule.getDayInSchedules().get(i).setDayTypeList(day.getDayTypeList());

                        schedule.getDayInSchedules().get(i).setEditing(false);
                        getFacade().edit(schedule);
                        getFacade().evict(schedule);
                        updateState(schedule);
                        recreateModel();
                        JsfUtil.addSuccessMessage(uiText.get("ScheduleUpdated"));
                        break;
                    }
                }
                //return "View";
            } catch (Exception e) {
                JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
                return null;
            }
        }

        return null;
    }

    public String cancelDay(Schedule schedule, DayInSchedule day) {
        try {
            for (int i = 0; i < schedule.getDayInSchedules().size(); i++) {
                if (schedule.getDayInSchedules().get(i).getDayId() == day.getDayId()) {
                    schedule.getDayInSchedules().get(i).setEditing(false);
                    break;
                }
            }
            recreateModel();

            //return "View";
        } catch (Exception e) {
            //JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
            return null;
        }
        return "";//"List";
    }

    @Override
    public String destroy(Schedule schedule) {
        if (!schedule.isCreating()) {
            performDestroy(schedule);
        }
        scheduleFilterChanged(null);
        /*Schedule scheduleFilter = this.plannedServiceController.getFilter().getScheduleFilter();
        if (scheduleFilter != null) {
            if (scheduleFilter.getScheduleId() == schedule.getScheduleId()) {
                longTermFilterController.setSelectedSchedule(null);
                longTermFilterController.selectedScheduleChanged();
                plannedServiceController.getFilter().setScheduleFilter(null);
                plannedServiceController.scheduleFilterChanged();
                timeBlockController.getFilter().setSchedule(null);
                timeBlockController.scheduleFilterChanged();
            }
        }
        else {
            longTermFilterController.initSchedules(scheduleFilter);
        }*/
        this.updateState(schedule);
        recreateModel();
        return "List";
    }

    /*private boolean isDeleteAllowed(Schedule schedule) {
        for (DayType dayType : schedule.getDayTypes()) {
            boolean exists = ejbTripFacade.hasSchedulesInTripsByDaytype(dayType);
            if (exists) {
                return false;
            }
        }

        return true;
    }*/
    private void performDestroy(Schedule schedule) {
        try {
            List<Integer> ids = new ArrayList<>();
            List<DayType> dts = new ArrayList<>();
            Iterator<DayType> it = schedule.getDayTypes().iterator();

            while (it.hasNext()) {
                DayType dayType = it.next();
                ids.add(dayType.getDayTypeId());
                dts.add(dayType);
            }

            if (ejbScheduledServiceFacade.isValidExistByDayType(ids)) {
                JsfUtil.addErrorMessage(uiText.get("Error_ScheduleInUse"));
            } else {
                List<ScheduledDuty> scheduledDuties = null;
                List<PlannedDuty> plannedDuties = null;
                List<TimeBlock> timeBlocks = null;
                userTransaction.begin();
                entityManager.joinTransaction();
                if (ConfigBean.getConfig().getPage("timeBlock").isVisible()) {
                    //timeBlockFacade.deleteByDayTypes(ids);
                    if (!ids.isEmpty()) {
                        //entityManager.createNamedQuery("TimeBlock.findByDayType").setParameter("a", ids).executeUpdate();
                        timeBlocks = timeBlockFacade.findByDayTypes(ids);
                        Iterator<TimeBlock> bIt = timeBlocks.iterator();
                        TimeBlock timeBlock;
                        while (bIt.hasNext()) {
                            timeBlock = bIt.next();
                            timeBlock = entityManager.merge(timeBlock);
                            entityManager.remove(timeBlock);
                        }
                    }
                }
                if (ConfigBean.getConfig().getPage("duty").isVisible()) {
                    scheduledDuties = ejbScheduledDutyFacade.findByDayTypes(ids);
                    Iterator<ScheduledDuty> it2 = scheduledDuties.iterator();
                    ScheduledDuty scheduledDuty;
                    while (it2.hasNext()) {
                        scheduledDuty = it2.next();
                        scheduledDuty.setRemoving(true);
                        scheduledDuty = entityManager.merge(scheduledDuty);
                        entityManager.remove(scheduledDuty);
                    }
                    //plannedDuties = ejbPlannedDutyFacade.findByDayTypes(ids);
                    plannedDuties = ejbPlannedDutyFacade.findByDayTypesList(dts);
                    Iterator<PlannedDuty> it3 = plannedDuties.iterator();
                    PlannedDuty plannedDuty;
                    while (it3.hasNext()) {
                        plannedDuty = it3.next();
                        plannedDuty.setRemoving(true);
                        plannedDuty = entityManager.merge(plannedDuty);
                        entityManager.remove(plannedDuty);

                    }
                    //ejbPlannedDutyFacade.delete(duties2);
                    //ejbPlannedDutyFacade.evictAll();
                }
                List<ScheduledService> scheduledServices = ejbScheduledServiceFacade.findInvalidByDayTypes(ids);
                Iterator<ScheduledService> iterator2 = scheduledServices.iterator();
                ScheduledService scheduledService;
                while (iterator2.hasNext()) {
                    scheduledService = iterator2.next();
                    scheduledService.setRemoving(true);
                    //this.ejbScheduledServiceFacade.remove(scheduledService);
                    scheduledService = entityManager.merge(scheduledService);
                    entityManager.remove(scheduledService);
                }

                List<PlannedService> plannedServices = ejbPlanndServiceFacade.findByDayTypeList(dts);
                Iterator<PlannedService> iterator = plannedServices.iterator();
                PlannedService plannedService;
                while (iterator.hasNext()) {
                    plannedService = iterator.next();
                    plannedService.setRemoving(true);
                    //this.ejbPlanndServiceFacade.remove(plannedService);
                    plannedService = entityManager.merge(plannedService);
                    entityManager.remove(plannedService);
                }
                //getFacade().remove(schedule);
                schedule = entityManager.merge(schedule);
                entityManager.remove(schedule);
                userTransaction.commit();
                scheduleFilterChanged(null);

                if (timeBlocks != null && !timeBlocks.isEmpty()) {
                    xmlMessageSender.sendTimeBlockMsg(timeBlocks, Operation.DELETE);
                }
                if (!plannedServices.isEmpty() || (plannedDuties != null && !plannedDuties.isEmpty())) {
                    xmlMessageSender.sendServiceDutyChangeMsg(plannedServices, plannedDuties);
                }
                if (!scheduledServices.isEmpty() || (scheduledDuties != null && !scheduledDuties.isEmpty())) {
                    xmlMessageSender.sendServiceDutyChangeMsg(scheduledServices, scheduledDuties);
                }

                xmlMessageSender.sendDayTypeMsg(schedule.getDayTypes(), Operation.DELETE);
                JsfUtil.addSuccessMessage(uiText.get("ScheduleDeleted"));
                this.ejbFacade.evictAll();
                this.ejbPlannedDutyFacade.evictAll();
                this.ejbScheduledDutyFacade.evictAll();
                this.ejbPlanndServiceFacade.evictAll();
                this.ejbScheduledServiceFacade.evictAll();
                this.timeBlockFacade.evictAll();
            }
        }catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            try {
                userTransaction.rollback();
            } catch (IllegalStateException | SecurityException | SystemException ex1) {
                Logger.getLogger(MaintenanceController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(MaintenanceController.class.getName()).log(Level.SEVERE, null, ex);
            JsfUtil.addErrorMessage(ex, uiText.get("PersistenceErrorOccured"));
        } 

    }

    private Schedule updateState(Schedule schedule) {

        if (schedule.getScheduleId() != null) {
            this.ejbFacade.evict(schedule);
            this.ejbDayTypeFacade.evictAll();
            // this is needed to avoid: e.g. a new day type is added to database, but the schedule filter has a null id daytype in other pages
            return this.ejbFacade.find(schedule.getScheduleId());
        } else {
            return schedule;
        }
    }

    public boolean isAddDayTypeAllowed(Schedule schedule) {
        List<DayType> array = schedule.getDayTypes();
        if (array != null && !array.isEmpty()) {
            DayType dayType = array.get(0);
            if (dayType != null && dayType.isCreating()) {
                return false;
            } else {
                Iterator<DayType> iterator = array.iterator();
                while (iterator.hasNext()) {
                    dayType = (DayType) iterator.next();
                    if (dayType.isEditing()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public String addNewDayType(Schedule schedule) {
        DayType dayType = new DayType();
        dayType.setCreating(true);
        dayType.setEditing(true);
        dayType.setScheduleParent(schedule);
        schedule.getDayTypes().add(0, dayType);
        return null;
    }

    public String activateEditDayType(Schedule schedule, DayType dayType) {
        for (int i = 0; i < schedule.getDayTypes().size(); i++) {
            if (schedule.getDayTypes().get(i).getDayTypeId() == dayType.getDayTypeId()) {
                schedule.getDayTypes().get(i).setEditing(true);
                break;
            }
        }
        //dayType.setEditing(true);
        return null;
    }

    public String saveDayType(Schedule schedule, DayType dayType) {
        try {
            dayType.setEditing(false);

            if (dayType.isCreating()) {
                dayType.setCreating(false);
                getFacade().edit(schedule);
                xmlMessageSender.sendDayTypeMsg(dayType, Operation.CREATE);
                JsfUtil.addSuccessMessage(uiText.get("DayTypeCreated"));
            } else {
                getFacade().edit(schedule);
                xmlMessageSender.sendDayTypeMsg(dayType, Operation.MODIFY);
                JsfUtil.addSuccessMessage(uiText.get("DayTypeUpdated"));
            }
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
            updateState(schedule);
            recreateModel();
            return null;
        }

        schedule = updateState(schedule);
        this.ejbDayTypeFacade.evictAll();

        scheduleFilterChanged(schedule);
        /*longTermFilterController.initSchedules(plannedServiceController.getFilter().getScheduleFilter());
        plannedServiceController.getFilter().setScheduleFilter(plannedServiceController.getFilter().getScheduleFilter());
        plannedServiceController.scheduleFilterChanged();
        plannedTripController.resetPagination();

        timeBlockController.getFilter().setSchedule(timeBlockController.getFilter().getSchedule());
        timeBlockController.scheduleFilterChanged();*/

        recreateModel();
        return null;
    }

    public String cancelDayType(Schedule schedule, DayType dayType) {
        dayType.setEditing(false);
        //recreateModel();
        return null;
    }

    public String destroyDayType(Schedule schedule, DayType dayType) {
        //current = scheduleTemplate;

        if (!dayType.isCreating()) {
            List<Integer> ids = new ArrayList<>();
            ids.add(dayType.getDayTypeId());
            List<DayType> dts = new ArrayList<>();
            dts.add(dayType);
            if (ejbScheduledServiceFacade.isValidExistByDayType(ids)) {
                JsfUtil.addErrorMessage(uiText.get("Error_ScheduleInUse"));
            } else {
                try {
                    userTransaction.begin();
                    entityManager.joinTransaction();
                    List<TimeBlock> timeBlocks = null;
                    List<PlannedDuty> plannedDuties = null;
                    List<ScheduledDuty> scheduledDuties = null;
                    if (ConfigBean.getConfig().getPage("timeBlock").isVisible()) {
                        if (!ids.isEmpty()) {
                            timeBlocks = timeBlockFacade.findByDayTypes(ids);
                            Iterator<TimeBlock> it = timeBlocks.iterator();
                            TimeBlock timeBlock;
                            while (it.hasNext()) {
                                timeBlock = it.next();
                                timeBlock = entityManager.merge(timeBlock);
                                entityManager.remove(timeBlock);
                                //timeBlockFacade.remove(bIt.next());
                            }
                        }
                    }
                    if (ConfigBean.getConfig().getPage("duty").isVisible()) {

                        if (ConfigBean.getConfig().getPage("duty").isVisible()) {
                            scheduledDuties = ejbScheduledDutyFacade.findByDayTypes(ids);
                            Iterator<ScheduledDuty> it2 = scheduledDuties.iterator();
                            ScheduledDuty scheduledDuty;
                            while (it2.hasNext()) {
                                scheduledDuty = it2.next();
                                scheduledDuty.setRemoving(true);
                                scheduledDuty = entityManager.merge(scheduledDuty);
                                entityManager.remove(scheduledDuty);
                            }
                            plannedDuties = ejbPlannedDutyFacade.findByDayTypesList(dts);
                            Iterator<PlannedDuty> it3 = plannedDuties.iterator();
                            PlannedDuty plannedDuty;
                            while (it3.hasNext()) {
                                plannedDuty = it3.next();
                                if(plannedDuty.getDayTypeList().size() == 1) {
                                    plannedDuty.setRemoving(true);
                                    plannedDuty = entityManager.merge(plannedDuty);
                                    entityManager.remove(plannedDuty);
                                }else {
                                    plannedDuty.getDayTypeList().remove(dayType);
                                    plannedDuty = entityManager.merge(plannedDuty);
                                }
                            }
                        }
                    }

                    Iterator<DefaultDayRule> dayRules = schedule.getDefaultDayRules().iterator();
                    DefaultDayRule dayRule;
                    while (dayRules.hasNext()) {
                        dayRule = dayRules.next();
                        if (dayRule.getDayType() != null && (dayRule.getDayType().getDayTypeId() == dayType.getDayTypeId())) {
                            dayRule.setDayType(null);
                        }
                        dayRule = entityManager.merge(dayRule);
                    }

                    Iterator<DayInSchedule> iterator = schedule.getDayInSchedules().iterator();
                    DayInSchedule dayInSchedule;
                    while (iterator.hasNext()) {
                        dayInSchedule = iterator.next();
//                        if (dayInSchedule.getDayType() != null && (dayInSchedule.getDayType().getDayTypeId() == dayType.getDayTypeId())) {
//                            dayInSchedule.setDayType(null);
//                        }
                        if (dayInSchedule.getDayTypeList() != null) {
                            dayInSchedule.getDayTypeList().remove(dayType);
                            dayInSchedule = this.entityManager.merge(dayInSchedule);
                        }
                    }

                    List<ScheduledService> scheduledServices = ejbScheduledServiceFacade.findInvalidByDayTypes(ids);
                    Iterator<ScheduledService> iterator2 = scheduledServices.iterator();
                    ScheduledService scheduledService;
                    while (iterator2.hasNext()) {
                        scheduledService = iterator2.next();
                        scheduledService = entityManager.merge(scheduledService);
                        entityManager.remove(scheduledService);
                        //ejbScheduledServiceFacade.remove(scheduledService);
                    }

                    List<PlannedService> plannedServices = ejbPlanndServiceFacade.findByDayTypeList(dts);
                    Iterator<PlannedService> iterator3 = plannedServices.iterator();
                    PlannedService plannedService;
                    while (iterator3.hasNext()) {
                        plannedService = iterator3.next();
                        if(plannedService.getDayTypeList().size() == 1) {
                            plannedService.setRemoving(true);
                            plannedService = entityManager.merge(plannedService);
                            entityManager.remove(plannedService);
                            //ejbPlanndServiceFacade.remove(plannedService);
                        }else {
                            plannedService.getDayTypeList().remove(dayType);
                            plannedService = entityManager.merge(plannedService);
                            //plannedService.setEditing(true);
                        }
                    }

                    List<PlannedTrip> plannedTrips = ejbPlannedTripFacade.findByDayTypeList(dts);
                    Iterator<PlannedTrip> iterator4 = plannedTrips.iterator();
                    PlannedTrip plannedTrip;
                    while (iterator4.hasNext()) {
                        plannedTrip = iterator4.next();
                        if(plannedTrip.getDayTypeList().size() == 1) {
                            plannedTrip.setRemoving(true);
                            plannedTrip = entityManager.merge(plannedTrip);
                            entityManager.remove(plannedTrip);
                        }else {
                            plannedTrip.getDayTypeList().remove(dayType);
                            plannedTrip = entityManager.merge(plannedTrip);
                        }
                    }

                    //dayType.setScheduleParent(null);
                    schedule.remove(dayType);
                    //this.ejbFacade.edit(schedule);
                    schedule = entityManager.merge(schedule);
                    this.entityManager.merge(schedule);

                    this.userTransaction.commit();

                    if (timeBlocks != null && !timeBlocks.isEmpty()) {
                        xmlMessageSender.sendTimeBlockMsg(timeBlocks, Operation.DELETE);
                    }
                    if (!plannedServices.isEmpty() || (plannedDuties != null && !plannedDuties.isEmpty())) {
                        xmlMessageSender.sendServiceDutyChangeMsg(plannedServices, plannedDuties);
                    }
                    if (!scheduledServices.isEmpty() || (scheduledDuties != null && !scheduledDuties.isEmpty())) {
                        xmlMessageSender.sendServiceDutyChangeMsg(scheduledServices, scheduledDuties);
                    }

                    xmlMessageSender.sendDayTypeMsg(dayType, Operation.DELETE);
                    //this.ejbDayTypeFacade.remove(dayType);
                    JsfUtil.addSuccessMessage(uiText.get("DayTypeDeleted"));

                } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException e) {
                    try {
                        userTransaction.rollback();

                    } catch (IllegalStateException | SecurityException | SystemException ex) {
                        Logger.getLogger(ScheduleController.class
                                .getName()).log(Level.SEVERE, null, ex);
                    }
                    JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
                }
            }
            //current = this.ejbFacade.find(current.getScheduleId());
            schedule = updateState(schedule);
            this.ejbPlannedDutyFacade.evictAll();
            this.ejbScheduledDutyFacade.evictAll();
            this.timeBlockFacade.evictAll();
            scheduleFilterChanged(schedule);
            /*longTermFilterController.initSchedules(plannedServiceController.getFilter().getScheduleFilter());
        plannedServiceController.getFilter().setScheduleFilter(plannedServiceController.getFilter().getScheduleFilter());
        plannedServiceController.scheduleFilterChanged();
        plannedTripController.resetPagination();

        timeBlockController.getFilter().setSchedule(timeBlockController.getFilter().getSchedule());
        timeBlockController.scheduleFilterChanged();*/

        }
        recreateModel();
        return null;

    }

    @FacesConverter(forClass = Schedule.class)
    public static class ScheduleControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ScheduleController controller = (ScheduleController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "scheduleController");
            return controller.ejbFacade.find(getKey(value));
        }

        java.lang.Integer getKey(String value) {
            java.lang.Integer key;
            key = Integer.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Integer value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Schedule) {
                Schedule o = (Schedule) object;
                return getStringKey(o.getScheduleId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + ScheduleController.class.getName());
            }
        }

    }
}
