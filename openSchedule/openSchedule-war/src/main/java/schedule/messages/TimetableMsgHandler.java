/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.messages;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.LocalBean;
import javax.inject.Inject;
import schedule.entities.ActionCargoStop;
import schedule.entities.ActionGlue;
import static schedule.entities.ActionGlue.ActionGlueTypeEnum.GLUE_WAIT;
import schedule.entities.ActionPassObject;
import schedule.entities.ActionPassengerStop;
import schedule.entities.ActionRunTrip;
import static schedule.entities.ActionRunTrip.ActionRunTripTypeEnum.RUN_NONCOMMERCIAL_TRIP;
import schedule.entities.ActionSchedulingStop;
import schedule.entities.ActionTrainMoving;
import schedule.entities.ActionType;
import schedule.entities.BasicTrip;
import schedule.entities.PlannedService;
import schedule.entities.PlannedTrip;
import schedule.entities.ScheduledDay;
import schedule.entities.ScheduledService;
import schedule.entities.SchedulingState;
import schedule.entities.TTObject;
import schedule.entities.TimedTrip;
import schedule.entities.Trip;
import schedule.entities.TripAction;
import static schedule.entities.TripAction.MainActionTypeEnum.GLUE;
import static schedule.entities.TripAction.MainActionTypeEnum.PASSENGER_STOP;
import static schedule.entities.TripAction.MainActionTypeEnum.PASS_OBJECT;
import static schedule.entities.TripAction.MainActionTypeEnum.RUN_TRIP;
import static schedule.entities.TripAction.MainActionTypeEnum.SCHEDULING_STOP;
import static schedule.entities.TripAction.MainActionTypeEnum.TRAIN_MOVING;
import schedule.sessions.ActionTypeFacade;
import schedule.sessions.PlannedServiceFacade;
import schedule.sessions.PlannedTripFacade;
import schedule.sessions.ScheduledDayFacade;
import schedule.sessions.ScheduledServiceFacade;
import schedule.sessions.SchedulingStateFacade;
import schedule.sessions.TripActionFacade;
import schedule.sessions.TripFacade;
import schedule.sessions.TripTypeFacade;
import schedule.uiclasses.ActionTypeFilter;
import schedule.uiclasses.BasicTripFilter;
import schedule.uiclasses.ScheduledServiceFilter;

/**
 *
 * @author jia
 */
@Stateless
@LocalBean
public class TimetableMsgHandler {

    @Inject
    private XmlMessageSender xmlMessageSender;
    @Inject
    private TripFacade ejbTripTemplateFacade;
    @Inject
    private PlannedTripFacade ejbPlannedTripFacade;
    @Inject
    private PlannedServiceFacade ejbPlannedServiceFacade;
    @Inject
    private ActionTypeFacade ejbActionTypeFacade;
    @Inject
    private TripTypeFacade ejbTripTypeFacade;
    @Inject
    private TripActionFacade ejbTripActionFacade;
    @Inject
    private ScheduledServiceFacade ejbScheduledServiceFacade;
    @Inject
    private ScheduledDayFacade ejbScheduledDayFacade;
    //@Inject
    //private PlannedServiceController plannedServiceController;
    @Inject
    private SchedulingStateFacade ejbSchedulingStateFacade;
    SchedulingState DEFAULT_STATE;

    static final Logger logger = Logger.getLogger("TimetableMsgHandler");
    // <editor-fold defaultstate="collapsed" desc="Default data objects">

    ActionType RUN_NONCOMMERCIAL_TRIP_ACTION;
    ActionType GLUE_WAIT_ACTION;

    // </editor-fold>
    @PostConstruct
    private void init() {

        RUN_NONCOMMERCIAL_TRIP_ACTION = GetActionType(RUN_TRIP, RUN_NONCOMMERCIAL_TRIP.getValue());
        DEFAULT_STATE = ejbSchedulingStateFacade.find(SchedulingState.State.PLANNED_TO_RUN.getStateValue());
        GLUE_WAIT_ACTION = GetActionType(GLUE, GLUE_WAIT.getValue());

        /*TripTypeFilter tripTypeFilter = new TripTypeFilter();
        tripTypeFilter.setValid(true);
        tripTypeFilter.setOnlyMainTripTypeFilter(true);
        tripTypeFilter.setTripType(TripType.TripMainType.MOVEMENT_TRIP_GROUP);
        MOVEMENT_TRIP_GROUP = this.ejbTripTypeFacade.findFirst(tripTypeFilter);

        MainActionType movementTripActionMainType = ejbActionMainTypeFacade.find(TripAction.MainActionTypeEnum.MOVEMENT_TRIP_ACTION.getValue());
        if (movementTripActionMainType != null && movementTripActionMainType.isUsed()) {
            //this.selectedMovementTripActionType = movementTripActionMainType.getActionTypes().get(0);
            ActionTypeFilter actionTypeFilter = new ActionTypeFilter();
            actionTypeFilter.setTypeFilter(movementTripActionMainType);
            actionTypeFilter.setActionSubTypeFilter(ActionMovementTrip.ActionMovementTripTypeEnum.DEFAULT.getValue());
            DEFAULT_MOVEMENT_TRIP_ACTION = this.ejbActionTypeFacade.findAll(actionTypeFilter).get(0);
            actionTypeFilter.setActionSubTypeFilter(ActionMovementTrip.ActionMovementTripTypeEnum.ALTERNATIVE.getValue());
            ALTERNATIVE_MOVEMENT_TRIP_ACTION = this.ejbActionTypeFacade.findAll(actionTypeFilter).get(0);
        }*/
    }

    private ActionType GetActionType(TripAction.MainActionTypeEnum mainActionTypeEnum, Integer subType) {
        ActionTypeFilter filter = new ActionTypeFilter();
        filter.setTypeFilter(mainActionTypeEnum);
        filter.setActionSubTypeFilter(subType);
        return ejbActionTypeFacade.findFirst(filter);
    }

    private ActionGlue createGlueAction(TTObject loc) {
        ActionGlue tripAction = new ActionGlue();
        tripAction.setConsumed(false);
        tripAction.setTimesValid(false);
        tripAction.setCreating(true);
        tripAction.setEditing(true);
        tripAction.setActionType(GLUE_WAIT_ACTION);
        tripAction.setTimetableObject(loc);
        tripAction.setMinSecs(GLUE_WAIT_ACTION.getDefaultMinSecs());
        tripAction.setPlannedSecs(0);
        return tripAction;
    }

    public void handleEditPlannedTripsRequest(String requestId, List<RequestItem> requestItems, String replyTo) {
        boolean serviceChanged = false;
        // if two requestItem has the same plannedtrip, the second one will be null as the transaction is not finished yet.
        Map<Integer, PlannedTrip> plannedTrips = new HashMap<>();
        Map<Integer, BasicTrip> tripTemplates = new HashMap<>();
        for (Object item : requestItems) {
            EditPlannedTripsRequest request = ((EditPlannedTripsRequest) item);
            PlannedService plannedService = null;
            for (EditPlannedTripsRequestItem requestItem : request.getItems()) {
                PlannedTrip plannedTrip = plannedTrips.get(requestItem.getTripId());
                if (plannedTrip == null) {
                    plannedTrip = ejbPlannedTripFacade.find(requestItem.getTripId());
                    plannedTrips.put(plannedTrip.getTripId(), plannedTrip);
                }
                BasicTrip tripTemplate = tripTemplates.get(requestItem.getTemplateId());
                if (tripTemplate == null) {
                    tripTemplate = ejbTripTemplateFacade.find(requestItem.getTemplateId());
                    tripTemplates.put(requestItem.getTemplateId(), tripTemplate);
                }
                if (plannedTrip == null) {
                    this.xmlMessageSender.sendPlannedTripResponseMsg(requestId, replyTo, request.getRequestId(), false, plannedService.getTripId());
                    return;
                }

                plannedService = (PlannedService) (plannedTrip.getServiceAction().getTrip());
                if (requestItem.getTemplateId() != 0) {//injection or withdrawl request
                    TripAction actionInTemplate = ejbTripActionFacade.find(requestItem.getActionId());//Action in old template used by planned trip
                    if (plannedTrip != null && tripTemplate != null) {
                        Integer seqNo = plannedTrip.getServiceAction().getSeqNo();
                        logger.log(Level.INFO, "handleEditPlannedTripsRequest: tripId {0}, seqNo {1}, from {2} to {3}", new Object[]{plannedTrip.getTripId(), seqNo, plannedTrip.getPlannedStartObj().getScheduleName(), plannedTrip.getPlannedStopObj().getScheduleName()});
                        logger.log(Level.INFO, "handleEditPlannedTripsRequest: templateId {0}, action in template seqNo {1}", new Object[]{tripTemplate.getTripId(), actionInTemplate.getSeqNo()});
                        //remove actions before
                        switch (tripTemplate.getTripType().getTripSubType()) {
                            case 1: {
                                removeActionsFromBeginning(plannedService, seqNo);

                                Iterator<TripAction> it;
                                it = plannedService.getActionsIterator();

                                while (it.hasNext()) {
                                    TripAction tripAction = it.next();
                                    if (tripAction.getSeqNo() > 1) {
                                        tripAction.setSeqNo(tripAction.getSeqNo() + 4);
                                    }
                                }

                                ActionGlue glueAction1 = createGlueAction(tripTemplate.getPlannedStartObj());
                                glueAction1.setSeqNo(2);
                                glueAction1.setTrip(plannedService);
                                plannedService.getTripActions().add(glueAction1);

                                TripAction injectionTripAction = this.createTripActionFromTemplate(plannedService, tripTemplate);
                                injectionTripAction.setSeqNo(3);

                                plannedService.getTripActions().add(injectionTripAction);

                                ActionGlue glueAction2 = createGlueAction(injectionTripAction.getTimetableObject2());
                                glueAction2.setSeqNo(4);
                                glueAction2.setTrip(plannedService);
                                plannedService.getTripActions().add(glueAction2);

                                TripAction newTripAction = cutTripAction((ActionRunTrip) (plannedTrip.getServiceAction()), actionInTemplate.getSeqNo(), true);
                                newTripAction.setSeqNo(5);
                                newTripAction.setTrip(plannedService);
                                plannedService.getTripActions().add(newTripAction);

                                Integer startSecs = plannedTrip.getPlannedStopSecs() - newTripAction.getPlannedSecs() - glueAction2.getPlannedSecs() - injectionTripAction.getPlannedSecs() - glueAction1.getPlannedSecs();
                                plannedService.setPlannedStartSecs(startSecs);
                                plannedService.getTripAction(1).setTimetableObject(tripTemplate.getPlannedStartObj());
                                plannedService.setPlannedStartObj(tripTemplate.getPlannedStartObj());

                                sort(plannedService.getTripActions());
                                plannedService.updateActionTimes();
                                TripAction lastAction = plannedService.getTripAction(plannedService.getNumberOfActions());
                                plannedService.setDurationSecs(lastAction.getTimeFromTripStart() + lastAction.getPlannedSecs());
                                plannedService.setPlannedStartSecs(plannedService.getPlannedStopSecs() - plannedService.getDurationSecs());
                                plannedService.updateTripTimes();
                                serviceChanged = true;
                                break;
                            }
                            case 2: {

                                if (seqNo <= plannedService.getNumberOfActions() - 2) {
                                    removeActionsToEnd(plannedService, seqNo);

                                    Iterator<TripAction> it = plannedService.getActionsIterator();
                                    while (it.hasNext()) {
                                        TripAction tripAction = it.next();
                                        if (tripAction.getSeqNo() >= seqNo) {
                                            tripAction.setSeqNo(tripAction.getSeqNo() + 4);
                                        }
                                    }

                                    TripAction newTripAction = cutTripAction((ActionRunTrip) (plannedTrip.getServiceAction()), actionInTemplate.getSeqNo(), false);
                                    newTripAction.setSeqNo(seqNo);
                                    newTripAction.setTrip(plannedService);
                                    plannedService.getTripActions().add(newTripAction);

                                    ActionGlue glueAction = createGlueAction(newTripAction.getTimetableObject2());
                                    glueAction.setSeqNo(seqNo + 1);
                                    glueAction.setTrip(plannedService);
                                    plannedService.getTripActions().add(glueAction);

                                    TripAction withdrawalTripAction = this.createTripActionFromTemplate(plannedService, tripTemplate);
                                    withdrawalTripAction.setSeqNo(seqNo + 2);
                                    plannedService.getTripActions().add(withdrawalTripAction);

                                    glueAction = createGlueAction(withdrawalTripAction.getTimetableObject2());
                                    glueAction.setSeqNo(seqNo + 3);
                                    glueAction.setTrip(plannedService);
                                    plannedService.getTripActions().add(glueAction);

                                    plannedService.getTripAction(plannedService.getNumberOfActions()).setTimetableObject(glueAction.getTimetableObject());
                                    plannedService.setPlannedStopObj(glueAction.getTimetableObject());
                                }

                                sort(plannedService.getTripActions());
                                plannedService.updateActionTimes();
                                plannedService.updatePlannedEndTime();
                                plannedService.updateTripTimes();
                                plannedService.setDurationSecs(plannedService.getPlannedStopSecs() - plannedService.getPlannedStartSecs());
                                serviceChanged = true;
                                break;
                            }
                            default:
                                logger.log(Level.INFO, "TimetableMsgHandler:handleEditPlannedTripsRequest invalid trip tempalte type: {0}", tripTemplate.getTripType().getTripTypeId());
                                break;
                        }
                        /*if (serviceChanged) {
                            plannedService.increaseVersion();
                            this.ejbPlannedServiceFacade.edit(plannedService);
                        }*/
                    }
                } else {//modifty dwelll time request
                    Trip service = plannedTrip.getServiceAction().getTrip();
                    TripAction glueAction = service.getTripAction(plannedTrip.getServiceAction().getSeqNo() - 1);
                    int minDwell = glueAction.getMinSecs();
                    /*int nextDwell = 0;
                    int prevDwell = 0;
                    tripTemplate = plannedTrip.getTripTemplate();
                    TripAction nextAction = tripTemplate.getTripAction(1);
                    if (nextAction != null && !nextAction.hasSecondObject()) {
                        nextDwell = nextAction.getPlannedSecs();
                        minDwell += nextDwell;
                    }
                    if (plannedTrip.getServiceAction().getSeqNo() > 3) {
                        TripAction previousRunTrip = service.getTripAction(plannedTrip.getServiceAction().getSeqNo() - 2);
                        if (previousRunTrip != null && previousRunTrip instanceof ActionRunTrip) {
                            BasicTrip previousTemplate = ((TimedTrip) ((ActionRunTrip) previousRunTrip).getRefTrip()).getTripTemplate();
                            if (previousTemplate != null) {
                                TripAction prevAction = previousTemplate.getTripAction(previousTemplate.getNumberOfActions());
                                if (prevAction != null && !prevAction.hasSecondObject()) {
                                    prevDwell = prevAction.getPlannedSecs();
                                    minDwell += prevDwell;
                                }
                            }
                        }
                    }*/

                    int glueDwell = glueAction.getPlannedSecs() + requestItem.getDwellChange() < minDwell ? minDwell : glueAction.getPlannedSecs() + requestItem.getDwellChange();
                    /*if (plannedTrip.getServiceAction().getSeqNo() == 3) {
                        plannedService.setPlannedStartSecs(plannedService.getPlannedStartSecs() + glueDwell -glueAction.getPlannedSecs());
                    }*/
                    glueAction.setPlannedSecs(glueDwell);
                    plannedService.updateActionTimes();
                    plannedService.updatePlannedEndTime();
                    plannedService.updateTripTimes();
                    plannedService.setDurationSecs(plannedService.getPlannedStopSecs() - plannedService.getPlannedStartSecs());
                    serviceChanged = true;
                }
            }

            if (serviceChanged && plannedService != null) {
                plannedService.increaseVersion();
                this.ejbPlannedServiceFacade.edit(plannedService);
                this.ejbTripTemplateFacade.evictAll();
                this.ejbPlannedServiceFacade.evict(plannedService);
                this.ejbPlannedTripFacade.evictAll();
                //xmlMessageSender.sendServiceMsg(plannedService, Operation.MODIFY, ServiceType.PLANNED, null);
                xmlMessageSender.sendServiceChangeMsg(plannedService, Operation.MODIFY);
                this.xmlMessageSender.sendPlannedTripResponseMsg(requestId, replyTo, request.getRequestId(), true, plannedService.getTripId());
            }
        }

    }

    private void sort(List<TripAction> tripActions) {
        TripAction temp;

        int n = tripActions.size();

        for (int i = 0; i < n; i++) {
            for (int j = 1; j < (n - i); j++) {

                if (tripActions.get(j - 1).getSeqNo() > tripActions.get(j).getSeqNo()) {
                    //swap the elements!
                    temp = tripActions.get(j - 1);
                    tripActions.set(j - 1, tripActions.get(j));
                    tripActions.set(j, temp);
                }

            }
        }
    }

    private Integer removeActionsFromBeginning(PlannedService plannedService, Integer seqNo) {
        Iterator<TripAction> it = plannedService.getActionsIterator();
        Integer removed = 0;
        //Integer secsRemoved = 0;
        while (it.hasNext()) {
            TripAction tripAction = it.next();
            if (tripAction.getSeqNo() <= seqNo) { // keep the glue action(seqno -1)
                if (tripAction.getSeqNo() > 1) { // keep the first train formation action
                    //          secsRemoved += tripAction.getPlannedSecs();
                    it.remove();
                    removed++;
                }
            } else //ordered list
            {
                break;
            }
        }

        //plannedService.setPlannedStartSecs(plannedService.getPlannedStartSecs() - secsRemoved);
        if (removed > 0) {
            it = plannedService.getActionsIterator();
            while (it.hasNext()) {
                TripAction tripAction = it.next();
                if (tripAction.getSeqNo() > seqNo) {
                    tripAction.setSeqNo(tripAction.getSeqNo() - removed);
                }
            }
        }
        //plannedService.updateActionTimes();
        //plannedService.updatePlannedEndTime();
        //plannedService.setDurationSecs(plannedService.getPlannedStopSecs() - plannedService.getPlannedStartSecs());
        return seqNo - removed;
    }

    private Integer removeActionsToEnd(PlannedService plannedService, Integer seqNo) {
        Iterator<TripAction> it = plannedService.getActionsIterator();
        Integer removed = 0;
        Integer numberOfActions = plannedService.getNumberOfActions();
        while (it.hasNext()) {
            TripAction tripAction = it.next();
            if (tripAction.getSeqNo() >= seqNo) {
                if (tripAction.getSeqNo() < numberOfActions) { // keep the last train formation action and glue action
                    //plannedService.setPlannedStopSecs(plannedService.getPlannedStopSecs() - tripAction.getPlannedSecs());
                    it.remove();
                    removed++;
                }
            }
        }

        if (removed > 0) {// change the last two action's seqno
            ListIterator it2 = plannedService.getTripActions().listIterator(plannedService.getNumberOfActions());
            while (it2.hasPrevious()) {
                TripAction tripAction = (TripAction) (it2.previous());
                if (tripAction.getSeqNo() > seqNo + 1) {
                    tripAction.setSeqNo(tripAction.getSeqNo() - removed);
                }
            }
        }

        //plannedService.updateActionTimes();
        //plannedService.updatePlannedEndTime();
        //plannedService.setDurationSecs(plannedService.getPlannedStopSecs() - plannedService.getPlannedStartSecs());
        return seqNo;
    }

    private TripAction createNewTripAction(TripAction.MainActionTypeEnum eMainActionType) {
        TripAction tripAction = null;
        switch (eMainActionType) {
            case PASSENGER_STOP:
                tripAction = new ActionPassengerStop();
                break;
            case TRAIN_MOVING:
                tripAction = new ActionTrainMoving();
                break;
            case SCHEDULING_STOP:
                tripAction = new ActionSchedulingStop();
                break;
            case PASS_OBJECT:
                tripAction = new ActionPassObject();
                break;
            case CARGO_STOP:
                tripAction = new ActionCargoStop();
                break;
        }
        if (tripAction != null) {
            tripAction.setCreating(true);
            tripAction.setEditing(true);
        }
        return tripAction;
    }

    private ActionRunTrip cutTripAction(ActionRunTrip oldAction, Integer seqNoInOldTemplate, boolean cutAllTripsFromBeginning) {
        BasicTrip oldTemplate = oldAction.getRefTrip().getTripTemplate();
        //boolean useTemplate = oldAction.getRefTrip().getActionsFromTemplate();

        ActionRunTrip tripAction = new ActionRunTrip();
        PlannedTrip plannedTrip = new PlannedTrip();
        plannedTrip.setConsumed(false);
        plannedTrip.setTimesAreValid(false);
        plannedTrip.setUtcTimes(false);
        plannedTrip.setAreaObj(oldAction.getRefTrip().getAreaObj());
        plannedTrip.setValid(Boolean.TRUE);
        //plannedTrip.setDayType(oldAction.getRefTrip().getDayType());
        plannedTrip.setDayTypeList(oldAction.getRefTrip().getDayTypeList());
        plannedTrip.setOrigoSecs(0);
        plannedTrip.setActionsFromTemplate(Boolean.TRUE);
        plannedTrip.setVersion(1);
        plannedTrip.setPlannedState(oldAction.getRefTrip().getPlannedState());
        plannedTrip.setTripType(oldAction.getRefTrip().getTripType());
        plannedTrip.setPlannedState(oldAction.getRefTrip().getPlannedState());

        //tripAction.setSeqNo(oldAction.getSeqNo());
        tripAction.setConsumed(false);
        tripAction.setTimesValid(false);
        tripAction.setCreating(true);
        tripAction.setEditing(true);
        tripAction.setActionType(oldAction.getActionType());
        TripAction tempAction = oldTemplate.getTripAction(seqNoInOldTemplate);
        TTObject loc1 = tempAction.hasSecondObject() ? ((ActionTrainMoving) tempAction).getTimetableObject2() : tempAction.getTimetableObject();

        if (cutAllTripsFromBeginning) {
            tripAction.setTimetableObject(loc1);
            TTObject loc2 = oldAction.getRefTrip().getPlannedStopObj();
            tripAction.setTimetableObject2(loc2);
            plannedTrip.setPlannedStartObj(loc1);
            plannedTrip.setPlannedStopObj(loc2);
            plannedTrip.setDescription(loc1.getDescription() + " - " + loc2.getDescription());
            BasicTrip newTemplate = findTripTemplate(oldTemplate, seqNoInOldTemplate, oldTemplate.getNumberOfActions());
            if (newTemplate == null) {
                newTemplate = new BasicTrip();
                oldTemplate.cloneDataToClonedTrip(newTemplate, seqNoInOldTemplate, oldTemplate.getNumberOfActions());
            }
            plannedTrip.setTripTemplate(newTemplate);

        } else {
            TTObject loc2 = oldAction.getRefTrip().getPlannedStartObj();
            tripAction.setTimetableObject(loc2);
            tripAction.setTimetableObject2(loc1);
            plannedTrip.setPlannedStartObj(loc2);
            plannedTrip.setPlannedStopObj(loc1);
            plannedTrip.setDescription(loc2.getDescription() + " - " + loc1.getDescription());
            BasicTrip newTemplate = findTripTemplate(oldTemplate, 1, seqNoInOldTemplate);
            if (newTemplate == null) {
                newTemplate = new BasicTrip();
                oldTemplate.cloneDataToClonedTrip(newTemplate, 1, seqNoInOldTemplate);
            }
            plannedTrip.setTripTemplate(newTemplate);
        }

        tripAction.setPlannedSecs(plannedTrip.getTripTemplate().getDurationSecs());
        plannedTrip.setDurationSecs(plannedTrip.getTripTemplate().getDurationSecs());
        tripAction.setMinSecs(oldAction.getActionType().getDefaultMinSecs());
        //tripAction.setPlannedSecs(0);

        /* Integer seqNoInTrip = 0;
        Integer durationSecs = 0;
        Iterator<TripAction> it = useTemplate ? oldTemplate.getTripActions().iterator() : oldAction.getRefTrip().getTripActions().iterator();
        //for (TripAction action : oldTemplate.getTripActions()) {
        while (it.hasNext()) {
            TripAction action = it.next();
            if ((cutAllTripsFromBeginning && action.getSeqNo() >= seqNoInOldAction)
                    || (!cutAllTripsFromBeginning && action.getSeqNo() <= seqNoInOldAction)) {
                TripAction newActionInTrip = createNewTripAction(action.getActionType().getMainActionTypeEnum());
                action.cloneDataToNonPersited(newActionInTrip);
                newActionInTrip.setSeqNo(++seqNoInTrip);
                plannedTrip.addTripAction(newActionInTrip);
                durationSecs += newActionInTrip.getPlannedSecs();
            }
        }
        tripAction.setPlannedSecs(durationSecs);
        plannedTrip.setDurationSecs(durationSecs);
        plannedTrip.setTripTemplate(null);*/
        //plannedTrip.setOrigoSecs(oldTemplate.getOrigoSecs());
        plannedTrip.setServiceAction(tripAction);
        tripAction.setRefTrip(plannedTrip);
        return tripAction;
    }

    private ActionRunTrip createTripActionFromTemplate(PlannedService plannedService, BasicTrip tripTemplate) {
        ActionRunTrip tripAction = new ActionRunTrip();

        tripAction.setConsumed(false);
        tripAction.setTimesValid(false);
        tripAction.setCreating(true);
        tripAction.setEditing(true);
        tripAction.setActionType(RUN_NONCOMMERCIAL_TRIP_ACTION);
        TTObject loc1 = tripTemplate.getPlannedStartObj();
        TTObject loc2 = tripTemplate.getPlannedStopObj();
        tripAction.setTimetableObject(loc1);
        tripAction.setTimetableObject2(loc2);
        tripAction.setMinSecs(getDefaultMinSecs(tripTemplate));
        tripAction.setPlannedSecs(tripTemplate.getDurationSecs());

        PlannedTrip plannedTrip = new PlannedTrip();
        plannedTrip.setConsumed(false);
        plannedTrip.setTimesAreValid(false);
        plannedTrip.setUtcTimes(false);
        plannedTrip.setAreaObj(tripTemplate.getAreaObj());
        plannedTrip.setValid(Boolean.TRUE);
        //We didn't consider different or multiple daytypes in one service
        //plannedTrip.setDayType(plannedService.getDayType());
        plannedTrip.setDayTypeList(plannedService.getDayTypeList());
        plannedTrip.setOrigoSecs(tripTemplate.getOrigoSecs());
        plannedTrip.setActionsFromTemplate(Boolean.TRUE);
        plannedTrip.setPlannedState(DEFAULT_STATE);
        plannedTrip.setVersion(1);

        plannedTrip.setServiceAction(tripAction);
        tripAction.setRefTrip(plannedTrip);
        tripAction.setTrip(plannedService);
        //plannedTrip.setTripType(plannedServiceController.GetToTripType(tripTemplate.getTripType()));
        plannedTrip.setTripType(tripTemplate.getTripType().getTripSubType() == 1 ? this.ejbTripTypeFacade.find(9) : this.ejbTripTypeFacade.find(10));
        plannedTrip.setTripTemplate(tripTemplate);
        plannedTrip.setDurationSecs(tripTemplate.getDurationSecs());
        plannedTrip.setOrigoSecs(tripTemplate.getOrigoSecs());
        plannedTrip.setPlannedStartObj(loc1);
        plannedTrip.setPlannedStopObj(loc2);
        plannedTrip.setDescription(loc1.getDescription() + " - " + loc2.getDescription());

        return tripAction;
    }

    public BasicTrip cloneTripTemplate(BasicTrip oldTemplate, Integer startSeqNo, Integer endSeqNo) {

        BasicTrip clonedTrip = new BasicTrip();

        try {

            //this.ejbTripTemplateFacade.cloneTrip(oldTemplate, clonedTrip);
            oldTemplate.cloneDataToClonedTrip(clonedTrip, startSeqNo, endSeqNo);
            xmlMessageSender.sendTripTemplateMsg(clonedTrip, Operation.CREATE);

        } catch (Exception e) {
            // TODO: NO GUI, cant put error out. log?
            System.out.println("PersistenceErrorOccured");
            return null;
        }
        return clonedTrip;
    }

    private Integer getDefaultMinSecs(BasicTrip template) {
        Integer minSecs = 0;
        if (template != null) {

            Iterator<TripAction> iterator = template.getTripActions().iterator();
            while (iterator.hasNext()) {
                minSecs += iterator.next().getMinSecs();
            }
        }
        return minSecs;

    }

    private BasicTripFilter constructTripTemplateFilter(PlannedTrip plannedTrip) {
        BasicTripFilter filter = new BasicTripFilter();
        if (plannedTrip.getTripTemplate() != null) {
            filter.setPlannedStartObjFilter(plannedTrip.getPlannedStartObj());
            filter.setPlannedStopObjFilter(plannedTrip.getPlannedStopObj());
            filter.setValidFilter(true);
            filter.setTripMainTypeFilter(plannedTrip.getTripTemplate().getTripType().getTripType());
        }

        return filter;
    }

    private BasicTrip findTripTemplate(BasicTrip tripTemplate, Integer startSeqNo, Integer endSeqNo) {
        BasicTripFilter filter = new BasicTripFilter();
        if (tripTemplate != null) {
            filter.setPlannedStartObjFilter(tripTemplate.getTripAction(startSeqNo).getTimetableObject());
            TripAction temp = tripTemplate.getTripAction(endSeqNo);
            filter.setPlannedStopObjFilter(temp.hasSecondObject() ? ((ActionTrainMoving) temp).getTimetableObject2() : temp.getTimetableObject());
            filter.setValidFilter(true);
            filter.setTripMainTypeFilter(tripTemplate.getTripType().getTripType());
            // Find all matching templates
            List<BasicTrip> trips = this.ejbTripTemplateFacade.findAll(filter);

            // LOOP THROUGH TRIPS AND CHECK THEIR HASHES AGAINST IMPTRIP
            for (BasicTrip bt : trips) {
                String hash = buildTripTemplateHash(bt, 1, bt.getNumberOfActions());
                String hash2 = buildTripTemplateHash(tripTemplate, startSeqNo, endSeqNo);
                if (hash == null ? hash2 == null : hash.equals(hash2)) {
                    return bt;
                }
            }
        }

        return null;
    }

    private String buildTripTemplateHash(BasicTrip bt, Integer startSeqNo, Integer endSeqNo) {

        String pathHash = bt.getAreaObj().getScheduleName() + ": ";
        String timeHash = "";
        ActionTrainMoving last = null;

        // Since railML does not contain FIRST dwell, we need to subrtrack it
        // from timefromstart
        int firstDwell = 0;
        TripAction first = bt.getTripAction(1);
        if (first != null && first instanceof ActionPassengerStop) {
            firstDwell = first.getPlannedSecs();
        }

        for (TripAction ta : bt.getTripActions()) {
            if (ta.getSeqNo() >= startSeqNo && ta.getSeqNo() <= endSeqNo && (ta instanceof ActionTrainMoving)) {
                last = (ActionTrainMoving) ta;
                pathHash += ta.getTimetableObject().getTTObjId() + "-";
                //timeHash += ta.getPlannedSecs() + "-";
                timeHash += "[null," + (ta.getTimeFromTripStart()) + "][" + (ta.getTimeFromTripStart() + ta.getPlannedSecs() - firstDwell) + ",null]";
            }
        }

        if (last != null) {
            if (last instanceof ActionTrainMoving) {
                pathHash += last.getTimetableObject2().getTTObjId() + "-";
            } else {
                pathHash += last.getTimetableObject().getTTObjId() + "-";
            }
        }

        return bt.getTripType().getExternalId() + ": " + pathHash + " == " + timeHash;
    }

    public void handleScheduledTimetableRequest(LinkedList<RequestItem> requestItems, String replyTo) {

        for (Object item : requestItems) {
            ScheduledTimetableRequest request = ((ScheduledTimetableRequest) item);
            LinkedList<ScheduledTimetableResponseItem> responseItems = null;
            ScheduledDay scheduledDay = null;
            if (request.getDayCode() != null) {
                scheduledDay = this.ejbScheduledDayFacade.find(request.getDayCode());
                responseItems = constructScheduledTimetableMsg(scheduledDay);
            }
            this.xmlMessageSender.sendScheduledTimetableMsg(replyTo, request.getRequestId(), responseItems, request.getDayCode(), isCurrentDay());
        }

    }

    public boolean isCurrentDay() {
        return true;
    }

    @SuppressWarnings("unchecked")
    public LinkedList<ScheduledTimetableResponseItem> constructScheduledTimetableMsg(ScheduledDay scheduledDay) {
        LinkedList<ScheduledTimetableResponseItem> responseItems = new LinkedList<>();

        if (scheduledDay != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMdd'T'HHmmss");
            
            Date ReferenceDate = SetDateToMidnight(scheduledDay.getDateOfDay());
            Date tmpDate = new Date();
                
            List<ScheduledService> scheduledServices = GetScheduledServicesForSpecificDate(scheduledDay);
            for (ScheduledService scheduledService : scheduledServices) 
            {
                Integer arrivalTimeSec = null;
                TTObject platform = null;
                
                for (TripAction tripAction : scheduledService.getTripActions()) 
                {
                    if (tripAction instanceof ActionRunTrip) 
                    {
                        TimedTrip scheduledTrip = ((ActionRunTrip) tripAction).getRefTrip();
                        BasicTrip tripTemplate = scheduledTrip.getTripTemplate();
                        
                        if (tripTemplate != null) 
                        {
                            for (TripAction templateAction : tripTemplate.getTripActions()) 
                            {
                                if (templateAction instanceof ActionTrainMoving) 
                                {
                                    ScheduledTimetableResponseItem responseItem = new ScheduledTimetableResponseItem();
                                    responseItem.setServiceId(scheduledService.getTripId());
                                    responseItem.setTripId(scheduledTrip.getTripId());
                                    responseItem.setService(scheduledService.getDescription());
                                    responseItem.setPlatform(templateAction.getTimetableObject().getScheduleName());
                                    responseItem.setPlatformExtName(templateAction.getTimetableObject().getExternalName());
                                    
                                    Integer departureTimeSec = ((ActionTrainMoving) templateAction).getTimeFromTripStart() + scheduledTrip.getPlannedStartSecs();
                                    
                                    if (arrivalTimeSec != null) 
                                    {
                                        tmpDate = AddSecondsToDate(ReferenceDate, arrivalTimeSec);
                                        responseItem.setArrivalTime(sdf.format(tmpDate));
                                    }
                                    
                                    tmpDate = AddSecondsToDate(ReferenceDate, departureTimeSec);
                                    responseItem.setDepartureTime(sdf.format(tmpDate));
                                    
                                    arrivalTimeSec = departureTimeSec + templateAction.getPlannedSecs();
                                    
                                    responseItems.add(responseItem);
                                    platform = ((ActionTrainMoving) templateAction).getTimetableObject2();
                                }
                            }
                        }
                    }
                    
                    if ((tripAction.getSeqNo() == scheduledService.getNumberOfActions() - 2) && 
                         arrivalTimeSec != null && 
                         platform != null) 
                    {
                        ScheduledTimetableResponseItem responseItem = new ScheduledTimetableResponseItem();
                        
                        responseItem.setService(scheduledService.getDescription());
                        responseItem.setPlatform(platform.getScheduleName());
                        responseItem.setPlatformExtName(platform.getExternalName());
                        
                        tmpDate = AddSecondsToDate(ReferenceDate, arrivalTimeSec);
                        responseItem.setArrivalTime(sdf.format(tmpDate));
                        
                        responseItems.add(responseItem);
                    }
                }
            }
        }
        
        return responseItems;
    }
    
    private Date AddSecondsToDate(Date date, int nSeconds)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.SECOND, nSeconds);
        
        return c.getTime();
    }
    
    private List<ScheduledService> GetScheduledServicesForSpecificDate(ScheduledDay scheduledDay)
    {
        ScheduledServiceFilter filter = new ScheduledServiceFilter();
        filter.setScheduledDayFilter(scheduledDay);
        filter.setValidFilter(true);

        return this.ejbScheduledServiceFacade.findAll(filter);
    }
    
    private Date SetDateToMidnight(Date date)
    {
        Date ReferenceDate = date;
        ReferenceDate.setHours(0);
        ReferenceDate.setMinutes(0);
        ReferenceDate.setSeconds(0);
        
        return ReferenceDate;
    }
}
