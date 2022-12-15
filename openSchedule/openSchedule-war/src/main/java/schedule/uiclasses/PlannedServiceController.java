package schedule.uiclasses;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.*;

import schedule.entities.DayType;
import schedule.entities.PlannedService;
import schedule.entities.TTArea;
import schedule.entities.TripAction;
import schedule.uiclasses.util.JsfUtil;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import schedule.entities.ActionFullTripDriveDuty;
import schedule.entities.ActionGlue;
import schedule.entities.ActionRunTrip;
import schedule.entities.ActionTrainFormation;
import schedule.entities.ActionTrainMoving;
import schedule.entities.ActionType;
import schedule.entities.BasicTrip;
import schedule.entities.Duty;
import schedule.entities.PlannedDuty;
import schedule.entities.PlannedTrip;
import schedule.entities.Schedule;
import schedule.entities.ScheduledDay;
import schedule.entities.ScheduledDuty;
import schedule.entities.ScheduledService;
import schedule.entities.ScheduledTrip;
import schedule.entities.SchedulingState;
import schedule.entities.TTObject;
import schedule.entities.TimedTrip;
import schedule.entities.TripAction.MainActionTypeEnum;
import schedule.entities.util.TimeHelper;
import schedule.messages.ServiceItem.ServiceType;
import schedule.sessions.AbstractFacade;
import schedule.sessions.PlannedDutyFacade;
import schedule.sessions.PlannedTripFacade;
import schedule.sessions.ScheduledDayFacade;
import schedule.uiclasses.util.UiText;

@SuppressWarnings("unchecked")
@Named("plannedServiceController")
@SessionScoped
public class PlannedServiceController extends BaseServiceController<PlannedService, PlannedServiceFilter> implements Serializable {

    @Inject
    private schedule.sessions.PlannedServiceFacade ejbFacade;
    @Inject
    private ScheduledServiceController scheduledServiceController;
    @Inject
    private PlannedTripFacade ejbPlannedTripFacade;
    @Inject
    private ScheduledDayFacade ejbScheduledDayFacade;
    @Inject
    private PlannedDutyFacade ejbPlannedDutyFacade;
    @Inject
    LongTermFilterController longTermFilterController;

    @Inject
    private UiText uiText;

    // For pop-up to create scheduled service in LIST.XHTML
    private Calendar dateSelector;

    // For pop-up to create cloned planned service in LIST.XHTML
    private Date clonedStartTime;
    private String clonedDescription;
    private DayType clonedDayType;
    private List<DayType> clonedDayTypeList;
    private List<DayType> clonedPSDayTypeList;
    private SchedulingState DEFAULT_STATE;
    private List<PlannedService> selectedPlannedServices;
    private Schedule cloneTargetSchedule;

    public PlannedServiceController() {
        super(new PlannedServiceFilter());
    }

    @PostConstruct
    public void myInit() {
        DEFAULT_STATE = ejbSchedulingStateFacade.find(SchedulingState.State.PLANNED_TO_RUN.getStateValue());
        dateSelector = Calendar.getInstance();

        // By default, get only valid services
        getFilter().setValidFilter(Boolean.TRUE);
        getFilter().setScheduleFilter(longTermFilterController.getSelectedSchedule());
        clonedDayTypeList = new ArrayList<>();
//        if (this.getDayTypes() != null && !this.getDayTypes().isEmpty()) {
//            clonedDayType = this.getDayTypes().get(0);
//        } else {
//            clonedDayType = null;
//        }
    }

    public List<DayType> getClonedDayTypeList() {
        return clonedDayTypeList;
    }

    public void setClonedDayTypeList(List<DayType> clonedDayTypeList) {
        this.clonedDayTypeList = clonedDayTypeList;
    }

    public List<DayType> getClonedPSDayTypeList() {
        return clonedPSDayTypeList;
    }

    public void setClonedPSDayTypeList(List<DayType> clonedPSDayTypeList) {
        this.clonedPSDayTypeList = clonedPSDayTypeList;
    }

    public Date getDateSelector() {
        return dateSelector.getTime();
    }

    public void setDateSelector(Date dateFilter) {
        if (dateFilter != null && this.dateSelector.getTime().getTime() != dateFilter.getTime()) {
            this.dateSelector.setTime(dateFilter);
        }
    }

    public List<PlannedService> getSelectedPlannedServices() {
        return selectedPlannedServices;
    }

    public void setSelectedPlannedServices(List<PlannedService> plannedServices) {
        this.selectedPlannedServices = plannedServices;
    }

    public Schedule getCloneTargetSchedule() {
        return cloneTargetSchedule;
    }

    public void setCloneTargetSchedule(Schedule schedule) {
        this.cloneTargetSchedule = schedule;
    }

    @Override
    protected Logger getLogger() {
        return Logger.getLogger(PlannedServiceController.class.getName());
    }

    // Called from LIST, when user selects to create scheduled service from planned service
    public String createScheduledService(PlannedService plannedService) {
        ScheduledDay selectedScheduledDay = ejbScheduledDayFacade.find(dateSelector);
        if (selectedScheduledDay == null) {
            // TODO: Should this create scheduled day
            JsfUtil.addErrorMessage(uiText.get("Error_ScheduledDayNotExist"));
            return null;
        }

        ScheduledService service = new ScheduledService();
        service.setCreating(true);
        try {
            userTransaction.begin();
            entityManager.joinTransaction();
            List<? extends Duty> duties = cloneToScheduled(service, plannedService, selectedScheduledDay);
            userTransaction.commit();

            JsfUtil.addSuccessMessage(uiText.get("ServiceCreated"));
            List<ScheduledService> list = new ArrayList<>();
            list.add(service);
            xmlMessageSender.sendServiceDutyChangeMsg(list, duties);

            // Force update to scheduled service pages
            this.scheduledServiceController.getFacade().evictAll();
            this.getDutyFacade().evictAll();
            ejbTripFacade.evictAll();

        } catch (NotSupportedException | SystemException | SecurityException | IllegalStateException | RollbackException | HeuristicMixedException | HeuristicRollbackException ex) {
            try {
                userTransaction.rollback();
            } catch (IllegalStateException | SecurityException | SystemException ex1) {
                getLogger().log(Level.SEVERE, null, ex1);
            }
            getLogger().log(Level.SEVERE, null, ex);
            JsfUtil.addErrorMessage(ex, uiText.get("PersistenceErrorOccured"));
        }
        return null;
    }

    private List<ScheduledDuty> cloneToScheduled(ScheduledService targetService, PlannedService source, ScheduledDay day) {

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

                    schedTrip.setTripTemplate(planTrip.getTripTemplate());
                    runTrip.setRefTrip(schedTrip);
                    if (planTrip.getFullTripDriveDutyAction() != null) {
                        schedTrip.setFullTripDriveDutyDescription(planTrip.getFullTripDriveDutyAction().getTrip().getDescription());
                        scheduledTrips.add(schedTrip);
                    }
                }
            }

        }
        entityManager.persist(targetService);

        List<ScheduledDuty> duties = new ArrayList<>();
        if (!scheduledTrips.isEmpty()) {
            Iterator<ScheduledTrip> it = scheduledTrips.iterator();
            while (it.hasNext()) {
                ScheduledTrip timedTrip = it.next();
                ScheduledDuty duty = this.scheduledServiceController.getDuty(timedTrip, timedTrip.getFullTripDriveDutyDescription().trim());
                ActionFullTripDriveDuty newFullDutyAction = createFullTripDriveDutyAction(timedTrip);
                newFullDutyAction.setRefTrip(timedTrip);
                newFullDutyAction.setTrip(duty);
                duty.addTripActionInOrder(newFullDutyAction);
                duty.actionChanged();
                if (duty.getTripId() == null) {
                    entityManager.persist(duty);
                    duty.setCreating(true);
                } else {

                    duty = entityManager.merge(duty);
                    duty.setEditing(true);
                }
                duties.add(duty);
            }
        }

        return duties;
    }

    /**
     * Called when user has selected 'Clone' in plannedService/List.xhtml, by
     * opening 'Clone Planned Service'
     *
     * @param service
     * @return
     */
    public String clonePlannedService(PlannedService service) {

        if (clonedDescription == null) {
            JsfUtil.addErrorMessage(uiText.get("RequiredMessage_description"));
            return null;
        }

//        if (clonedDayType == null) {
//            JsfUtil.addErrorMessage(uiText.get("RequiredMessage_daytype"));
//            return null;
//        }
        if (clonedDayTypeList == null || clonedDayTypeList.isEmpty()) {
            JsfUtil.addErrorMessage(uiText.get("RequiredMessage_daytype"));
            return null;
        }

        if (clonedDayTypeList.size() > 3) {
            JsfUtil.addErrorMessage(uiText.get("RequiredMessage_maxdaytypes"));
            return null;
        }

        Integer startSecs;
        if (clonedStartTime != null) {
            startSecs = safeLongtoInteger(TimeHelper.getLocalSecsFrom(service.getUtcTimes(), clonedStartTime, service.getPlannedStartObj().getTimeZone()));
        } else {
            JsfUtil.addErrorMessage(uiText.get("RequiredMessage_starttime"));
            return null;
        }

        if (startSecs == null) {
            return null;
        }

        PlannedService clonedService = new PlannedService();

        clonedService.setDescription(clonedDescription);
        clonedService.setVersion(1);
        clonedService.setCreating(true);

        try {

            //List<PlannedDuty> duties = cloneTo(clonedService, service, startSecs, clonedDayType);
            List<PlannedDuty> duties = cloneTo(clonedService, service, startSecs, clonedDayTypeList);

            JsfUtil.addSuccessMessage(uiText.get("ServiceCreated"));
            List<PlannedService> list = new ArrayList<>();
            list.add(clonedService);
            xmlMessageSender.sendServiceDutyChangeMsg(list, duties);
            this.ejbTripFacade.evictAll();
            this.ejbFacade.evictAll();
            this.getDutyFacade().evictAll();

        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, null, ex);
            JsfUtil.addErrorMessage(ex, uiText.get("PersistenceErrorOccured"));
        }
        recreateModel();

        return null;
    }


    /**
     * Called when user has selected 'Clone' in plannedService/List.xhtml, by
     * opening 'Clone Planned Service'
     * @param schedule
     * @param plannedServices
     * @return
     */
    public String clonePlannedServiceToSchedule(Schedule schedule, List<PlannedService> plannedServices) {
        System.out.println("~~~~~~~~~~~~~~~~~~~~Cloning multiple planned services");
        return null;
    }

    //public List<PlannedDuty> cloneTo(PlannedService targetService, PlannedService source, Integer startSecs, DayType dayType) {
    public List<PlannedDuty> cloneTo(PlannedService targetService, PlannedService source, Integer startSecs, List<DayType> dayTypeList) {
        targetService.FormFromPlanned(source, startSecs);

//        boolean resetDayType = !Objects.equals(source.getDayType().getDayTypeId(), dayType.getDayTypeId());
//        if (resetDayType) {
//            targetService.setDayType(dayType);
//        }
        //targetService.setDayType(null);
        boolean resetDayType = false;
        for(DayType d: dayTypeList) {
            if(!targetService.getDayTypeList().contains(d)) {
                resetDayType = true;
                break;
            }
        }
        if(resetDayType) {
            targetService.setDayTypeList(dayTypeList);
        }

        Iterator<TripAction> ite = targetService.getActionsIterator();

        Integer diff = startSecs - source.getPlannedStartSecs();
        List<PlannedTrip> plannedTrips = new ArrayList<>();
        while (ite.hasNext()) {
            TripAction newlycreatedAction = ite.next();
            if (newlycreatedAction instanceof ActionRunTrip) {
                ActionRunTrip runTrip = (ActionRunTrip) newlycreatedAction;
                PlannedTrip planTrip = (PlannedTrip) ((ActionRunTrip) runTrip.getClonedFrom()).getRefTrip();

                if (planTrip != null && planTrip.isValid()) {
                    PlannedTrip newPlannedTrip = new PlannedTrip();
                    newPlannedTrip.setVersion(1);
                    planTrip.cloneDataToClonedTrip(newPlannedTrip, planTrip.getPlannedStartSecs() + diff);
                    newPlannedTrip.setValid(planTrip.isValid());
                    newPlannedTrip.setServiceAction(runTrip);
                    if (resetDayType) {
                        //newPlannedTrip.setDayType(dayType);
                        //newPlannedTrip.setDayType(dayTypeList.get(0));  // @Todo: how to treat in case planned service has multiple daytypes? For now just considering 1st daytype
                        newPlannedTrip.setDayTypeList(dayTypeList);
                    }

                    ((ActionRunTrip) newlycreatedAction).setRefTrip(newPlannedTrip);

                    BasicTrip tripTemplate = this.ejbTripFacade.find(planTrip.getTripTemplateID());
                    newPlannedTrip.setTripTemplate(tripTemplate);

                    if (planTrip.getFullTripDriveDutyAction() != null) {
                        newPlannedTrip.setFullTripDriveDutyDescription(planTrip.getFullTripDriveDutyAction().getTrip().getDescription());
                        plannedTrips.add(newPlannedTrip);
                    }
                }
            }
        }
        List<PlannedDuty> duties = new ArrayList<>();
        try {
            userTransaction.begin();
            entityManager.joinTransaction();
            entityManager.persist(targetService);

            if (!plannedTrips.isEmpty()) {
                Iterator<PlannedTrip> it = plannedTrips.iterator();
                while (it.hasNext()) {
                    PlannedTrip timedTrip = it.next();
                    PlannedDuty duty = getDuty(timedTrip, timedTrip.getFullTripDriveDutyDescription().trim());
                    ActionFullTripDriveDuty newFullDutyAction = createFullTripDriveDutyAction(timedTrip);
                    newFullDutyAction.setRefTrip(timedTrip);
                    newFullDutyAction.setTrip(duty);
                    duty.addTripActionInOrder(newFullDutyAction);
                    duty.actionChanged();
                    if (duty.getTripId() == null) {
                        entityManager.persist(duty);
                    } else {
                        duty = entityManager.merge(duty);
                    }
                    duties.add(duty);
                }
            }
            userTransaction.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            try {
                userTransaction.rollback();
            } catch (IllegalStateException | SecurityException | SystemException ex1) {
                getLogger().log(Level.SEVERE, null, ex1);
            }
            getLogger().log(Level.SEVERE, null, ex);
            JsfUtil.addErrorMessage(ex, uiText.get("PersistenceErrorOccured"));
        }
        return duties;
    }

    @Override
    public PlannedDuty getDuty(TimedTrip timedTrip, String description) {
        PlannedDutyFilter filter = new PlannedDutyFilter();
        //filter.setDayType(timedTrip.getDayType());
        filter.setDayTypeList(timedTrip.getDayTypeList());
        filter.setDescription(description);
        filter.setValid(timedTrip.isValid());
        PlannedDuty duty = ejbPlannedDutyFacade.findFirst(filter);
        if (duty == null) {
            duty = new PlannedDuty();
            duty.setCreating(true);
            duty.setDescription(description);
            //duty.setDayType(timedTrip.getDayType());
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
        }
        return duty;
    }

    public Schedule getSchedule() {
        //It is possible that the current filter is deleted by another session
        if (this.getFilter().getScheduleFilter() == null) {
            ScheduleFilter filter = new ScheduleFilter();
            filter.setValid(true);
            this.ejbScheduleFacade.findFirst(filter);
            this.getFilter().setScheduleFilter(this.ejbScheduleFacade.findFirst(filter));
            scheduleFilterChanged();
        }
        return this.getFilter().getScheduleFilter();
    }

    public void setSchedule(Schedule schedule) {
        this.getFilter().setScheduleFilter(schedule);
    }

    public void setClonedServiceStartTime(Date clonedStartTime) {
        this.clonedStartTime = clonedStartTime;
    }

    public Date getClonedServiceStartTime() {
        if (clonedStartTime == null) {
            this.clonedStartTime = Calendar.getInstance().getTime();
        }

        return clonedStartTime;
    }

    public String getClonedDescription() {
        return clonedDescription;
    }

    public void setClonedDescription(String clonedDescription) {
        this.clonedDescription = clonedDescription;
    }

    public DayType getClonedDayType() {
        return clonedDayType;
    }

    public void scheduleFilterChanged() {
        getFilter().setDayTypeFilter(null);
        clonedDayTypeList = new ArrayList<>();
//        if (this.getDayTypes() != null && !this.getDayTypes().isEmpty()) {
//            clonedDayType = this.getDayTypes().get(0);
//        } else {
//            clonedDayType = null;
//        }

        resetPagination();
    }

    public void scheduleFilterChanged1() {
        selectedPlannedServices = new ArrayList<>();
        // @Todo: get daytypes list for the schedule
        List<DayType> dts = this.getFilter().getScheduleFilter().getDayTypes();
    }

    public void setClonedDayType(DayType clonedDayType) {
        this.clonedDayType = clonedDayType;
    }

    public StreamedContent getTrainGraph(PlannedTrip plannedTrip) {
        StreamedContent tripChart = null;
        try {
            JFreeChart jfreechart = ChartFactory.createLineChart(plannedTrip.getDescription(), "Station", "Time", createTripDataset(plannedTrip), PlotOrientation.VERTICAL, true, true, false);
            languageBean.applyChartTheme(jfreechart);
            File chartFile = new File("dynamichart");
            ChartUtilities.saveChartAsPNG(chartFile, jfreechart, 1000, 1000);
            tripChart = new DefaultStreamedContent(new FileInputStream(chartFile), "image/png");
        } catch (Exception e) {

        }
        return tripChart;
    }

    private DefaultCategoryDataset createTripDataset(PlannedTrip tripToDraw) {

        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        List<TripAction> allActions = tripToDraw.getTripActions();

        for (TripAction action : allActions) {
            if (action.getClass() != ActionTrainMoving.class) {
                if (action.getTimetableObject() != null) {
                    dataset.addValue(action.getTimeFromTripStart(), "PLANNED", action.getMainObjectName());
                }
            }
        }
        return dataset;
    }

    private Integer safeLongtoInteger(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(l + " cannot be converted to Integer!");
        }
        return (Integer.valueOf("" + l));
    }

    @Override
    protected void updateState(PlannedService service) {
        ejbFacade.evict(service);
        ejbPlannedTripFacade.evictAll();

        // Be cunning and update selected item!
        service = ejbFacade.find(service.getTripId());
        prepareView(service);
        if (service != null) {
            service.updateActionTimes();
        }
    }

    @Override
    protected void updateState() {
        ejbFacade.evictAll();
        ejbPlannedTripFacade.evictAll();
    }

    @Override
    protected ServiceType getServiceType() {
        return ServiceType.PLANNED;
    }

    /**
     * Called from XHTML
     *
     * @return
     */
    @Override
    public String addNew() {
        PlannedService ps = constructNewItem();

        List<PlannedService> oldArray = (List<PlannedService>) getItems().getWrappedData();
        oldArray.add(0, ps);
        getItems().setWrappedData(oldArray);
        return null;
    }

    public TripAction createNewTripAction(ActionType actionType, TTArea area, List<DayType> dayTypeList, TTObject loc1, TTObject loc2, BasicTrip template) {
        TripAction tripAction = null;
        switch (actionType.getMainActionTypeEnum()) {
            case RUN_TRIP:
                tripAction = new ActionRunTrip();
                break;
            case TRAIN_FORMATION:
                tripAction = new ActionTrainFormation();
                break;
            case GLUE:
                tripAction = new ActionGlue();
                break;
            default:
                return null; // Sanity
        }

        tripAction.setConsumed(false);
        tripAction.setTimesValid(false);
        tripAction.setCreating(true);
        tripAction.setEditing(true);
        tripAction.setActionType(actionType);
        tripAction.setTimetableObject(loc1);
        tripAction.setMinSecs(actionType.getDefaultMinSecs());
        tripAction.setPlannedSecs(0);
        if (tripAction instanceof ActionRunTrip) {
            PlannedTrip plannedTrip = new PlannedTrip();
            plannedTrip.setConsumed(false);
            plannedTrip.setTimesAreValid(false);
            plannedTrip.setUtcTimes(false);
            plannedTrip.setAreaObj(area);
            plannedTrip.setServiceAction((ActionRunTrip) tripAction);
            plannedTrip.setValid(Boolean.TRUE);
            plannedTrip.setDayTypeList(dayTypeList);
            plannedTrip.setDurationSecs(0);
            plannedTrip.setOrigoSecs(0);
            plannedTrip.setActionsFromTemplate(Boolean.TRUE);
            plannedTrip.setVersion(1);
//RESTORE            plannedTrip.setPlannedState(service.getPlannedState());

            ((ActionRunTrip) tripAction).setRefTrip(plannedTrip);

            ((ActionRunTrip) tripAction).setTimetableObject2(loc2);

            if (template != null) {
                //needed by railML importer
                plannedTrip.setTripType(GetToTripType(template.getTripType()));
                plannedTrip.setTripTemplate(template);
                plannedTrip.setDurationSecs(template.getDurationSecs());
                plannedTrip.setPlannedStartObj(template.getPlannedStartObj());
                plannedTrip.setPlannedStopObj(template.getPlannedStopObj());
                plannedTrip.setOrigoSecs(template.getOrigoSecs());

                if (template.getNumberOfActions() == 1) {
                    if (template.getTripAction(1).getTimetableObject() != null) {
                        plannedTrip.setDescription(template.getTripAction(1).getTimetableObject().getText(languageBean.getOSLocale()));
                    } else {
                        plannedTrip.setDescription(" - ");
                    }
                } else if (template.getNumberOfActions() > 1) {
                    if (template.getTripAction(1).getTimetableObject() != null && template.getTripAction(template.getNumberOfActions()).getTimetableObject() != null) {
                        plannedTrip.setDescription(template.getTripAction(1).getTimetableObject().getText(languageBean.getOSLocale())
                                + " - " + template.getTripAction(template.getNumberOfActions()).getTimetableObject().getText(languageBean.getOSLocale()));
                    } else {
                        plannedTrip.setDescription(" - ");
                    }
                } else {
                    plannedTrip.setDescription(" - ");
                }
            }
        }

        return tripAction;
    }

    /**
     * Called from XHTML
     *
     * @param service
     * @return
     */
    @Override
    public String addNewServiceAction(PlannedService service) {
        TTObject loc1 = null;
        TTObject loc2 = null;

        switch (MainActionTypeEnum.parse(getSelectedMainActionType().getMactionTypeId())) {
            case RUN_TRIP:
                if (!getPossibleActionLocations().isEmpty()) {
                    loc1 = getPossibleActionLocations().get(0);
                }
                if (!getPossibleActionLocations2().isEmpty()) {
                    loc2 = getPossibleActionLocations2().get(0);
                }
                break;
            case TRAIN_FORMATION:
                if (!getPossibleActionLocations().isEmpty()) {
                    loc1 = getPossibleActionLocations().get(0);
                }
                break;
        }

        TripAction tripAction = createNewTripAction(getSelectedActionType(), service.getAreaObj(), service.getDayTypeList(), loc1, loc2, null);
        tripAction.setTimeFromTripStart(super.getLastActionRunTripEndTime(service));
        tripAction.setTimesValid(Boolean.TRUE);
        service.addTripAction(tripAction, 0);
        tripAction.setTrip(service);

        // Set first template
        if (tripAction instanceof ActionRunTrip) {
            List<BasicTrip> templates = getTripTemplates(tripAction);
            if (templates != null && templates.size() > 0) {
                ((TimedTrip) ((ActionRunTrip) tripAction).getRefTrip()).setTripTemplate(templates.get(0));
                templateChanged(tripAction); // Update tripaction times
            }
        }

        recreateModel();

        return null;
    }

    @Override
    public String destroy(PlannedService service) {
        if (!service.isCreating()) {
            performDestroy(service);
        }
        recreateModel();
        return "List";
    }

    private void performDestroy(PlannedService service) {
        try {

            userTransaction.begin();
            entityManager.joinTransaction();
            //detached
            service = entityManager.merge(service);
            List<Duty> duties = null;
            if (supportDuty) {
                duties = deleteFullTripDriveDuties(service);
            }
            service.setRemoving(true);
            entityManager.remove(service);
            userTransaction.commit();

            updateState();
            this.getDutyFacade().evictAll();
            JsfUtil.addSuccessMessage(uiText.get("ServiceDeleted"));
            xmlMessageSender.sendServiceDutyChangeMsg(service, duties);

        } catch (NotSupportedException | SystemException | SecurityException | IllegalStateException | RollbackException | HeuristicMixedException | HeuristicRollbackException ex) {
            try {
                userTransaction.rollback();
            } catch (IllegalStateException | SecurityException | SystemException ex1) {
                getLogger().log(Level.SEVERE, null, ex1);
            }
            getLogger().log(Level.SEVERE, null, ex);
            JsfUtil.addErrorMessage(ex, uiText.get("PersistenceErrorOccured"));
        }
    }

    @Override
    public PlannedService constructNewItem() {
        PlannedService ps = new PlannedService();
        ps.setEditing(true);
        ps.setCreating(true);
        ps.setTripId(0);
        ps.setTimesAreValid(false);
        ps.setUtcTimes(false);
        ps.setConsumed(false);
        ps.setOrigoSecs(0);
        ps.setDurationSecs(0);
        ps.setTripType(serviceTripType); // Uses default service trip type
        ps.setValid(Boolean.TRUE);
        ps.setPlannedState(DEFAULT_STATE);
        ps.setActionsFromTemplate(false);

        return ps;
    }

    @Override
    public AbstractFacade<PlannedService> getFacade() {
        return ejbFacade;
    }

    // <editor-fold defaultstate="collapsed" desc="Stateless actions for planned service handling">
    public PlannedService createPlannedService(DayType dt, TTArea tta) {
        PlannedService ps = constructNewItem();
        //ps.setDayType(dt);
        ps.setDayTypeList(new ArrayList<DayType>(Arrays.asList(dt)));
        ps.setAreaObj(tta);

        return ps;
    }

    public void addInitialActions(PlannedService ps) {
        ActionTrainFormation trainFormationStart = new ActionTrainFormation();
        trainFormationStart.setActionType(this.trainFormationStartRun);
        trainFormationStart.setTrip(ps);
        trainFormationStart.setSeqNo(1);
        trainFormationStart.setTimetableObject(ps.getPlannedStartObj());
        trainFormationStart.setTimesValid(false);
        trainFormationStart.setPlannedSecs(this.trainFormationStartRun.getDefaultPlanSecs());
        trainFormationStart.setMinSecs(this.trainFormationStartRun.getDefaultMinSecs());
        trainFormationStart.setConsumed(false);

        ActionTrainFormation trainFormationStop = new ActionTrainFormation();
        trainFormationStop.setActionType(this.trainFormationEndRun);
        trainFormationStop.setTrip(ps);
        trainFormationStop.setSeqNo(2);
        trainFormationStop.setTimetableObject(ps.getPlannedStopObj());
        trainFormationStop.setTimesValid(false);
        trainFormationStop.setPlannedSecs(this.trainFormationEndRun.getDefaultPlanSecs());
        trainFormationStop.setMinSecs(this.trainFormationEndRun.getDefaultMinSecs());
        trainFormationStop.setConsumed(false);

        ps.addTripAction(trainFormationStart, 0);
        ps.addTripAction(trainFormationStop, 1);
        ps.updateActionTimes();
        addGlueAction(ps, trainFormationStart, trainFormationStop, false);

        ps.updatePlannedEndTime();
        ps.setDurationSecs(ps.getPlannedStopSecs() - ps.getPlannedStartSecs());
        ps.updateActionTimes();

    }

    //    @Override
//    protected Duty getDuty(TimedTrip timedTrip, String description) {
//        return this.ejbFacade.getDuty(timedTrip, description);
//    }
    // </editor-fold>
    @Override
    protected AbstractFacade getDutyFacade() {
        return this.ejbPlannedDutyFacade;
    }

    PlannedTripFacade getPlannedTripFacade() {
        return this.ejbPlannedTripFacade;
    }

    public void destroyAll() {
        if (this.getFilter().getScheduleFilter() != null) {
            try {
//                List<Integer> ids = new ArrayList<>();
                List<DayType> dts = this.getFilter().getScheduleFilter().getDayTypes();
//                Iterator<DayType> it = this.getFilter().getScheduleFilter().getDayTypes().iterator();

//                while (it.hasNext()) {
//                    DayType dayType = it.next();
//                    ids.add(dayType.getDayTypeId());
//                }
                userTransaction.begin();
                entityManager.joinTransaction();
                List<Duty> duties = new ArrayList<>();

                //List<PlannedService> plannedServices = ejbFacade.findByDayType(ids);
                List<PlannedService> plannedServices = ejbFacade.findByDayTypeList(dts);
                Iterator<PlannedService> iterator = plannedServices.iterator();
                PlannedService plannedService;
                while (iterator.hasNext()) {
                    plannedService = iterator.next();
                    plannedService.setRemoving(true);
                    plannedService = entityManager.merge(plannedService);
                    if (supportDuty) {
                        duties.addAll(deleteFullTripDriveDuties(plannedService));
                    }
                    entityManager.remove(plannedService);
                }

                userTransaction.commit();
                if (!plannedServices.isEmpty()) {
                    xmlMessageSender.sendServiceDutyChangeMsg(plannedServices, duties);
                }
                ejbPlannedDutyFacade.evictAll();
                updateState();
                recreateModel();
                JsfUtil.addSuccessMessage(uiText.get("PlannedServicesDeleted"));

            } catch (NotSupportedException | SystemException | SecurityException | IllegalStateException ex) {
                try {
                    userTransaction.rollback();
                } catch (IllegalStateException | SecurityException | SystemException ex1) {
                    getLogger().log(Level.SEVERE, null, ex1);
                }
                getLogger().log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage(ex, uiText.get("PersistenceErrorOccured"));

            } catch (HeuristicMixedException | HeuristicRollbackException | RollbackException ex) {
                Logger.getLogger(PlannedServiceController.class.getName()).log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage(ex, uiText.get("PersistenceErrorOccured"));
            }
        }
    }
}
