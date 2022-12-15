/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.uiclasses;

import eventlog.EventLogBean;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import schedule.entities.ActionFullTripDriveDuty;
import schedule.entities.ActionRunTrip;
import schedule.entities.ActionType;
import schedule.entities.DayInSchedule;
import schedule.entities.DayType;
import schedule.entities.MainActionType;
import schedule.entities.PlannedDuty;
import schedule.entities.PlannedService;
import schedule.entities.PlannedTrip;
import schedule.entities.ScheduledDay;
import schedule.entities.ScheduledDuty;
import schedule.entities.ScheduledService;
import schedule.entities.ScheduledTrip;
import schedule.entities.TimedTrip;
import schedule.entities.TripAction;
import schedule.entities.TripType;
import schedule.messages.Operation;
import schedule.messages.XmlMessageSender;
import schedule.sessions.ActionTypeFacade;
import schedule.sessions.MainActionTypeFacade;
import schedule.sessions.PlannedServiceFacade;
import schedule.sessions.ScheduleFacade;
import schedule.sessions.ScheduledDayFacade;
import schedule.sessions.ScheduledDutyFacade;
import schedule.sessions.ScheduledServiceFacade;
import schedule.sessions.ScheduledTripFacade;
import schedule.sessions.TripTypeFacade;

/**
 *
 * @author Jia Li
 */
@Stateless
public class ScheduleCreatorBean {

    @Inject
    private ScheduleFacade scheduleFacade;

    @Inject
    private ScheduledDayFacade scheduledDayFacade;

    @Inject
    private PlannedServiceFacade plannedServiceFacade;

    @Inject
    private ScheduledServiceFacade scheduledServiceFacade;
    @Inject
    private ScheduledTripFacade scheduledTripFacade;
    @Inject
    private ScheduledDutyFacade scheduledDutyFacade;
    @Inject
    protected MainActionTypeFacade ejbActionMainTypeFacade;
    @Inject
    protected ActionTypeFacade ejbActionTypeFacade;
    @Inject
    private TripTypeFacade ejbTripTypeFacade;

    @Inject
    private XmlMessageSender xmlMessageSender;
    @Inject
    private EventLogBean eventLog;
   
    private ActionType fullTripDriveDuty;
    private TripType normalDutyTripType;

    static final Logger logger = Logger.getLogger("ScheduleCreatorBean");

    @PostConstruct
    private void init() {
        MainActionType actionFullTripDriveDuty = this.ejbActionMainTypeFacade.find(TripAction.MainActionTypeEnum.FULL_TRIP_DRIVE_DUTY.getValue());
        List<ActionType> fullTripDriveDuties = this.ejbActionTypeFacade.findAll(actionFullTripDriveDuty, ActionFullTripDriveDuty.ActionFullTripDriveDutyTypeEnum.DRIVER.getValue());
        if (fullTripDriveDuties.size() > 0) {
            this.fullTripDriveDuty = fullTripDriveDuties.get(0);
        }
        TripTypeFilter tripTypeFilter = new TripTypeFilter();
        tripTypeFilter.setValid(true);
        tripTypeFilter.setTripType(TripType.TripMainType.NORMAL_DUTY);
        tripTypeFilter.setTripSubType(1);
        this.normalDutyTripType = this.ejbTripTypeFacade.findFirst(tripTypeFilter);
    }

    public void createScheduledDay(EntityManager entityManager) {

        logger.log(Level.INFO, String.format("ScheduleCreatorBean.createScheduledDay STARTED: [%s]", new Date()));
        
        eventLog.addEvent("ScheduleCreatorBean", "Started to load days");

        ScheduleFilter scheduleFilter = new ScheduleFilter();
        scheduleFilter.setValid(true);
        List<schedule.entities.Schedule> schedules = scheduleFacade.findAll(scheduleFilter);

        Calendar calendar = Calendar.getInstance();

        for (schedule.entities.Schedule scheduleTemp : schedules) {
            calendar.setTime(new Date());
            for (int i = 0; i < scheduleTemp.getLoadDayWidth(); i++) {
                if (i > 0) {
                    calendar.add(Calendar.DATE, 1);
                }
                ScheduledDay scheduledDay = null;

                if (!scheduleTemp.getStartTime().after(calendar.getTime()) && !scheduleTemp.getEndTime().before(calendar.getTime())) {
                    int scheduledDayCode = schedule.entities.Schedule.convertToDayCode(calendar);
                    ScheduledDayFilter filter = new ScheduledDayFilter();
                    filter.setScheduledDayCode(scheduledDayCode);
                    List<ScheduledDay> temp = scheduledDayFacade.findAll(filter);

                    //only the first execution insert into db
                    if (temp.isEmpty()) {

                        scheduledDay = new ScheduledDay(scheduledDayCode);
                        scheduledDay.setStartYear(calendar.get(Calendar.YEAR));
                        scheduledDay.setStartMonth(calendar.get(Calendar.MONTH) + 1);
                        scheduledDay.setStartDay(calendar.get(Calendar.DAY_OF_MONTH));
                        scheduledDay.setArchived(false);
                        scheduledDay.setActive(true);
                        scheduledDay.setSchedulingState(1); // todo enums..
                        scheduledDay.setVersion(1);

                        scheduledDayFacade.create(scheduledDay);
                        scheduledDayFacade.evictAll();
                        //scheduledServiceController.newDayCreated(scheduledDay.getDateOfDay());
                        logger.log(Level.INFO, String.format("ScheduleCreatorBean.createScheduledDay scheduleDay %d created", scheduledDay.getScheduledDayCode()));
                    } else {
                        scheduledDay = scheduledDayFacade.find(calendar);
                        scheduledDay.setActive(true);
                        //scheduledDay.setArchived(false);
                        //scheduledDay.setArchivedState(null);
                        scheduledDayFacade.edit(scheduledDay);
                        scheduledDayFacade.evictAll();
                    }

                    List<DayType> dayTypeList = getDayType(scheduleTemp, scheduledDay);
                    int serviceCount = 0;

                    if (dayTypeList != null && !dayTypeList.isEmpty()) {
                        //TODO: Accoridng to valid schedule
//                        PlannedServiceFilter plannedServiceFilter = new PlannedServiceFilter();
//                        plannedServiceFilter.setDayTypeFilter(dayType);
//                        plannedServiceFilter.setValidFilter(Boolean.TRUE);
//                        List<PlannedService> plannedServices = plannedServiceFacade.findAll(plannedServiceFilter);
                        List<PlannedService> plannedServices = plannedServiceFacade.findByDayTypeList(dayTypeList);
                        serviceCount = plannedServices.size();
                        
                        ScheduledServiceFilter scheduledServiceFilter = new ScheduledServiceFilter();
                        scheduledServiceFilter.setScheduledDayFilter(scheduledDay);
                        scheduledServiceFilter.setValidFilter(Boolean.TRUE);
                        List<ScheduledService> scheduledServices;// = scheduledServiceFacade.findAll(scheduledServiceFilter);

                        Iterator<PlannedService> iterator = plannedServices.iterator();

                        while (iterator.hasNext()) {
                            PlannedService sourceService = iterator.next();
                            scheduledServiceFilter.setSourceTripFilter(sourceService);
                            scheduledServices = scheduledServiceFacade.findAll(scheduledServiceFilter);

                            if (scheduledServices == null || scheduledServices.isEmpty()) {
                                ScheduledService targetService = new ScheduledService();
                                
                                String eventText = "Cloning planned service " + sourceService.getDescription();
                                String objectName = sourceService.getDescription();
                                eventLog.addEvent(objectName,eventText);
                                cloneToScheduled(targetService, sourceService, scheduledDay, entityManager);
                            }
                            else
                            {
                                String eventText = String.format("Scheduled service %s already loaded. Skipping cloning",
                                        sourceService.getDescription());
                                String objectName = sourceService.getDescription();
                                eventLog.addEvent(objectName,eventText);   
                            }

                        }
                    }
                    else
                    {
                        String eventText = "No daytypes defined for today in schedule " + scheduleTemp.getDescription();
                        String objectName = scheduledDay.getDateOfDay().toString();
                        eventLog.addEvent(objectName,eventText);
                    }

                    //this.scheduledServiceFacade.evictAll();
                    //this.dutyFacade.evictAll();
//                        userTransaction.commit();
                    if (scheduledDay != null) {
                        
                        //String eventText = "Services count : " + scheduledDay.getScheduledServicesCount();
                        String eventText = String.format("Schedule '%s' services count: %d",
                                scheduleTemp.getDescription(), serviceCount);
                        String objectName = scheduledDay.getDateOfDay().toString();
                        eventLog.addEvent(objectName,eventText);
                        
                        xmlMessageSender.sendScheduledDayMsg(scheduledDay, Operation.CREATE);
                    }
                }

            }
        }

        this.scheduledServiceFacade.evictAll();
        this.scheduledDutyFacade.evictAll();
        this.scheduledTripFacade.evictAll();

        logger.log(Level.INFO, String.format("ScheduleCreatorBean.createScheduledDay FINISHED: [%s]", new Date()));

    }

    private List<DayType> getDayType(schedule.entities.Schedule schedule, ScheduledDay scheduledDay) {
        //scheduleFilter.setSelected(Boolean.TRUE);
        //         List<schedule.entities.Schedule> schedules2 = scheduleFacade.findAll(scheduleFilter);
        //       schedule.entities.Schedule selectedSchedule = schedules2.size() >0 ? schedules2.get(0):null;
        if (schedule != null && scheduledDay != null) {
            List<DayInSchedule> days = schedule.getDayInSchedules();
            if (days != null) {
                Iterator<DayInSchedule> iterator = days.iterator();
                while (iterator.hasNext()) {
                    DayInSchedule day = iterator.next();

                    if (day.getDayCodeNo().equals(scheduledDay.getScheduledDayCode())) {
                        return day.getDayTypeList();
                    }
                }
            }
        }
        return null;
    }

    public List<ScheduledDuty> cloneToScheduled(ScheduledService targetService, PlannedService source, ScheduledDay day, EntityManager entityManager) {

        targetService.FormFromPlanned(source, day);
        Iterator<TripAction> ite = targetService.getActionsIterator();
        List<ScheduledTrip> scheduledTrips = new ArrayList<>();
        while (ite.hasNext()) {
            TripAction newlyCreatedAction = ite.next();
            if (newlyCreatedAction instanceof ActionRunTrip) {
                ActionRunTrip runTrip = (ActionRunTrip) newlyCreatedAction;
                PlannedTrip planTrip = (PlannedTrip) ((ActionRunTrip) runTrip.getClonedFrom()).getRefTrip();

                if (planTrip != null) {

                    ScheduledTrip schedTrip = new ScheduledTrip();
                    schedTrip.setValid(planTrip.isValid());
                    schedTrip.setServiceAction(runTrip);
                    schedTrip.setVersion(1);
                    schedTrip.FormFromPlanned(planTrip, day);

                    //This is needed if we use casadetype for maps from timedtrip to trip template, since FormFromPlanned cloneDataToNonPersisted; use planTrip.getTripTemplate() will lead trip template be cloned.
                    //BasicTrip tripTemplate = this.ejbTripFacade.find(planTrip.getTripTemplateID());
                    schedTrip.setTripTemplate(planTrip.getTripTemplate());
                    runTrip.setRefTrip(schedTrip);
                    if (planTrip.getFullTripDriveDutyAction() != null) {
                        schedTrip.setFullTripDriveDutyDescription(planTrip.getFullTripDriveDutyAction().getTrip().getDescription());
                        scheduledTrips.add(schedTrip);
                    }
                }
            }

        }
        if (entityManager == null) {
            this.scheduledServiceFacade.create(targetService);
        } else {
            entityManager.persist(targetService);
        }
        List<ScheduledDuty> duties = new ArrayList<>();
        if (!scheduledTrips.isEmpty()) {
            Iterator<ScheduledTrip> it = scheduledTrips.iterator();
            while (it.hasNext()) {
                ScheduledTrip timedTrip = it.next();
                ScheduledDuty duty = this.getDuty(timedTrip, timedTrip.getFullTripDriveDutyDescription().trim());
                ActionFullTripDriveDuty newFullDutyAction = createFullTripDriveDutyAction(timedTrip);
                newFullDutyAction.setRefTrip(timedTrip);
                newFullDutyAction.setTrip(duty);
                //timedTrip.setFullTripDriveDutyAction(newFullDutyAction); not needed, otherwise two objects
                duty.addTripActionInOrder(newFullDutyAction);
                duty.actionChanged();
                if (duty.getTripId() == null) {
                    if (entityManager == null) {
                        this.scheduledDutyFacade.create(duty);
                    } else {
                        entityManager.persist(duty);
                    }
                    duty.setCreating(true);
                } else {
                    if (entityManager == null) {
                        this.scheduledDutyFacade.edit(duty);

                    } else {
                        duty = entityManager.merge(duty);
                    }
                    duty.setEditing(true);

                }
                duties.add(duty);
            }
        }
        return duties;
    }

    private ActionFullTripDriveDuty createFullTripDriveDutyAction(TimedTrip timedTrip) {
        ActionFullTripDriveDuty fullDutyAction = new ActionFullTripDriveDuty();
        fullDutyAction.setConsumed(false);
        fullDutyAction.setActionType(this.fullTripDriveDuty);
        fullDutyAction.setTimetableObject(timedTrip.getPlannedStartObj());
        fullDutyAction.setTimetableObject2(timedTrip.getPlannedStopObj());
        fullDutyAction.setPlannedSecs(timedTrip.getDurationSecs());
        fullDutyAction.setMinSecs(timedTrip.getDefaultMinSecs());
        //fullDutyAction.setRefTrip(timedTrip);
        fullDutyAction.setTimesValid(false);
        fullDutyAction.setTimeFromTripStart(timedTrip.getServiceAction().getTimeFromTripStart());
        //fullDutyAction.setSeqNo(1);
        return fullDutyAction;
    }

    private ScheduledDuty getDuty(TimedTrip timedTrip, String description) {
        ScheduledDutyFilter filter = new ScheduledDutyFilter();
        filter.setDescription(description);
        filter.setDayType(timedTrip.getDayTypeList().get(0));
        filter.setDayTypeList(timedTrip.getDayTypeList());
        filter.setScheduledDay(((ScheduledTrip) timedTrip).getDay());
        filter.setValid(timedTrip.isValid());
        ScheduledDuty duty = scheduledDutyFacade.findFirst(filter);
        if (duty == null) {
            duty = new ScheduledDuty();
            duty.setCreating(true);
            duty.setDescription(description);
            duty.setDayTypeList(timedTrip.getDayTypeList());
            duty.setTripType(this.normalDutyTripType);
            duty.setActionsFromTemplate(false);
            duty.setAreaObj(timedTrip.getAreaObj());
            duty.setDurationSecs(0);
            duty.setConsumed(false);
            duty.setUtcTimes(timedTrip.getUtcTimes());
            duty.setOrigoSecs(0);
            duty.setTimesAreValid(false);
            duty.setValid(timedTrip.isValid());
            if (timedTrip.getFullTripDriveDutyAction() != null) {
                duty.setSourceTrip((PlannedDuty) timedTrip.getFullTripDriveDutyAction().getTrip());
            }
            duty.setDay(((ScheduledTrip) timedTrip).getDay());
        }
        return duty;
    }
}
