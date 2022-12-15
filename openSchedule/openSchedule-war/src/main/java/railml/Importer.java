package railml;

import java.io.InputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.railml.schemas._2011.Railml;
import org.railml.schemas._2011.EArrivalDepartureTimes;
import org.railml.schemas._2011.EBlock;
import org.railml.schemas._2011.EBlockPartSequence;
import org.railml.schemas._2011.ECategory;
import org.railml.schemas._2011.EFormation;
import org.railml.schemas._2011.EOcp;
import org.railml.schemas._2011.EOcpTT;
import org.railml.schemas._2011.EOperatingPeriod;
import org.railml.schemas._2011.ERostering;
import org.railml.schemas._2011.ETimetablePeriod;
import org.railml.schemas._2011.ETrainPart;
import org.railml.schemas._2011.ETrainParts;
import org.railml.schemas._2011.TBlockPart;
import org.railml.schemas._2011.TElementWithIDAndName;
import org.railml.schemas._2011.TOcpTTType;
import org.railml.schemas._2011.TOnOff;
import static schedule.entities.ActionGlue.ActionGlueTypeEnum.GLUE_WAIT;
import static schedule.entities.ActionGlue.ActionGlueTypeEnum.GLUE_WAIT_AND_TURN;
import schedule.entities.ActionMovementTrip;
import static schedule.entities.ActionPassObject.ActionPassObjectTypeEnum.PASS_OBJECT_NO_PLANNED_SPEED;
import schedule.entities.ActionPassengerStop;
import static schedule.entities.ActionPassengerStop.ActionPassengerStopTypeEnum.LEAVE_PASSENGER;
import static schedule.entities.ActionPassengerStop.ActionPassengerStopTypeEnum.TAKE_LEAVE_PASSENGER;
import static schedule.entities.ActionPassengerStop.ActionPassengerStopTypeEnum.TAKE_PASSENGER;
import schedule.entities.ActionRunTrip;
import static schedule.entities.ActionRunTrip.ActionRunTripTypeEnum.RUN_COMMERCIAL_TRIP;
import static schedule.entities.ActionRunTrip.ActionRunTripTypeEnum.RUN_NONCOMMERCIAL_TRIP;
import static schedule.entities.ActionSchedulingStop.ActionSchedulingStopTypeEnum.SCHEDULING_STOP_TRAIN_WAITING;
import static schedule.entities.ActionSchedulingStop.ActionSchedulingStopTypeEnum.SCHEDULING_STOP_TRAIN_WAITING_TURING;
import static schedule.entities.ActionTrainFormation.ActionTrainFormationTypeEnum.END_RUN;
import static schedule.entities.ActionTrainFormation.ActionTrainFormationTypeEnum.START_RUN;
import schedule.entities.ActionTrainMoving;
import static schedule.entities.ActionTrainMoving.ActionTrainMovingTypeEnum.TRAIN_MOVING_WITHOUT_SPEED;
import schedule.entities.ActionType;
import schedule.entities.BasicTrip;
import schedule.entities.DayInSchedule;
import schedule.entities.DayType;
import schedule.entities.MainActionType;
import schedule.entities.MovementTrip;
import schedule.entities.MovementTripGroup;
import schedule.entities.PlannedService;
import schedule.entities.Schedule;
import schedule.entities.SchedulingState;
import schedule.entities.TTArea;
import schedule.entities.TTObject;
import schedule.entities.TimedTrip;
import schedule.entities.TrainType;
import schedule.entities.TripAction;
import static schedule.entities.TripAction.MainActionTypeEnum.GLUE;
import static schedule.entities.TripAction.MainActionTypeEnum.PASSENGER_STOP;
import static schedule.entities.TripAction.MainActionTypeEnum.PASS_OBJECT;
import static schedule.entities.TripAction.MainActionTypeEnum.RUN_TRIP;
import static schedule.entities.TripAction.MainActionTypeEnum.SCHEDULING_STOP;
import static schedule.entities.TripAction.MainActionTypeEnum.TRAIN_FORMATION;
import static schedule.entities.TripAction.MainActionTypeEnum.TRAIN_MOVING;
import schedule.entities.TripType;
import static schedule.entities.TripType.TripMainType.COMMERCIAL_TEMPLATE;
import schedule.entities.TripUserType;
import schedule.sessions.ActionTypeFacade;
import schedule.sessions.DayTypeFacade;
import schedule.sessions.DefaultObjectFacade;
import schedule.sessions.MainActionTypeFacade;
import schedule.sessions.MovementTripFacade;
import schedule.sessions.PlannedServiceFacade;
import schedule.sessions.ScheduleFacade;
import schedule.sessions.SchedulingStateFacade;
import schedule.sessions.TTObjectFacade;
import schedule.sessions.TrainTypeFacade;
import schedule.sessions.TripFacade;
import schedule.sessions.TripTypeFacade;
import schedule.sessions.TripUserTypeFacade;
import schedule.uiclasses.ActionTypeFilter;
import schedule.uiclasses.BasicTripFilter;
import schedule.uiclasses.MovementTripFilter;
import schedule.uiclasses.PlannedServiceController;
import schedule.uiclasses.ScheduleController;
import schedule.uiclasses.TripTemplateController;
import schedule.uiclasses.TripTypeFilter;
import schedule.uiclasses.TripUserTypeFilter;
import schedule.uiclasses.util.JsfUtil;
import schedule.uiclasses.util.UiText;
import schedule.uiclasses.util.config.ConfigBean;

/**
 *
 * @author spirttin
 */
@SessionScoped
@TransactionManagement(TransactionManagementType.BEAN)
public class Importer implements Serializable {

    // Parsed in XML to objects
    private Railml railml;
    // Conversion map from railml object OpenSchedule DB object
    // -- Mostly contains TIMETABLE LOCATIONS, TRAINTYPES --
    // key == String. Represented as an Object, since RailML gives references as such
    // value == OpenSchedule Entity. Represented as an Object, since there are many different entities
    private Map<String, Object> conversionMap;
    // Conversion map from template hash to OpenSchedule DB object
    // key == String. Template hash key
    // value == OpenSchedule Entity. Actual template stored in DB
    private Map<String, BasicTrip> templateMap;

    // -- RAILML DATA maps --
    Map<String, List<ImporterTrip>> PathData; // Key==PathHash. EXAMPLE: L1: ocp0000011-ocp0000020-
    Map<String, List<ImporterTrip>> TemplateData; // Key==TemplateHash. EXAMPLE: L1: ocp0000011-ocp0000020- == [null,0][2062,null]

    boolean tryMapDirection = false;
    boolean glueWaitTurn = false;

    // <editor-fold defaultstate="collapsed" desc="Default data objects">
    ActionType PASSENGER_TAKE_ACTION; // = GetActionType(PASSENGER_STOP, TAKE_PASSENGER.getValue());
    ActionType PASSENGER_TAKE_LEAVE_ACTION; // = GetActionType(PASSENGER_STOP, TAKE_LEAVE_PASSENGER.getValue());
    ActionType PASSENGER_LEAVE_ACTION; // = GetActionType(PASSENGER_STOP, LEAVE_PASSENGER.getValue());
    ActionType PASS_ACTION; // = GetActionType(PASS_OBJECT, LEAVE_PASSENGER.getValue());
    ActionType SCHEDULING_STOP_WAITING_ACTION;
    ActionType SCHEDULING_STOP_WAITING_TURING_ACTION;

    ActionType TRAIN_MOVING_ACTION; //= GetActionType(TRAIN_MOVING, TRAIN_MOVING_WITHOUT_SPEED.getValue());
    ActionType RUN_COMMERCIAL_TRIP_ACTION; //= GetActionType(RUN_TRIP, RUN_COMMERCIAL_TRIP.getValue());
    ActionType RUN_NONCOMMERCIAL_TRIP_ACTION;
    ActionType TRAIN_FORMATION_START_ACTION;
    ActionType TRAIN_FORMATION_END_ACTION;
    ActionType GLUE_WAIT_ACTION;
    ActionType GLUE_WAIT_AND_TURN_ACTION;
    ActionType DEFAULT_MOVEMENT_TRIP_ACTION;
    ActionType ALTERNATIVE_MOVEMENT_TRIP_ACTION;

    SchedulingState DEFAULT_STATE;
    TripUserType SYSTEM_USER;
    TripType MOVEMENT_TRIP_GROUP;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Injectors">
    @Inject
    private TTObjectFacade ejbTTObjectFacade;
    @Inject
    private TrainTypeFacade ejbTrainTypeFacade;
    @Inject
    private ScheduleFacade ejbScheduleFacade;
    @Inject
    private DayTypeFacade ejbDayTypeFacade;
    @Inject
    private TripTypeFacade ejbTripTypeFacade;
    @Inject
    private TripFacade ejbTripFacade;
    @Inject
    private ActionTypeFacade ejbActionTypeFacade;
    @Inject
    private PlannedServiceFacade ejbPlannedServiceFacade;
    @Inject
    private PlannedServiceFacade ejbPlannedTripFacade;
    @Inject
    private SchedulingStateFacade ejbSchedulingStateFacade;
    @Inject
    private DefaultObjectFacade ejbDefaultObjectFacade;
    @Inject
    private TripUserTypeFacade ejbTripUserTypeFacade;
    @Inject
    private MovementTripFacade ejbMovementTripFacade;
    @Inject
    private ScheduleController scheduleController;
    @Inject
    private PlannedServiceController plannedServiceController;
    @Inject
    private MainActionTypeFacade ejbActionMainTypeFacade;

    @Resource
    private UserTransaction userTransaction;
    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager entityManager;

    @Inject
    protected UiText uiText;
    // </editor-fold>

    public Importer() {
    }

    @PostConstruct
    public void init() {
        PASSENGER_TAKE_ACTION = GetActionType(PASSENGER_STOP, TAKE_PASSENGER.getValue());
        PASSENGER_TAKE_LEAVE_ACTION = GetActionType(PASSENGER_STOP, TAKE_LEAVE_PASSENGER.getValue());
        PASSENGER_LEAVE_ACTION = GetActionType(PASSENGER_STOP, LEAVE_PASSENGER.getValue());
        PASS_ACTION = GetActionType(PASS_OBJECT, PASS_OBJECT_NO_PLANNED_SPEED.getValue());
        SCHEDULING_STOP_WAITING_ACTION = GetActionType(SCHEDULING_STOP, SCHEDULING_STOP_TRAIN_WAITING.getValue());
        SCHEDULING_STOP_WAITING_TURING_ACTION = GetActionType(SCHEDULING_STOP, SCHEDULING_STOP_TRAIN_WAITING_TURING.getValue());

        TRAIN_MOVING_ACTION = GetActionType(TRAIN_MOVING, TRAIN_MOVING_WITHOUT_SPEED.getValue());
        RUN_COMMERCIAL_TRIP_ACTION = GetActionType(RUN_TRIP, RUN_COMMERCIAL_TRIP.getValue());
        RUN_NONCOMMERCIAL_TRIP_ACTION = GetActionType(RUN_TRIP, RUN_NONCOMMERCIAL_TRIP.getValue());
        TRAIN_FORMATION_START_ACTION = GetActionType(TRAIN_FORMATION, START_RUN.getValue());
        TRAIN_FORMATION_END_ACTION = GetActionType(TRAIN_FORMATION, END_RUN.getValue());
        GLUE_WAIT_AND_TURN_ACTION = GetActionType(GLUE, GLUE_WAIT_AND_TURN.getValue());
        GLUE_WAIT_ACTION = GetActionType(GLUE, GLUE_WAIT.getValue());

        DEFAULT_STATE = GetSchedulingState();
        SYSTEM_USER = GetTripUserType(TripUserType.State.SYSTEM_USER.getValue());

        TripTypeFilter tripTypeFilter = new TripTypeFilter();
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
        }
        glueWaitTurn = ConfigBean.getConfig().getSetting("glueWaitTurn").getEnable();
    }

    public void Load(InputStream is) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Railml.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        railml = (Railml) jaxbUnmarshaller.unmarshal(is);

        //System.out.println("RailML version: "+railml.getVersion());
    }

    public boolean Process() {
        try {
            conversionMap = new HashMap<>();
            templateMap = new HashMap<>();

            // Find stopping places
            for (EOcp ocp : railml.getInfrastructure().getOperationControlPoints().getOcp()) {
                MapTTObject(ocp);
            }
            // Find traintypes
            for (EFormation f : railml.getRollingstock().getFormations().getFormation()) {
                MapTrainType(f);
            }
            userTransaction.begin();
            // HUGE WORK HERE
            // Loop through every trainpart and try to create templates out from it
            ConstructTrainPartMaps(railml.getTimetable().getTrainParts());
            buildTripTemplates();
            //this.ejbTripFacade.evictAll();

            // Create Schedule
            for (ETimetablePeriod tp : railml.getTimetable().getTimetablePeriods().getTimetablePeriod()) {
                buildSchedule(tp);
            }

            // Facades must be evicted, that other pages will load new items
            // (Mainly LongTermFilter)
            //this.ejbScheduleFacade.evictAll();
            // Add Daytypes to schedule
            for (EOperatingPeriod op : railml.getTimetable().getOperatingPeriods().getOperatingPeriod()) {
                buildDaytype(op);
                this.plannedServiceController.setClonedDayType(this.plannedServiceController.getDayTypes().get(0));
                UpdateDaysInSchedule(op);
            }
            //this.ejbDayTypeFacade.evictAll();

            // Create services to schedule
            for (ERostering r : railml.getTimetable().getRosterings().getRostering()) {
                // We do not care about depot yards, where train comes from
                for (EBlock b : r.getBlocks().getBlock()) {
                    buildService(b);
                }
            }
            //this.ejbPlannedTripFacade.evictAll();
            //this.ejbPlannedServiceFacade.evictAll();
            userTransaction.commit();
            this.ejbTripFacade.evictAll();
            this.ejbScheduleFacade.evictAll();
            this.ejbDayTypeFacade.evictAll();
            this.ejbPlannedTripFacade.evictAll();
            this.ejbPlannedServiceFacade.evictAll();
            
            return true;
            
        } catch (NotSupportedException | SystemException | ImporterException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            try {
                userTransaction.rollback();
            } catch (IllegalStateException | SecurityException | SystemException ex1) {
                Logger.getLogger(Importer.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(Importer.class.getName()).log(Level.SEVERE, null, ex);
            JsfUtil.addErrorMessage(ex, uiText.get("RailMLFileUploadFailed"));
        
            return false;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="OPENSCHEDULE DATABASE ITEMS CREATION">
    private void buildSchedule(ETimetablePeriod tp) throws ImporterException {
        Schedule s = scheduleController.constructNewItem();
        conversionMap.put(tp.getId(), s);

        Calendar start = tp.getStartDate().toGregorianCalendar();
        Calendar end = tp.getEndDate().toGregorianCalendar();

        s.setStartTime(start.getTime());
        s.setEndTime(end.getTime());
        s.generateDaysInSchedule(start, end);
        s.setValid(true);
        s.setCreating(false);
        s.setEditing(false);
        String desc = "RailML (id: " + tp.getId() + ") " + start.get(Calendar.DATE) + "." + (start.get(Calendar.MONTH) + 1) + "." + start.get(Calendar.YEAR) + " -> "
                + end.get(Calendar.DATE) + "." + (end.get(Calendar.MONTH) + 1) + "." + end.get(Calendar.YEAR) + " "+ DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Calendar.getInstance().getTime());
        // ALTERNATIVE IF NAME IS INTRODUCED INTO RAILML
        //String desc = tp.getName(); // TP Name&Code does not exists in orginal xml

        s.setDescription(desc);

        //ejbScheduleFacade.create(s);
        entityManager.joinTransaction();
        entityManager.persist(s);

        scheduleController.scheduleFilterChanged(s);
    }

    private void buildDaytype(EOperatingPeriod op) throws ImporterException {
        // Schedule where daytypes are going to be created
        Schedule s = (Schedule) conversionMap.get(((ETimetablePeriod) op.getTimetablePeriodRef()).getId());
        if (s == null) {
            throw new UnsupportedOperationException("Schedule null.");
        }

        DayType dt = new DayType();
        conversionMap.put(op.getId(), dt);
        dt.setScheduleParent(s);

        //dt.setDescription("RailML DT ("+op.getId()+")"); 
        //dt.setAbbr(op.getId().substring(op.getId().length()-3));    // OP Name&Code does not exists in orginal xml   
        // ALTERNATIVE IF NAME IS INTRODUCED INTO RAILML
        dt.setDescription(op.getName());    // OP Name&Code does not exists in orginal xml
        if (op.getCode() != null) {
            dt.setAbbr(op.getCode());           // OP Name&Code does not exists in orginal xml   
        } else {
            dt.setAbbr(op.getName());
        }

        s.add(dt);
        //ejbDayTypeFacade.create(dt); // Daytype needs to be explicitely created before schedule changes are saved, to prevent multiple daytypes
        entityManager.joinTransaction();
        entityManager.persist(dt);
    }

    private PlannedService createService(EBlock railB) {
        // Find initial data from railML
        TrainType tt = GetTrainType(railB);
        DayType dt = GetDayType(railB);
        TTArea tta = GetTTArea(railB);
        TTObject startObj = GetFromPlatform(railB);
        TTObject stopObj = GetToPlatform(railB);
        int startsecs = GetStartSecs(railB) - this.GLUE_WAIT_ACTION.getDefaultPlanSecs();
        int stopsecs = GetStopSecs(railB) + this.GLUE_WAIT_ACTION.getDefaultPlanSecs();

        // Create and fill planned service
        PlannedService ps = plannedServiceController.createPlannedService(dt, tta);

        ps.setTrainType(tt);
        String desc = railB.getName();
        String format = ResourceBundle.getBundle("Bundle").getString("SERVICE_REGEXP_PATTERN");
        int zeros = Character.getNumericValue(format.charAt(3)) - railB.getName().length();

        while (zeros > 0) {
            desc = "0" + desc;
            zeros--;
        }
        ps.setDescription(desc); // description
        ps.setPlannedStartSecs(startsecs); // Start secs will be modified according to first templates stop time
        ps.setPlannedStopSecs(stopsecs); // Stop secs will be fixed, when trips are added to trip
        ps.setPlannedStartObj(startObj);
        ps.setPlannedStopObj(stopObj);

        ps.setPlannedState(null);

        ps.setPlannedState(DEFAULT_STATE);
        ps.setEditing(false);
        ps.setCreating(false);
        ps.setVersion(1);
        //conversionMap.put(railB.getId(), ps);

        return ps;
    }

    private PlannedService buildService(EBlock railB) throws ImporterException {
        int actionIndex = 1;
//        int lastStopDwell = 0;

        PlannedService ps = createService(railB);
        if (ps.getPlannedStartObj() == null || ps.getPlannedStopObj() == null) {
            System.out.println("Importer: buildService failed: " + ps.getDescription());
        } else // Save service to DB, that when it is updated later, templates are not multiplied
        {
            //ejbPlannedServiceFacade.create(ps);
            entityManager.joinTransaction();
            entityManager.persist(ps);
        }

        // Create TrainFormationAction
        TripAction lastAction = createTrainFormationAction(ps, TRAIN_FORMATION_START_ACTION, GetFromPlatform(railB), actionIndex++);
        ps.addTripAction(lastAction, 0);

        // Loop through blockpartsequence to create trips to service
        for (EBlockPartSequence railBPS : railB.getBlockPartSequence()) {
            // Get actual trainpart matching to this sequence
            TBlockPart railBP = (TBlockPart) railBPS.getBlockPartRef().get(0).getRef();
            ETrainPart railTP = (ETrainPart) railBP.getTrainPartRef();
            ImporterTrip trip = new ImporterTrip(railTP);

            // Find template
            BasicTrip template = templateMap.get(trip.getTemplateHash());
            // Modify planned services start time
//            if (lastAction instanceof ActionTrainFormation) {
//                //lastAction.setTimeFromTripStart(lastAction.getTimeFromTripStart() - FirstStopDwell(template));
//                ps.setPlannedStartSecs(ps.getPlannedStartSecs() - FirstStopDwell(template));
//            }

            TripAction glue = createGlueAction(ps, lastAction, trip.getStartSecs() /*- FirstStopDwell(template)*/, actionIndex++, false);
            

            ps.addTripAction(glue, 0);
            lastAction = glue;

            // Create Action
            TripAction runtrip = createRuntripAction(ps, template, lastAction, trip, actionIndex++);
            ps.addTripAction(runtrip, 0);
            this.plannedServiceController.templateChanged(runtrip);
            lastAction = runtrip;
//            lastStopDwell = LastStopDwell(template);
        }

//        ps.setPlannedStopSecs(ps.getPlannedStopSecs() + lastStopDwell); // Update end time to contain stopdwell

        // And finally create last Glue + TrainFormation
        lastAction = createGlueAction(ps, lastAction, ps.getPlannedStopSecs(), actionIndex++, true);
        ps.addTripAction(lastAction, 0);

        lastAction = createTrainFormationAction(ps, TRAIN_FORMATION_END_ACTION, GetToPlatform(railB), actionIndex++);
        ps.addTripAction(lastAction, 0);

        ps.setDurationSecs(ps.getPlannedStopSecs() - ps.getPlannedStartSecs());

        // Call EDIT, since it is created to database, prevent this way new tempalates from creating
        if (ps.getPlannedStartObj() == null || ps.getPlannedStopObj() == null) {
            System.out.println("Importer: build service failed: " + ps.getDescription());
        } else {
            ps.updateActionTimes();
            //ejbPlannedServiceFacade.edit(ps);
            entityManager.merge(ps);
        }

        return ps;
    }

    private ImporterTrip getPrevImporterTrip(ImporterTrip impTrip) {
        ImporterTrip prevTrip = null;
        ETrainPart prevTP = null;

        for (ERostering r : railml.getTimetable().getRosterings().getRostering()) {
            // We do not care about depot yards, where train comes from
            for (EBlock b : r.getBlocks().getBlock()) {
                for (EBlockPartSequence railBPS : b.getBlockPartSequence()) {
                    // Get actual trainpart matching to this sequence
                    TBlockPart railBP = (TBlockPart) railBPS.getBlockPartRef().get(0).getRef();
                    ETrainPart railTP = (ETrainPart) railBP.getTrainPartRef();
                    if (prevTP != null && (railTP.getId() == null ? impTrip.getRailMLTripId() == null : railTP.getId().equals(impTrip.getRailMLTripId()))) {
                        prevTrip = new ImporterTrip(prevTP);
                        break;
                    }
                    prevTP = railTP;
                }
            }
        }
        return prevTrip;
    }

    private MovementTripGroup createMovementTripGroup(MovementTrip defaultMovement, MovementTrip alternativeMovement, TTArea areaObj) {
        MovementTripGroup movementTripGroup = new MovementTripGroup();
        movementTripGroup.setConsumed(false);
        movementTripGroup.setTimesAreValid(false);
        movementTripGroup.setUtcTimes(false);
        movementTripGroup.setAreaObj(areaObj);
        movementTripGroup.setValid(Boolean.TRUE);
        movementTripGroup.setTripType(MOVEMENT_TRIP_GROUP);
        movementTripGroup.setCreating(true);
        movementTripGroup.setEditing(true);

        TTObject start = null;
        TTObject end = null;
        if (defaultMovement != null) {
            if (defaultMovement.getPlannedStartObj().getParentObject() != null) {
                start = defaultMovement.getPlannedStartObj().getParentObject();
            } else {
                start = defaultMovement.getPlannedStartObj();
            }
            if (defaultMovement.getPlannedStopObj().getParentObject() != null) {
                end = defaultMovement.getPlannedStopObj().getParentObject();
            } else {
                end = defaultMovement.getPlannedStopObj();
            }
        } else if (alternativeMovement != null) {
            if (alternativeMovement.getPlannedStartObj().getParentObject() != null) {
                start = alternativeMovement.getPlannedStartObj().getParentObject();
            } else {
                start = alternativeMovement.getPlannedStartObj();
            }
            if (alternativeMovement.getPlannedStopObj().getParentObject() != null) {
                end = alternativeMovement.getPlannedStopObj().getParentObject();
            } else {
                end = alternativeMovement.getPlannedStopObj();
            }
        }

        if (start != null && end != null) {
            movementTripGroup.setPlannedStartObj(start);
            movementTripGroup.setPlannedStopObj(end);
            movementTripGroup.setDescription(start.getDescription() + "->" + end.getDescription());
        }
        //duration and origo are meaningless for movementTripGroup;
        movementTripGroup.setDurationSecs(0);
        movementTripGroup.setOrigoSecs(0);

        createMovementTripAction(movementTripGroup, defaultMovement, defaultMovement.getPlannedStartObj(), defaultMovement.getPlannedStopObj(), this.DEFAULT_MOVEMENT_TRIP_ACTION);
        createMovementTripAction(movementTripGroup, alternativeMovement, alternativeMovement.getPlannedStartObj(), alternativeMovement.getPlannedStopObj(), this.ALTERNATIVE_MOVEMENT_TRIP_ACTION);

        return movementTripGroup;
        //actionTrainMoving.setRefTrip(movementTripGroup);
    }

    private ActionMovementTrip createMovementTripAction(MovementTripGroup movementTripGroup, MovementTrip refTrip, TTObject startObject, TTObject endObject, ActionType actionType) {
        ActionMovementTrip actionMovementTrip = new ActionMovementTrip();
        actionMovementTrip.setActionType(actionType);
        actionMovementTrip.setTimesValid(false);
        actionMovementTrip.setConsumed(false);
        actionMovementTrip.setCreating(true);
        actionMovementTrip.setEditing(true);
        actionMovementTrip.setSeqNo(movementTripGroup.getNumberOfActions() + 1);
        actionMovementTrip.setActionId(0);
        actionMovementTrip.setTrip(movementTripGroup);
        actionMovementTrip.setPlannedSecs(0);
        actionMovementTrip.setMinSecs(0);
        actionMovementTrip.setTimetableObject(startObject);
        actionMovementTrip.setTimetableObject2(endObject);
        actionMovementTrip.setRefTrip(refTrip);
        actionMovementTrip.setCreating(false);
        actionMovementTrip.setEditing(false);
        movementTripGroup.addTripAction(actionMovementTrip);
        return actionMovementTrip;
    }

    private BasicTrip createTemplate(ImporterTrip impTrip) {
        int duration = 0;

        TripType commercialTripType = GetTripType(impTrip.getTrainpart().getCategoryRef());

        BasicTrip template = TripTemplateController.createNewTripTemplate(GetTTArea(impTrip.getLine()), commercialTripType);
        template.setTripUserType(SYSTEM_USER);
        template.setDescription(impTrip.getTemplateName() + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Calendar.getInstance().getTime()));
        template.setPlannedStartObj(this.getPlaform(impTrip.getStartPlatform()));
        template.setPlannedStopObj(this.getPlaform(impTrip.getStopPlatform()));
        TTArea areaObj = GetTTArea(impTrip.getLine());
        template.setAreaObj(areaObj);
        template.setEditing(false);
        template.setCreating(false);
        //template.setPlannedStartObj(GetStation(impTrip.getStartStation()));
        //template.setPlannedStopObj(GetStation(impTrip.getStopStation()));

        EOcpTT last = null;
        EOcpTT railOTT;
        int size = impTrip.getOcpTT().size();

        for (int index = 0; index < size; index++) {
            railOTT = impTrip.getOcpTT().get(index);

            TTObject platform = this.getPlatform(railOTT);
            if (platform != null) {
                TripAction tripAction = null;
                if (railOTT.getOcpType() == TOcpTTType.PASS) {//pass action
                    tripAction = TripTemplateController.createNewTripAction(template, PASS_ACTION.getMainActionTypeEnum(), PASS_ACTION, platform, null,
                            PASS_ACTION.getDefaultMinSecs(), 0);
                    tripAction.setCreating(false);
                    tripAction.setEditing(false);
                    duration += tripAction.getPlannedSecs();
                } else if (railOTT.getOcpType() == TOcpTTType.STOP) {

                    if (last != null) {
                        Integer lastDepTime = ImporterTrip.ConvertTimeInSeconds(last.getTimes().get(0).getDeparture(), last.getTimes().get(0).getDepartureDay(), impTrip.getOrigoTime());
                        Integer arrTime = ImporterTrip.ConvertTimeInSeconds(railOTT.getTimes().get(0).getArrival(), railOTT.getTimes().get(0).getArrivalDay(), impTrip.getOrigoTime());

                        // Build MOVE
                        int travelTime = arrTime - lastDepTime;
                        TTObject platform1 = getPlatform(last);
                        TTObject platform2 = platform;
                        tripAction = TripTemplateController.createNewTripAction(template, TRAIN_MOVING_ACTION.getMainActionTypeEnum(), TRAIN_MOVING_ACTION, platform1, platform2,
                                travelTime, travelTime);

                        MovementTrip defaultMovement = getDefaultMovement(platform1.getScheduleName(), platform2.getScheduleName());
                        MovementTrip alternativeMovement = getAlternativeMovement(platform1.getScheduleName(), platform2.getScheduleName());
                        if (defaultMovement != null || alternativeMovement != null) {
                            MovementTripGroup movementTripGroup = this.createMovementTripGroup(defaultMovement, alternativeMovement, areaObj);
                            ((ActionTrainMoving) tripAction).setRefTrip(movementTripGroup);
                        }
                        tripAction.setCreating(false);
                        tripAction.setEditing(false);
                        duration += tripAction.getPlannedSecs();

                    }
                    if (railOTT.getStopDescription() != null) {//scheduling or passenger or cargo stop action. But we don't handle cargo stop action now
                        tripAction = createStopAction(railOTT, template);
                        tripAction.setCreating(false);
                        tripAction.setEditing(false);
                        duration += tripAction.getPlannedSecs();
                    }

                }

            }
            last = railOTT;
        }

        // Save it
        template.setDurationSecs(duration);
        template.setActionTimesValid(false);
        return template;
    }

    private TripAction createStopAction(EOcpTT ocpTT, BasicTrip template) {
        ActionType actionType;
        EArrivalDepartureTimes time = (EArrivalDepartureTimes) ocpTT.getTimes().get(0);
        if (!ocpTT.getStopDescription().isCommercial()) {
            actionType = this.SCHEDULING_STOP_WAITING_ACTION;
        } //could also be cargo stop. But we don't handle it now
        else if (ocpTT.getStopDescription().getOnOff() == TOnOff.BOTH) {
            actionType = this.PASSENGER_TAKE_LEAVE_ACTION;
        } else if (ocpTT.getStopDescription().getOnOff() == TOnOff.ON) {
            actionType = this.PASSENGER_TAKE_ACTION;
        } else {
            actionType = this.PASSENGER_LEAVE_ACTION;
        }

        Integer depTime = null;
        Integer arrTime = null;
        if (time.getDeparture() != null) {
            depTime = ImporterTrip.ConvertTimeInSeconds(time.getDeparture(), time.getDepartureDay(), 0);
        }
        if (time.getArrival() != null) {
            arrTime = ImporterTrip.ConvertTimeInSeconds(time.getArrival(), time.getArrivalDay(), 0);
        }
        int secs = (depTime != null && arrTime != null) ? depTime - arrTime : actionType.getDefaultPlanSecs();
        TripAction tripAction = TripTemplateController.createNewTripAction(template, actionType.getMainActionTypeEnum(), actionType, this.getPlatform(ocpTT), null,
                actionType.getDefaultMinSecs(), secs);
        return tripAction;
    }

    private TripAction createTrainFormationAction(PlannedService ps, ActionType type, TTObject loc, Integer seqNo) {
        TripAction ta = plannedServiceController.createNewTripAction(type, ps.getAreaObj(), ps.getDayTypeList(), loc, null, null);
        ta.setTrip(ps);
        ta.setSeqNo(seqNo);
        if (seqNo == 1) {
            ta.setTimeFromTripStart(0);
        } else {
            ta.setTimeFromTripStart(ps.getDurationSecs());
        }
        ta.setPlannedSecs(type.getDefaultPlanSecs());
        ta.setEditing(false);
        ta.setCreating(false);

        return ta;
    }

    private TripAction createGlueAction(PlannedService ps, TripAction lastAction, Integer nextStartSecs, Integer seqNo, boolean lastGlue) {
        TTObject loc = lastAction.getTimetableObject();
        if (lastAction.hasSecondObject()) {
            loc = ((ActionRunTrip) lastAction).getTimetableObject2();
        }

        TripAction ta;
   
        if (this.glueWaitTurn && seqNo!=2 && !lastGlue ) {
            ta = plannedServiceController.createNewTripAction(GLUE_WAIT_AND_TURN_ACTION, ps.getAreaObj(), ps.getDayTypeList(), loc, null, null);
        } else {
            ta = plannedServiceController.createNewTripAction(GLUE_WAIT_ACTION, ps.getAreaObj(), ps.getDayTypeList(), loc, null, null);
        }
        ta.setTrip(ps);
        ta.setSeqNo(seqNo);
        ta.setTimeFromTripStart(lastAction.getTimeFromTripStart() + lastAction.getPlannedSecs());
        ta.setPlannedSecs(nextStartSecs - ps.getPlannedStartSecs() - ta.getTimeFromTripStart());
        if (ta.getPlannedSecs() < ta.getActionType().getDefaultMinSecs()) {
            System.out.println("Error: CreateGlueAction duration < DefaultMinSecs for seqno: " + seqNo + " for trip: " + ps.getDescription());
        }

        ta.setEditing(false);
        ta.setCreating(false);
        return ta;
    }

    private TripAction createRuntripAction(PlannedService ps, BasicTrip template, TripAction lastAction, ImporterTrip trip, Integer seqNo) {

        TripAction ta = plannedServiceController.createNewTripAction(getActionType(template.getTripType()), ps.getAreaObj(), ps.getDayTypeList(), this.getPlaform(trip.getStartPlatform()),
                this.getPlaform(trip.getStopPlatform()), template);
        ta.setTrip(ps);
        ta.setSeqNo(seqNo);
        ta.setTimeFromTripStart(lastAction.getTimeFromTripStart() + lastAction.getPlannedSecs());
        ta.setPlannedSecs(template.getDurationSecs());

        // Set TimedTrip variables
        ((TimedTrip) ((ActionRunTrip) ta).getRefTrip()).setPlannedStartSecs(trip.getStartSecs()/* - FirstStopDwell(template)*/);
        ((TimedTrip) ((ActionRunTrip) ta).getRefTrip()).setPlannedStopSecs(trip.getStopSecs() /*+ LastStopDwell(template)*/);
        ((TimedTrip) ((ActionRunTrip) ta).getRefTrip()).setPlannedState(ps.getPlannedState());

        ((TimedTrip) ((ActionRunTrip) ta).getRefTrip()).updateActionTimes();
        ta.setEditing(false);
        ta.setCreating(false);

        return ta;
    }
    // </editor-fold>

    private void MapTTObject(EOcp ocp) {
        TTObject o = ejbTTObjectFacade.find(ocp.getCode());
        conversionMap.put(ocp.getId(), o);
        if (o == null) {
            System.out.println("Importer: MapTTObject: cannot find ocp = " + ocp.getCode());
            tryMapDirection = true;
        }
    }

    private void MapTrainType(EFormation f) {
        TrainType tt = ejbTrainTypeFacade.find(f.getCode());
        conversionMap.put(f.getId(), tt);
    }

    private void UpdateDaysInSchedule(EOperatingPeriod op) throws ImporterException {
        // Schedule where daytypes are going to be created
        Schedule s = (Schedule) conversionMap.get(((ETimetablePeriod) op.getTimetablePeriodRef()).getId());
        if (s == null) {
            throw new ImporterException("Schedule null.");
        }

        // Schedule where daytypes are going to be created
        DayType dt = (DayType) conversionMap.get(op.getId());
        if (dt == null) {
            throw new ImporterException("DayType null.");
        }

        // Tricky part:       
        // Update daytypes into days in schedule
        // Loop through every char in bitmask and find corresponding daytype
        Calendar cal = Calendar.getInstance();
        cal.setTime(s.getStartTime());
        for (char c : op.getBitMask().toCharArray()) {
            if (c == '1') {
                for (DayInSchedule dis : s.getDayInSchedules()) {
                    if (dis.getDayCodeNo() == cal.get(Calendar.YEAR) * 1000 + cal.get(Calendar.DAY_OF_YEAR)) {
                        dis.setDayType(dt);
                        continue;
                    }
                }
            }
            cal.add(Calendar.DATE, 1);
        }

        //ejbScheduleFacade.edit(s);
        entityManager.joinTransaction();
        entityManager.merge(s);
    }

    // <editor-fold defaultstate="collapsed" desc="RailML data extractor methods">
    // Accept unchecked warning, as caller class must make check that call is right
    @SuppressWarnings("unchecked")
    private <T> T GetFromMap(TElementWithIDAndName o) {
        if (o != null) {
            return (T) conversionMap.get(o.getId());
        } else {
            return null;
        }
    }

    private TTObject getPlaform(EOcp railO) {
        return GetFromMap(railO);
    }

    private TTObject getPlatform(EOcpTT railOTT) {
        return GetFromMap((EOcp) railOTT.getOcpRef());
    }

    private MovementTrip getDefaultMovement(String prevScheduleName, String scheduleName) {
        if (prevScheduleName != null & scheduleName != null) {
            Integer id = ejbDefaultObjectFacade.getDefaultMovementId(prevScheduleName, scheduleName);
            if (id == null) {
                //System.out.println("Importer: getDefaultMovement cannot find default movement for:" + scheduleName + ", " + direction);
                return null;
            }

            MovementTripFilter movementTripFilter = new MovementTripFilter();
            movementTripFilter.setIdFilter(id);
            MovementTrip movementTrip = ejbMovementTripFacade.findFirst(movementTripFilter);
            return movementTrip;
        }
        return null;
    }

    private MovementTrip getAlternativeMovement(String prevScheduleName, String scheduleName) {
        if (prevScheduleName != null & scheduleName != null) {
            Integer id = ejbDefaultObjectFacade.getAlternativeMovementId(prevScheduleName, scheduleName);
            if (id == null) {
                //System.out.println("Importer: getAlternativeMovement cannot find alternative movement for:" + scheduleName + ", " + direction);
                return null;
            }

            MovementTripFilter movementTripFilter = new MovementTripFilter();
            movementTripFilter.setIdFilter(id);
            MovementTrip movementTrip = ejbMovementTripFacade.findFirst(movementTripFilter);
            return movementTrip;
        }
        return null;
    }

    // <editor-fold defaultstate="collapsed" desc="Default data objects">
    /*private TTObject GetStation(EOcpTT railOTT) {
        return GetFromMap((EOcp) railOTT.getOcpRef());
    }*/
 /*private TTObject GetFromStation(EBlock railB) {
        // Get first blockpart
        TBlockPart railFBP = (TBlockPart)railB.getBlockPartSequence().get(0).getBlockPartRef().get(0).getRef();
        // Get first trainpart - to find start stations & start time reference
        ETrainPart railFTP = (ETrainPart)railFBP.getTrainPartRef();
        // Get first ocpTT
        EOcpTT railFOTT = (EOcpTT)railFTP.getOcpsTT().getOcpTT().get(0);

        // And this from station..
        return GetStation(railFOTT);
    

    private TTObject GetToStation(EBlock railB) {
        // TODO: IF THERE ARE MULTIPLE BLOCKPARTREF-elements, this doesnt work
        TBlockPart railLBP = (TBlockPart)railB.getBlockPartSequence().get(railB.getBlockPartSequence().size()-1).getBlockPartRef().get(0).getRef(); 
        // Last trackpart
        ETrainPart railLTP = (ETrainPart)railLBP.getTrainPartRef();
        // last ocpTT
        // Get last ocpTT
        EOcpTT railLOTT = (EOcpTT)railLTP.getOcpsTT().getOcpTT().get(railLTP.getOcpsTT().getOcpTT().size()-1);
        
        return GetFromMap((TElementWithIDAndName) railLOTT.getOcpRef());
    }*/
//    private Integer GetDirection(ETrainPart trainpart) {
//        Integer direction = 0;
//        if (tryMapDirection) {
//            int size = trainpart.getOcpsTT().getOcpTT().size();
//            if (size > 0) {
//                EOcp ocpRef0 = (EOcp) trainpart.getOcpsTT().getOcpTT().get(0).getOcpRef();
//                //EOcp ocpRef1 = (EOcp) trainpart.getOcpsTT().getOcpTT().get(1).getOcpRef();
//                if (ocpRef0.getCode().compareToIgnoreCase("DEP-PL") == 0) {
//                    direction = 2;
//                } else if ((ocpRef0.getCode().compareToIgnoreCase("PP01") == 0)) {
//                    EOcp ocpRef1 = (EOcp) trainpart.getOcpsTT().getOcpTT().get(1).getOcpRef();
//                    if (ocpRef1.getCode().compareToIgnoreCase("PP02") == 0) {
//                        direction = 2;
//                    } else {
//                        direction = 1;
//                    }
//                } else if (ocpRef0.getCode().compareToIgnoreCase("PT4") == 0) {
//                    direction = 2;
//                } else {
//                    direction = 1;
//                }
//                /*if (ocpRef0.getId().compareToIgnoreCase(ocpRef1.getId()) < 0) //direction:2
//
//                    direction = 1;
//                } else {
//                    direction = 2;
//                }*/
//
//            } else {
//                System.out.println("ImporterTrip: cannot decide direction for train part: " + trainpart.getId());
//            }
//        }
//        return direction;
//    }
    // <editor-fold>
    private TTObject GetFromPlatform(EBlock railB) {
        // Get first blockpart
        TBlockPart railFBP = (TBlockPart) railB.getBlockPartSequence().get(0).getBlockPartRef().get(0).getRef();
        // Get first trainpart - to find start stations & start time reference
        ETrainPart railFTP = (ETrainPart) railFBP.getTrainPartRef();
        // Get first ocpTT
        EOcpTT railFOTT = (EOcpTT) railFTP.getOcpsTT().getOcpTT().get(0);

        // And this from station..
        return getPlatform(railFOTT);
    }

    private TTObject GetToPlatform(EBlock railB) {
        // TODO: IF THERE ARE MULTIPLE BLOCKPARTREF-elements, this doesnt work
        TBlockPart railLBP = (TBlockPart) railB.getBlockPartSequence().get(railB.getBlockPartSequence().size() - 1).getBlockPartRef().get(0).getRef();
        // Last trackpart
        ETrainPart railLTP = (ETrainPart) railLBP.getTrainPartRef();
        // last ocpTT
        // Get last ocpTT
        EOcpTT railLOTT = (EOcpTT) railLTP.getOcpsTT().getOcpTT().get(railLTP.getOcpsTT().getOcpTT().size() - 1);

        return getPlatform(railLOTT);
    }

    private int GetStartSecs(EBlock railB) {
        // Get first blockpart
        TBlockPart railFBP = (TBlockPart) railB.getBlockPartSequence().get(0).getBlockPartRef().get(0).getRef();
        // Get first trainpart - to find start stations & start time reference
        ETrainPart railFTP = (ETrainPart) railFBP.getTrainPartRef();

        // Get first ocpTT
        EOcpTT railFOTT = (EOcpTT) railFTP.getOcpsTT().getOcpTT().get(0);

        EArrivalDepartureTimes railADT = railFOTT.getTimes().get(0);

        Calendar cal = null;
        if (railADT.getArrival() == null) {
            cal = railADT.getDeparture().toGregorianCalendar();
        } else {
            cal = railADT.getArrival().toGregorianCalendar();
        }

        int secs = cal.get(Calendar.HOUR_OF_DAY) * 3600 + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.SECOND) + 3600 * 24 * railADT.getDepartureDay().intValue();

        // Since secs is actual DEPARTURE time, we need to subtract default stop time from it
        return secs;
    }

    private int GetStopSecs(EBlock railB) {
        // Get Last blockpart
        TBlockPart railFBP = (TBlockPart) railB.getBlockPartSequence().get(railB.getBlockPartSequence().size() - 1).getBlockPartRef().get(0).getRef();
        // Get Last trainpart - to find last stations & start time reference
        ETrainPart railLTP = (ETrainPart) railFBP.getTrainPartRef();

        // Get first ocpTT
        EOcpTT railLOTT = (EOcpTT) railLTP.getOcpsTT().getOcpTT().get(railLTP.getOcpsTT().getOcpTT().size() - 1);

        EArrivalDepartureTimes railADT = railLOTT.getTimes().get(0);

        Calendar cal = null;
        if (railADT.getDeparture() == null) {
            cal = railADT.getArrival().toGregorianCalendar();
        } else {
            cal = railADT.getDeparture().toGregorianCalendar();
        }

        int secs = cal.get(Calendar.HOUR_OF_DAY) * 3600 + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.SECOND) + 3600 * 24 * railADT.getArrivalDay().intValue();

        // Since secs is actual DEPARTURE time, we need to subtract default stop time from it
        return secs;
    }

    private TrainType GetTrainType(EBlock railB) {
        // Sanity check
        if (railB.getBlockPartSequence() == null || railB.getBlockPartSequence().isEmpty()
                || railB.getBlockPartSequence().get(0).getBlockPartRef() == null || railB.getBlockPartSequence().get(0).getBlockPartRef().isEmpty()
                || railB.getBlockPartSequence().get(0).getBlockPartRef().get(0).getRef() == null) {
            return null;
        }

        // Get first blockpart - to find traintype and daytype
        TBlockPart railFBP = (TBlockPart) railB.getBlockPartSequence().get(0).getBlockPartRef().get(0).getRef();

        return GetFromMap((TElementWithIDAndName) railFBP.getFormationRef());
        // And use it to find some data
//        TrainType tt = (TrainType)conversionMap.get(((EFormation)railFBP.getFormationRef()).getId());
//        return tt;
    }

    private DayType GetDayType(EBlock railB) {
        // Sanity check
        if (railB.getBlockPartSequence() == null || railB.getBlockPartSequence().isEmpty()
                || railB.getBlockPartSequence().get(0).getBlockPartRef() == null || railB.getBlockPartSequence().get(0).getBlockPartRef().isEmpty()
                || railB.getBlockPartSequence().get(0).getBlockPartRef().get(0).getRef() == null) {
            return null;
        }

        // Get first blockpart - to find traintype and daytype
        TBlockPart railFBP = (TBlockPart) railB.getBlockPartSequence().get(0).getBlockPartRef().get(0).getRef();

//        DayType dt = (DayType)conversionMap.get(((EOperatingPeriod)railFBP.getOperatingPeriodRef()).getId());
        return GetFromMap((TElementWithIDAndName) railFBP.getOperatingPeriodRef());
    }

    private TTArea GetTTArea(String line) {
        // Note: At this point we actually have to go to cache, as 'Line' is just an schedulename, not
        // reference to ocp in railML
        TTArea tta = (TTArea) ejbTTObjectFacade.find(line); // Can use TTObjectFacade, as TTArea is inherited from it

        if (tta == null) {
            System.out.println("Importer: GetTTArea: cannot find " + line);
        }

        return tta;
    }

    private TTArea GetTTArea(EBlock railB) {
        // Sanity check
        if (railB.getBlockPartSequence() == null || railB.getBlockPartSequence().isEmpty()
                || railB.getBlockPartSequence().get(0).getBlockPartRef() == null || railB.getBlockPartSequence().get(0).getBlockPartRef().isEmpty()
                || railB.getBlockPartSequence().get(0).getBlockPartRef().get(0).getRef() == null) {
            return null;
        }

        // Get first blockpart - to find traintype and daytype
        TBlockPart railFBP = (TBlockPart) railB.getBlockPartSequence().get(0).getBlockPartRef().get(0).getRef();
        // Get first trainpart - to find start stations & start time reference
        ETrainPart railFTP = (ETrainPart) railFBP.getTrainPartRef();

        return GetTTArea(railFTP.getLine());

    }
    // </editor-fold>

    private void ConstructTrainPartMaps(ETrainParts trainParts) {

        PathData = new HashMap<>();
        TemplateData = new HashMap<>();
        // Create hashed map which separates different paths
        for (ETrainPart tp : trainParts.getTrainPart()) {
            ImporterTrip trip = new ImporterTrip(tp);

            // Insert to PATH map
            if (PathData.get(trip.getPathHash()) == null) {
                PathData.put(trip.getPathHash(), new ArrayList<ImporterTrip>());
            }
            PathData.get(trip.getPathHash()).add(trip);

            // Insert to TEMPLATE map
            if (TemplateData.get(trip.getTemplateHash()) == null) {
                TemplateData.put(trip.getTemplateHash(), new ArrayList<ImporterTrip>());
            }
            TemplateData.get(trip.getTemplateHash()).add(trip);
        }

        // Dump stuff
        System.out.println("PATH BASED OUTPUT");
        DumpData(PathData, true);
        System.out.println("TEMPLATE BASED OUTPUT");
        DumpData(TemplateData, false);

    }

    private void DumpData(Map<String, List<ImporterTrip>> data, boolean printTrainParts) {
        int size = 0;
        for (Map.Entry<String, List<ImporterTrip>> entry : data.entrySet()) {
            size += entry.getValue().size();
        }

        System.out.println("Different keys: " + data.size() + " Sum of ImporterTrips: " + size);
        ArrayList<String> sortedKeys = new ArrayList<>(data.keySet());
        Collections.sort(sortedKeys);
        for (String key : sortedKeys) {
            System.out.println("[" + data.get(key).size() + "]: " + key);
            if (printTrainParts) {
                for (ImporterTrip it : data.get(key)) {
                    System.out.println(it.getRailMLTripId() + ": " + it.getTimeHash());

                }
            }
        }
    }

    /**
     * Goes through all found railML templates and finds (or creates) matching
     * openSchedule template to DB.
     */
    private void buildTripTemplates() throws ImporterException {
        for (Map.Entry<String, List<ImporterTrip>> entry : TemplateData.entrySet()) {
            BasicTrip template = FindTripTemplate(entry.getValue().get(0));
            if (template == null) {
                template = createTemplate(entry.getValue().get(0));
                if (template.getPlannedStartObj() == null || template.getPlannedStopObj() == null) {
                    System.out.println("Importer: BuildTripTemplates failed: " + template.getDescription());
                } else {
                    //ejbTripFacade.create(template);
                    //this.ejbTripFacade.evictAll();
                    entityManager.joinTransaction();
                    entityManager.persist(template);
                    templateMap.put(entry.getKey(), template);
                }
            }
        }
    }

    private BasicTripFilter ConstructTripTemplateFilter(ImporterTrip impTrip) {
        BasicTripFilter filter = new BasicTripFilter();
        filter.setPlannedStartObjFilter(this.getPlaform(impTrip.getStartPlatform()));
        filter.setPlannedStopObjFilter(this.getPlaform(impTrip.getStopPlatform()));
        filter.setAreaFilter(GetTTArea(impTrip.getLine()));
        filter.setValidFilter(true);
        filter.setTripMainTypeFilter(COMMERCIAL_TEMPLATE);
        filter.setDescriptionFilter(impTrip.getTemplateName());

        return filter;
    }

    private BasicTrip FindTripTemplate(ImporterTrip impTrip) throws ImporterException {
        // Construct filter
        BasicTripFilter filter = ConstructTripTemplateFilter(impTrip);
        // Find all matching templates
        List<BasicTrip> trips = ejbTripFacade.findAll(filter);

        // LOOP THROUGH TRIPS AND CHECK THEIR HASHES AGAINST IMPTRIP
        for (BasicTrip bt : trips) {
            String hash = BuildImporterTripHash(bt);
            System.out.println("existing:" + hash);
            if (hash == null ? impTrip.getTemplateHash() == null : hash.equals(impTrip.getTemplateHash())) {
                return bt;
            }
        }
        System.out.println("new:" + impTrip.getTemplateHash());
        return null;
    }

    private TripType GetTripType(Object categoryRef) {
        TripTypeFilter tripTypeFilter = new TripTypeFilter();
        tripTypeFilter.setValid(true);
        //tripTypeFilter.setOnlyMainTripTypeFilter(true);
        //tripTypeFilter.setTripType(TripType.TripMainType.COMMERCIAL_TEMPLATE);
        tripTypeFilter.setExternalId(((ECategory) categoryRef).getId());
        return this.ejbTripTypeFacade.findFirst(tripTypeFilter);
    }

    /**
     * HARD-CODED. Converts triptype to actiontype. This is needed as commercial
     * link between those types are missing in db
     *
     * @param tripType
     * @return
     */
    public ActionType getActionType(TripType tripType) {
        switch (tripType.getTripType()) {
            case COMMERCIAL_TEMPLATE:
                return this.RUN_COMMERCIAL_TRIP_ACTION;
            case NONCOMMERCIAL_TEMPLATE:
                return this.RUN_NONCOMMERCIAL_TRIP_ACTION;
            default:
                break;
        }

        return null;
    }

    private ActionType GetActionType(TripAction.MainActionTypeEnum mainActionTypeEnum, Integer subType) {
        ActionTypeFilter filter = new ActionTypeFilter();
        filter.setTypeFilter(mainActionTypeEnum);
        filter.setActionSubTypeFilter(subType);
        return ejbActionTypeFacade.findFirst(filter);
    }

    private TripUserType GetTripUserType(Integer id) {
        TripUserTypeFilter filter = new TripUserTypeFilter();
        filter.setTripUserTypeId(id);
        return ejbTripUserTypeFacade.findFirst(filter);
    }

    /**
     * Since template does start time 0 seconds and there is dwell on first
     * station, schedule's start time must be reduced that much. This is because
     * RailML xml's first entry is departure time
     *
     * @param template
     * @return
     */
//    private int FirstStopDwell(BasicTrip template) {
//        if (template != null && !template.getTripActions().isEmpty()) {
//            Integer typeId = template.getTripAction(1).getActionType().getAtypeId();
//            if ((typeId == PASSENGER_TAKE_ACTION.getAtypeId())
//                    || (typeId == SCHEDULING_STOP_WAITING_ACTION.getAtypeId()) || (typeId == SCHEDULING_STOP_WAITING_TURING_ACTION.getAtypeId())) {
//                return template.getTripAction(1).getPlannedSecs();
//            }
//            /*for (TripAction action : template.getTripActions()) {
//                if (action.getActionType().getAtypeId() == PASSENGER_TAKE_ACTION.getAtypeId()) {
//                    return action.getPlannedSecs();
//                }
//            }*/
//        }
//
//        return 0;
//    }

    /**
     * Since template does end at train arriving to station instead of waiting
     * passengers to leave, there is extra leave time, which needs to be added
     * to trip's end duration
     *
     * @param template
     * @return
     */
//    private int LastStopDwell(BasicTrip template) {
//        if (template != null && !template.getTripActions().isEmpty()) {
//            Integer typeId = template.getTripAction(template.getTripActions().size()).getActionType().getAtypeId();
//            if ((typeId == PASSENGER_LEAVE_ACTION.getAtypeId()) || (typeId == SCHEDULING_STOP_WAITING_ACTION.getAtypeId())
//                    || (typeId == SCHEDULING_STOP_WAITING_TURING_ACTION.getAtypeId())) {
//                return template.getTripAction(template.getTripActions().size()).getPlannedSecs();
//            }
//            /*for (TripAction action : template.getTripActions()) {
//                if (action.getActionType().getAtypeId() == PASSENGER_LEAVE_ACTION.getAtypeId()) {
//                    return action.getPlannedSecs();
//                }
//            }*/
//        }
//        return 0;
//    }

    private SchedulingState GetSchedulingState() {
        return ejbSchedulingStateFacade.find(SchedulingState.State.PLANNED_TO_RUN.getStateValue());
    }

    private String BuildImporterTripHash(BasicTrip bt) {

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
            if (ta instanceof ActionTrainMoving) {
                last = (ActionTrainMoving) ta;
                pathHash += FindEOcpKey(ta.getTimetableObject()) + "-";
                //timeHash += ta.getPlannedSecs() + "-";
                timeHash += "[null," + (ta.getTimeFromTripStart() - firstDwell) + "][" + (ta.getTimeFromTripStart() + ta.getPlannedSecs() - firstDwell) + ",null]";
            }
        }

        if (last != null) {
            pathHash += FindEOcpKey(last.getTimetableObject2()) + "-";
        }

        return bt.getTripType().getExternalId() + ": " + pathHash + " == " + timeHash;
    }

    private String FindEOcpKey(TTObject ttobj) {
        for (Map.Entry<String, Object> item : conversionMap.entrySet()) {
            if (item.getValue() instanceof TTObject && ((TTObject) item.getValue()).getTTObjId() == ttobj.getTTObjId()) {

                return item.getKey();
            }
        }
        return "NoN"; // Item does not exists, which is fine. DB can contain more TTObjects than in RailML
    }

    public boolean scheduledata_from_railml( Railml importedFile )
    {
        railml = importedFile;
        return this.Process();
    }
}
