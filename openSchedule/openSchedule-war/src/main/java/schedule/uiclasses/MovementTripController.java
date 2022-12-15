/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Jia Li
 */
package schedule.uiclasses;

import schedule.entities.ActionTrainMoving;
import schedule.entities.TTObject;
import schedule.entities.TripAction;
import schedule.uiclasses.util.JsfUtil;
import schedule.uiclasses.util.PaginationHelper;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.inject.Inject;
import javax.inject.Named;
import schedule.entities.ActionCmdTrip;
import schedule.entities.ActionMovementTrip;
import schedule.entities.ActionRouteTrip;
import schedule.entities.ActionShuntingRouteTrip;
import schedule.entities.ActionTrainFormation;
import schedule.entities.ActionTrainInternal;
import schedule.entities.ActionType;
import schedule.entities.MainActionType;
import schedule.entities.MovementTrip;
import schedule.entities.MovementTrip.MovementTripSubType;
import schedule.entities.MovementTripGroup;
import schedule.entities.TTArea;
import schedule.entities.TTObjectType;
import schedule.entities.TTRoute;
import schedule.entities.TTSingleCmd;
import schedule.entities.TTTrainCmd;
import schedule.entities.TripType;
import schedule.entities.TripType.TripMainType;
import schedule.messages.Operation;
import schedule.messages.XmlMessageSender;
import schedule.sessions.ActionMovementTripFacade;
import schedule.sessions.ActionTrainFormationFacade;
import schedule.sessions.MainActionTypeFacade;
import schedule.sessions.MovementTripFacade;
import schedule.sessions.TTAreaFacade;
import schedule.sessions.TTObjectTypeFacade;
import schedule.sessions.TTRouteFacade;
import schedule.sessions.TTSingleCmdFacade;
import schedule.sessions.TTTrainCmdFacade;
import schedule.sessions.TripTypeFacade;
import schedule.uiclasses.util.UiText;

@Named("movementTripController")
@SessionScoped
public class MovementTripController implements Serializable {

    private MovementTrip current;
    private DataModel items = null;
    //private DataModel areaItems = null;
    //private List<TTArea> areaFilterList = null; //"All" is included compared with areas
    private TTArea areaAll;
    //selected Area Filter
    private TTArea selectedArea;

    private List<TTArea> areas = null;
    @Inject
    private MovementTripFacade ejbFacade;
    @Inject
    private XmlMessageSender xmlMessageSender;
    @Inject
    private MainActionTypeFacade ejbActionMainTypeFacade;
    @Inject
    private ActionMovementTripFacade ejbActionMovementTripFacade;
    @Inject
    private ActionTrainFormationFacade ejbActionTrainFormationFacade;
    @Inject
    private TTAreaFacade ejbTTAreaFacade;
    @Inject
    private TTRouteFacade ejbTTRouteFacade;
    @Inject
    private TTSingleCmdFacade ejbTTSingleCmdFacade;
    @Inject
    private TTTrainCmdFacade ejbTTTrainCmdFacade;

    @Inject
    private TripTypeFacade ejbTripTypeFacade;
    @Inject
    private TTObjectTypeFacade ejbTTObjectTypeFacade;
    @Inject
    private UiText uiText;

    private PaginationHelper pagination;
    private int selectedItemIndex;
    private final int itemsPerPage = 50;

    private List<TripType> tripMainTypes;
    private TripType selectedTripMainType;
    private TripType selectedTripType;
    private TTObjectType selectedFromTTObjectType;
    private TTObjectType selectedToTTobjectType;

    private List<TTObjectType> ttobjectTypes;
    List<MainActionType> mainActionTypes;
    private MainActionType selectedMainActionType;
    private ActionType selectedActionType;
    private TTObjectType selectedActionTTObjectType1;

    private TripType tripTypeAllFilter;
    private TripType selectedTripTypeFilter;
    private List<TripType> tripTypesFilter = null;

    String clonedDescription;

    //private TTObjectType selectedActionTTObjectType3;
    //private Integer defaultOrigoSecs = 0;
    private static Map<String, Integer> signs = new LinkedHashMap<>();

    public MovementTripController() {
    }

    @PostConstruct
    public void init() {
        /*if (areaFilterList == null) {

         areaFilterList = ejbTTAreaFacade.findAll();
         if (areaFilterList.size() != 1) {

         areaFilterList.add(0, new TTArea(0, uiText.get("FilterAll")));
         }
         }*/
        tripTypeAllFilter = new TripType(0);
        if (tripTypesFilter == null) {
            TripTypeFilter filter = new TripTypeFilter();
            filter.setOnlySubTripTypeFilter(true);
            filter.setTripType(TripMainType.MOVEMENT_TRIP);
            filter.setValid(true);
            tripTypesFilter = ejbTripTypeFacade.findAll(filter);
        }

        areaAll = new TTArea(0, uiText.get("FilterAll"));

        if (areas == null) {
            areas = ejbTTAreaFacade.findAll();
        }
        if (selectedArea == null && areas.size() > 0) {
            selectedArea = areas.get(0);
        }

        tripMainTypes = new ArrayList<>();
        TripTypeFilter tripTypeFilter = new TripTypeFilter();
        tripTypeFilter.setValid(true);
        tripTypeFilter.setOnlyMainTripTypeFilter(true);
        tripTypeFilter.setTripType(TripMainType.MOVEMENT_TRIP);
        List<TripType> temp = this.ejbTripTypeFacade.findAll(tripTypeFilter);
        if (temp != null && (temp.isEmpty() == false) && (temp.get(0).isValid())) {
            tripMainTypes.add(temp.get(0));
        }

        /*temp = this.ejbTripTypeFacade.findAll(TripMainType.COMMERCIAL_TEMPLATE);
         if (temp != null && temp.isEmpty() == false) {
         tripMainTypes.add(temp.get(0));
         }
         temp = this.ejbTripTypeFacade.findAll(TripMainType.OPPORTUNITY_TEMPLATE);
         if (temp != null && temp.isEmpty() == false) {
         tripMainTypes.add(temp.get(0));
         }*/
        if (this.tripMainTypes.isEmpty() == false) {
            this.selectedTripMainType = tripMainTypes.get(0);
            tripTypeFilter.setTripType(this.selectedTripMainType.getTripType());
            tripTypeFilter.setOnlySubTripTypeFilter(true);
            temp = ejbTripTypeFacade.findAll(tripTypeFilter);
            if (temp != null && temp.size() > 0 && temp.get(0).isValid()) {
                this.selectedTripType = temp.get(0);
            } else {
                this.selectedTripType = this.selectedTripMainType;
            }
        }

        this.selectedFromTTObjectType = this.ejbTTObjectTypeFacade.findAll().get(0);
        this.selectedToTTobjectType = selectedFromTTObjectType;

        mainActionTypes = new ArrayList<>();

        MainActionType actionMainType = ejbActionMainTypeFacade.find(TripAction.MainActionTypeEnum.ROUTE_ACTION.getValue());
        if (actionMainType != null && actionMainType.isUsed()) {
            mainActionTypes.add(actionMainType);
            selectedMainActionType = actionMainType;
            selectedActionType = selectedMainActionType.getActionTypes().get(0);
            selectedActionTTObjectType1 = selectedActionType.getTTObjTypes().get(0);
            //selectedActionTTObjectType3 = selectedActionType.getTTObjTypes().get(0);
            /*List<ActionType> takePassengers = this.ejbActionTypeFacade.findAll(actionMainType, ActionPassengerStopTypeEnum.TAKE_PASSENGER.getValue());
             if (takePassengers.size() > 0) {
             defaultOrigoSecs = takePassengers.get(0).getDefaultPlanSecs();
             }*/

        }

        actionMainType = ejbActionMainTypeFacade.find(TripAction.MainActionTypeEnum.SINGLE_CMD_ACTION.getValue());
        if (actionMainType != null && actionMainType.isUsed()) {
            mainActionTypes.add(actionMainType);
        }
        actionMainType = ejbActionMainTypeFacade.find(TripAction.MainActionTypeEnum.SHUNTING_ROUTE_ACTION.getValue());
        if (actionMainType != null && actionMainType.isUsed()) {
            mainActionTypes.add(actionMainType);
        }
        actionMainType = ejbActionMainTypeFacade.find(TripAction.MainActionTypeEnum.TRAIN_ACTION.getValue());
        if (actionMainType != null && actionMainType.isUsed()) {
            mainActionTypes.add(actionMainType);
        }

        signs.put("-", 0);
        signs.put("+", 1);

        TTObjectTypeFilter filter = new TTObjectTypeFilter();
        filter.setStartTTObjectTypeFilter();
        this.ttobjectTypes = this.ejbTTObjectTypeFacade.findAll(filter);
    }

    public TTArea getAreaAll() {
        areaAll.setDescription(uiText.get("FilterAll"));
        return this.areaAll;
    }

    public Map<String, Integer> getSigns() {
        return MovementTripController.signs;
    }

    public TripType getSelectedTripTypeFilter() {
        return selectedTripTypeFilter;
    }

    public void setSelectedTripTypeFilter(TripType selectedTripTypeFilter) {
        this.selectedTripTypeFilter = selectedTripTypeFilter;
    }

    public TripType getTripTypeAllFilter() {
        tripTypeAllFilter.setDescription(uiText.get("FilterAll"));
        return tripTypeAllFilter;
    }

    public List<TripType> getTripTypesFilter() {
        return tripTypesFilter;
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

    public List<Integer> getPossibleSeqNos(MovementTrip movementTrip, TripAction tripAction) {
        List<Integer> possibleSeqNos = null;
        if (movementTrip != null) {
            possibleSeqNos = new ArrayList<>();
            Integer maxSeqNo;
            if (movementTrip.getNumberOfActions() > 0) {
                //movementTrip.getTripActions() gives sorted list.
                maxSeqNo = movementTrip.getTripActions().get(movementTrip.getNumberOfActions() - 1).getSeqNo();
                if (maxSeqNo == null) //only one new trip action
                {
                    maxSeqNo = 1;
                } else if (tripAction.isCreating()) {
                    maxSeqNo++;
                } else if (tripAction.isEditing() && maxSeqNo < movementTrip.getNumberOfActions()) {
                    maxSeqNo++;
                }

                for (Integer i = 1; i <= maxSeqNo; i++) {
                    possibleSeqNos.add(i);
                }
            }
        }
        return possibleSeqNos;
    }

    public List<TTObjectType> getPossibleFromTTObjectTypes() {
        return ttobjectTypes;
    }

    public List<TTObjectType> getPossibleToTTObjectTypes() {
        return ttobjectTypes;
    }

    public List<TTObject> getPossibleRouteObjects(TripAction tripAction) {
        if (tripAction != null) {
            ArrayList<TTObject> list = new ArrayList<>();
            if (tripAction instanceof ActionRouteTrip) {
                List<TTRoute> locations = this.ejbTTRouteFacade.findAll();
                Iterator<TTRoute> iterator = locations.iterator();
                while (iterator.hasNext()) {
                    list.add(iterator.next());
                }
            } else if (tripAction instanceof ActionCmdTrip) {
                List<TTSingleCmd> locations = this.ejbTTSingleCmdFacade.findAll();
                Iterator<TTSingleCmd> iterator = locations.iterator();
                while (iterator.hasNext()) {
                    list.add(iterator.next());
                }
            } else if (tripAction instanceof ActionTrainInternal) {
                List<TTTrainCmd> locations = this.ejbTTTrainCmdFacade.findAll();
                Iterator<TTTrainCmd> iterator = locations.iterator();
                while (iterator.hasNext()) {
                    list.add(iterator.next());
                }
            }
            
            Collections.sort(list, new Comparator<TTObject>() {
                @Override
                public int compare(TTObject ttObj1, TTObject ttObj2)
                {

                    return  ttObj1.getDescription().compareTo(ttObj2.getDescription());
                }
            });
            return list;
        }
        return null;
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

    public TTObjectType getSelectedActionTTObjectType1() {
        return selectedActionTTObjectType1;
    }

    public void setSelectedActionTTObjectType1(TTObjectType selectedActionTTObjectType1) {
        this.selectedActionTTObjectType1 = selectedActionTTObjectType1;
    }

    public List<TTObjectType> getPossibleActionTTObjectTypes() {
        return this.selectedActionType.getTTObjTypes();

    }

    public List<TTObject> getPossibleActionLocations1() {
        List<TTObject> list = this.selectedActionTTObjectType1.getTTObjects();
        Iterator<TTObject> iterator = list.iterator();
        TTObject ttObject;
        while (iterator.hasNext()) {
            ttObject = iterator.next();
            if (ttObject.getScheduleName() == null || ttObject.getScheduleName().isEmpty()) {
                iterator.remove();
            }
        }
        return list;
    }

    public void selectedMainActionTypeChanged() {
        this.selectedActionType = this.selectedMainActionType.getActionTypes().get(0);
        selectedActionTypeChanged();
        /*if (selectedActionType.getTTObjTypes() != null) {
         selectedActionTTObjectType1 = selectedActionType.getTTObjTypes().get(0);
         }*/
    }

    public void selectedActionTypeChanged() {
        selectedActionTTObjectType1 = selectedActionType.getTTObjTypes().get(0);
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
        if (getSelected() != null) {
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

    private void updateState() {
        this.ejbFacade.evict(current);
        current = this.ejbFacade.find(current.getTripId());
    }

    public List<ActionMovementTrip> getActionMovementTrips(MovementTrip movement) {
        ActionMovementTripFilter movementTripFilter = new ActionMovementTripFilter();
        movementTripFilter.setRefTripFilter(movement);

        return ejbActionMovementTripFacade.findAll(movementTripFilter);

        /*List<ActionMovementTrip> tripActions = ejbActionMovementTripFacade.findAll(movementTripFilter);
        if (tripActions.isEmpty() == false) {
            List<ActionMovementTrip> actionMovementTrips = new ArrayList<>();
            Iterator<ActionMovementTrip> iterator = tripActions.iterator();
            ActionMovementTrip tripAction;
            while (iterator.hasNext()) {
                tripAction = iterator.next();
                if (actionMovementTrips.contains(tripAction) == false) {
                    actionMovementTrips.add(tripAction);
                }
            }
            return actionMovementTrips;
        }
        return null;*/
    }

    public List<ActionTrainFormation> getActionTrainFormation(MovementTrip movement) {
        ActionTrainFormationFilter filter = new ActionTrainFormationFilter();
        filter.setRefTripFilter(movement);

        return ejbActionTrainFormationFacade.findAll(filter);
    }

    public List<TripType> getPossibleTripMainTypes() {

        return this.tripMainTypes;
    }

    public List<TripType> getPossibleTripTypes() {
        TripTypeFilter tripTypeFilter = new TripTypeFilter();
        tripTypeFilter.setValid(true);
        tripTypeFilter.setOnlySubTripTypeFilter(true);
        tripTypeFilter.setTripType(this.selectedTripMainType.getTripType());
        return ejbTripTypeFacade.findAll(tripTypeFilter);
    }

    public void selectedTripMainTypeChanged() {
        List<TripType> list = this.getPossibleTripTypes();
        this.selectedTripType = list.isEmpty() ? this.selectedTripMainType : list.get(0);
    }

    public MovementTrip getSelected() {
        if (current == null) {
            current = new MovementTrip();
            selectedItemIndex = -1;
        }
        return current;
    }

    private MovementTripFacade getFacade() {
        return ejbFacade;
    }

    public PaginationHelper getPagination() {

        if (pagination == null) {
            pagination = new PaginationHelper(this.itemsPerPage) {

                @Override
                public int getItemsCount() {
                    MovementTripFilter filter = new MovementTripFilter();
                    if (selectedArea.getTTObjId() != 0) {
                        filter.setAreaFilter((TTArea) selectedArea);
                    }

                    if (selectedTripTypeFilter != null && selectedTripTypeFilter.getTripTypeId() != 0) {
                        filter.setTripTypeFilter(selectedTripTypeFilter);
                    }
                    return getFacade().count(filter);
                    //filter.setUseOrder(true);

                }

                @Override
                public DataModel createPageDataModel() {
                    MovementTripFilter filter = new MovementTripFilter();
                    if (selectedArea.getTTObjId() != 0) {
                        filter.setAreaFilter((TTArea) selectedArea);
                    }

                    if (selectedTripTypeFilter != null && selectedTripTypeFilter.getTripTypeId() != 0) {
                        filter.setTripTypeFilter(selectedTripTypeFilter);
                    }

                    return new ListDataModel<>(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}, filter));
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
        current = (MovementTrip) getItems().getRowData();
        updateState();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareGraph() {
        return "ViewGraph";
    }

    public String prepareCreate() {
        current = new MovementTrip();
        selectedItemIndex = -1;
        return "Create";
    }

    public String getClonedDescription() {
        return clonedDescription;
    }

    public void setClonedDescription(String clonedDescription) {
        this.clonedDescription = clonedDescription;
    }

    public String cloneMovementTrip() {

        current = (MovementTrip) getItems().getRowData();

        MovementTrip clonedTrip = new MovementTrip();

        if (clonedDescription != null && !clonedDescription.isEmpty()) {
            clonedTrip.setDescription(clonedDescription);
        } else {
            JsfUtil.addErrorMessage(uiText.get("RequiredMessage_description"));
            return null;
        }

        try {

            getFacade().cloneTrip(current, clonedTrip);
            updateState();
            JsfUtil.addSuccessMessage(uiText.get("MovementCreated"));
            recreateModel();
            xmlMessageSender.sendMovementTripMsg(current, Operation.CREATE);

            return "List";

        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
            return null;
        }

    }

    public String prepareEdit() {
        current = (MovementTrip) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            current.increaseVersion();
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(uiText.get("MovementUpdated"));
            xmlMessageSender.sendMovementTripMsg(current, Operation.MODIFY);
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
            return null;
        }
    }

    /*public boolean isCurrentInUse(boolean usedByMultiple) {
        
        //MovementTrip temp = this.ejbFacade.find(current.getTripId());
        //return temp.getTimedTrips().isEmpty() == false;
        int temp = usedByMultiple ? 1 : 0;
        if (MovementTripSubType.PLANNED.getValue() == current.getTripType().getTripSubType()) {
            List<ActionMovementTrip> actionMovementTrips = getActionMovementTripsForSelected();
            return actionMovementTrips != null && (actionMovementTrips.size() > temp);
        } else if (MovementTripSubType.FORMATION.getValue() == current.getTripType().getTripSubType()) {
            List<ActionTrainFormation> actionTrainFormation = getActionTrainFormationForSelected();
            return actionTrainFormation != null && (actionTrainFormation.size() > temp);
        }
        return false;
    }*/

    public int getUsedBy(MovementTrip movement) {
        int usedBy = 0;
        
        if(movement.isCreating() == true)
            return usedBy;
        
        if (MovementTripSubType.PLANNED.getValue() == movement.getTripType().getTripSubType()) {
            List<ActionMovementTrip> actionMovementTrips = getActionMovementTrips(movement);
            if (actionMovementTrips != null && !actionMovementTrips.isEmpty()) {
                Iterator<ActionMovementTrip> iterator = actionMovementTrips.iterator();
                MovementTripGroup movementTripGroup;
                while (iterator.hasNext()) {
                    movementTripGroup = (MovementTripGroup) (iterator.next().getTrip());
                    //if (movementTripGroup.getRefTrip().getTrip().isValid()) {//trip template
                    usedBy++;
                    //}
                    if (usedBy > 1) {
                        break;
                    }
                }
            }

        } else if (MovementTripSubType.FORMATION.getValue() == movement.getTripType().getTripSubType()) {
            List<ActionTrainFormation> actionTrainFormation = getActionTrainFormation(movement);
            if (actionTrainFormation != null && !actionTrainFormation.isEmpty()) {
                Iterator<ActionTrainFormation> iterator = actionTrainFormation.iterator();
                while (iterator.hasNext()) {
                    //if (iterator.next().getTrip().isValid()) {//planned service
                    usedBy++;
                    iterator.next();
                    //}
                    if (usedBy > 1) {
                        break;
                    }
                }
            }
        }
        return usedBy;
    }

    public void fromLocationChanged() {
        if(current.getTripType().getTripSubType() == MovementTripSubType.FORMATION.getValue()) {
            current.setPlannedStopObj(current.getPlannedStartObj());
        }
    }
    
    public String save() {
        current = (MovementTrip) getItems().getRowData();
        if (current.isCreating()) {
            try {
                //current.setOrigoSecs(0 - Math.abs(current.getOrigoSecs()));
                if(current.getTripType().getTripSubType() == MovementTrip.MovementTripSubType.FORMATION.getValue())
                    current.setPlannedStopObj(current.getPlannedStartObj());
                
                current.setCreating(false);
                current.setEditing(false);
                current.setVersion(1);
                getFacade().create(current);
                xmlMessageSender.sendMovementTripMsg(current, Operation.CREATE);
                JsfUtil.addSuccessMessage(uiText.get("MovementCreated"));
            } catch (Exception e) {
                JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
                current.setCreating(true);
                current.setEditing(true);
                return null;
            }
        } else {

            try {

                //if (getUsedBy(current) <= 1) {
                    current.setEditing(false);
                    if(current.getTripType().getTripSubType() == MovementTrip.MovementTripSubType.FORMATION.getValue())
                        current.setPlannedStopObj(current.getPlannedStartObj());                       
                    current.increaseVersion();
                    getFacade().edit(current);
                    JsfUtil.addSuccessMessage(uiText.get("MovementUpdated"));
                    xmlMessageSender.sendMovementTripMsg(current, Operation.MODIFY);
//                } else {
//                    current.setEditing(false);
//                    if (current.getTripType().getTripSubType() == MovementTripSubType.FORMATION.getValue()) {
//                        JsfUtil.addErrorMessage(uiText.get("Error_MovementTripUsed3"));
//                    } else {
//                        JsfUtil.addErrorMessage(uiText.get("Error_MovementTripUsed2"));
//                    }
//                }
            } catch (Exception e) {
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
            current = (MovementTrip) getItems().getRowData();
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
        //tripAction.getTrip().updateActionTimes();
        //recreateModel();
        return "";
    }

    public String saveTripAction(TripAction tripAction) {
        try {
//            if (getUsedBy(current) > 1) {
//                recreateModel();
//                current = this.ejbFacade.find(current.getTripId());
//                tripAction.setEditing(false);
//                if (current.getTripType().getTripSubType() == MovementTripSubType.FORMATION.getValue()) {
//                    JsfUtil.addErrorMessage(uiText.get("Error_MovementTripUsed3"));
//                } else {
//                    JsfUtil.addErrorMessage(uiText.get("Error_MovementTripUsed2"));
//                }
//                return null;
//            }

            /*if (tripAction instanceof ActionTrainMoving) {
             tripAction.setMinSecs(tripAction.getPlannedSecs());
             }*/
            boolean creating = tripAction.isCreating();
            //current.setDurationSecs(getTripDuration());
            tripAction.setEditing(false);
            tripAction.setCreating(false);
            //Collections.sort(current.getTripActions());

            Integer currentSeqNo;
            List<TripAction> sortedActions = current.getTripActions();
            for (int i = 1; i < sortedActions.size(); i++) {
                currentSeqNo = sortedActions.get(i - 1).getSeqNo();
                int diff = sortedActions.get(i).getSeqNo() - currentSeqNo;
                if (diff > 1) {
                    currentSeqNo++;
                    for (int j = i; j < sortedActions.size(); j++) {
                        sortedActions.get(j).setSeqNo(sortedActions.get(j).getSeqNo() - diff + 1);
                    }
                }
            }
            current.setTripActions(sortedActions);
            current.updateActionTimes();
            current.increaseVersion();
            getFacade().edit(current);

            //current = this.ejbFacade.find(current.getTripId());
            this.updateState();
            xmlMessageSender.sendMovementTripMsg(current, Operation.MODIFY);
            if (creating) {
                JsfUtil.addSuccessMessage(uiText.get("MovementActionCreated"));
            } else {
                JsfUtil.addSuccessMessage(uiText.get("MovementActionUpdated"));
            }
            recreateModel();
            return null;
        } catch (Exception e) {
            tripAction.setEditing(true);
            if (tripAction.getActionId() == 0) {
                tripAction.setCreating(true);
            }
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
            tripAction.setCreating(true);
            tripAction.setEditing(true);
            return null;
        }

    }

    public void addNewTripAction() {
        TripAction tripAction = null;

        switch (TripAction.MainActionTypeEnum.parse(this.selectedMainActionType.getMactionTypeId())) {
            case ROUTE_ACTION:
                tripAction = new ActionRouteTrip();
                break;
            case SINGLE_CMD_ACTION:
                tripAction = new ActionCmdTrip();
                /*if ( !this.getPossibleActionLocations2().isEmpty()) {
                 ((ActionTrainMoving) tripAction).setTimetableObject2(this.getPossibleActionLocations2().get(0));
                 }*/
                break;
            case SHUNTING_ROUTE_ACTION:
                tripAction = new ActionShuntingRouteTrip();
                /*if ( !this.getPossibleActionLocations2().isEmpty()) {
                 ((ActionTrainMoving) tripAction).setTimetableObject2(this.getPossibleActionLocations2().get(0));
                 }*/
                break;
            case TRAIN_ACTION:
                tripAction = new ActionTrainInternal();
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
            //tripAction.setSeqNo(current.getNumberOfActions() + 1);
            tripAction.setActionId(0);
            /*if (current.getNumberOfActions() > 0) {
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
             } else if (this.getPossibleActionLocations1().isEmpty() == false) {*/
            tripAction.setTimetableObject(this.getPossibleActionLocations1().get(0));
            //}

            current.addTripAction(tripAction, 0);
            //current.setDurationSecs(current.getDurationSecs() + tripAction.getPlannedSecs());
        }
    }

    public boolean isAddTripTemplateAllowed() {
        @SuppressWarnings("unchecked")
        List<MovementTrip> array = (List<MovementTrip>) getItems().getWrappedData();
        if (array == null || array.isEmpty()) {
            return true;
        } else {
            MovementTrip e = (MovementTrip) array.get(0);
            if (e.isCreating()) {
                return false;
            } else {
                Iterator<MovementTrip> iterator = array.iterator();
                while (iterator.hasNext()) {
                    MovementTrip item = (MovementTrip) iterator.next();
                    if (item.isEditing()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public String addNew() {
        current = new MovementTrip();
        current.setEditing(true);
        current.setCreating(true);
        current.setConsumed(false);
        current.setUtcTimes(true);
        current.setTimesAreValid(false);
        current.setDurationSecs(0);
        current.setOrigoSecs(0);
        current.setValid(Boolean.TRUE);
        //current.setOrigoSecs(0 - defaultOrigoSecs);//defaultplansecs for Action passenger stop
        current.setTripType(this.selectedTripType);
        current.setAreaObj(selectedArea);
        current.setVersion(1);
        @SuppressWarnings("unchecked")
        List<MovementTrip> oldArray = (List<MovementTrip>) getItems().getWrappedData();
        oldArray.add(0, current);
        getItems().setWrappedData(oldArray);

        selectedItemIndex = -1;
        return null;
    }

    public String destroy() {
        current = (MovementTrip) getItems().getRowData();
        //current = this.ejbFacade.find(current.getTripId());
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        if (!current.isCreating()) {
            if (this.getUsedBy(current) == 0) {
                performDestroy();
            } else {
                JsfUtil.addErrorMessage(uiText.get("Error_MovementTripUsed"));
            }
        }
        current = null;
        recreateModel();
        return "List";
    }

    public String destroyTripAction(TripAction tripAction) {
        //current = (MovementTrip) getItems().getRowData();
        //selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        try {
            if (!tripAction.isCreating()) {
                current = this.ejbFacade.find(current.getTripId());
                //if (this.getUsedBy(current) <= 1) {
                    //F5 will make the persistence lost
                    //current = this.ejbFacade.find(current.getTripId());
                    //TripAction tripActionInDB = this.ejbTripActionFacade.find(tripAction.getActionId());
                    //updateSeqNo(false, tripActionInDB, tripAction.getSeqNo());
                    current.removeTripAction(tripAction);
                    //current.setDurationSecs(getTripDuration());
                    current.setTimesAreValid(false);
                    current.increaseVersion();
                    current.updateActionTimes();
                    getFacade().edit(current);

                    JsfUtil.addSuccessMessage(uiText.get("MovementActionDeleted"));
                    xmlMessageSender.sendMovementTripMsg(current, Operation.MODIFY);
//                } else {
//                    JsfUtil.addErrorMessage(uiText.get("Error_MovementTripUsed"));
//                }
            }/* else {
             current.removeTripAction(tripAction);
             }*/

        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
        }

        //current = this.ejbFacade.find(current.getTripId());
        this.updateState();
        recreateModel();
        return null;
    }

    /*private void updateSeqNo(boolean edit, TripAction tripActionInDb, int seqNo) {
     Iterator<TripAction> iterator = current.getTripActions().iterator();
     while (iterator.hasNext()) {
     TripAction tripAction = iterator.next();
     if (tripActionInDb != null && tripAction.getActionId() == tripActionInDb.getActionId()) {
     continue;
     }
     if (edit) {
     if (tripActionInDb != null) {//modify
     if (tripActionInDb.getSeqNo() > seqNo) {//seqNo decreases
     if (tripAction.getSeqNo() < tripActionInDb.getSeqNo() && tripAction.getSeqNo() >= seqNo) {
     tripAction.setSeqNo(tripAction.getSeqNo() + 1);
     }
     } else {
     if (tripAction.getSeqNo() > tripActionInDb.getSeqNo() && tripAction.getSeqNo() <= seqNo) {
     tripAction.setSeqNo(tripAction.getSeqNo() - 1);
     }
     }
     } else {//add new tripAction
     if (tripAction.isCreating() == false && tripAction.getSeqNo() >= seqNo) {
     tripAction.setSeqNo(tripAction.getSeqNo() + 1);
     }
     }
     } else {//delete
     if (tripAction.getSeqNo() > seqNo) {
     tripAction.setSeqNo(tripAction.getSeqNo() - 1);
     }
     }
     }
     }*/
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

    private void performDestroy() {
        try {
            getFacade().remove(current);
            JsfUtil.addSuccessMessage(uiText.get("MovementDeleted"));
            xmlMessageSender.sendMovementTripMsg(current, Operation.DELETE);
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

    /*public List<MovementTrip> getItems(TripAction tripAction) {
     TTObject startObj = tripAction.getTimetableObject();
     MovementTripFilter filter = new MovementTripFilter();
     filter.SetPlannedStartObjFilter(startObj);
     List<MovementTrip> list = this.ejbFacade.findAll(filter);
     if (tripAction.getRefTrip() != null && tripAction.getRefTrip().getTripTemplate() == null && list.isEmpty() == false) {
     tripAction.getRefTrip().setTripTemplate(list.get(0));
     }
     return list;
     }*/
 /*public List<TTArea> getAreaFilter() {
     //areaItems = getAreaItems();
     //List<TTArea> areas = (List<TTArea>) areaItems.getWrappedData();
     return areaFilterList;
     }*/
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

    public String activateEdit() {
        //current = trip;
        current = (MovementTrip) getItems().getRowData();
        current.setEditing(true);
        this.selectedFromTTObjectType = current.getPlannedStartObj().getTTObjectType();
        this.selectedToTTobjectType = current.getPlannedStopObj().getTTObjectType();
        return null;
    }

    public String activateEditTripAction(TripAction tripAction) {
        //refresh page will lose 
        tripAction.setEditing(true);

        selectedActionType = tripAction.getActionType();
        selectedMainActionType = selectedActionType.getMainActionType();
        selectedActionTTObjectType1 = tripAction.getTimetableObject().getTTObjectType();
        return null;
    }
}
