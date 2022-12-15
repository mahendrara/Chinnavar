package schedule.uiclasses;

import schedule.entities.ActionCargoStop;
import schedule.entities.ActionPassObject;
import schedule.entities.ActionPassengerStop;
import schedule.entities.ActionSchedulingStop;
import schedule.entities.ActionTrainMoving;
import schedule.entities.BasicTrip;
import schedule.entities.TTObject;
import schedule.entities.TripAction;
import schedule.uiclasses.util.JsfUtil;
import schedule.uiclasses.util.PaginationHelper;
import schedule.sessions.TripFacade;
import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.inject.Inject;
import javax.inject.Named;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import schedule.entities.ActionFullTripDriveDuty;
import schedule.entities.ActionGlue;
import schedule.entities.ActionMovementTrip;
import schedule.entities.ActionRunTrip;
import schedule.entities.ActionTrainFormation;
import schedule.entities.ActionType;
import schedule.entities.Duty;
import schedule.entities.MainActionType;
import schedule.entities.MovementTrip;
import schedule.entities.MovementTripGroup;
import schedule.entities.PlannedDuty;
import schedule.entities.PlannedService;
import schedule.entities.Schedule;
import schedule.entities.ScheduledTrip;
import schedule.entities.TTArea;
import schedule.entities.TTObjectType;
import schedule.entities.TimedTrip;
import schedule.entities.TripAction.MainActionTypeEnum;
import schedule.entities.TripType;
import schedule.entities.TripType.TripMainType;
import schedule.entities.TripUserType;
import schedule.messages.Operation;
import schedule.messages.XmlMessageSender;
import schedule.sessions.ActionTypeFacade;
import schedule.sessions.MainActionTypeFacade;
import schedule.sessions.MovementTripFacade;
import schedule.sessions.PlannedDutyFacade;
import schedule.sessions.PlannedServiceFacade;
import schedule.sessions.TTAreaFacade;
import schedule.sessions.TTObjectTypeFacade;
import schedule.sessions.TimedTripFacade;
import schedule.sessions.TripActionFacade;
import schedule.sessions.TripTypeFacade;
import schedule.sessions.TripUserTypeFacade;
import schedule.uiclasses.util.UiText;
import schedule.uiclasses.util.config.ConfigBean;

// @TODO: Needs to extend BaseController
@SuppressWarnings("unchecked")
@Named("tripTemplateController")
@SessionScoped
public class TripTemplateController implements Serializable {

    private BasicTrip current;
    private DataModel items = null;
    //private DataModel areaItems = null;
    //private List<TTArea> areaFilterList = null; //"All" is included compared with areas
    //selected Area Filter
    private TTArea selectedArea;
    private TTArea areaAll;
    //private TripType mainTripTypeFilter;
    private TripType subTripTypeFilter;
    private TripType tripTypeAll;
    private TripType movementTripDefault;

    private List<TTArea> areas = null;
    private List<TripType> allSubTripTypes = null;
    @Inject
    private TripFacade ejbFacade;

    @Inject
    private MainActionTypeFacade ejbActionMainTypeFacade;
    @Inject
    private ActionTypeFacade ejbActionTypeFacade;
    @Inject
    private LanguageBean languageBean;
    @Inject
    private UiText uiText;
    @Inject
    private TTAreaFacade ejbTTAreaFacade;
    @Inject
    private TripTypeFacade ejbTripTypeFacade;
    @Inject
    private TTObjectTypeFacade ejbTTObjectTypeFacade;
    @Inject
    private MovementTripFacade ejbMovementTripFacade;
    @Inject
    private TripActionFacade ejbTripActionFacade;
    @Inject
    private TripUserTypeFacade ejbTripUserTypeFacade;
    @Inject
    private TimedTripFacade ejbTimedTripFacade;
    @Inject
    private PlannedServiceFacade ejbPlannedServiceFacade;
    @Inject
    private MaintenanceController maintenanceController;
    @Inject
    private PlannedServiceController plannedServiceController;
    @Inject
    protected XmlMessageSender xmlMessageSender;
    //@Inject
    //private MovementTripGroupFacade ejbMovementTripGroupFacade;

    private PaginationHelper pagination;
    private int selectedItemIndex;
    private final int itemsPerPage = 50;
    private StreamedContent chart = null;
    private StreamedContent tripChart = null;

    private List<TTObjectType> ttobjectTypes;
    private List<TripType> tripMainTypes;
    private TripType selectedTripMainType;
    private TripType selectedTripType;
    private TTObjectType selectedFromTTObjectType;
    private TTObjectType selectedToTTobjectType;
    private List<TripUserType> tripUserTypes = null;
    private TripUserType tripUserTypeAll;
    private TripUserType selectedTripUserType;

    List<MainActionType> mainActionTypes;
    private MainActionType selectedMainActionType;
    private ActionType selectedActionType;
    private TTObjectType selectedActionTTObjectType1;
    private TTObjectType selectedActionTTObjectType2;

    private MainActionType movementTripActionMainType;
    //private ActionType selectedMovementTripActionType;
    private ActionType defaultMovementTripActionType;
    private ActionType alternativeMovementTripActionType;
    private TripType movementTripGroupTripType;
    private MovementTrip selectedMovementTrip;

    private String clonedDescription;

    private Map<String, Integer> days;
    private boolean valid;

    /*static {
     days = new LinkedHashMap<>();
     for (int i = 0; i < 8; i++) {
     days.put("+" + i, i);
     }
     }*/
    public TripTemplateController() {
        this.days = new LinkedHashMap<>();
    }

    @PostConstruct
    public void init() {
        /*if (areaFilterList == null) {

         areaFilterList = ejbTTAreaFacade.findAll();
         if (areaFilterList.size() != 1) {

         areaFilterList.add(0, new TTArea(0, uiText.get("FilterAll")));
         }
         }*/

        areaAll = new TTArea(0, uiText.get("FilterAll"));

        if (areas == null) {
            areas = ejbTTAreaFacade.findAll();
        }
        if (selectedArea == null && areas.size() > 0) {
            selectedArea = areas.get(0);
        }
        this.tripTypeAll = new TripType(0, uiText.get("FilterAll"));
        //this.mainTripTypeFilter = tripTypeAll;
        this.subTripTypeFilter = tripTypeAll;

        allSubTripTypes = new ArrayList<>();
        tripMainTypes = new ArrayList<>();
        TripTypeFilter tripTypeFilter = new TripTypeFilter();
        tripTypeFilter.setValid(true);
        tripTypeFilter.setOnlyMainTripTypeFilter(true);
        tripTypeFilter.setTripType(TripMainType.COMMERCIAL_TEMPLATE);
        List<TripType> temp = this.ejbTripTypeFacade.findAll(tripTypeFilter);
        if (temp != null && temp.isEmpty() == false) {
            tripMainTypes.add(temp.get(0));
            TripTypeFilter tripTypeFilter2 = new TripTypeFilter();
            tripTypeFilter2.setValid(true);
            tripTypeFilter2.setOnlySubTripTypeFilter(true);
            tripTypeFilter2.setTripType(temp.get(0).getTripType());
            allSubTripTypes.addAll(ejbTripTypeFacade.findAll(tripTypeFilter2));
        }

        tripTypeFilter.setTripType(TripMainType.NONCOMMERCIAL_TEMPLATE);
        temp = this.ejbTripTypeFacade.findAll(tripTypeFilter);
        if (temp != null && temp.isEmpty() == false) {
            tripMainTypes.add(temp.get(0));
            TripTypeFilter tripTypeFilter2 = new TripTypeFilter();
            tripTypeFilter2.setValid(true);
            tripTypeFilter2.setOnlySubTripTypeFilter(true);
            tripTypeFilter2.setTripType(temp.get(0).getTripType());
            allSubTripTypes.addAll(ejbTripTypeFacade.findAll(tripTypeFilter2));
        }

        tripTypeFilter.setTripType(TripMainType.OPPORTUNITY_TEMPLATE);
        temp = this.ejbTripTypeFacade.findAll(tripTypeFilter);
        if (temp != null && temp.isEmpty() == false) {
            tripMainTypes.add(temp.get(0));
        }
        if (this.tripMainTypes.isEmpty() == false) {
            this.selectedTripMainType = tripMainTypes.get(0);
            tripTypeFilter.setTripType(selectedTripMainType.getTripType());
            tripTypeFilter.setOnlySubTripTypeFilter(true);
            temp = ejbTripTypeFacade.findAll(tripTypeFilter);
            if (temp != null && temp.size() > 0 && temp.get(0).isValid()) {
                this.selectedTripType = temp.get(0);
            } else {
                this.selectedTripType = this.selectedTripMainType;
            }
        }

        tripTypeFilter.setTripType(TripMainType.MOVEMENT_TRIP);
        tripTypeFilter.setTripSubType(MovementTrip.MovementTripSubType.PLANNED.getValue());
        tripTypeFilter.setOnlyMainTripTypeFilter(false);
        tripTypeFilter.setOnlySubTripTypeFilter(false);
        movementTripDefault = ejbTripTypeFacade.findFirst(tripTypeFilter);

        this.selectedFromTTObjectType = this.ejbTTObjectTypeFacade.findAll().get(0);
        this.selectedToTTobjectType = this.ejbTTObjectTypeFacade.findAll().get(0);

        mainActionTypes = new ArrayList<>();

        MainActionType actionMainType = ejbActionMainTypeFacade.find(TripAction.MainActionTypeEnum.PASSENGER_STOP.getValue());
        if (actionMainType != null && actionMainType.isUsed()) {
            mainActionTypes.add(actionMainType);
            selectedMainActionType = actionMainType;
            selectedActionType = selectedMainActionType.getActionTypes().get(0);
            selectedActionTTObjectType1 = selectedActionType.getTTObjTypes().get(0);
            selectedActionTTObjectType2 = selectedActionType.getTTObjTypes().get(0);
            /*List<ActionType> takePassengers = this.ejbActionTypeFacade.findAll(actionMainType, ActionPassengerStopTypeEnum.TAKE_PASSENGER.getValue());
             if (takePassengers.size() > 0) {
             defaultOrigoSecs = takePassengers.get(0).getDefaultPlanSecs();
             }*/

        }

        actionMainType = ejbActionMainTypeFacade.find(TripAction.MainActionTypeEnum.SCHEDULING_STOP.getValue());
        if (actionMainType != null && actionMainType.isUsed()) {
            mainActionTypes.add(actionMainType);
        }

        actionMainType = ejbActionMainTypeFacade.find(TripAction.MainActionTypeEnum.CARGO_STOP.getValue());
        if (actionMainType != null && actionMainType.isUsed()) {
            mainActionTypes.add(actionMainType);
        }

        actionMainType = ejbActionMainTypeFacade.find(TripAction.MainActionTypeEnum.TRAIN_MOVING.getValue());
        if (actionMainType != null && actionMainType.isUsed()) {
            mainActionTypes.add(actionMainType);
        }

//        actionMainType = ejbActionMainTypeFacade.find(TripAction.MainActionTypeEnum.TRAIN.getValue());
//        if (actionMainType != null && actionMainType.isUsed()) {
//            mainActionTypes.add(actionMainType);
//        }
        actionMainType = ejbActionMainTypeFacade.find(TripAction.MainActionTypeEnum.PASS_OBJECT.getValue());
        if (actionMainType != null && actionMainType.isUsed()) {
            mainActionTypes.add(actionMainType);
        }

        movementTripActionMainType = ejbActionMainTypeFacade.find(TripAction.MainActionTypeEnum.MOVEMENT_TRIP_ACTION.getValue());
        if (movementTripActionMainType != null && movementTripActionMainType.isUsed()) {
            //this.selectedMovementTripActionType = movementTripActionMainType.getActionTypes().get(0);
            ActionTypeFilter actionTypeFilter = new ActionTypeFilter();
            actionTypeFilter.setTypeFilter(movementTripActionMainType);
            actionTypeFilter.setActionSubTypeFilter(ActionMovementTrip.ActionMovementTripTypeEnum.DEFAULT.getValue());
            this.defaultMovementTripActionType = this.ejbActionTypeFacade.findAll(actionTypeFilter).get(0);
            actionTypeFilter.setActionSubTypeFilter(ActionMovementTrip.ActionMovementTripTypeEnum.ALTERNATIVE.getValue());
            this.alternativeMovementTripActionType = this.ejbActionTypeFacade.findAll(actionTypeFilter).get(0);
        }

        tripTypeFilter = new TripTypeFilter();
        tripTypeFilter.setValid(true);
        tripTypeFilter.setOnlyMainTripTypeFilter(true);
        tripTypeFilter.setTripType(TripMainType.MOVEMENT_TRIP_GROUP);
        List<TripType> tripTypes = this.ejbTripTypeFacade.findAll(tripTypeFilter);
        if (tripTypes.size() > 0) {
            this.movementTripGroupTripType = tripTypes.get(0);
        }

        TTObjectTypeFilter filter = new TTObjectTypeFilter();
        filter.setStartTTObjectTypeFilter();
        this.ttobjectTypes = this.ejbTTObjectTypeFacade.findAll(filter);

        if (this.tripUserTypes == null) {
            this.tripUserTypes = ejbTripUserTypeFacade.findAll();

            tripUserTypeAll = new TripUserType();
            tripUserTypeAll.setTripUserTypeId(0);
            tripUserTypeAll.setDescription(uiText.get("FilterAll"));
            this.selectedTripUserType = tripUserTypeAll;
        }

        valid = true;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public List<ActionType> getActionTypes(TripAction tripAction) {
        return tripAction.getActionType().getMainActionType().getActionTypes();
    }

    public TripUserType getSelectedTripUserType() {
        return selectedTripUserType;
    }

    public void setSelectedTripUserType(TripUserType selectedTripUserType) {
        this.selectedTripUserType = selectedTripUserType;
    }

    public TripUserType getTripUserTypeAll() {
        tripUserTypeAll.setDescription(uiText.get("FilterAll"));
        return this.tripUserTypeAll;
    }

    public List<TripUserType> getTripUserTypes() {
        return tripUserTypes;
    }

    public TTArea getAreaAll() {
        areaAll.setDescription(uiText.get("FilterAll"));
        return this.areaAll;
    }

    public TripType getSelectedTripMainType() {
        return selectedTripMainType;
    }

    public void setSelectedTripMainType(TripType selectedTripMainType) {
        this.selectedTripMainType = selectedTripMainType;
    }

    public TripType getSelectedTripType() {
        return selectedTripType;
    }

    public void setSelectedTripType(TripType selectedTripType) {
        this.selectedTripType = selectedTripType;
    }

    /*public TripType getMainTripTypeFilter() {
        return mainTripTypeFilter;
    }

    public void setMainTripTypeFilter(TripType mainTripTypeFilter) {
        this.mainTripTypeFilter = mainTripTypeFilter;
    }*/
    public TripType getSubTripTypeFilter() {
        return subTripTypeFilter;
    }

    public void setSubTripTypeFilter(TripType subTripTypeFilter) {
        this.subTripTypeFilter = subTripTypeFilter;
    }

    public TripType getTripTypeAll() {
        return tripTypeAll;
    }

    public void setTripTypeAll(TripType tripTypeAll) {
        this.tripTypeAll = tripTypeAll;
    }

    public List<TTArea> getAreas() {
        return areas;
    }

    public void setAreas(List<TTArea> areas) {
        this.areas = areas;
    }

    public TTArea getSelectedArea() {
        return selectedArea;
    }

    public void setSelectedArea(TTArea selectedArea) {
        this.selectedArea = selectedArea;
    }

    public TTObjectType getSelectedFromTTObjectType() {
        return selectedFromTTObjectType;
    }

    public void setSelectedFromTTObjectType(TTObjectType selectedFromTTObjectType) {
        this.selectedFromTTObjectType = selectedFromTTObjectType;
    }

    public TTObjectType getSelectedToTTObjectType() {
        return selectedToTTobjectType;
    }

    public void setSelectedToTTObjectType(TTObjectType selectedToTTobjectType) {
        this.selectedToTTobjectType = selectedToTTobjectType;
    }

    public List<TTObjectType> getPossibleFromTTObjectTypes() {
        return ttobjectTypes;
        // return this.ejbTTObjectTypeFacade.findAll();
    }

    public List<TTObjectType> getPossibleToTTObjectTypes() {
        return ttobjectTypes;
        // return this.ejbTTObjectTypeFacade.findAll();
    }

    public List<TTObject> getPossibleFromLocations() {
        if (this.selectedFromTTObjectType == null) {
            this.selectedFromTTObjectType = this.ejbTTObjectTypeFacade.findAll().get(0);
        }
        return this.selectedFromTTObjectType.getTTObjects();
    }

    public List<TTObject> getPossibleToLocations() {
        return this.selectedToTTobjectType.getTTObjects();
    }

    public MainActionType getSelectedMainActionType() {
        return selectedMainActionType;
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

    public List<MainActionType> getMainActionTypes() {
        return mainActionTypes;
    }

    public List<ActionType> getMovementTripActionTypes() {
        return this.movementTripActionMainType.getActionTypes();
    }

    public List<ActionType> getActionTypes() {
        List<ActionType> actionTypes = new ArrayList();
        Iterator<ActionType> iterator = this.selectedMainActionType.getActionTypes().iterator();
        while (iterator.hasNext()) {
            ActionType actionType = iterator.next();
            if (actionType.isUsed()) {
                actionTypes.add(actionType);
            }
        }
        return actionTypes;
    }

    public TTObjectType getSelectedActionTTObjectType1() {
        return selectedActionTTObjectType1;
    }

    public void setSelectedActionTTObjectType1(TTObjectType selectedActionTTObjectType1) {
        this.selectedActionTTObjectType1 = selectedActionTTObjectType1;
    }

    public TTObjectType getSelectedActionTTObjectType2() {
        return selectedActionTTObjectType2;
    }

    public void setSelectedActionTTObjectType2(TTObjectType selectedActionTTObjectType2) {
        this.selectedActionTTObjectType2 = selectedActionTTObjectType2;
    }

    public List<TTObjectType> getPossibleActionTTObjectTypes() {
        return this.selectedActionType.getTTObjTypes();
    }

    public List<TTObject> getPossibleActionLocations1() {
        return this.selectedActionTTObjectType1.getTTObjects();
    }

    public List<TTObject> getPossibleActionLocations2() {
        return this.selectedActionTTObjectType2.getTTObjects();
    }

    public void selectedMainActionTypeChanged() {
        this.selectedActionType = this.selectedMainActionType.getActionTypes().get(0);
        selectedActionTypeChanged();
    }

    public void selectedActionTypeChanged() {
        selectedActionTTObjectType1 = selectedActionType.getTTObjTypes().get(0);
    }

    public void mainTripTypeFilterChanged() {
        this.subTripTypeFilter = this.tripTypeAll;
    }

    public boolean isAddTripActionAllowed() {

        /*if (current != null) {
            if (current.getTripActions().isEmpty()) {
                return true;
            } else {
                TripAction tripAction = current.getTripActions().get(current.getTripActions().size() - 1);
                return tripAction != null && !tripAction.isCreating(); // the new item is always at index 0
            }
        } else {
            return false;
        }*/
        if (current != null) {
            List<TripAction> list = (List<TripAction>) getSelected().getTripActions();
            if (list == null || list.isEmpty()) {
                return true;
            } else {
                TripAction e = (TripAction) list.get(current.getTripActions().size() - 1);
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

    private List<TimedTrip> getTimedTripsForTrip(BasicTrip trip) {
        return ejbTimedTripFacade.getTimedTripsForTemplate(trip);
    }

    public List<TimedTrip> getTimedTripForSelected() {
        //when template might have been changed in plannedservice or scheduled service page
        //current might not be managed
        return getTimedTripsForTrip(this.ejbFacade.find(current.getTripId()));
    }

    public List<TripType> getPossibleTripMainTypes() {
        return this.tripMainTypes;
    }

    public List<TripType> getPossibleTripTypes() {
        TripTypeFilter tripTypeFilter = new TripTypeFilter();
        tripTypeFilter.setValid(true);
        tripTypeFilter.setOnlySubTripTypeFilter(true);
        tripTypeFilter.setTripType(this.selectedTripMainType.getTripType());
        List<TripType> temp = ejbTripTypeFacade.findAll(tripTypeFilter);
        return temp;
        //return ejbTripTypeFacade.findAll(tripTypeFilter);
    }

    public List<TripType> getPossibleSubTripTypes(TripType tripType) {
        TripTypeFilter tripTypeFilter = new TripTypeFilter();
        tripTypeFilter.setValid(true);
        tripTypeFilter.setOnlySubTripTypeFilter(true);
        tripTypeFilter.setTripType(tripType.getTripType());
        List<TripType> temp = ejbTripTypeFacade.findAll(tripTypeFilter);
        return temp;
    }

    public List<TripType> getAllSubTripTypes() {
        return this.allSubTripTypes;
    }

    public List<Integer> getPossibleSeqNos() {
        List<Integer> list;
        list = new ArrayList<>();
        for (int i = 0; i < current.getNumberOfActions(); i++) {
            list.add(i + 1);
        }
        return list;
    }

    public void selectedTripMainTypeChanged() {
        List<TripType> list = this.getPossibleTripTypes();
        this.selectedTripType = list.isEmpty() ? this.selectedTripMainType : list.get(0);
    }

    public MovementTrip getSelectedMovementTrip() {
        return selectedMovementTrip;
    }

    public void setSelectedMovementTrip(MovementTrip selectedMovementTrip) {
        this.selectedMovementTrip = selectedMovementTrip;
    }

    public List<MovementTrip> getMovementTrips(TripAction tripAction) {
        //e.g. ActionTrainMoving from platform A1 to platform B1, we get all movement trips from station A to station B
        TTObject startChildObj = tripAction.getTimetableObject();
        TTObject stopChildObj = null;
        if (tripAction.hasSecondObject()) {
            if (tripAction instanceof ActionTrainMoving) {
                stopChildObj = ((ActionTrainMoving) tripAction).getTimetableObject2();
            }
        }
        if (startChildObj == null || stopChildObj == null) {
            selectedMovementTrip = null;
            return null;
        }

        MovementTripFilter filter = new MovementTripFilter();
        // If parent object is missing, use object itself 
        if (startChildObj.getParentObject() != null) {
            filter.setPlannedStartObjParentFilter(startChildObj.getParentObject());
        } else {
            filter.setPlannedStartObjFilter(startChildObj);
        }
        if (stopChildObj.getParentObject() != null) {
            filter.setPlannedStopObjParentFilter(stopChildObj.getParentObject());
        } else {
            filter.setPlannedStopObjFilter(stopChildObj);
        }
        filter.setAreaFilter(current.getAreaObj());
        filter.setTripTypeFilter(this.movementTripDefault);

        List<MovementTrip> list = this.ejbMovementTripFacade.findAll(filter);

        // Set selection
        if (list.isEmpty()) {
            selectedMovementTrip = null;
        } else if (list.indexOf(selectedMovementTrip) == -1) {
            selectedMovementTrip = list.get(0);
        }

        return list;
    }

    public void actionTTObjectType1Changed(TripAction tripAction) {
        if (this.selectedActionTTObjectType1.getTTObjects().size() > 0) {
            tripAction.setTimetableObject(this.selectedActionTTObjectType1.getTTObjects().get(0));
        } else {
            tripAction.setTimetableObject(null);
        }

        //if (tripAction.getTimetableObject() != null) {
        possibleLocationsChanged(tripAction);
        //}
    }

    public void actionTTObjectType2Changed(TripAction tripAction) {
        if (tripAction instanceof ActionTrainMoving) {
            if (this.selectedActionTTObjectType2.getTTObjects().size() > 0) {
                ((ActionTrainMoving) tripAction).setTimetableObject2(this.selectedActionTTObjectType2.getTTObjects().get(0));

            } else {
                ((ActionTrainMoving) tripAction).setTimetableObject2(null);
                //((ActionTrainMoving) tripAction).setRefTrip(null);
            }
        }

        //if (tripAction.getTimetableObject() != null) {
        possibleLocationsChanged(tripAction);
        //}
    }

    public void possibleLocationsChanged(TripAction tripAction) {
        if (tripAction.isEditing()) {
            if (tripAction instanceof ActionTrainMoving) {
                ActionTrainMoving actionTrainMoving = (ActionTrainMoving) tripAction;
                MovementTripGroup movementTripGroup = (MovementTripGroup) actionTrainMoving.getRefTrip();
                if (movementTripGroup != null) {
                    if (actionTrainMoving.getTimetableObject() != null) {
                        if (actionTrainMoving.getTimetableObject().getParentObject() != null) {
                            movementTripGroup.setPlannedStartObj(actionTrainMoving.getTimetableObject().getParentObject());
                        } else {
                            movementTripGroup.setPlannedStartObj(actionTrainMoving.getTimetableObject());
                        }
                    } else {
                        movementTripGroup.setPlannedStartObj(null);
                    }
                    if (actionTrainMoving.getTimetableObject2() != null) {
                        if (actionTrainMoving.getTimetableObject2().getParentObject() != null) {
                            movementTripGroup.setPlannedStopObj(actionTrainMoving.getTimetableObject2().getParentObject());
                        } else {
                            movementTripGroup.setPlannedStartObj(actionTrainMoving.getTimetableObject2());
                        }
                    } else {
                        movementTripGroup.setPlannedStopObj(null);
                    }

                    if (movementTripGroup.getPlannedStartObj() != null && movementTripGroup.getPlannedStopObj() != null) {
                        movementTripGroup.setDescription(actionTrainMoving.getTimetableObject().getDescription() + "->" + actionTrainMoving.getTimetableObject2().getDescription());
                    } else {
                        movementTripGroup.setDescription("->");
                    }

                    List<MovementTrip> list = getMovementTrips(tripAction);
                    if (list != null && !list.isEmpty()) {
                        selectedMovementTrip = list.get(0);
                        Iterator iterator = movementTripGroup.getActionsIterator();
                        ActionMovementTrip actionMovementTrip;
                        while (iterator.hasNext()) {
                            actionMovementTrip = (ActionMovementTrip) iterator.next();
                            if (actionMovementTrip.isCreating()) {
                                actionMovementTrip.setTimetableObject(selectedMovementTrip.getPlannedStartObj());
                                actionMovementTrip.setTimetableObject2(selectedMovementTrip.getPlannedStopObj());
                                actionMovementTrip.setRefTrip(selectedMovementTrip);
                                actionMovementTrip.setPlannedSecs(0);
                                actionMovementTrip.setMinSecs(0);
                            }
                        }
                    } else {
                        selectedMovementTrip = null;
                        Iterator iterator = movementTripGroup.getActionsIterator();
                        ActionMovementTrip actionMovementTrip;
                        while (iterator.hasNext()) {
                            actionMovementTrip = (ActionMovementTrip) iterator.next();
                            if (actionMovementTrip.isCreating()) {
                                actionMovementTrip.setTimetableObject(null);
                                actionMovementTrip.setTimetableObject2(null);
                                actionMovementTrip.setRefTrip(null);
                                actionMovementTrip.setPlannedSecs(0);
                                actionMovementTrip.setMinSecs(0);
                            }
                        }
                    }
                }
                //this.movementTripChanged((ActionTrainMoving)tripAction);
            }
        }
    }

    public StreamedContent getTrainGraph() {
        //Chart
        try {
            BasicTrip tripToDraw = getSelected();

            JFreeChart jfreechart = ChartFactory.createLineChart(tripToDraw.getDescription(), "Station", "Time", createTripDataset(tripToDraw), PlotOrientation.VERTICAL, true, true, false);
            languageBean.applyChartTheme(jfreechart);
            File chartFile = new File("dynamichart");
            ChartUtilities.saveChartAsPNG(chartFile, jfreechart, 1000, 1000);
            tripChart = new DefaultStreamedContent(new FileInputStream(chartFile), "image/png");
        } catch (Exception e) {
            //logger.severe(e.getMessage());
        }
        return tripChart;
    }

    private DefaultCategoryDataset createTripDataset(BasicTrip tripToDraw) {

        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        List<TripAction> allActions = tripToDraw.getTripActions();
        for (TripAction action : allActions) {
            if (action.getClass() != ActionTrainMoving.class) {
                if (action.getTimetableObject() != null) {
                    dataset.addValue(action.getTimeFromTripStart(), "NORMAL", action.getMainObjectName());
                    dataset.addValue(action.getMinTimeFromTripStart(), "MIN", action.getMainObjectName());
                }
            }
        }
        return dataset;
    }

    public BasicTrip getSelected() {
        if (current == null) {
            current = new BasicTrip();
            selectedItemIndex = -1;
        }
        return current;
    }

    private TripFacade getFacade() {
        return ejbFacade;
    }

    public PaginationHelper getPagination() {

        if (pagination == null) {
            pagination = new PaginationHelper(this.itemsPerPage) {

                @Override
                public int getItemsCount() {
                    BasicTripFilter filter = new BasicTripFilter();
                    filter.setValidFilter(valid);
                    if (selectedArea.getTTObjId() != 0) {
                        filter.setAreaFilter((TTArea) selectedArea);
                    }
                    /*if (mainTripTypeFilter.getTripTypeId() != 0) {
                        if (subTripTypeFilter.getTripTypeId() == 0) {
                            filter.setTripTypeFilter(mainTripTypeFilter.getTripType());
                        } else {
                            filter.setTripTypeFilter(subTripTypeFilter);
                        }
                    }*/
                    if (subTripTypeFilter != null && subTripTypeFilter.getTripTypeId() != 0) {
                        filter.setTripSubTypeFilter(subTripTypeFilter);
                    }

                    if (selectedTripUserType != null && selectedTripUserType.getTripUserTypeId() != 0) {
                        filter.setTripUserTypeFilter(selectedTripUserType);
                    }
                    return getFacade().count(filter);

                }

                @Override
                public DataModel createPageDataModel() {
                    BasicTripFilter filter = new BasicTripFilter();
                    filter.setValidFilter(valid);
                    if (selectedArea.getTTObjId() != 0) {
                        filter.setAreaFilter((TTArea) selectedArea);
                    }

                    if (subTripTypeFilter != null && subTripTypeFilter.getTripTypeId() != 0) {
                        filter.setTripSubTypeFilter(subTripTypeFilter);
                    }

                    if (selectedTripUserType != null && selectedTripUserType.getTripUserTypeId() != 0) {
                        filter.setTripUserTypeFilter(selectedTripUserType);
                    }
                    return new ListDataModel(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}, filter));
                    //filter.setUseOrder(true);

                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        //area = null;
        recreateModel();
        return "List";
    }

    public String prepareView() {
        current = (BasicTrip) getItems().getRowData();
        updateState();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareGraph() {
        return "ViewGraph";
    }

    public String backToDetailsView() {
        return "View";
    }

    public String getClonedDescription() {
        return clonedDescription;
    }

    public void setClonedDescription(String clonedDescription) {
        this.clonedDescription = clonedDescription;
    }

    public String cloneBasicTrip() {

        current = (BasicTrip) getItems().getRowData();

        BasicTrip clonedTrip = new BasicTrip();

        if (clonedDescription != null && !clonedDescription.isEmpty()) {
            clonedTrip.setDescription(clonedDescription);
        } else {
            JsfUtil.addErrorMessage(uiText.get("RequiredMessage_description"));
            return null;
        }

        try {

            getFacade().cloneTrip(current, clonedTrip);
            xmlMessageSender.sendTripTemplateMsg(clonedTrip, Operation.CREATE);
            JsfUtil.addSuccessMessage(uiText.get("TripCreated"));
            recreateModel();
//            return "List";
            return null;

        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
            return null;
        }

    }

    public String prepareEdit() {
        current = (BasicTrip) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public Long usedBy(BasicTrip basicTrip) {
        if (basicTrip != null && basicTrip.getTripId() != null) {
            return this.ejbTimedTripFacade.countByTripTemplate(basicTrip.getTripId());
        } else {
            return 0L;
        }
    }

    private void updateState() {
        this.ejbFacade.evict(current);
        this.ejbTripActionFacade.evictAll();
        current = this.ejbFacade.find(current.getTripId());
    }

    private void updatePlannedServices(BasicTrip tripTemplate) {
        boolean supportDuty = ConfigBean.getConfig().getPage("duty").isVisible();
        if (usedBy(tripTemplate) > 0) {
            List<PlannedService> plannedServices = this.ejbPlannedServiceFacade.findAll();
            List<Duty> duties = plannedServiceController.getDutyFacade().findAll();
            for (PlannedService service : plannedServices) {
                Iterator<TripAction> iterator = service.getTripActions().iterator();
                BasicTrip template;
                while (iterator.hasNext()) {
                    TripAction tripAction = iterator.next();
                    if (tripAction instanceof ActionRunTrip) {
                        TimedTrip timedTrip = (TimedTrip) ((ActionRunTrip) tripAction).getRefTrip();
                        template = timedTrip.getTripTemplate();
                        if (template != null) {
                            timedTrip.setPlannedStartSecs(service.getPlannedStartSecs() + tripAction.getTimeFromTripStart());
                            timedTrip.setPlannedStopSecs(timedTrip.getPlannedStartSecs() + template.getDurationSecs());
                            timedTrip.setAreaObj(template.getAreaObj());
                            tripAction.setMinSecs(template.getDefaultMinSecs());
                            tripAction.setPlannedSecs(template.getDurationSecs());
                            tripAction.setTimetableObject(template.getPlannedStartObj());
                            ((ActionRunTrip) tripAction).setTimetableObject2(template.getPlannedStopObj());

                            if (template.getPlannedStartObj() != null) {
                                timedTrip.setPlannedStartObj(template.getPlannedStartObj());
                                timedTrip.setDescription(template.getPlannedStartObj().getText(languageBean.getOSLocale()));
                            }
                            if (template.getPlannedStopObj() != null) {
                                timedTrip.setPlannedStopObj(template.getPlannedStopObj());
                                timedTrip.setDescription(timedTrip.getDescription() + " - " + template.getPlannedStopObj().getText(languageBean.getOSLocale()));
                            }

                            timedTrip.setDurationSecs(template.getDurationSecs());
                            timedTrip.setTripType(this.plannedServiceController.GetToTripType(template.getTripType()));
                            timedTrip.increaseVersion();
                            if (supportDuty) {
                                ActionFullTripDriveDuty dutyAction = timedTrip.getFullTripDriveDutyAction();
                                if (dutyAction != null) {
                                    dutyAction.setTimetableObject(timedTrip.getPlannedStartObj());
                                    dutyAction.setTimetableObject2(timedTrip.getPlannedStopObj());
                                    dutyAction.setPlannedSecs(template.getDurationSecs());
                                    dutyAction.setMinSecs(template.getDefaultMinSecs());
                                    Duty duty = (Duty) (dutyAction.getTrip());
                                    duty.actionChanged();
                                    this.plannedServiceController.getDutyFacade().edit(duty);
                                }
                            }
                        }
                    }
                }
                if (service.getNumberOfActions() > 3) {
                    service.setPlannedStartObj(service.getTripAction(3).getTimetableObject());
                    service.getTripAction(1).setTimetableObject(service.getPlannedStartObj());
                    service.getTripAction(2).setTimetableObject(service.getPlannedStartObj());

                    service.setPlannedStopObj(service.getTripAction(service.getNumberOfActions() - 2).getTimetableObject2());
                    service.getTripAction(service.getNumberOfActions()).setTimetableObject(service.getPlannedStopObj());
                    service.getTripAction(service.getNumberOfActions() - 1).setTimetableObject(service.getPlannedStopObj());
                }
                service.updateActionTimes();
                service.updateTripTimes();
                service.updatePlannedEndTime();
                service.setDurationSecs(service.getPlannedStopSecs() - service.getPlannedStartSecs());
                service.increaseVersion();

                this.ejbPlannedServiceFacade.edit(service);
            }
            xmlMessageSender.sendServiceDutyChangeMsg(plannedServices, duties);
            this.ejbPlannedServiceFacade.evictAll();
            plannedServiceController.getDutyFacade().evictAll();

            //We will not handle history changes
            //Calendar today = Calendar.getInstance();
            //maintenanceController.deleteScheduledServices(Schedule.convertToDayCode(today), Integer.MAX_VALUE);
            //maintenanceController.loadScheduledDays();
        }
    }

    public String save() {
        current = (BasicTrip) getItems().getRowData();
        if (current.isCreating()) {
            try {
                current.setOrigoSecs(0 - Math.abs(current.getOrigoSecs()));
                current.setCreating(false);
                current.setEditing(false);
                getFacade().create(current);
                xmlMessageSender.sendTripTemplateMsg(current, Operation.CREATE);
                JsfUtil.addSuccessMessage(uiText.get("TripCreated"));
            } catch (Exception e) {
                JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
                current.setCreating(true);
                current.setEditing(true);
                return null;
            }
        } else {
            try {
                Iterator it = current.getActionsIterator();
                while (it.hasNext()) {
                    TripAction action = (TripAction) it.next();
                    if (action instanceof ActionTrainMoving) {
                        if (((ActionTrainMoving) action).getRefTrip() != null) {
                            ((ActionTrainMoving) action).getRefTrip().setValid(current.isValid());
                        }
                    }
                }
                current.setEditing(false);
                current.increaseVersion();
                getFacade().edit(current);
                
                updatePlannedServices(current);

                xmlMessageSender.sendTripTemplateMsg(current, Operation.MODIFY);
                JsfUtil.addSuccessMessage(uiText.get("TripUpdated"));
            } catch (Exception e) {
                Logger.getLogger(TripTemplateController.class.getName()).log(Level.SEVERE, null, e);
                JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
                current.setEditing(true);
                return null;
            }
        }
        recreateModel();
        getFacade().evict(current);
        current = null;
        selectedItemIndex = -1;
        return null;
        //return "View";

    }

    public String cancel() {
        try {
            current = (BasicTrip) getItems().getRowData();
            current.setEditing(false);
            return null;
            //return "View";
        } catch (Exception e) {
            return null;
        }
    }

    public String cancelTripAction(TripAction tripAction) {
        tripAction.setEditing(false);
        tripAction.setCreating(false);
        tripAction.getTrip().updateActionTimes();
        // Need to dump current object to get new one
        current = this.ejbFacade.find(current.getTripId());

        return "";
    }

    public String cancelMovementTripAction(ActionMovementTrip movementTripAction) {
        movementTripAction.setEditing(false);
        movementTripAction.setCreating(false);
        return "";
    }

    public String saveTripAction(TripAction tripAction) {
        try {
            /*if (isCurrentInUse(true)) {
                recreateModel();
                current = this.ejbFacade.find(current.getTripId());
                tripAction.setEditing(false);
                JsfUtil.addErrorMessage(uiText.get("Error_TripTemplateUsed2"));
                return null;
            }*/

            if (tripAction instanceof ActionTrainMoving) {
                tripAction.setMinSecs(tripAction.getPlannedSecs());
                MovementTripGroup movementTripGroup = (MovementTripGroup) ((ActionTrainMoving) tripAction).getRefTrip();
                if (movementTripGroup != null) {
                    movementTripGroup.setValid(tripAction.getTrip().isValid());
                    Iterator<TripAction> iterator = movementTripGroup.getTripActions().iterator();
                    ActionMovementTrip actionMovementTrip;

                    //int defaultCount = 0;
                    while (iterator.hasNext()) {
                        actionMovementTrip = (ActionMovementTrip) (iterator.next());

                        TTObject start;
                        TTObject end;
                        if (actionMovementTrip.getTimetableObject().getParentObject() != null) {
                            start = actionMovementTrip.getTimetableObject().getParentObject();
                        } else {
                            start = actionMovementTrip.getTimetableObject();
                        }
                        if (actionMovementTrip.getTimetableObject2().getParentObject() != null) {
                            end = actionMovementTrip.getTimetableObject2().getParentObject();
                        } else {
                            end = actionMovementTrip.getTimetableObject2();
                        }

                        if (movementTripGroup.getPlannedStartObj().getTTObjId() != start.getTTObjId()
                                || movementTripGroup.getPlannedStopObj().getTTObjId() != end.getTTObjId()) {
                            JsfUtil.addErrorMessage(uiText.get("Error_ExistedMovementTripNotMatch"));
                            return null;
                        }

                        if (actionMovementTrip.getPlannedSecs() > actionMovementTrip.getMinSecs()) {
                            JsfUtil.addErrorMessage(uiText.get("Error_invalidTimeSpan"));
                            return null;
                        }
                        // Calculate how many default actions there are
                        //if (actionMovementTrip.getActionType().getAtypeId() == this.defaultMovementTripActionType.getAtypeId())
                        //defaultCount++;
                    }

                    /*if (defaultCount == 0) {
                        JsfUtil.addErrorMessage(uiText.get("Error_noDefaultAction"));
                        return null;
                    } else if (defaultCount > 1) {
                        JsfUtil.addErrorMessage(uiText.get("Error_multipleDefaultActions"));
                        return null;
                    }*/
                }

                // Since TRipAction does not automatically save Trip-object in refTrip, it needs to be saved explicitely
                /*if (movementTripGroup != null)
                {
                    if (movementTripGroup.getNumberOfActions() == 0)
                        ejbMovementTripGroupFacade.remove(movementTripGroup);
                    else if (movementTripGroup.isCreating())
                        ejbMovementTripGroupFacade.create(movementTripGroup);
                    else
                        ejbMovementTripGroupFacade.edit(movementTripGroup);
                }*/
            }

            boolean creating = tripAction.isCreating();
            current.recalculateTripDuration();
            current.updateActionTimes();
            current.increaseVersion();
            getFacade().edit(current);
            updatePlannedServices(current);
            
            xmlMessageSender.sendTripTemplateMsg(current, Operation.MODIFY);
            if (creating) {
                JsfUtil.addSuccessMessage(uiText.get("TripActionCreated"));
            } else {
                JsfUtil.addSuccessMessage(uiText.get("TripActionUpdated"));
            }
            recreateModel();
            updateState();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
        }

        tripAction.setEditing(false);
        tripAction.setCreating(false);
        return null;
    }

    /*
    public String saveMovementTripAction(ActionTrainMoving actionTrainMoving, ActionMovementTrip actionMovementTrip) {
        try {
            if (isCurrentInUse()) {
                recreateModel();
                current = this.ejbFacade.find(current.getTripId());
                actionMovementTrip.setEditing(false);
                JsfUtil.addErrorMessage(uiText.get("Error_TripTemplateUsed"));
                return null;
            }

            if (actionMovementTrip.getPlannedSecs() > actionMovementTrip.getMinSecs()) {
                JsfUtil.addErrorMessage(uiText.get("Error_invalidTimeSpan"));
                return null;
            }

            MovementTripGroup movementTripGroup = (MovementTripGroup) actionTrainMoving.getRefTrip();
            if (movementTripGroup != null) {
                Iterator<TripAction> iterator = movementTripGroup.getTripActions().iterator();
                ActionMovementTrip actionMovementTrip1;
                while (iterator.hasNext()) {
                    actionMovementTrip1 = (ActionMovementTrip) (iterator.next());
                    // There can be start/stop objects, which dont have a parent in actionmovementtrip.
                    // These are Virtual turning berths, in middle of station. Accept all those, if movementTripGroups object is null also.
                    // TODO: In such case movementtripgroup start/stop object should point to that virtual turning berth instead of null
                    if (!Objects.equals(movementTripGroup.getPlannedStartObj(), actionMovementTrip1.getTimetableObject().getParentObject()) ||
                            !Objects.equals(movementTripGroup.getPlannedStopObj(), actionMovementTrip1.getTimetableObject2().getParentObject())) {
                        JsfUtil.addErrorMessage(uiText.get("Error_ExistedMovementTripNotMatch"));
                        return null;
                    }
                }
            }

            // Make sure that old default is moved to alternative, if new default is selected
            if (actionMovementTrip.getActionType().getAtypeId() == this.defaultMovementTripActionType.getAtypeId()) {
                for (TripAction ta: actionTrainMoving.getRefTrip().getTripActions()) {
                    ta.setActionType(alternativeMovementTripActionType);
                }
                actionMovementTrip.setActionType(defaultMovementTripActionType); // Since for-loop replaced all, put back this item
            }
            
            Iterator<TripAction> iterator = actionTrainMoving.getRefTrip().getTripActions().iterator();
            if (actionMovementTrip.getActionType().getAtypeId() == this.defaultMovementTripActionType.getAtypeId()) {
                ActionMovementTrip currentAction;
                while (iterator.hasNext()) {
                    currentAction = (ActionMovementTrip) iterator.next();
                    if (!currentAction.isEditing() && currentAction.getActionType().getAtypeId() == this.defaultMovementTripActionType.getAtypeId()) {
                        JsfUtil.addErrorMessage(uiText.get("Error_multipleDefaultActions"));
                    }
                }
            } else {
                boolean defaultExist = isDefaultActionMovementTripExist((MovementTripGroup)actionTrainMoving.getRefTrip());
                if (!defaultExist) {
                    JsfUtil.addErrorMessage(uiText.get("Error_noDefaultAction"));
                }
            }
            getFacade().edit(current);
            actionMovementTrip.setEditing(false);
            actionMovementTrip.setCreating(false);
            if (actionMovementTrip.isCreating()) {
                JsfUtil.addSuccessMessage(uiText.get("MovementTripActionCreated"));
            } else {
                JsfUtil.addSuccessMessage(uiText.get("MovementTripActionUpdated"));
            }
            recreateModel();
            updateState();
            return null;
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
            return null;
        }
    }
     */
 /*    //private void validateTripAction(tripAction)
    private int getTripDuration() {
        Iterator<TripAction> iterator = current.getTripActions().iterator();
        int duration = 0;
        while (iterator.hasNext()) {
            TripAction tripAction = iterator.next();
            duration += tripAction.getPlannedSecs();
        }
        return duration;
    }
     */
    static public TripAction createNewTripAction(BasicTrip parent, MainActionTypeEnum eMainActionType, ActionType actionType, TTObject loc1, TTObject loc2, Integer minSecs, Integer planSecs) {
        TripAction tripAction = null;

//        switch (TripAction.MainActionTypeEnum.parse(mainType.getMactionTypeId())) {
        switch (eMainActionType) {
            case PASSENGER_STOP:
                tripAction = new ActionPassengerStop();
                break;
            case TRAIN_MOVING:
                tripAction = new ActionTrainMoving();
                ((ActionTrainMoving) tripAction).setTimetableObject2(loc2);
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
            tripAction.setActionType(actionType);
            tripAction.setTimesValid(false);
            tripAction.setPlannedSecs(planSecs);
            tripAction.setMinSecs(minSecs);
            tripAction.setConsumed(false);
            tripAction.setCreating(true);
            tripAction.setEditing(true);
            tripAction.setSeqNo(parent.getNumberOfActions() + 1);
            tripAction.setActionId(0);

            tripAction.setTimetableObject(loc1);

            parent.addTripAction(tripAction, 0);
            parent.increaseVersion();
            parent.setDurationSecs(parent.getDurationSecs() + tripAction.getPlannedSecs());
        }
        return tripAction;
    }

    // Called from XHTML
    public void addNewTripAction() {

        TTObject loc1 = null;
        TTObject loc2 = null;

        // Figure out locations
        if (current.getNumberOfActions() > 0) {
            TripAction prevAction = current.getTripAction(current.getNumberOfActions());
            if (prevAction != null) {
                if (prevAction instanceof ActionTrainMoving) {
                    loc1 = ((ActionTrainMoving) prevAction).getTimetableObject2();
                    this.selectedActionTTObjectType1 = ((ActionTrainMoving) prevAction).getTimetableObject2().getTTObjectType();
                } else {
                    loc1 = prevAction.getTimetableObject();
                    this.selectedActionTTObjectType1 = prevAction.getTimetableObject().getTTObjectType();
                }
            }
        } else if (this.getPossibleActionLocations1().isEmpty() == false) {
            loc1 = this.getPossibleActionLocations1().get(0);
        }

        switch (selectedMainActionType.getMainActionTypeEnum()) {
//        switch (TripAction.MainActionTypeEnum.parse(this.selectedMainActionType.getMactionTypeId())) {
            case TRAIN_MOVING:
                if (!this.getPossibleActionLocations2().isEmpty()) {
                    loc2 = getPossibleActionLocations2().get(0);
                }
                break;
        }

        TripAction tripAction = createNewTripAction(current, selectedMainActionType.getMainActionTypeEnum(), selectedActionType, loc1, loc2,
                selectedActionType.getDefaultMinSecs(), selectedActionType.getDefaultPlanSecs());

        possibleLocationsChanged(tripAction);
        /*TripAction tripAction = null;

        switch (TripAction.MainActionTypeEnum.parse(this.selectedMainActionType.getMactionTypeId())) {
            case PASSENGER_STOP:
                tripAction = new ActionPassengerStop();
                break;
            case TRAIN_MOVING:
                tripAction = new ActionTrainMoving();

                if (tripAction.hasSecondObject() && !this.getPossibleActionLocations2().isEmpty()) {
                    ((ActionTrainMoving) tripAction).setTimetableObject2(this.getPossibleActionLocations2().get(0));
                }

                //addNewMovementTripAction((ActionTrainMoving) tripAction);
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
        if (this.selectedActionType != null) {
            tripAction.setActionType(selectedActionType);
            tripAction.setTimesValid(false);
            tripAction.setPlannedSecs(this.selectedActionType.getDefaultPlanSecs());
            if (tripAction instanceof ActionTrainMoving == false) {
                tripAction.setMinSecs(this.selectedActionType.getDefaultMinSecs());
            }
            tripAction.setConsumed(false);
            tripAction.setCreating(true);
            tripAction.setEditing(true);
            tripAction.setSeqNo(current.getNumberOfActions() + 1);
            tripAction.setActionId(0);
            if (current.getNumberOfActions() > 0) {
                TripAction prevAction = current.getTripAction(current.getNumberOfActions());
                if (prevAction != null) {
                    if (prevAction instanceof ActionTrainMoving) {
                        tripAction.setTimetableObject(((ActionTrainMoving) prevAction).getTimetableObject2());
                        this.selectedActionTTObjectType1 = ((ActionTrainMoving) prevAction).getTimetableObject2().getTTObjectType();
                    } else {
                        tripAction.setTimetableObject(prevAction.getTimetableObject());
                        this.selectedActionTTObjectType1 = prevAction.getTimetableObject().getTTObjectType();
                    }
                }
            } else if (this.getPossibleActionLocations1().isEmpty() == false) {
                tripAction.setTimetableObject(this.getPossibleActionLocations1().get(0));
            }

            current.addTripAction(tripAction, 0);
            current.setDurationSecs(current.getDurationSecs() + tripAction.getPlannedSecs());
            possibleLocationsChanged(tripAction);
        }*/

    }

    public void addNewMovementTripAction(ActionTrainMoving actionTrainMoving) {
        MovementTripGroup movementTripGroup;
        if (actionTrainMoving.getRefTrip() == null) {
            movementTripGroup = new MovementTripGroup();
            movementTripGroup.setConsumed(false);
            movementTripGroup.setTimesAreValid(false);
            movementTripGroup.setUtcTimes(false);
            movementTripGroup.setAreaObj(current.getAreaObj());
            movementTripGroup.setValid(Boolean.TRUE);
            movementTripGroup.setTripType(movementTripGroupTripType);
            movementTripGroup.setCreating(true);
            movementTripGroup.setEditing(true);

            TTObject start;
            TTObject end;
            if (actionTrainMoving.getTimetableObject().getParentObject() != null) {
                start = actionTrainMoving.getTimetableObject().getParentObject();
            } else {
                start = actionTrainMoving.getTimetableObject();
            }
            if (actionTrainMoving.getTimetableObject2().getParentObject() != null) {
                end = actionTrainMoving.getTimetableObject2().getParentObject();
            } else {
                end = actionTrainMoving.getTimetableObject2();
            }

            movementTripGroup.setPlannedStartObj(start);
            movementTripGroup.setPlannedStopObj(end);
            movementTripGroup.setDescription(actionTrainMoving.getTimetableObject().getDescription() + "->" + actionTrainMoving.getTimetableObject2().getDescription());
            //duration and origo are meaningless for movementTripGroup;
            movementTripGroup.setDurationSecs(0);
            movementTripGroup.setOrigoSecs(0);

        } else {
            movementTripGroup = (MovementTripGroup) actionTrainMoving.getRefTrip();

        }

        ActionMovementTrip actionMovementTrip = new ActionMovementTrip();
        //if (selectedMovementTripActionType != null) {
        //    actionMovementTrip.setActionType(selectedMovementTripActionType);
        //    selectedMovementTripActionType.getTTActionCollection().add(actionMovementTrip);
        if (isDefaultActionMovementTripExist(movementTripGroup)) {
            actionMovementTrip.setActionType(this.alternativeMovementTripActionType);
        } else {
            actionMovementTrip.setActionType(this.defaultMovementTripActionType);
        }

        actionMovementTrip.setTimesValid(false);
        actionMovementTrip.setConsumed(false);
        actionMovementTrip.setCreating(true);
        actionMovementTrip.setEditing(true);
        actionMovementTrip.setSeqNo(movementTripGroup.getNumberOfActions() + 1);
        actionMovementTrip.setActionId(0);
        actionMovementTrip.setTrip(movementTripGroup);
        actionMovementTrip.setPlannedSecs(0);
        actionMovementTrip.setMinSecs(0);
        actionMovementTrip.setTimetableObject(selectedMovementTrip.getPlannedStartObj());
        actionMovementTrip.setTimetableObject2(selectedMovementTrip.getPlannedStopObj());
        actionMovementTrip.setRefTrip(selectedMovementTrip);
        actionMovementTrip.setCreating(true);
        actionMovementTrip.setEditing(true);

        movementTripGroup.addTripAction(actionMovementTrip, 0);
        //}
        actionTrainMoving.setRefTrip(movementTripGroup);
    }

    private boolean isDefaultActionMovementTripExist(MovementTripGroup movementTripGroup) {
        for (TripAction actionMovementTrip : movementTripGroup.getTripActions()) {
            if (((ActionMovementTrip) actionMovementTrip).getActionType().getActionSubtype() == ActionMovementTrip.ActionMovementTripTypeEnum.DEFAULT.getValue()) {
                return true;
            }
        }
        return false;
    }

    public boolean isAddTripTemplateAllowed() {
        List<BasicTrip> array = (List<BasicTrip>) getItems().getWrappedData();

        if (array == null || array.isEmpty()) {
            return true;
        } else {
            BasicTrip e = (BasicTrip) array.get(0);
            if (e != null && e.isCreating()) {
                return false;
            } else {
                Iterator<BasicTrip> iterator = array.iterator();
                while (iterator.hasNext()) {
                    BasicTrip item = (BasicTrip) iterator.next();
                    if (item.isEditing()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean isAddMovementTripActionAllowed(ActionTrainMoving tripAction) {
        boolean allowed = this.selectedMovementTrip != null;
        /*
        if (allowed && tripAction.getRefTrip() != null) {
            Iterator<TripAction> iterator = tripAction.getRefTrip().getTripActions().iterator();
            while (iterator.hasNext()) {
                allowed = !((ActionMovementTrip) (iterator.next())).isCreating();
            }
        }
         */
        return allowed;
    }

    static public BasicTrip createNewTripTemplate(TTArea area, TripType tripType) {
        BasicTrip template = new BasicTrip();
        template.setEditing(true);
        template.setCreating(true);
        template.setConsumed(false);
        template.setUtcTimes(false);
        template.setTimesAreValid(false);
        template.setDurationSecs(0);
        template.setOrigoSecs(0);
        template.setValid(true);
        // Parameterized values
        template.setTripType(tripType);
        template.setAreaObj(area);
        template.setVersion(1);

        return template;
    }

    // Called from XHTML
    public String addNew() {
        current = createNewTripTemplate(selectedArea, selectedTripType);

        List<BasicTrip> oldArray = (List<BasicTrip>) getItems().getWrappedData();
        oldArray.add(0, current);
        getItems().setWrappedData(oldArray);

        selectedItemIndex = -1;
        return null;
    }

    public String destroy() {
        current = (BasicTrip) getItems().getRowData();
        //current = this.ejbFacade.find(current.getTripId());
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        if (!current.isCreating()) {
            //if (isCurrentInUse(false) == false) {
            if (usedBy(current) == 0) {
                performDestroy();
            } else {
                JsfUtil.addErrorMessage(uiText.get("Error_TripTemplateUsed"));
            }
        }
        current = null;
        recreateModel();
        return "List";
    }

    public String destroyTripAction(TripAction tripAction) {
        //current = (BasicTrip) getItems().getRowData();
        //selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        try {
            if (!tripAction.isCreating()) {
                //current = this.ejbFacade.find(current.getTripId());
                //if (isCurrentInUse(true) == false) {
                //F5 will make the persistence lost
                //current = this.ejbFacade.find(current.getTripId());
                //TripAction tripActionInDB = this.ejbTripActionFacade.find(tripAction.getActionId());
                //updateSeqNo(false, tripActionInDB, tripAction.getSeqNo());
                current.removeTripAction(tripAction);
                current.recalculateTripDuration();
                current.setTimesAreValid(false);
                current.updateActionTimes();
                current.increaseVersion();
                getFacade().edit(current);

                // Clean ref link to movementtrip, if it exists
                //removeMovementTripActionReference(tripAction);
                // And delete trip action and its data
                ejbTripActionFacade.remove(tripAction);
                current.updateActionTimes();
                xmlMessageSender.sendTripTemplateMsg(current, Operation.MODIFY);
                JsfUtil.addSuccessMessage(uiText.get("TripActionDeleted"));
                //} else {
                //    JsfUtil.addErrorMessage(uiText.get("Error_TripTemplateUsed"));
                //}
            }/* else {
             current.removeTripAction(tripAction);
             }*/

        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
        }

        //current = this.ejbFacade.find(current.getTripId());
        updateState();
        recreateModel();
        return null;
    }

    public String destroyMovementTripAction(ActionTrainMoving actionTrainMoving, ActionMovementTrip actionMovementTrip) {

        MovementTripGroup movementTripGroup = (MovementTripGroup) actionTrainMoving.getRefTrip();
        movementTripGroup.removeTripAction(actionMovementTrip);

        if (movementTripGroup.getNumberOfActions() == 0) {
            // Last item in train moving group, remove it
            actionTrainMoving.setRefTrip(null);
        }
        /*else {
            // Test if action which is to be removed, is default. In that case
            // set new default
            if (actionMovementTrip.getActionType() == defaultMovementTripActionType)
                movementTripGroup.getTripAction(1).setActionType(defaultMovementTripActionType);
        }*/

 /*
        if (!actionMovementTrip.isCreating()) {
            try {
                //current = this.ejbFacade.find(current.getTripId());
                //F5 will make the persistence lost
                //current = this.ejbFacade.find(current.getTripId());
                //TripAction tripActionInDB = this.ejbTripActionFacade.find(tripAction.getActionId());
                //updateSeqNo(false, tripActionInDB, tripAction.getSeqNo());
                if (isCurrentInUse() == false) {
                    MovementTripGroup movementTripGroup = (MovementTripGroup)actionTrainMoving.getRefTrip();

                    if (movementTripGroup.getTripActions().size() == 1) {
                        actionTrainMoving.setRefTrip(null);
                        ejbTripActionFacade.edit(actionTrainMoving);
                        
                        // Prevent deletion of movements
                        Iterator<TripAction> moveIt = movementTripGroup.getActionsIterator();
                        while (moveIt.hasNext()) {
                            TripAction movTripAction = moveIt.next();
                            movTripAction.setRefTrip(null);
                        }
                        
                        ejbMovementTripGroupFacade.remove(movementTripGroup);
                    }
                    else {
                        movementTripGroup.removeTripAction(actionMovementTrip);

                        // Try to keep default action movement trip, by setting new
                        if (!isDefaultActionMovementTripExist(movementTripGroup)) {
                            if ( movementTripGroup.getTripActions().size() > 0)
                                movementTripGroup.getTripActions().get(0).setActionType(defaultMovementTripActionType);
                        }
                        ejbMovementTripGroupFacade.edit(movementTripGroup);
                    }                    
                    JsfUtil.addSuccessMessage(uiText.get("MovementTripActionDeleted"));
                } else {
                    JsfUtil.addErrorMessage(uiText.get("Error_TripTemplateUsed"));
                }
            } catch (Exception e) {
                JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
            }
            updateState();
            recreateModel();
        } 
        else {
            // This is when user deletes ActionMovementTrip while TrainMovementAction is being created
            // All what is needed at this point, is to remove items reference, as it is not saved yet
            actionTrainMoving.setRefTrip(null);
        }
         */
        return null;
    }

    public Date getDate(TimedTrip timedTrip) {
        if (timedTrip instanceof ScheduledTrip) {

            Date date = ((ScheduledTrip) timedTrip).getDay().getDateOfDay();
            //SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            //return sdf.format(date);
            return date;
        }
        return null;
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "List";
        }
    }

    /*private void removeMovementTripActionReference(TripAction action) {
        if (action instanceof ActionTrainMoving) {
            Trip refTrip = ((ActionTrainMoving)action).getRefTrip();
            if (refTrip instanceof MovementTripGroup) {
                Iterator<TripAction> moveIt = refTrip.getActionsIterator();
                while (moveIt.hasNext()) {
                    TripAction movTripAction = moveIt.next();
                    ((ActionMovementTrip)movTripAction).setRefTrip(null);
                }
            }
        }        
    }*/
    private void performDestroy() {
        try {
            // Deleteing trip template tries to delete all objects attached to given object, but
            // there might be movement trips attached to it, which are not supposed to be deleted.
            // Remove those links before trying to delete.
            /*Iterator<TripAction> it = current.getActionsIterator();
            while (it.hasNext()) {
                TripAction action = it.next();
                removeMovementTripActionReference(action);
            }*/

            getFacade().remove(current);
            xmlMessageSender.sendTripTemplateMsg(current, Operation.DELETE);
            JsfUtil.addSuccessMessage(uiText.get("TripDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getFacade().count();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = getFacade().findRange(new int[]{selectedItemIndex, selectedItemIndex + 1}).get(0);
        }
    }

    public DataModel getItems() {
        if (selectedArea == null && areas.size() > 0) {
            selectedArea = areas.get(0);
        }

        if (items == null) {
            items = getPagination().createPageDataModel();
        }

        return items;
    }

    public void filterChanged() {
        recreateModel();
        this.pagination = null;
    }

    private void recreateModel() {
        items = null;
    }

    public String next() {
        getPagination().nextPage();
        recreateModel();
        return "List";
    }

    public String previous() {
        getPagination().previousPage();
        recreateModel();
        return "List";
    }

    public Map<String, Integer> getDays() {
        String day = uiText.get("day");
        days.clear();
        for (int i = 0; i < 8; i++) {
            days.put("+" + i + " " + day, i);
        }
        return days;
    }

    public StreamedContent getChart() {
        try {
            JFreeChart jfreechart = ChartFactory.createPieChart(uiText.get("TripTemplate_Statistic"), createDataset(), true, true, false);
            languageBean.applyChartTheme(jfreechart);
            File chartFile = new File("dynamichart");
            ChartUtilities.saveChartAsPNG(chartFile, jfreechart, 450, 300);
            chart = new DefaultStreamedContent(new FileInputStream(chartFile), "image/png");
        } catch (Exception e) {
            //logger.severe(e.getMessage());
        }
        return chart;
    }

    private PieDataset createDataset() {

        List<TripAction> actions = current.getTripActions();
        Iterator<TripAction> iteActions = actions.iterator();

        int movingSecs = 0;
        int nonmovingSecs = 0;

        while (iteActions.hasNext()) {
            TripAction cur = iteActions.next();

            if (cur.getClass() == ActionTrainMoving.class) {
                movingSecs += cur.getPlannedSecs();
            } else {
                nonmovingSecs += cur.getPlannedSecs();
            }

        }
        DefaultPieDataset dataset = new DefaultPieDataset();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        //String str = String.format("Moving (%d)", movingSecs);
        String str = simpleDateFormat.format(new Date(movingSecs * 1000L));
        int day = movingSecs / 3600 / 24;

        if (day > 0) {
            str = String.format(uiText.get("PieChart_Moving") + "(+%d " + uiText.get("day") + " %s)", day, str);
        } else {
            str = String.format(uiText.get("PieChart_Moving") + "(%s)", str);
        }
        dataset.setValue(str, new Double(movingSecs));

        day = nonmovingSecs / 3600 / 24;
        str = simpleDateFormat.format(new Date(nonmovingSecs * 1000L));

        if (day > 0) {
            str = String.format(uiText.get("PieChart_Dwelling") + "(+%d " + uiText.get("day") + " %s)", day, str);
        } else {
            str = String.format(uiText.get("PieChart_Dwelling") + "(%s)", str);
        }
        dataset.setValue(str, new Double(nonmovingSecs));
        return dataset;
    }

    public String activateEdit() {
        //current = trip;
        current = (BasicTrip) getItems().getRowData();
        current.setEditing(true);
        this.selectedFromTTObjectType = current.getPlannedStartObj().getTTObjectType();
        this.selectedToTTobjectType = current.getPlannedStopObj().getTTObjectType();
        return null;
    }

    public String activateEditTripAction(TripAction tripAction) {
        //refresh page will lose 
        tripAction.setEditing(true);
        selectedActionTTObjectType1 = tripAction.getTimetableObject().getTTObjectType();
        if (tripAction instanceof ActionTrainMoving) {
            selectedActionTTObjectType2 = ((ActionTrainMoving) tripAction).getTimetableObject2().getTTObjectType();
            List<MovementTrip> list = this.getMovementTrips(tripAction);
            if (list != null && !list.isEmpty()) {
                this.selectedMovementTrip = list.get(0);
            }
        }
        return null;
    }
}
