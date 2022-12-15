package schedule.uiclasses;

import eventlog.EventLogBean;
import java.util.ArrayList;
import schedule.entities.TTArea;
import schedule.entities.TripAction;

import schedule.uiclasses.util.JsfUtil;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import javax.persistence.PersistenceContext;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import schedule.entities.ActionFullTripDriveDuty;
import schedule.entities.ActionGlue;
import schedule.entities.ActionRunTrip;
import schedule.entities.ActionRunTrip.ActionRunTripTypeEnum;
import schedule.entities.ActionTrainFormation;
import schedule.entities.ActionType;
import schedule.entities.BasicTrip;
import schedule.entities.DayType;
import schedule.entities.Duty;
import schedule.entities.EditableEntity;
import schedule.entities.MainActionType;
import schedule.entities.MovementTrip;
import schedule.entities.Schedule;
import schedule.entities.SchedulingState;
import schedule.entities.ServiceActionBase;
import schedule.entities.ServiceTrip;
import schedule.entities.TTObject;
import schedule.entities.TTObjectType;
import schedule.entities.TimedTrip;
import schedule.entities.TrainType;
import schedule.entities.Trip;
import schedule.entities.TripType;
import schedule.messages.Operation;
import schedule.messages.ServiceItem.ServiceType;
import schedule.messages.XmlMessageSender;
import schedule.sessions.AbstractFacade;
import schedule.sessions.ActionTypeFacade;
import schedule.sessions.MainActionTypeFacade;
import schedule.sessions.MovementTripFacade;
import schedule.sessions.SchedulingStateFacade;
import schedule.sessions.TTAreaFacade;
import schedule.sessions.TimedTripFacade;
import schedule.sessions.TrainTypeFacade;
import schedule.sessions.TripFacade;
import schedule.sessions.TripTypeFacade;
import schedule.uiclasses.util.UiText;
import schedule.uiclasses.util.config.ConfigBean;

public abstract class BaseServiceController<T extends ServiceTrip, F extends ServiceFilter<T>> extends FilterController<T, F> {

    @Inject
    protected XmlMessageSender xmlMessageSender;

    @Inject
    protected schedule.sessions.TripActionFacade ejbTripActionFacade;
    @Inject
    protected TripFacade ejbTripFacade;
    @Inject
    protected schedule.sessions.ScheduleFacade ejbScheduleFacade;
    @Inject
    protected schedule.sessions.TTObjectFacade ejbTTObjectFacade;
    @Inject
    protected schedule.sessions.TTObjectTypeFacade ejbTTObjectTypeFacade;
    @Inject
    protected MainActionTypeFacade ejbActionMainTypeFacade;
    @Inject
    protected ActionTypeFacade ejbActionTypeFacade;
    @Inject
    protected TripTypeFacade ejbTripTypeFacade;
    @Inject
    protected TimedTripFacade ejbTimedTripFacade;
    @Inject
    protected EventLogBean eventLog;

    @Inject
    protected TrainTypeFacade ejbTrainTypeFacade;
    @Inject
    protected TTAreaFacade ejbTTAreaFacade;
    @Inject
    protected SchedulingStateFacade ejbSchedulingStateFacade;
    @Inject
    protected LanguageBean languageBean;
    @Inject
    private UiText uiText;
    @Inject
    protected MovementTripFacade ejbMovementTripFacade;

    @Resource
    protected UserTransaction userTransaction;
    //This is important, we need to make the controler's method in a transaction for operations related to both service and duty.
    @PersistenceContext(unitName = "openSchedule-warPU")
    protected EntityManager entityManager;

    private List<ActionType> glueTypes = null;
    private List<TTObjectType> ttobjectTypes;

    // For VIEW
    protected List<MainActionType> mainActionTypes;
    protected MainActionType selectedMainActionType;
    protected ActionType selectedActionType;
    protected TTObjectType selectedActionTTObjectType;
    protected TTObjectType selectedActionTTObjectType2;

    private TTObjectType selectedServiceStartTTObjectType;
    private TTObjectType selectedServiceStopTTObjectType;

    //private ResourceBundle bundle;
    protected ActionType glueWait = null;
    protected ActionType glueWaitTurn = null;
    protected ActionType trainFormationStartRun = null;
    protected ActionType trainFormationEndRun = null;
    protected ActionType fullTripDriveDuty = null;
    protected TripType serviceTripType;
    protected TripType commercialTripType;
    protected TripType nonCommercialTripType;
    protected TripType normalDutyTripType;
    protected boolean supportDuty = false;
    protected boolean waitTurnEnabled;

    public BaseServiceController() {
        super();
    }

    public BaseServiceController(F filter) {
        super(filter);
    }

    public BaseServiceController(F filter, int itemsPerPage) {
        super(filter, itemsPerPage);
    }

    @PostConstruct
    public void init() {
        // Initial schedule selection
        if (getSchedules() != null && !getSchedules().isEmpty()) {
            getFilter().setScheduleFilter(getSchedules().get(0));
        }
        mainActionTypes = new ArrayList<>();

        MainActionType actionMainType = ejbActionMainTypeFacade.find(TripAction.MainActionTypeEnum.RUN_TRIP.getValue());
        if (actionMainType != null && actionMainType.isUsed()) {
            mainActionTypes.add(actionMainType);
            selectedMainActionType = actionMainType;
            //trick : put non-commercial to the default one
            Iterator<ActionType> iterator = selectedMainActionType.getActionTypes().iterator();
            while (iterator.hasNext()) {
                ActionType actionType = iterator.next();
                if (actionType.isUsed()) {
                    selectedActionType = actionType;
                    break;
                }
            }

            /*else {
             selectedActionType = selectedMainActionType.getActionTypes().get(0);
             }*/
            selectedActionTTObjectType = selectedActionType.getTTObjTypes().get(0);
            selectedActionTTObjectType2 = selectedActionTTObjectType;
            selectedServiceStartTTObjectType = selectedActionTTObjectType;
            selectedServiceStopTTObjectType = selectedActionTTObjectType;
        }

        glueTypes = new ArrayList<>();

        MainActionType actionGlue = this.ejbActionMainTypeFacade.find(TripAction.MainActionTypeEnum.GLUE.getValue());
        List<ActionType> glueActions = this.ejbActionTypeFacade.findAll(actionGlue, ActionGlue.ActionGlueTypeEnum.GLUE_WAIT.getValue());
        if (glueActions.size() > 0) {
            glueWait = glueActions.get(0);
            glueTypes.add(glueWait);
        }

        glueActions = this.ejbActionTypeFacade.findAll(actionGlue, ActionGlue.ActionGlueTypeEnum.GLUE_WAIT_AND_TURN.getValue());
        if (glueActions.size() > 0) {
            glueWaitTurn = glueActions.get(0);
            glueTypes.add(glueWaitTurn);
        }

        MainActionType actionTrainFormation = this.ejbActionMainTypeFacade.find(TripAction.MainActionTypeEnum.TRAIN_FORMATION.getValue());
        List<ActionType> trainFormationStart = this.ejbActionTypeFacade.findAll(actionTrainFormation, ActionTrainFormation.ActionTrainFormationTypeEnum.START_RUN.getValue());
        if (trainFormationStart.size() > 0) {
            this.trainFormationStartRun = trainFormationStart.get(0);
        }
        List<ActionType> trainFormationStop = this.ejbActionTypeFacade.findAll(actionTrainFormation, ActionTrainFormation.ActionTrainFormationTypeEnum.END_RUN.getValue());
        if (trainFormationStop.size() > 0) {
            this.trainFormationEndRun = trainFormationStop.get(0);
        }

        MainActionType actionFullTripDriveDuty = this.ejbActionMainTypeFacade.find(TripAction.MainActionTypeEnum.FULL_TRIP_DRIVE_DUTY.getValue());
        List<ActionType> fullTripDriveDuties = this.ejbActionTypeFacade.findAll(actionFullTripDriveDuty, ActionFullTripDriveDuty.ActionFullTripDriveDutyTypeEnum.DRIVER.getValue());
        if (fullTripDriveDuties.size() > 0) {
            this.fullTripDriveDuty = fullTripDriveDuties.get(0);
        }

        TripTypeFilter tripTypeFilter = new TripTypeFilter();
        tripTypeFilter.setValid(true);
        tripTypeFilter.setTripType(TripType.TripMainType.SERVICE);
        tripTypeFilter.setTripSubType(ActionRunTripTypeEnum.RUN_COMMERCIAL_TRIP.getValue());
        List<TripType> tripTypes = this.ejbTripTypeFacade.findAll(tripTypeFilter);
        if (tripTypes.size() > 0) {
            this.serviceTripType = tripTypes.get(0);
        }

        tripTypeFilter.setTripType(TripType.TripMainType.COMMERCIAL);
        tripTypeFilter.setTripSubType(0);
        tripTypes = this.ejbTripTypeFacade.findAll(tripTypeFilter);
        if (tripTypes.size() > 0) {
            this.commercialTripType = tripTypes.get(0);
        }

        tripTypeFilter.setTripType(TripType.TripMainType.NONCOMMERCIAL);
        tripTypeFilter.setTripSubType(0);
        tripTypes = this.ejbTripTypeFacade.findAll(tripTypeFilter);
        if (tripTypes.size() > 0) {
            this.nonCommercialTripType = tripTypes.get(0);
        }

        supportDuty = ConfigBean.getConfig().getPage("duty").isVisible();
        if (supportDuty) {
            tripTypeFilter.setTripType(TripType.TripMainType.NORMAL_DUTY);
            tripTypeFilter.setTripSubType(1);
            this.normalDutyTripType = this.ejbTripTypeFacade.findFirst(tripTypeFilter);
        }

        TTObjectTypeFilter filter = new TTObjectTypeFilter();
        filter.setStartTTObjectTypeFilter();
        this.ttobjectTypes = this.ejbTTObjectTypeFacade.findAll(filter);

        waitTurnEnabled = ConfigBean.getConfig().getSetting("glueWaitTurn").getEnable();
    }

    public Map<String, Integer> getDays() {
        Map<String, Integer> days = new LinkedHashMap<>();
        String day = uiText.get("day");
        for (int i = 0; i < 8; i++) {
            days.put("+" + i + " " + day, i);
        }
        return days;
    }

    public List<ActionType> getGlueTypes() {
        return this.glueTypes;
    }

    public void filterChanged() {
        recreateModel();
    }

    public void actionTTObjectType1Changed(TripAction tripAction) {
        if (this.selectedActionTTObjectType.getTTObjects().size() > 0) {
            tripAction.setTimetableObject(this.selectedActionTTObjectType.getTTObjects().get(0));
        } else {
            tripAction.setTimetableObject(null);
            /*if (tripAction instanceof ActionRunTrip) {
                ((TimedTrip) ((ActionRunTrip) tripAction).getRefTrip()).setTripTemplate(null);
                possibleLocationsChanged(tripAction);
            }else if(tripAction instanceof ActionTrainFormation) {
                ((ActionTrainFormation) tripAction).setRefTrip(null);
            }*/
        }
        possibleLocationsChanged(tripAction);
    }

    public void actionTTObjectType2Changed(TripAction tripAction) {
        if (tripAction instanceof ActionRunTrip) {
            if (this.selectedActionTTObjectType2.getTTObjects().size() > 0) {
                ((ActionRunTrip) tripAction).setTimetableObject2(this.selectedActionTTObjectType2.getTTObjects().get(0));

            } else {
                ((ActionRunTrip) tripAction).setTimetableObject2(null);
            }
            possibleLocationsChanged(tripAction);
        }
    }

    public void actionTypeChanged(TripAction tripAction) {
        if (tripAction instanceof ActionRunTrip) {
            List<BasicTrip> templates = getTripTemplates(tripAction);
            ((ActionRunTrip) tripAction).getRefTrip().setTripTemplate((templates != null && templates.size() > 0) ? templates.get(0) : null);
            templateChanged(tripAction);
        }
    }

    public void possibleLocationsChanged(TripAction tripAction) {
        if (tripAction.isEditing()) {
            if (tripAction instanceof ActionRunTrip) {
                List<BasicTrip> list = getTripTemplates(tripAction);
                ActionRunTrip actionRunTrip = (ActionRunTrip) tripAction;
                if (list.size() > 0) {
                    ((TimedTrip) actionRunTrip.getRefTrip()).setTripTemplate(list.get(0));

                } else {
                    ((TimedTrip) actionRunTrip.getRefTrip()).setTripTemplate(null);

                }
                this.templateChanged(tripAction);
            } else if (tripAction instanceof ActionTrainFormation) {
                List<MovementTrip> list = getFormationMovementTrips(tripAction);
                ActionTrainFormation actionFormation = (ActionTrainFormation) tripAction;
                if (list.size() > 0) {
                    actionFormation.setRefTrip(list.get(0));

                } else {
                    actionFormation.setRefTrip(null);

                }
                this.formationTemplateChanged(tripAction);
            }
        }
        /*if (tripAction.isEditing() && (tripAction instanceof ActionRunTrip)) {
            ActionRunTrip actionRunTrip = (ActionRunTrip) tripAction;
            if (actionRunTrip.getRefTrip() != null) {
                List<BasicTrip> list = getTripTemplates(tripAction);
                if (list.size() > 0) {
                    BasicTrip template = list.get(0);
                    ((TimedTrip) actionRunTrip.getRefTrip()).setTripTemplate(template);

                } else {
                    ((TimedTrip) actionRunTrip.getRefTrip()).setTripTemplate(null);

                }
                this.templateChanged(tripAction);
            }
        }*/
    }

    public List<MainActionType> getMainActionTypes() {
        return this.mainActionTypes;
    }

    public MainActionType getSelectedMainActionType() {
        return selectedMainActionType;
    }

    public List<ActionType> getActionTypes() {
        List<ActionType> actionTypes = new ArrayList<>();
        Iterator<ActionType> iterator = this.selectedMainActionType.getActionTypes().iterator();
        while (iterator.hasNext()) {
            ActionType actionType = iterator.next();
            if (actionType.isUsed()) {
                actionTypes.add(actionType);
            }
        }
        return actionTypes;
    }

    public List<ActionType> getSubActionTypes(TripAction tripAction) {
        List<ActionType> actionTypes = new ArrayList<>();
        Iterator<ActionType> iterator = tripAction.getActionType().getMainActionType().getActionTypes().iterator();
        while (iterator.hasNext()) {
            ActionType actionType = iterator.next();
            if (actionType.isUsed()) {
                actionTypes.add(actionType);
            }
        }
        return actionTypes;
    }

    public void setSelectedMainActionType(MainActionType selectedMainActionType) {
        this.selectedMainActionType = selectedMainActionType;
    }

    public ActionType getSelectedActionType() {
        return selectedActionType;
    }

    public void setSelectedActionType(ActionType selectedActionType) {
        this.selectedActionType = selectedActionType;
    }

    public TTObjectType getSelectedActionTTObjectType() {
        return selectedActionTTObjectType;
    }

    public TTObjectType getSelectedActionTTObjectType2() {
        return selectedActionTTObjectType2;
    }

    public void setSelectedActionTTObjectType2(TTObjectType selectedActionTTObjectType2) {
        this.selectedActionTTObjectType2 = selectedActionTTObjectType2;
    }

    public void setSelectedActionTTObjectType(TTObjectType selectedActionTTObjectType) {
        this.selectedActionTTObjectType = selectedActionTTObjectType;
    }

    public TTObjectType getSelectedServiceStartTTObjectType() {
        return selectedServiceStartTTObjectType;
    }

    public void setSelectedServiceStartTTObjectType(TTObjectType selectedServiceStartTTObjectType) {
        this.selectedServiceStartTTObjectType = selectedServiceStartTTObjectType;
    }

    public TTObjectType getSelectedServiceStopTTObjectType() {
        return selectedServiceStopTTObjectType;
    }

    public void setSelectedServiceStopTTObjectType(TTObjectType selectedServiceStopTTObjectType) {
        this.selectedServiceStopTTObjectType = selectedServiceStopTTObjectType;
    }

    public List<TTObject> getPossibleActionLocations() {
        return this.selectedActionTTObjectType.getTTObjects();
    }

    public List<TTObject> getPossibleActionLocations2() {
        return this.selectedActionTTObjectType2.getTTObjects();
    }

    public List<TTObjectType> getPossibleActionTTObjectTypes() {
        return this.selectedActionType.getTTObjTypes();
    }

    public List<TTObjectType> getPossibleServiceStartTTObjectTypes() {
        return ttobjectTypes;
    }

    public List<TTObjectType> getPossibleServiceStopTTObjectTypes() {
        return ttobjectTypes;
    }

    public List<TTObject> getPossibleActionTTObjects() {
        TTObjectFilter filter = new TTObjectFilter();

        filter.setObjectTypeFilter(selectedActionTTObjectType);
        return this.ejbTTObjectFacade.findAll(filter);
    }

    public List<TTObject> getPossibleServiceStartLocations() {
        TTObjectFilter filter = new TTObjectFilter();

        filter.setObjectTypeFilter(selectedServiceStartTTObjectType);
        return this.ejbTTObjectFacade.findAll(filter);
    }

    public List<TTObject> getPossibleServiceStopLocations() {
        TTObjectFilter filter = new TTObjectFilter();

        filter.setObjectTypeFilter(selectedServiceStopTTObjectType);
        return this.ejbTTObjectFacade.findAll(filter);
    }

    public void selectedMainActionTypeChanged() {
        this.selectedActionType = this.selectedMainActionType.getActionTypes().get(0);
        selectedActionTypeChanged();
    }

    public void selectedActionTypeChanged() {
        selectedActionTTObjectType = selectedActionType.getTTObjTypes().get(0);
    }

    public List<TimedTrip> getTimedTripsForSelected() {
        T item = getSelected();
        List<TimedTrip> timedTrips = new ArrayList<>();
        for (TimedTrip timedTrip : item.getTimedTrips()) {
            if (!timedTrip.getServiceAction().isCreating()) {
                timedTrips.add(timedTrip);
            }
        }
        return timedTrips;
    }

    public boolean isAddServiceActionAllowed() {
        /*if (!isEditing) {
            T item = getSelected();

            if (item != null && !item.getTripActions().isEmpty()) {
                TripAction tripAction = item.getTripActions().get(0);
                return tripAction != null && !tripAction.isCreating(); // the new item is always at index 0
            }
        }

        return false;*/
        if (getSelected() != null) {
            List<TripAction> list = (List<TripAction>) getSelected().getTripActions();
            if (list == null || list.isEmpty()) {
                return true;
            } else {
                TripAction e = (TripAction) list.get(0);
                if (e != null && e.isCreating()) {
                    return false;
                } else {
                    Iterator<TripAction> iterator = list.iterator();
                    while (iterator.hasNext()) {
                        TripAction item = (TripAction) iterator.next();
                        if (item.isEditing()) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    public String create(T service) {
        if (validateService(service) == false) {
            return null;
        }

        try {
            ActionTrainFormation trainFormationStart = new ActionTrainFormation();
            trainFormationStart.setActionType(trainFormationStartRun);
            trainFormationStart.setTrip(service);
            trainFormationStart.setSeqNo(1);
            trainFormationStart.setTimetableObject(service.getPlannedStartObj());
            trainFormationStart.setTimesValid(false);
            trainFormationStart.setPlannedSecs(trainFormationStartRun.getDefaultPlanSecs());
            trainFormationStart.setMinSecs(trainFormationStartRun.getDefaultMinSecs());
            trainFormationStart.setConsumed(false);

            ActionTrainFormation trainFormationStop = new ActionTrainFormation();
            trainFormationStop.setActionType(this.trainFormationEndRun);
            trainFormationStop.setTrip(service);
            trainFormationStop.setSeqNo(2);
            trainFormationStop.setTimetableObject(service.getPlannedStopObj());
            trainFormationStop.setTimesValid(false);
            trainFormationStop.setPlannedSecs(trainFormationEndRun.getDefaultPlanSecs());
            trainFormationStop.setMinSecs(trainFormationEndRun.getDefaultMinSecs());
            trainFormationStop.setConsumed(false);

            service.addTripAction(trainFormationStart, 0);
            service.addTripAction(trainFormationStop, 1);
            service.updateActionTimes();
            addGlueAction(service, trainFormationStart, trainFormationStop, false);

            service.updatePlannedEndTime();
            service.setDurationSecs(service.getPlannedStopSecs() - service.getPlannedStartSecs());
            service.updateActionTimes();
            service.setVersion(1);
            service.setEditing(false);
            service.setCreating(false);
            getFacade().create(service);

            recreateModel();
            this.updateState(service);

            JsfUtil.addSuccessMessage(uiText.get("ServiceCreated"));
            xmlMessageSender.sendServiceChangeMsg(service, Operation.CREATE);
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
            return null;
        }

        return null;
    }

    private int getTripSeqNo(TripAction tripAction) {
        Iterator<TripAction> iterator = getSelected().getTripActions().iterator();
        int seqNo = 1;
        while (iterator.hasNext()) {
            TripAction temp = iterator.next();
            //Trick: the newly added tripAction 's timeFromTripStart is zero and calcActionStartHour return a smaller value 
            if (temp.getActionId() != null && (temp.getActionId() != tripAction.getActionId())
                    && (temp.getActionType().getAtypeId() != trainFormationEndRun.getAtypeId())) {
                if (tripAction.getTimeFromTripStart() >= temp.getTimeFromTripStart()) {
//                    if (temp instanceof ActionRunTrip) {
//                        if (tripAction.getTimeFromTripStart() + tripAction.getPlannedSecs() <= temp.getTimeFromTripStart() + temp.getPlannedSecs()) {
//                            seqNo++;
//                        }
//                    } else {
//                        seqNo++;
//                    }
                    seqNo++;
                } else {
                    return seqNo;
                }
            }
        }
        return seqNo;
    }

    private boolean validateService(T service) {
//        if(service.getDayTypeList().size() > 3) {
//            JsfUtil.addErrorMessage(uiText.get("RequiredMessage_maxdaytypes"));
//            return false;
//        }
        return true;
    }

    private boolean validateServiceAction(TripAction tripAction, TripAction prevAction, Integer startSecs) {

        if (prevAction == null) {
            return false;
        }

        if (tripAction instanceof ActionRunTrip) {
            if (((ActionRunTrip) tripAction).getRefTrip().getActionsFromTemplate() && ((ActionRunTrip) tripAction).getRefTrip().getTripTemplate() == null) {
                JsfUtil.addErrorMessage(uiText.get("RequiredMessage_tripTemplate"));
                return false;
            }
        }
        boolean valid = true;
        if (prevAction instanceof ActionGlue) {
            Integer prevStartSecs = ((ActionGlue) prevAction).getActionStartSecs();
            if (startSecs < prevStartSecs) {
                valid = false;
            }
        } else if (prevAction instanceof ActionRunTrip) {
            Integer prevEndSecs = ((ActionRunTrip) prevAction).getActionEndSecs();
            if (startSecs < prevEndSecs) {
                valid = false;
            }
        } else if (prevAction instanceof ActionTrainFormation) {
            Integer prevEndSecs = ((ActionTrainFormation) prevAction).getActionEndSecs();
            if (startSecs < prevEndSecs) {
                valid = false;
            }
        }

        if (!valid) {
            if (tripAction.isCreating()) {
                JsfUtil.addErrorMessage(uiText.get("Error_StartTimeBeforePrevEndTime"));
            } else {
                JsfUtil.addErrorMessage(uiText.get("Error_StartTimeBeforePrevEndTime2"));
            }
        }

        return valid;

    }

    // Called from VIEW.XHTML, when user changes 'trip template' for run trip in edit-mode
    // TODO: When user changes template, this should change also
    // ** END TIME
    // ** END TIME FROM SERVICE START
    // ** MIN END TIME FROM SERVICE START
    // also this should be called when user changes start time!
    // Changing TEMPLATE/STARTTIME should execute them both
    public String templateChanged(TripAction tripAction) {
        TimedTrip timedTrip = ((TimedTrip) ((ActionRunTrip) tripAction).getRefTrip());
        Trip template = timedTrip.getTripTemplate();

        if (template != null) {
            tripAction.setPlannedSecs(template.getDurationSecs());

            Iterator<TripAction> iterator = template.getTripActions().iterator();
            Integer minSecs = 0;
            while (iterator.hasNext()) {
                minSecs += iterator.next().getMinSecs();
            }
            tripAction.setMinSecs(minSecs);
            if (supportDuty && timedTrip.getFullTripDriveDutyAction() != null) {
                timedTrip.getFullTripDriveDutyAction().setMinSecs(minSecs);
                timedTrip.getFullTripDriveDutyAction().setPlannedSecs(tripAction.getPlannedSecs());
            }
        } else {
            tripAction.setMinSecs(0);
            tripAction.setPlannedSecs(0);
            if (supportDuty && timedTrip.getFullTripDriveDutyAction() != null) {
                timedTrip.getFullTripDriveDutyAction().setMinSecs(0);
                timedTrip.getFullTripDriveDutyAction().setPlannedSecs(0);
            }
        }

        return null;
    }

    public String formationTemplateChanged(TripAction tripAction) {
        MovementTrip temp = (MovementTrip) ((ActionTrainFormation) tripAction).getRefTrip();

        if (temp != null) {
            tripAction.setPlannedSecs(temp.getDurationSecs());

            Iterator<TripAction> iterator = temp.getTripActions().iterator();
            Integer minSecs = 0;
            while (iterator.hasNext()) {
                minSecs += iterator.next().getMinSecs();
            }
            tripAction.setMinSecs(minSecs);
        } else {
            tripAction.setMinSecs(0);
            tripAction.setPlannedSecs(0);
        }

        return null;
    }

    // Inherited class needs to take care of cleaning facades
    protected abstract void updateState(T service);

    protected abstract void updateState();

    protected abstract ServiceType getServiceType();

    protected abstract Logger getLogger();

    /*
    private void updateState() {
        this.ejbFacade.evict(service);
        this.ejbScheduledTripFacade.evictAll();
        selected = this.ejbFacade.find(service.getTripId()); <--- CAN CALL PREPAREVIEW()
        selected.updateActionTimes();

    }
     */
    // Offer hook for ScheduledServiceController to fill DAY
    protected void fillServiceAction(T service, TimedTrip trip) {
//        timedTrip.setDay(service.getDay());
    }

    public TripType GetToTripType(TripType fromTripType) {
        TripTypeFilter tripTypeFilter = new TripTypeFilter();
        tripTypeFilter.setValid(true);
        tripTypeFilter.setTripType(TripType.TripMainType.parse(fromTripType.getToTripType()));
        tripTypeFilter.setTripSubType(fromTripType.getTripSubType());
        return this.ejbTripTypeFacade.findFirst(tripTypeFilter);
    }

    protected abstract Duty getDuty(TimedTrip timedTrip, String description);

    protected abstract AbstractFacade getDutyFacade();

    protected ActionFullTripDriveDuty createFullTripDriveDutyAction(TimedTrip timedTrip) {
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

    protected List<Duty> deleteFullTripDriveDuties(ServiceTrip service) {
        List<Duty> duties = new ArrayList<>();
        Iterator<TripAction> iterator = service.getTripActions().iterator();
        TripAction tripAction;
        while (iterator.hasNext()) {
            tripAction = iterator.next();
            if (tripAction instanceof ActionRunTrip) {
                TimedTrip timedTrip = ((ActionRunTrip) tripAction).getRefTrip();
                if (timedTrip.getFullTripDriveDutyAction() != null) {
                    Duty duty = (Duty) (timedTrip.getFullTripDriveDutyAction().getTrip());
                    duty.removeTripAction(timedTrip.getFullTripDriveDutyAction());
                    duty.actionChanged();
                    if (duty.getNumberOfActions() > 0) {
                        entityManager.merge(duty);
                        duty.setEditing(true);
                        duties.add(duty);
                    } else {
                        duty = entityManager.merge(duty);
                        entityManager.remove(duty);
                        duty.setRemoving(true);
                        duties.add(duty);
                    }
                }

            }
        }
        return duties;
    }

    protected List<Duty> updateFullTripDriveDutyAction(TimedTrip timedTrip) {
        List<Duty> duties = new ArrayList<>();
        String newDutyDes = timedTrip.getFullTripDriveDutyDescription() != null ? timedTrip.getFullTripDriveDutyDescription().trim() : null;
        ActionFullTripDriveDuty oldFullDutyAction = timedTrip.getFullTripDriveDutyAction();

        if (newDutyDes != null && newDutyDes.trim().length() > 0) {

            if (oldFullDutyAction != null) {
                Duty oldDuty = (Duty) oldFullDutyAction.getTrip();
                // update daytypelist
                if(oldDuty.getDayTypeList() != timedTrip.getDayTypeList()) {
                    oldDuty.setDayTypeList(timedTrip.getDayTypeList());
                }
                if (!oldDuty.getDescription().trim().equals(newDutyDes)) {
                    oldFullDutyAction.setTrip(null);
                    oldDuty.removeTripAction(oldFullDutyAction);
                    oldDuty.actionChanged();
                    if (oldDuty.getNumberOfActions() > 0) {
                        //this.getDutyFacade().edit(oldDuty);
                        oldDuty = entityManager.merge(oldDuty);
                        oldDuty.setEditing(true);
                        duties.add(oldDuty);
                    } else {
                        //this.getDutyFacade().remove(oldDuty);
                        oldDuty = entityManager.merge(oldDuty);
                        entityManager.remove(oldDuty);
                        oldDuty.setRemoving(true);
                        duties.add(oldDuty);
                    }

                    Duty duty = getDuty(timedTrip, newDutyDes);
                    ActionFullTripDriveDuty newAction = createFullTripDriveDutyAction(timedTrip);
                    newAction.setRefTrip(timedTrip);
                    //timedTrip.setFullTripDriveDutyAction(newAction);
                    newAction.setTrip(duty);
                    duty.addTripActionInOrder(newAction);
                    duty.actionChanged();
                    if (duty.getTripId() == null) {
                        //this.getDutyFacade().create(duty);
                        entityManager.persist(duty);
                        duty.setCreating(true);
                        duties.add(duty);

                    } else {
                        //this.getDutyFacade().edit(duty);
                        duty = entityManager.merge(duty);
                        duty.setEditing(true);
                        duties.add(duty);
                    }
                } else {
                    oldFullDutyAction.setTimetableObject(timedTrip.getPlannedStartObj());
                    oldFullDutyAction.setTimetableObject2(timedTrip.getPlannedStopObj());
                    oldFullDutyAction.setPlannedSecs(timedTrip.getDurationSecs());
                    oldDuty.sortByStartTime();
                    oldDuty.actionChanged();
                    //this.getDutyFacade().edit(oldDuty);
                    oldDuty = entityManager.merge(oldDuty);
                    oldDuty.setEditing(true);
                    duties.add(oldDuty);
                }
            } else {
                Duty duty = getDuty(timedTrip, newDutyDes);
                ActionFullTripDriveDuty newFullDutyAction = createFullTripDriveDutyAction(timedTrip);
                newFullDutyAction.setRefTrip(timedTrip);
                //timedTrip.setFullTripDriveDutyAction(newFullDutyAction);
                newFullDutyAction.setTrip(duty);
                duty.addTripActionInOrder(newFullDutyAction);
                duty.actionChanged();
                if (duty.getTripId() == null) {
                    //this.getDutyFacade().create(duty);
                    entityManager.persist(duty);
                    duty.setCreating(true);
                    duties.add(duty);
//                    return duty;
                } else {
                    //this.getDutyFacade().edit(duty);
                    duty = entityManager.merge(duty);
                    duty.setEditing(true);
                    duties.add(duty);
                }
            }
        } else {
            timedTrip.setFullTripDriveDutyAction(null);
            if (oldFullDutyAction != null) {
                Duty oldDuty = (Duty) oldFullDutyAction.getTrip();
                oldDuty.removeTripAction(oldFullDutyAction);
                oldDuty.actionChanged();
                if (oldDuty.getNumberOfActions() > 0) {
                    //this.getDutyFacade().edit(oldDuty);
                    oldDuty = entityManager.merge(oldDuty);
                    oldDuty.setEditing(true);
                    duties.add(oldDuty);

                } else {
                    oldDuty = entityManager.merge(oldDuty);
                    entityManager.remove(oldDuty);
                    oldDuty.setRemoving(true);
                    duties.add(oldDuty);
                }
            }
        }
        return duties;
    }

    protected String createServiceAction(T service, TripAction tripAction) {
        try {
            //Only Run_trip Action allowed
            if (tripAction instanceof ActionRunTrip) {
                userTransaction.begin();
                entityManager.joinTransaction();
                if (tripAction.isCreating()) {
                    // valid is set to true when a new action is added initialize start time; now it is recovered to 0
                    tripAction.setTimesValid(false);
                }
                int seqNo = getTripSeqNo(tripAction);

                TripAction prevAction = null;

                // Check that there is previous action, atleast formation&glue should exists
                if (seqNo > 1) {
                    prevAction = service.getTripAction(seqNo - 1);
                }
                if (prevAction == null) {
                    JsfUtil.addErrorMessage(uiText.get("Error_StartTimeBeforeEndRun"));
                    return null;
                }

                TimedTrip timedTrip = (TimedTrip) ((ActionRunTrip) tripAction).getRefTrip();
                BasicTrip template = null;
                if (timedTrip instanceof TimedTrip) {
                    template = ((TimedTrip) timedTrip).getTripTemplate();
                }
                //Integer diff = template == null ? null : template.getDurationSecs();
                if (!validateServiceAction(tripAction, prevAction, ((ActionRunTrip) tripAction).getActionStartSecs())) {
                    return null;
                }

                tripAction.setSeqNo(seqNo);

                //the start time and stop time don't consider origo secs.
                timedTrip.setPlannedStartSecs(service.getPlannedStartSecs() + tripAction.getTimeFromTripStart());
                timedTrip.setPlannedStopSecs(service.getPlannedStartSecs() + tripAction.getTimeFromTripStart() + template.getDurationSecs());
                timedTrip.setPlannedStartObj(template.getPlannedStartObj());
                timedTrip.setPlannedStopObj(template.getPlannedStopObj());
                timedTrip.setDurationSecs(template.getDurationSecs());
                timedTrip.setOrigoSecs(template.getOrigoSecs());
                timedTrip.setAreaObj(template.getAreaObj());
                //timedTrip.setDayType(service.getDayType());
                timedTrip.setDayTypeList(service.getDayTypeList());
                if (service.getDayTypeList() != null && !service.getDayTypeList().isEmpty()) {
                    timedTrip.setDayTypeList(service.getDayTypeList());
                }
                timedTrip.setVersion(1);
                timedTrip.setTripType(GetToTripType(template.getTripType()));
                fillServiceAction(service, timedTrip); // Hook for scheduledservicecontroller

                timedTrip.setDescription(template.getPlannedStartObj().getText(languageBean.getOSLocale()) + " - " + template.getPlannedStopObj().getText(languageBean.getOSLocale()));

                newTripActionCreated(service, tripAction);
                // Previous action must be always GLUE
                // - Trips are rejected, since then it would overlap
                // - Formations are ignored and moved to glue
                if (prevAction instanceof ActionGlue) {
                    TripAction oldAction = service.getTripAction(tripAction.getSeqNo() + 1);

                    boolean creating = tripAction.isCreating();
                    boolean editing = tripAction.isEditing();
                    // Hide current tripAction from editing, that new glue add won't break things
                    tripAction.setCreating(false);
                    tripAction.setEditing(false);

                    // In case, next action would be trainformation, we need to update
                    // it's time, that add would success
                    if (oldAction instanceof ActionTrainFormation) {
                        oldAction.setTimeFromTripStart(tripAction.getTimeFromTripStart() + tripAction.getPlannedSecs() + (waitTurnEnabled ? glueWaitTurn.getDefaultPlanSecs() : glueWait.getDefaultPlanSecs()));
                    }
                    boolean glueadded = addGlueAction(service, tripAction, oldAction, true);
                    tripAction.setCreating(creating);
                    tripAction.setEditing(editing);
                    //Allows to add a run trip action as the beginniing run trip action
//                    if (tripAction.getSeqNo() == 3 && !(service.getTripAction(5) instanceof ActionTrainFormation)) {
//                        service.setPlannedStartSecs(service.getPlannedStartSecs() - tripAction.getPlannedSecs() - service.getTripAction(4).getPlannedSecs());
//                    }
                    // Previous actions duration must be set right, according to this actions 
                    // start time
                    prevAction.setPlannedSecs(tripAction.getTimeFromTripStart() - prevAction.getTimeFromTripStart());

                    if (!glueadded) {
                        return null;
                    }
                } else {
                    addGlueAction(service, prevAction, tripAction, true);
                    prevAction.setPlannedSecs(tripAction.getTimeFromTripStart() - prevAction.getTimeFromTripStart());
                }

                if (tripAction.getSeqNo() == 3) {
                    service.getTripAction(1).setTimetableObject(template.getPlannedStartObj());
                    service.getTripAction(2).setTimetableObject(template.getPlannedStartObj());
                    service.setPlannedStartObj(template.getPlannedStartObj());
                }
                if (tripAction.getSeqNo() == service.getNumberOfActions() - 2) {
                    service.getTripAction(service.getNumberOfActions()).setTimetableObject(template.getPlannedStopObj());
                    service.getTripAction(service.getNumberOfActions() - 1).setTimetableObject(template.getPlannedStopObj());
                    service.setPlannedStopObj(template.getPlannedStopObj());
                }
                service.updateActionTimes();
                service.updateTripTimes();
                service.updatePlannedEndTime();
                service.setDurationSecs(service.getPlannedStopSecs() - service.getPlannedStartSecs());
                service.increaseVersion();
                //getFacade().edit(service);
                service = this.entityManager.merge(service);
                service.setEditing(true);
                //service = getFacade().find(service.getTripId());
                List<Duty> duties = null;
                if (supportDuty) {
                    String fullDriveDutyDes = timedTrip.getFullTripDriveDutyDescription();
                    ActionRunTrip temp = (ActionRunTrip) (service.getTripAction(seqNo));
                    temp.getRefTrip().setFullTripDriveDutyDescription(fullDriveDutyDes);
                    duties = updateFullTripDriveDutyAction(temp.getRefTrip());

                }
                userTransaction.commit();
                xmlMessageSender.sendServiceDutyChangeMsg(service, duties);
                this.getDutyFacade().evictAll();
                recreateModel();
                JsfUtil.addSuccessMessage(uiText.get("ServiceActionCreated"));
                updateState(service);
            }
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
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

    private void newTripActionCreated(T service, TripAction tripAction) {
        Iterator<TripAction> iterator = service.getTripActions().iterator(); //sorted list
        TripAction temp;
        while (iterator.hasNext()) {
            temp = iterator.next();
            if (temp.isCreating() == false && temp.getSeqNo() >= tripAction.getSeqNo()) {
                temp.setSeqNo(temp.getSeqNo() + 1);
                // Adjust 'temp's start time ONLY if 'temp' is ActionTrainFormation and 
                // 'tripAction' inserted exists just BEFORE ActionTrainFormation.
                // IE. 'tripAction' is last action in sequence.
                // This allows new actions to be added at the end of service
                if (temp.getSeqNo() == tripAction.getSeqNo() + 1 && temp instanceof ActionTrainFormation) {
                    temp.setTimeFromTripStart(temp.getTimeFromTripStart() + tripAction.getPlannedSecs());
                }
            }
        }
    }

    protected boolean addGlueAction(T service, TripAction tripAction1, TripAction tripAction2, boolean beforeNewTripAction) {
        ActionGlue actionGlue = new ActionGlue();
        service.addTripAction(actionGlue, tripAction2.getSeqNo());//this index can be any number

        if (tripAction1.hasSecondObject()) {
            actionGlue.setTimetableObject(((ActionRunTrip) tripAction1).getTimetableObject2());
        } else {
            actionGlue.setTimetableObject(tripAction1.getTimetableObject());
        }

        actionGlue.setTimeFromTripStart(tripAction1.getTimeFromTripStart() + tripAction1.getPlannedSecs());

        if ((tripAction2 instanceof ActionTrainFormation) || (tripAction1 instanceof ActionTrainFormation)) {
            actionGlue.setActionType(glueWait);
            actionGlue.setMinSecs(glueWait.getDefaultMinSecs());
            actionGlue.setPlannedSecs(actionGlue.getPlannedSecs());
        } else {
            actionGlue.setPlannedSecs(tripAction2.getTimeFromTripStart() - actionGlue.getTimeFromTripStart());
            actionGlue.setActionType(waitTurnEnabled ? glueWaitTurn : glueWait);
            actionGlue.setMinSecs(this.waitTurnEnabled ? glueWaitTurn.getDefaultMinSecs() : glueWait.getDefaultMinSecs());

            //It could be minus when add a service at the first glue start time
            if (actionGlue.getPlannedSecs() < actionGlue.getMinSecs()) {
                actionGlue.setPlannedSecs(waitTurnEnabled ? glueWaitTurn.getDefaultMinSecs() : glueWait.getDefaultMinSecs());
            }
        }

        actionGlue.setSeqNo(tripAction2.getSeqNo());
        actionGlue.setConsumed(false);
        actionGlue.setTimesValid(false);//true: startTime and endTIme columns are not used at all.
        actionGlue.setCreating(true);
        newTripActionCreated(service, actionGlue);
        actionGlue.setCreating(false);
        return true;
    }

    @Override
    public String activateEdit(EditableEntity e) {
        super.activateEdit(e);
        this.selectedServiceStartTTObjectType = ((ServiceTrip) e).getPlannedStartObj().getTTObjectType();
        this.selectedServiceStopTTObjectType = ((ServiceTrip) e).getPlannedStopObj().getTTObjectType();
        return null;
    }

    public String activateEditServiceAction(TripAction serviceAction) {
        serviceAction.setEditing(true);
        selectedActionTTObjectType = serviceAction.getTimetableObject().getTTObjectType();
        if (serviceAction.hasSecondObject()) {
            selectedActionTTObjectType2 = ((ActionRunTrip) serviceAction).getTimetableObject2().getTTObjectType();
        }
        return null;
    }

    protected String updateServiceAction(T service, TripAction tripAction) {
        try {
            userTransaction.begin();
            entityManager.joinTransaction();
            List<Duty> duties = null;
            //Only Run_trip Action allowed
            if (tripAction instanceof ActionRunTrip) {
                TimedTrip timedTrip = ((TimedTrip) ((ActionRunTrip) tripAction).getRefTrip());
                BasicTrip template = timedTrip.getTripTemplate();
                Integer startSecs = ((ActionRunTrip) tripAction).getActionStartSecs();

                Integer seqNo = tripAction.getSeqNo();
                TripAction prevAction = null;
                TripAction afterAction = null;
                TripAction prevPrevAction = null;

                if (seqNo - 2 >= 0) {
                    prevAction = service.getTripAction(seqNo - 1);//Glue action
                    prevPrevAction = service.getTripAction(seqNo - 2);
//                    endTimeFromTripStart = startSecs + template.getDurationSecs() - service.getPlannedStartSecs();
                }

                if (!validateServiceAction(tripAction, prevAction, startSecs)) {
                    return null;
                }

                if (seqNo + 2 <= service.getNumberOfActions()) {
                    afterAction = service.getTripAction(seqNo + 1);//Glue action
                }

                timedTrip.setPlannedStartSecs(startSecs);
                timedTrip.setPlannedStopSecs(startSecs + template.getDurationSecs());

                timedTrip.setDurationSecs(template.getDurationSecs());
                if (template.getPlannedStartObj() != null) {
                    timedTrip.setPlannedStartObj(template.getPlannedStartObj());
                    timedTrip.setDescription(template.getPlannedStartObj().getText(languageBean.getOSLocale()));
                }
                if (template.getPlannedStopObj() != null) {
                    timedTrip.setPlannedStopObj(template.getPlannedStopObj());
                    timedTrip.setDescription(timedTrip.getDescription() + " - " + template.getPlannedStopObj().getText(languageBean.getOSLocale()));
                }
                //needed, since trip template might be changed
                timedTrip.setTripType(GetToTripType(template.getTripType()));

                timedTrip.increaseVersion();

                Integer endSecs = 0;
                if (prevPrevAction instanceof ActionTrainFormation) {
                    endSecs = ((ActionTrainFormation) prevPrevAction).getActionEndSecs();
                } else {
                    endSecs = ((ActionRunTrip) prevPrevAction).getActionEndSecs();
                }
                prevAction.setPlannedSecs(startSecs - endSecs);

                //remove for NCR3527
                if (tripAction.hasSecondObject()) {
                    afterAction.setTimetableObject(((ActionRunTrip) tripAction).getTimetableObject2());
                }

                if (tripAction.getSeqNo() == 3) {
                    service.getTripAction(1).setTimetableObject(template.getPlannedStartObj());
                    service.getTripAction(2).setTimetableObject(template.getPlannedStartObj());
                    service.setPlannedStartObj(template.getPlannedStartObj());
                }
                if (tripAction.getSeqNo() == service.getNumberOfActions() - 2) {
                    service.getTripAction(service.getNumberOfActions()).setTimetableObject(template.getPlannedStopObj());
                    service.getTripAction(service.getNumberOfActions() - 1).setTimetableObject(template.getPlannedStopObj());
                    service.setPlannedStopObj(template.getPlannedStopObj());
                }

                service.updateActionTimes();
                service.updateTripTimes();
                service.updatePlannedEndTime();
                service.setDurationSecs(service.getPlannedStopSecs() - service.getPlannedStartSecs());

                service.increaseVersion();
                service.setEditing(true);
                service = entityManager.merge(service);

                if (supportDuty) {
                    duties = updateFullTripDriveDutyAction(timedTrip);
                }
            } else if (tripAction instanceof ActionTrainFormation) {

                Integer startSecs = ((ActionTrainFormation) tripAction).getActionStartSecs();

                if (tripAction.getSeqNo() == 1) {
                    TripAction afterAction = service.getTripAction(2);
                    if (afterAction instanceof ActionGlue) {
                        afterAction.setTimetableObject(tripAction.getTimetableObject());

                        service.setPlannedStartSecs(startSecs);
                        service.updateActionTimes();
                        service.updateTripTimes();
                        service.updatePlannedEndTime();
                        service.setDurationSecs(service.getPlannedStopSecs() - service.getPlannedStartSecs());

                        if (supportDuty) {
                            Iterator<TripAction> iterator = service.getTripActions().iterator();
                            while (iterator.hasNext()) {
                                TripAction tripAction1 = iterator.next();
                                if (tripAction1 instanceof ActionRunTrip) {
                                    TimedTrip timedTrip = (TimedTrip) ((ActionRunTrip) tripAction1).getRefTrip();
                                    duties = updateFullTripDriveDutyAction(timedTrip);
                                }
                            }
                        }
                    }
                } else {
                    TripAction beforeAction = service.getTripAction(tripAction.getSeqNo() - 1);
                    TripAction beforeBeforeAction = service.getTripAction(tripAction.getSeqNo() - 2);

                    if (beforeAction instanceof ActionGlue) {
                        if (startSecs < ((ServiceActionBase) beforeBeforeAction).getActionEndSecs()) {
                            if (beforeBeforeAction instanceof ActionRunTrip) {
                                JsfUtil.addErrorMessage(uiText.get("Error_StartTimeBeforePrevEndTime"));
                                return null;
                            } else {
                                JsfUtil.addErrorMessage(uiText.get("Error_StartTimeBeforeEndRun"));
                                return null;
                            }
                        }

                        beforeAction.setPlannedSecs(startSecs - ((ActionGlue) beforeAction).getActionStartSecs());
                        
                        service.setPlannedStopSecs(startSecs + tripAction.getPlannedSecs());
                    }
                }
                service.increaseVersion();
                service.setEditing(true);
                entityManager.merge(service);

            } else if (tripAction instanceof ActionGlue) {

            }
            userTransaction.commit();
            xmlMessageSender.sendServiceDutyChangeMsg(service, duties);
            recreateModel();
            JsfUtil.addSuccessMessage(uiText.get("ServiceActionUpdated"));
            updateState(service);

            this.getDutyFacade().evictAll();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
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

    // Called from List, when service is being edited/created and save is selected
    @Override
    public String save(T service) {
        if (service.isCreating()) {
            create(service);
        } else {
            try {
                userTransaction.begin();
                entityManager.joinTransaction();
                service.setPlannedStopSecs(service.getPlannedStartSecs() + service.getDurationSecs());
                //current.setDurationSecs(current.getPlannedStopSecs() - current.getPlannedStartSecs());
                ActionTrainFormation trainFormationStart = (ActionTrainFormation) service.getTripActions().get(0);
                trainFormationStart.setTimetableObject(service.getPlannedStartObj());
                ActionGlue actionGlue = (ActionGlue) service.getTripActions().get(1);
                actionGlue.setTimetableObject(service.getPlannedStartObj());

                ActionTrainFormation trainFormationStop = (ActionTrainFormation) service.getTripActions().get(service.getTripActions().size() - 1);
                trainFormationStop.setTimetableObject(service.getPlannedStopObj());
                if (service.getTripActions().size() - 2 != 1) {
                    actionGlue = (ActionGlue) service.getTripActions().get(service.getTripActions().size() - 2);
                    actionGlue.setTimetableObject(service.getPlannedStopObj());
                }

                //boolean useDuty = service.getServiceDutyDescription() != null && service.getServiceDutyDescription().trim().length() > 0;
                Iterator<TripAction> iterator = service.getTripActions().iterator();
                List<Duty> duties = new ArrayList<>();
                while (iterator.hasNext()) {
                    TripAction tripAction = iterator.next();
                    if (tripAction instanceof ActionRunTrip) {
                        TimedTrip timedTrip = (TimedTrip) ((ActionRunTrip) tripAction).getRefTrip();
                        timedTrip.setPlannedStartSecs(service.getPlannedStartSecs() + tripAction.getTimeFromTripStart());
                        timedTrip.setPlannedStopSecs(service.getPlannedStartSecs() + tripAction.getTimeFromTripStart() + tripAction.getPlannedSecs());
                        timedTrip.setTrainType(service.getTrainType());
                        // update daytypeList
                        if(service.getDayTypeList() != timedTrip.getDayTypeList()) {
                            timedTrip.setDayTypeList(service.getDayTypeList());
                        }
                        if (supportDuty) {
                            duties = updateFullTripDriveDutyAction(timedTrip);
                            this.getDutyFacade().evictAll();
                        }
                    }
                }
                service.increaseVersion();

                //getFacade().edit(service);
                service = entityManager.merge(service);
                service.setEditing(true);
                userTransaction.commit();
                xmlMessageSender.sendServiceDutyChangeMsg(service, duties);
                updateState(service);
                this.getDutyFacade().evictAll();
                JsfUtil.addSuccessMessage(uiText.get("ServiceUpdated"));
              
                this.getDutyFacade().evictAll();
            } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
                try {
                    userTransaction.rollback();
                } catch (IllegalStateException | SecurityException | SystemException ex1) {
                    getLogger().log(Level.SEVERE, null, ex1);
                }
                getLogger().log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage(ex, uiText.get("PersistenceErrorOccured"));
            }

        }

        recreateModel();
        return null;
    }

    // Called from View, when action is being edited/created and save is selected
    public String saveServiceAction(T service, TripAction tripAction) {
            
        if (tripAction instanceof ActionRunTrip) {
            TimedTrip refTrip = (TimedTrip) ((ActionRunTrip) tripAction).getRefTrip();
            
            if (refTrip.getActionsFromTemplate() != null && refTrip.getActionsFromTemplate() && refTrip.getTripTemplate() == null) {
                JsfUtil.addErrorMessage(uiText.get("Error_TripTemplateNotExist"));
                return null;
            }
            eventLog.addEvent(refTrip.getDescription(), refTrip.getTripDescriber());
            /*tähän pitäsi katsoa kun trip kkodia muutetaan nin pitää tallentaa myös trip!!
            eli UI ssa input text refTrip.tripCode
                    output text runtrip.setNewTripcode ..
                    joka voiddan tässä tutkia ja tallentaa timedtrip myös!!*/
        }

        if (tripAction.isCreating()) {
            createServiceAction(service, tripAction);
        } else {
            updateServiceAction(service, tripAction);
        }

        return null;
    }

    public String cancel(T service) {
        try {
            updateState(service);
            recreateModel();
            service = this.getFacade().find(service.getTripId());
            this.selectedServiceStartTTObjectType = service.getPlannedStartObj().getTTObjectType();
            this.selectedServiceStopTTObjectType = service.getPlannedStopObj().getTTObjectType();
        } catch (Exception ex) {
        }
        return "List";
    }

    public String cancelServiceAction(T service, TripAction tripAction) {
        //This is needed. e.g. edit a start location which leads to empty template-> submit->cacel->edit: the start location will be recovered.
        try {
            updateState(service);
            service = this.getFacade().find(service.getTripId());
            recreateModel();
            tripAction = service.getTripAction(tripAction.getSeqNo());
            this.selectedActionTTObjectType = tripAction.getTimetableObject().getTTObjectType();
            this.selectedActionTTObjectType2 = tripAction.getTimetableObject2().getTTObjectType();
        } catch (Exception ex) {

        }
        return null;
    }

    protected Integer getLastActionRunTripEndTime(ServiceTrip service) {
        if (service.getTripActions() != null) {
            Iterator<TripAction> ite = service.getTripActions().iterator();

            int totalSecs = 0;
            while (ite.hasNext()) {
                TripAction curAction = ite.next();

                curAction.setTimeFromTripStart(totalSecs);

                if (curAction.getPlannedSecs() != null && curAction.getSeqNo() != null && curAction.getSeqNo() != service.getNumberOfActions()) {
                    totalSecs += curAction.getPlannedSecs();
                }

            }
            return totalSecs;
        }
        return 0;
    }

    // This needs to be in final class
    public abstract String addNewServiceAction(T service);

    private List<MovementTrip> findFormationMovementTrips(TripAction tripAction) {
        if (tripAction.getTimetableObject() == null) {
            return new ArrayList<>();
        }

        TTObject startObj = tripAction.getTimetableObject();
        MovementTripFilter filter = new MovementTripFilter();
        filter.setPlannedStartObjFilter(startObj);
        filter.setPlannedStopObjFilter(startObj);
        //filter.setAreaFilter(tripAction.getTrip().getAreaObj());

        List<MovementTrip> list = ejbMovementTripFacade.findAll(filter);
        Iterator<MovementTrip> iterator = list.iterator();
        while (iterator.hasNext()) {
            MovementTrip temp = iterator.next();
            if (temp.getTripType().getTripSubType() != MovementTrip.MovementTripSubType.FORMATION.getValue()) {
                iterator.remove();
            }
        }
        return list;
    }

    public List<BasicTrip> getTripTemplates(TripAction tripAction) {
        if (tripAction.getTimetableObject() == null || (tripAction.hasSecondObject() && ((ActionRunTrip) tripAction).getTimetableObject2() == null)) {
            return new ArrayList<>();
        }

        TTObject startObj = tripAction.getTimetableObject();
        BasicTripFilter filter = new BasicTripFilter();
        filter.setPlannedStartObjFilter(startObj);
        //filter.setAreaFilter(tripAction.getTrip().getAreaObj()); // Check parents area, if so we need to set location to action even when object does not contain objects
        if (tripAction.hasSecondObject()) {
            filter.setPlannedStopObjFilter(((ActionRunTrip) tripAction).getTimetableObject2());
        }
        TripType tripType = null;
        if ((tripAction.getActionType().getActionSubtype() == ActionRunTrip.ActionRunTripTypeEnum.RUN_COMMERCIAL_TRIP.getValue())
                || (tripAction.getActionType().getActionSubtype() == ActionRunTrip.ActionRunTripTypeEnum.TURN_AND_RUN_COMMERCIAL_TRIP.getValue())) {
            tripType = this.commercialTripType;
        } else if ((tripAction.getActionType().getActionSubtype() == ActionRunTrip.ActionRunTripTypeEnum.RUN_NONCOMMERCIAL_TRIP.getValue())
                || (tripAction.getActionType().getActionSubtype() == ActionRunTrip.ActionRunTripTypeEnum.TURN_AND_RUN_NONCOMMERCIAL_TRIP.getValue())) {
            tripType = this.nonCommercialTripType;
        }

        List<BasicTrip> list = this.ejbTripFacade.findAll(filter);
        if (tripType != null) {
            Iterator<BasicTrip> iterator = list.iterator();
            while (iterator.hasNext()) {
                BasicTrip temp = iterator.next();
                if (temp.getTripType().getToTripType() != tripType.getTripType().ordinal()) {
                    iterator.remove();
                }
            }
        }
        return list;
    }

    public List<MovementTrip> getFormationMovementTrips(TripAction tripAction) {
        List<MovementTrip> list = findFormationMovementTrips(tripAction);
        return list;
    }

    public String destroyServiceAction(TripAction tripAction) {

        T service = (T) tripAction.getTrip();
        if (!tripAction.isCreating()) {
            try {
                userTransaction.begin();
                entityManager.joinTransaction();
                List<Duty> duties = new ArrayList<>();
                TripAction prevAction = null;
                TripAction afterAction = null;
                Integer seqNo = tripAction.getSeqNo();
                if (seqNo - 2 >= 0) {
                    prevAction = service.getTripAction(seqNo - 1);
                }
                if (seqNo < service.getTripActions().size()) {
                    afterAction = service.getTripAction(seqNo + 1);
                }

                if (prevAction != null && afterAction != null && prevAction instanceof ActionGlue && afterAction instanceof ActionGlue) {
                    TripAction prevPrevAction = service.getTripAction(seqNo - 2); //RunTripAction or Formation
                    TripAction afterAfterAction = service.getTripAction(seqNo + 2);//RunTripAction or Formation

                    //Trip refTrip = ((ActionRunTrip) tripAction).getRefTrip();
                    service.removeTripAction(prevAction);

                    if (tripAction instanceof ActionRunTrip) {
                        TimedTrip timedTrip = ((ActionRunTrip) tripAction).getRefTrip();
                        if (timedTrip.getFullTripDriveDutyAction() != null) {
                            ActionFullTripDriveDuty fullTripDriveDutyAction = timedTrip.getFullTripDriveDutyAction();
                            Duty duty = (Duty) fullTripDriveDutyAction.getTrip();
                            timedTrip.setFullTripDriveDutyAction(null);
                            fullTripDriveDutyAction.setTrip(null);
                            duty.removeTripAction(fullTripDriveDutyAction);
                            duty.actionChanged();
                            if (duty.getNumberOfActions() > 0) {
                                //this.getDutyFacade().edit(duty);
                                duty = entityManager.merge(duty);
                                duty.setEditing(true);
                                duties.add(duty);
                            } else {
                                //this.getDutyFacade().remove(duty);
                                duty = entityManager.merge(duty);
                                entityManager.remove(duty);
                                duty.setRemoving(true);
                                duties.add(duty);
                            }
                            //We have to do this. Otherwise, it complains the fullTripDriveDutyAction is still being used when commit.
                            entityManager.flush();
                        }
                        // remove daytypes
                        timedTrip.getDayTypeList().clear();
                        entityManager.merge(timedTrip);
                    }

                    service.removeTripAction(tripAction);
                    service.removeTripAction(afterAction);
                    if (!(afterAfterAction instanceof ActionTrainFormation) && !(prevPrevAction instanceof ActionTrainFormation)) {
                        afterAfterAction.setTimeFromTripStart(afterAfterAction.getTimeFromTripStart() +/* tripAction.getPlannedSecs() + afterAction.getPlannedSecs() + prevAction.getPlannedSecs()*/ + +(waitTurnEnabled ? glueWaitTurn.getDefaultPlanSecs() : this.glueWait.getDefaultPlanSecs()));
                    } else {
                        afterAfterAction.setTimeFromTripStart(afterAfterAction.getTimeFromTripStart() + this.glueWait.getDefaultPlanSecs());
                        afterAfterAction.setTimetableObject(prevPrevAction.getTimetableObject2() != null ? prevPrevAction.getTimetableObject2() : prevPrevAction.getTimetableObject());
                        service.setPlannedStopObj(afterAfterAction.getTimetableObject());
                    }
                    addGlueAction(service, prevPrevAction, afterAfterAction, false);

                    service.updatePlannedEndTime();
                    service.setDurationSecs(service.getPlannedStopSecs() - service.getPlannedStartSecs());

                    service.increaseVersion();
                    entityManager.merge(service);
                    service.setEditing(true);
                    userTransaction.commit();
                    service.updateActionTimes();
                    xmlMessageSender.sendServiceDutyChangeMsg(service, duties);

                    JsfUtil.addSuccessMessage(uiText.get("ServiceActionDeleted"));
                }

            } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
                try {
                    userTransaction.rollback();
                } catch (IllegalStateException | SecurityException | SystemException ex1) {
                    getLogger().log(Level.SEVERE, null, ex1);
                }
                getLogger().log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage(ex, uiText.get("PersistenceErrorOccured"));
            }
        }

        updateState(service);
        this.getDutyFacade().evictAll();
        recreateModel();
        return null;
    }

    public List<Schedule> getSchedules() {
        ScheduleFilter scheduleFilter = new ScheduleFilter();
        scheduleFilter.setValid(Boolean.TRUE);
        List<Schedule> schedules = ejbScheduleFacade.findAll(scheduleFilter);

        if (schedules == null) {
            schedules = new ArrayList<>();
        }
        return schedules;
    }
    // No all for schedule

    public List<DayType> getDayTypes() {
        if (getFilter().getScheduleFilter() != null) {
            return getFilter().getScheduleFilter().getDayTypes();
        } else {
            return null;
        }
    }

    public DayType getDayTypeAll() {
        return new DayType(0, uiText.get("FilterAll"), uiText.get("FilterAll"));
    }

    public TrainType getTrainTypeAll() {
        return new TrainType(0, uiText.get("FilterAll"), "");
    }

    public List<TrainType> getTrainTypes() {
        // Always create a new list, since it can be changed 
        TrainTypeFilter filter = new TrainTypeFilter();
        filter.setCanBeConsist(true);
        List<TrainType> trainTypes = ejbTrainTypeFacade.findAll(filter);
        
        return trainTypes;
    }

    public List<TTArea> getAreas() {
        return ejbTTAreaFacade.findAll();
    }

    public TTArea getAreaAll() {
        return new TTArea(0, uiText.get("FilterAll"));
    }

    public List<SchedulingState> getSchedulingStates() {
        return ejbSchedulingStateFacade.findAll();
    }

    public SchedulingState getSchedulingStateAll() {
        return new SchedulingState(0, uiText.get("FilterAll"));
    }

}
