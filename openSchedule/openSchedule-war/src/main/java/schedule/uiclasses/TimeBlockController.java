/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.uiclasses;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
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
import static schedule.entities.ActionGlue.ActionGlueTypeEnum.GLUE_WAIT;
import static schedule.entities.ActionGlue.ActionGlueTypeEnum.GLUE_WAIT_AND_TURN;
import schedule.entities.ActionRunTrip;
import static schedule.entities.ActionRunTrip.ActionRunTripTypeEnum.RUN_COMMERCIAL_TRIP;
import static schedule.entities.ActionTrainFormation.ActionTrainFormationTypeEnum.END_RUN;
import static schedule.entities.ActionTrainFormation.ActionTrainFormationTypeEnum.START_RUN;
import schedule.entities.ActionTrainMoving;
import schedule.entities.ActionType;
import schedule.entities.BasicTrip;
import schedule.entities.DayType;
import schedule.entities.PlannedService;
import schedule.entities.Schedule;
import schedule.entities.SchedulingState;
import schedule.entities.TTObject;
import schedule.entities.TTObjectType;
import schedule.entities.TimeBlock;
//import schedule.entities.TimeBlockTripTemplate;
import schedule.entities.TimedTrip;
import schedule.entities.TrainType;
import schedule.entities.TripAction;
import static schedule.entities.TripAction.MainActionTypeEnum.GLUE;
import static schedule.entities.TripAction.MainActionTypeEnum.RUN_TRIP;
import static schedule.entities.TripAction.MainActionTypeEnum.TRAIN_FORMATION;
import schedule.entities.TripType;
import schedule.messages.Operation;
import schedule.messages.XmlMessageSender;
import schedule.sessions.ActionTypeFacade;
import schedule.sessions.ScheduleFacade;
import schedule.sessions.SchedulingStateFacade;
import schedule.sessions.TTObjectFacade;
import schedule.sessions.TTObjectTypeFacade;
import schedule.sessions.TimeBlockFacade;
import schedule.sessions.TrainTypeFacade;
import schedule.sessions.TripFacade;
import schedule.uiclasses.util.JsfUtil;
import schedule.uiclasses.util.PaginationHelper;
import schedule.uiclasses.util.UiText;
import schedule.uiclasses.util.config.ConfigBean;
import schedule.uiclasses.util.config.TimeBlockPage;

/**
 *
 * @author Jia Li
 */
@Named(("timeBlockController"))
@SessionScoped
public class TimeBlockController extends FilterController<TimeBlock, TimeBlockFilter> implements Serializable {

    @Inject
    private XmlMessageSender xmlMessageSender;

    @Inject
    private TimeBlockFacade ejbFacade;
    @Inject
    private ScheduleFacade ejbScheduleFacade;
    @Inject
    private TTObjectFacade ejbTTObjectFacade;
    @Inject
    private TTObjectTypeFacade ejbTTObjectTypeFacade;
    @Inject
    private TripFacade ejbTripTemplateFacade;
    @Inject
    private ActionTypeFacade ejbActionTypeFacade;
    @Inject
    private SchedulingStateFacade ejbSchedulingStateFacade;
    @Inject
    private PlannedServiceController plannedServiceController;
    @Inject
    private TrainTypeFacade ejbTrainTypeFacade;

    private TTObjectType selectedFromTTObjectType;
    private TTObjectType selectedToTTObjectType;
    private TTObject selectedFromTTObject;
    private TTObject selectedToTTObject;

    private TripType subTripTypeFilter;
    private TTObjectType ttObjectTypeAll;
    private List<TTObjectType> ttobjectTypes;

    private ActionType GLUE_WAIT_ACTION;
    private ActionType GLUE_WAIT_AND_TURN_ACTION;
    private ActionType TRAIN_FORMATION_START_ACTION;
    private ActionType TRAIN_FORMATION_END_ACTION;
    private ActionType RUN_COMMERCIAL_TRIP_ACTION;
    private SchedulingState DEFAULT_STATE;
    private TrainType DEFAULT_TRAINTYPE;

    @Resource
    private UserTransaction userTransaction;
    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager entityManager;

    @Inject
    private UiText uiText;

    private String timeZone;
    private Integer noOfTrains;
    private DataModel<BasicTrip> templateItems = null; // All items to be shown in 'List'
    private PaginationHelper<BasicTrip> templatePagination = null;
    private LinkedHashMap<Integer, BasicTrip> selectedTemplates;
    private boolean reverseSelectedTemplates = false;
    private Map<String, Integer> days;
    private Integer selectedTemplateView = 1;
    private ActionType glueType;

    public TimeBlockController() {
        super(new TimeBlockFilter());
    }

    public TimeBlockController(TimeBlockFilter filter) {
        super(filter);
    }

    public TimeBlockController(TimeBlockFilter filter, int itemsPerPage) {
        super(filter, itemsPerPage);
    }

    @PostConstruct
    public void init() {
        days = new LinkedHashMap<>();
        String day = uiText.get("day");
        for (int i = 0; i < 8; i++) {
            days.put("+" + i + " " + day, i);
        }
        selectedTemplates = new LinkedHashMap<>();

        ScheduleFilter scheduleFilter = new ScheduleFilter();
        scheduleFilter.setValid(Boolean.TRUE);
        List<Schedule> schedules = ejbScheduleFacade.findAll(scheduleFilter);

        if (!schedules.isEmpty()) {
            Schedule schedule = schedules.get(0);
            this.getFilter().setSchedule(schedule);
            //this.getFilter().setDayType(schedule.getDayTypes().isEmpty() ? null : schedule.getDayTypes().get(0));
        }

        TTObjectTypeFilter filter = new TTObjectTypeFilter();
        filter.setStartTTObjectTypeFilter();

        TTObjectFilter areaFilter = new TTObjectFilter();
        areaFilter.setObjectClassFilter('A');
        TTObject area = this.ejbTTObjectFacade.findFirst(areaFilter);
        if (area != null) {
            timeZone = area.getTimeZone();
        }

        TRAIN_FORMATION_START_ACTION = getActionType(TRAIN_FORMATION, START_RUN.getValue());
        TRAIN_FORMATION_END_ACTION = getActionType(TRAIN_FORMATION, END_RUN.getValue());
        GLUE_WAIT_AND_TURN_ACTION = getActionType(GLUE, GLUE_WAIT_AND_TURN.getValue());
        GLUE_WAIT_ACTION = getActionType(GLUE, GLUE_WAIT.getValue());
        RUN_COMMERCIAL_TRIP_ACTION = getActionType(RUN_TRIP, RUN_COMMERCIAL_TRIP.getValue());

        DEFAULT_STATE = ejbSchedulingStateFacade.find(SchedulingState.State.PLANNED_TO_RUN.getStateValue());
        //SYSTEM_USER = GetTripUserType(TripUserType.State.SYSTEM_USER.getValue());

        this.subTripTypeFilter = new TripType(0, uiText.get("FilterAll"));
        this.ttObjectTypeAll = new TTObjectType(0, uiText.get("FilterAll"));
        this.ttobjectTypes = this.ejbTTObjectTypeFacade.findAll(filter);
        selectedToTTObjectType = ttObjectTypeAll;
        selectedFromTTObjectType = ttObjectTypeAll;
        DEFAULT_TRAINTYPE = ejbTrainTypeFacade.find(((TimeBlockPage) (ConfigBean.getConfig().getPage("timeBlock"))).getDefaultTrainType());
        if (DEFAULT_TRAINTYPE == null) {
            DEFAULT_TRAINTYPE = ejbTrainTypeFacade.find(1);
        }

        boolean isGlueWaitTurn = ((TimeBlockPage) (ConfigBean.getConfig().getPage("timeBlock"))).isGlueWaitTurn();
        glueType = isGlueWaitTurn ? GLUE_WAIT_AND_TURN_ACTION : GLUE_WAIT_ACTION;
    }

    private ActionType getActionType(TripAction.MainActionTypeEnum mainActionTypeEnum, Integer subType) {
        ActionTypeFilter filter = new ActionTypeFilter();
        filter.setTypeFilter(mainActionTypeEnum);
        filter.setActionSubTypeFilter(subType);
        return ejbActionTypeFacade.findFirst(filter);
    }

    /*public void updateTimeBlockTripTemplates() {

        //timeBlockTripTemplates.clear();
        BasicTripFilter templatefilter = new BasicTripFilter();
        templatefilter.setValidFilter(true);
        if (this.subTripTypeFilter != null && this.subTripTypeFilter.getTripTypeId() != 0) {
            templatefilter.setTripSubTypeFilter(subTripTypeFilter);
        }
        if (selectedFromTTObject != null && selectedFromTTObject.getTTObjectType().getTTObjTypeId() != 0) {
            templatefilter.setPlannedStartObjFilter(this.selectedFromTTObject);
        }
        if (selectedToTTObject != null && selectedToTTObject.getTTObjectType().getTTObjTypeId() != 0) {
            templatefilter.setPlannedStopObjFilter(selectedToTTObject);
        }
        timeBlockTripTemplates = this.ejbTripTemplateFacade.findAll(templatefilter);

        for (BasicTrip tripTemplate : timeBlockTripTemplates) {

            if (this.selectedTimeBlockTripTemplates.containsKey(tripTemplate.getTripId())) {
                ((TimeBlockTripTemplate)tripTemplate).setSeqNo(selectedTimeBlockTripTemplates.get(tripTemplate.getTripId()).getSeqNo());
            } 
        }
    }*/
//<editor-fold defaultstate="collapsed" desc="comment">
    public PaginationHelper<BasicTrip> getTemplatePagination() {

        if (templatePagination == null) {
            templatePagination = new PaginationHelper<BasicTrip>(this.itemsPerPage) {
                //this pagination is only used when allTemplates is true
                @Override
                public int getItemsCount() {
                    BasicTripFilter filter = new BasicTripFilter();
                    filter.setValidFilter(true);
                    /*if (!allTemplates && !selectedTemplates.isEmpty()) {
                        filter.setSelectedIds(selectedTemplates.keySet());
                    }*/
                    if (subTripTypeFilter != null && subTripTypeFilter.getTripTypeId() != 0) {
                        filter.setTripSubTypeFilter(subTripTypeFilter);
                    }
                    if (selectedFromTTObject != null || (selectedFromTTObjectType != null)) {
                        filter.setPlannedStartObjFilter(selectedFromTTObject);
                    }
                    if (selectedToTTObject != null || (selectedToTTObjectType != null)) {
                        filter.setPlannedStopObjFilter(selectedToTTObject);
                    }
                    return ejbTripTemplateFacade.count(filter);
                }

                @Override
                public DataModel createPageDataModel() {
                    BasicTripFilter filter = new BasicTripFilter();
                    filter.setValidFilter(true);

                    /*if (!allTemplates && !selectedTemplates.isEmpty()) {
                        filter.setSelectedIds(selectedTemplates.keySet());
                    }*/
                    if (subTripTypeFilter != null && subTripTypeFilter.getTripTypeId() != 0) {
                        filter.setTripSubTypeFilter(subTripTypeFilter);
                    }
                    if (selectedFromTTObject != null || (selectedFromTTObjectType != null)) {
                        filter.setPlannedStartObjFilter(selectedFromTTObject);
                    }
                    if (selectedToTTObject != null || (selectedToTTObjectType != null)) {
                        filter.setPlannedStopObjFilter(selectedToTTObject);
                    }
                    List<BasicTrip> list = getTripFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}, filter);
                    if (!selectedTemplates.isEmpty()) {
                        Iterator<BasicTrip> it = list.iterator();
                        BasicTrip template;
                        while (it.hasNext()) {
                            template = it.next();
                            if (selectedTemplates.containsKey(template.getTripId()) && selectedTemplates.get(template.getTripId()).isSelected()) {
                                template.setSelected(true);
                            }
                        }

                    }
                    return new ListDataModel(list);

                    //return new ListDataModel<>(ejbTripTemplateFacade.findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}, filter));
                }
            };
        }
        return templatePagination;
    }

    public String templateNext() {
        getTemplatePagination().nextPage();
        templateItems = null;
        return "View";
    }

    public String templatePrevious() {
        getTemplatePagination().previousPage();
        templateItems = null;
        return "View";
    }
//</editor-fold>

    public TripFacade getTripFacade() {
        return ejbTripTemplateFacade;
    }

    public boolean isAllTemplates() {
        return selectedTemplateView == 1;
    }

    public DataModel<BasicTrip> getAllTripTemplates() {
        if (templateItems == null) {
            templateItems = getTemplatePagination().createPageDataModel();
        }
        return templateItems;
    }

    public void selectedTemplateChanged(BasicTrip template) {
        if (template != null) {
            if (selectedTemplates.containsKey(template.getTripId())) {
                if (!template.isSelected()) {
                    selectedTemplates.remove(template.getTripId());
                    templateItems = null;
                }

            } else if (template.isSelected()) {
                List<BasicTrip> list = getSelectedTripTemplates();
                if (list.size() >= 2) {
                    //selectedTemplates.remove(list.get(0).getTripId());
                    //templateItems = null;

                    template.setSelected(false);
                    JsfUtil.addErrorMessage(uiText.get("TripTemplateTooMany"));
                } else {
                    selectedTemplates.put(template.getTripId(), template);
                    templateItems = null;
                }
            }
            /*Iterator<BasicTrip> it = ((List<BasicTrip>) templateItems.getWrappedData()).iterator();
            BasicTrip temp;
            while (it.hasNext()) {
                temp = it.next();
                if (Objects.equals(temp.getTripId(), template.getTripId())) {
                    temp.setSelected(template.isSelected());
                    break;
                }
            }*/
        }
    }

    /*public DataModel<BasicTrip> getSelectedTripTemplates() {
        List<BasicTrip> allTripTemplates = (List<BasicTrip>) getAllTripTemplates().getWrappedData();
        Iterator<BasicTrip> it = allTripTemplates.iterator();
        BasicTrip temp;
        List<BasicTrip> selected = new ArrayList<>();
        while(it.hasNext()) {
            temp = it.next();
            if(temp.isSelected())
                selected.add(temp);
        }    
                    
        return new ListDataModel(selected);
    }*/
//<editor-fold defaultstate="collapsed" desc="comment">
//    public Integer getSeqNo(TimeBlock timeBlock) {
//        Integer seqNo = 0;
//        TimeBlockFilter timeBlockFilter = new TimeBlockFilter();
//        timeBlockFilter.setDayType(this.getFilter().getDayType());
//        Iterator<TimeBlock> it = this.ejbFacade.findAll(timeBlockFilter).iterator();
//
//        while (it.hasNext()) {
//            if (it.next().getStartSecs() > timeBlock.getStartSecs()) {
//                break;
//            } else {
//                seqNo++;
//            }
//        }
//
//        return seqNo;
//    }
//</editor-fold>
    public String getTimeZone() {
        return timeZone;
    }

    public void scheduleFilterChanged() {
//        if (getFilter().getSchedule() != null && getFilter().getSchedule().getValid() && getFilter().getSchedule().getDayTypes() != null) {
//            getFilter().setDayType(getFilter().getSchedule().getDayTypes().isEmpty() ? null : getFilter().getSchedule().getDayTypes().get(0));
//        } else {
            getFilter().setDayType(null);
//        }
        resetPagination();
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

    public void dayTypeFilterChanged() {
        resetPagination();
    }

    @Override
    protected TimeBlockFacade getFacade() {
        return ejbFacade;
    }

    @Override
    public TimeBlock constructNewItem() {
        TimeBlock item = new TimeBlock();
        item.setEditing(true);
        item.setCreating(true);
        item.setDayType(this.getFilter().getDayType());
        item.setTimeZone(timeZone);
        return item;
    }

    private void updateState(TimeBlock timeBlock) {
        if (timeBlock.getBlockId() != null) {
            this.ejbFacade.evict(timeBlock);
        }
    }

    public String activateEditTripTemplate(BasicTrip tripTemplate) {
        //refresh page will lose 
        tripTemplate.setEditing(true);

        return null;
    }

    @Override
    public String prepareView(TimeBlock item) {
        /*/Integer oldSelected = this.getSelected() != null ? this.getSelected().getBlockId() : null;
        String view = super.prepareView(item);// update selected
        if (this.getSelected() != null && (!Objects.equals(oldSelected, item.getBlockId()))) {
            this.updateTimeBlockTripTemplates();// do this after selected is updated, so it can update the cache for the new selected item
        }*/
        String view = super.prepareView(item);

        return view;
    }

    public List<BasicTrip> getSelectedTripTemplates() {
        List<BasicTrip> list = new ArrayList<>();

        Iterator<BasicTrip> it = (new ArrayList<BasicTrip>(this.selectedTemplates.values())).iterator();
        while (it.hasNext()) {
            BasicTrip template = it.next();
            if (template.isSelected()) {
                list.add(template);
            } else {
                // it is possible that the cached template is removed in database.
                this.selectedTemplates.remove(template.getTripId());
            }
        }

        return list;
    }

    //<editor-fold defaultstate="collapsed" desc="comment">
    /*public void addNewTripTemplate() {
    this.getSelected().setEditing(true);
    if (this.getSelected().getTemplate1() == null) {
    BasicTripFilter filter = new BasicTripFilter();
    filter.setTripSubTypeFilter(selectedTripType);
    BasicTrip basicTrip = this.ejbTripTemplateFacade.findFirst(filter);
    this.getSelected().setTemplate1(basicTrip);
    } else if (this.getSelected().getTemplate2() == null) {
    BasicTripFilter filter = new BasicTripFilter();
    filter.setTripSubTypeFilter(selectedTripType);
    BasicTrip basicTrip = this.ejbTripTemplateFacade.findFirst(filter);
    this.getSelected().setTemplate2(basicTrip);
    } else {
    JsfUtil.addErrorMessage(uiText.get("TripTemplateTooMany"));
    return;
    }
    }*/
 /*public boolean isAddTripTemplateAllowed() {
    List<BasicTrip> array = this.getSelected().getTripTemplates();
    
    if (array == null || array.isEmpty()) {
    return true;
    } else {
    BasicTrip e = (BasicTrip) array.get(0);
    if (e != null && e.isCreating()) {
    return false;
    } else {
    if (array.size() >= 2) {
    return false;
    }
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
    }*/
//</editor-fold>
    @SuppressWarnings("deprecation")
    public Date getHeadway() {
        if (this.selectedTemplates.size() < 2 || this.noOfTrains == null) {
            return null;
        }

        List<BasicTrip> list = this.getSelectedTripTemplates();
        int roundTripSecs = list.get(0).getDurationSecs() + glueType.getDefaultPlanSecs() + list.get(1).getDurationSecs();
        int secs = roundTripSecs / noOfTrains;
        Date headway = new Date();
        headway.setHours(Math.abs(secs) / 3600);
        headway.setMinutes(Math.abs(secs) % 3600 / 60);
        headway.setSeconds(Math.abs(secs) % 60);
        return headway;
    }

    private PlannedService getClosetServiceByEndTime(List<PlannedService> services, Integer newSecs, int lastSecs, BasicTrip newTemplate) {
        if (services != null) {
            Integer newDeparture = newSecs;
            Integer lastDeparture = lastSecs;
            if (!(newTemplate.getTripAction(1) instanceof ActionTrainMoving)) {
                newDeparture = newSecs + newTemplate.getTripAction(1).getPlannedSecs();
                lastDeparture = lastSecs + newTemplate.getTripAction(1).getPlannedSecs();
            }
            //System.out.println("newSecs:" + newSecs + "   lastSecs" + lastSecs +"   newDeparture:" + newDeparture + "lastDeparture:" + lastDeparture );

            for (int i = services.size() - 1; i >= 0; i--) {
                PlannedService service1 = services.get(i);
                ActionRunTrip lastAction = (ActionRunTrip) (service1.getTripAction(service1.getNumberOfActions() - 2));
                Integer endSecs = lastAction.getRefTrip().getPlannedStopSecs();
                Integer arrivalTime;
                BasicTrip template = lastAction.getRefTrip().getTripTemplate();
                TripAction lastActionInTemplate = template.getTripAction(template.getNumberOfActions());
                if (lastActionInTemplate instanceof ActionTrainMoving) {
                    arrivalTime = endSecs;
                } else {
                    arrivalTime = endSecs - lastActionInTemplate.getPlannedSecs();
                }

                if ((endSecs + this.glueType.getDefaultPlanSecs()) <= newSecs && arrivalTime >= lastDeparture) {
                    if (i + 1 < services.size()) {
                        PlannedService nextService = services.get(i + 1);
                        TripAction nextAction = nextService.getTripAction(nextService.getNumberOfActions() - 2);
                        if (nextAction instanceof ActionRunTrip) {// could be TrainFormation
                            Integer nextArrivalTime = ((ActionRunTrip) nextAction).getRefTrip().getPlannedStopSecs();
                            template = ((ActionRunTrip) nextAction).getRefTrip().getTripTemplate();
                            lastActionInTemplate = template.getTripAction(template.getNumberOfActions());
                            if (!(lastActionInTemplate instanceof ActionTrainMoving)) {
                                nextArrivalTime = nextArrivalTime - lastActionInTemplate.getPlannedSecs();
                            }

                            if (newDeparture <= nextArrivalTime) {
                                //String str = "found:service1 " + service1.getDescription() + "   newDeparture:" + newDeparture + "   nextArriavalTime:" + nextArrivalTime;
                                //Logger.getLogger(TimeBlockController.class.getName()).log(Level.INFO, str);
                                return service1;
                            }//else
                            //System.out.println("nextArrivalTime:" + nextArrivalTime);
                        } else {
                            return service1;
                        }
                    } else {
                        return service1;
                    }
                }
            }
        }
        return null;
    }

    public void generatePlannedServices(TimeBlock timeBlock) {
        try {
            List<BasicTrip> tripTemplates = getSelectedTripTemplates();

            if (tripTemplates.size() < 2) {
                JsfUtil.addErrorMessage(uiText.get("Error_invalidTimeblockTemplates"));
                return;
            }

            userTransaction.begin();
            entityManager.joinTransaction();
//            TimeBlock prevBlock = getPrevTimeBlock(timeBlock);
//            if (prevBlock == null) {
//                if (!generatePlannedServicesFromBothSides(timeBlock, tripTemplates)) {
//                    return;
//                }
//            } else if (!generatePlannedServicesFromOneSide(timeBlock, tripTemplates)) {
//                return;
//            }
            //make the start time earlier since all the services are starting from one side
            //timeBlock.setStartSecs(timeBlock.getStartSecs() - tripTemplates.get(0).getDurationSecs());
            List<PlannedService> list = generatePlannedServicesFromOneSide(timeBlock, tripTemplates);
            if (list == null) {
                return;
            }
            //all the services should stop at one end
            PlannedServiceFilter filter = new PlannedServiceFilter();
            filter.setValidFilter(true);
            filter.setDayTypeFilter(timeBlock.getDayType());
            filter.setMinPlannedStopSecs(timeBlock.getEndSecs());
            //int biggestDuration = tripTemplates.get(0).getDurationSecs() > tripTemplates.get(1).getDurationSecs() ? tripTemplates.get(0).getDurationSecs() : tripTemplates.get(1).getDurationSecs();
            //filter.setMaxPlannedStopSecs(timeBlock.getEndSecs() + glueType.getDefaultPlanSecs() + biggestDuration);
            filter.setSortByStopSecs(true);
            PlannedService plannedService = this.plannedServiceController.getFacade().findFirst(filter);
            TripAction lastAction = plannedService.getTripAction(plannedService.getNumberOfActions() - 2);
            
            if (lastAction instanceof ActionRunTrip) {
                BasicTrip tripTemplate = ((ActionRunTrip) lastAction).getRefTrip().getTripTemplate();
                BasicTrip newTripTemplate;
                if (Objects.equals(tripTemplates.get(0).getTripId(), tripTemplate.getTripId())) {
                    newTripTemplate = tripTemplates.get(0);
                } else {
                    newTripTemplate = tripTemplates.get(1);
                }
                filter.setPlannedStopObj(newTripTemplate.getPlannedStartObj());
                List<PlannedService> list2 = this.plannedServiceController.getFacade().findAll(filter);
                if (list2 != null && !list2.isEmpty()) {
                    Iterator<PlannedService> it = list2.iterator();
                    while (it.hasNext()) {
                        PlannedService ps = it.next();
                        ps.setEditing(true);
                        TripAction lastGlue = ps.getTripAction(ps.getNumberOfActions() - 1);
                        TripAction lastFormation = ps.getTripAction(ps.getNumberOfActions());
                        int seqNo = ps.getNumberOfActions() - 2;
                        ActionRunTrip lastRunTrip = (ActionRunTrip) ps.getTripAction(seqNo);

                        TripAction glueAction = createGlueAction(ps, lastRunTrip, ++seqNo, glueType);
                        ps.addTripAction(glueAction);

                        int startSecs = lastRunTrip.getRefTrip().getPlannedStopSecs() + glueAction.getPlannedSecs();
                        TripAction runTripAction = createRuntripAction(ps, newTripTemplate, glueAction, startSecs,
                                startSecs + newTripTemplate.getDurationSecs(), ++seqNo);
                        ps.addTripAction(runTripAction);
                        this.plannedServiceController.templateChanged(runTripAction);

                        lastGlue.setSeqNo(++seqNo);
                        lastGlue.setTimetableObject(runTripAction.getTimetableObject2());
                        lastFormation.setSeqNo(++seqNo);
                        lastFormation.setTimetableObject(runTripAction.getTimetableObject2());

                        ps.updateActionTimes();
                        ps.setDurationSecs(lastFormation.getTimeFromTripStart() + lastFormation.getPlannedSecs());
                        ps.setPlannedStopSecs(ps.getPlannedStartSecs() + ps.getDurationSecs());
                        ps.setPlannedStopObj(lastFormation.getTimetableObject());
                        entityManager.merge(ps);
                    }

                }
            }

            userTransaction.commit();
            if (list != null && !list.isEmpty()) {
                xmlMessageSender.sendServiceDutyChangeMsg(list, null);
            }

        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            try {
                userTransaction.rollback();
            } catch (IllegalStateException | SecurityException | SystemException ex1) {
                Logger.getLogger(TimeBlockController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(TimeBlockController.class.getName()).log(Level.SEVERE, null, ex);
            JsfUtil.addErrorMessage(ex, uiText.get("Error_GenerateTimetableFailed"));
        }
    }

    private boolean generatePlannedServicesFromBothSides(TimeBlock timeBlock, List<BasicTrip> tripTemplates) {
        BasicTrip template1 = tripTemplates.get(0);
        BasicTrip template2 = tripTemplates.get(1);

        int roundTripSecs = template1.getDurationSecs() + glueType.getDefaultPlanSecs() + template2.getDurationSecs();
        int headway = roundTripSecs / (noOfTrains / 2);

        TimeBlock prevBlock = getPrevTimeBlock(timeBlock);
        if (prevBlock == null) {
            List<PlannedService> newServices = new ArrayList<>();
            try {

                userTransaction.begin();
                entityManager.joinTransaction();
                for (int i = 0; i < this.noOfTrains / 2; i++) {
                    PlannedService plannedService = createService(timeBlock, template1, template2, timeBlock.getStartSecs() + i * headway, glueType);
                    plannedService.setDescription(String.format(ConfigBean.getConfig().getFormat("serviceName").getFormat(), i + 1));

                    entityManager.persist(plannedService);
                    newServices.add(plannedService);
                }

                //int adjustSecs = getAdjustSecs(template1, roundTripSecs, headway);
                int adjustSecs = headway * 3 / 2 - (template2.getDurationSecs() + this.glueType.getDefaultPlanSecs()) % headway;
                PlannedServiceFilter filter2 = new PlannedServiceFilter();
                filter2.setValidFilter(true);
                filter2.setDayTypeFilter(this.getFilter().getDayType());

                int existing = this.plannedServiceController.getFacade().count(filter2);
                for (int i = 0; i < this.noOfTrains - this.noOfTrains / 2; i++) {
                    PlannedService plannedService = createService(timeBlock, template2, template1, timeBlock.getStartSecs() + adjustSecs + i * headway, glueType);
                    plannedService.setDescription(String.format(ConfigBean.getConfig().getFormat("serviceName").getFormat(), i + existing + 1));
                    plannedService.setCreating(true);
                    entityManager.persist(plannedService);
                    newServices.add(plannedService);
                }

                entityManager.merge(timeBlock);
                userTransaction.commit();
            } catch (SecurityException | IllegalStateException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SystemException ex) {
                try {
                    userTransaction.rollback();
                } catch (IllegalStateException | SecurityException | SystemException ex1) {
                    Logger.getLogger(TimeBlockController.class.getName()).log(Level.SEVERE, null, ex1);
                }
                Logger.getLogger(TimeBlockController.class.getName()).log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage(ex, uiText.get("Error_GenerateTimetableFailed"));
            } catch (NotSupportedException ex) {
                Logger.getLogger(TimeBlockController.class.getName()).log(Level.SEVERE, null, ex);
            }
            xmlMessageSender.sendServiceDutyChangeMsg(newServices, null);
            this.plannedServiceController.updateState();
        }
        JsfUtil.addSuccessMessage(uiText.get("TimetableGenerated"));

        return true;
    }

    private List<PlannedService> generatePlannedServicesFromOneSide(TimeBlock timeBlock, List<BasicTrip> tripTemplates) {
        BasicTrip template1 = tripTemplates.get(0);
        BasicTrip template2 = tripTemplates.get(1);

        List<PlannedService> template1Services;
        List<PlannedService> template2Services;
        PlannedServiceFilter filter = new PlannedServiceFilter();
        filter.setValidFilter(true);
        filter.setDayTypeFilter(timeBlock.getDayType());
        filter.setMinPlannedStopSecs(timeBlock.getStartSecs());
        filter.setMaxPlannedStopSecs(timeBlock.getEndSecs());
        filter.setPlannedStopObj(template1.getPlannedStartObj());
        filter.setSortByStopSecs(true);
        template1Services = this.plannedServiceController.getFacade().findAll(filter);
        filter.setPlannedStopObj(template2.getPlannedStartObj());
        template2Services = this.plannedServiceController.getFacade().findAll(filter);

        Iterator<PlannedService> it = template1Services.iterator();
        while (it.hasNext()) {
            PlannedService service = it.next();
            if (service.getNumberOfActions() > 3) {
                TimedTrip lastTrip = ((ActionRunTrip) (service.getTripAction(service.getNumberOfActions() - 2))).getRefTrip();
                if (lastTrip.getTripType().getTripTypeId() == 10) // withdrawal trip
                {
                    it.remove();
                }
            }
        }

        it = template2Services.iterator();
        while (it.hasNext()) {
            PlannedService service = it.next();
            if (service.getNumberOfActions() > 3) {
                TimedTrip lastTrip = ((ActionRunTrip) (service.getTripAction(service.getNumberOfActions() - 2))).getRefTrip();
                if (lastTrip.getTripType().getTripTypeId() == 10) // withdrawal trip
                {
                    it.remove();
                }
            }
        }

        List<PlannedService> existingServices = null;
        if (!template1Services.isEmpty() && !template2Services.isEmpty()) {
            JsfUtil.addErrorMessage(uiText.get("Error_notEndAtSameLocation"));
            return null;
        } else if (template1Services.isEmpty() && template2Services.isEmpty()) {
            if (reverseSelectedTemplates) {
                template1 = tripTemplates.get(1);
                template2 = tripTemplates.get(0);
            }

        } else if (template1Services.isEmpty()) {
            existingServices = template2Services;
            template1 = tripTemplates.get(1);
            template2 = tripTemplates.get(0);
        } else {
            existingServices = template1Services;
            template1 = tripTemplates.get(0);
            template2 = tripTemplates.get(1);
        }

        int roundTripSecs = template1.getDurationSecs() + glueType.getDefaultPlanSecs() + template2.getDurationSecs();
        int headway = roundTripSecs / noOfTrains;

        PlannedService firstService = (existingServices == null ? null : (existingServices.size() >= 1 ? existingServices.get(0) : null));
        int startSecs = timeBlock.getStartSecs();

        if (firstService != null && firstService.getNumberOfActions() > 3) {
            startSecs = this.glueType.getDefaultPlanSecs() + ((ActionRunTrip) (firstService.getTripAction(firstService.getNumberOfActions() - 2))).getRefTrip().getPlannedStopSecs();

        }
        List<PlannedService> services = new ArrayList<>();
        PlannedServiceFilter filter2 = new PlannedServiceFilter();
        filter2.setValidFilter(true);
        filter2.setDayTypeFilter(timeBlock.getDayType());
        int existing = this.plannedServiceController.getFacade().count(filter2);
        int created = 0;
        //startSecs for last(inserted or new) service
        //lastSecs can not be caculated from this timeblock's glue. We set it 0 to make the first one(pass arrivalTime >= lastDeparture) as the insert trip.
        //Another way is to find it from last block
        int lastSecs = 0;

        for (int i = 0; i < this.noOfTrains; i++) {
            if (i > 0) {
                startSecs = lastSecs + headway;
            }

            PlannedService insertService = getClosetServiceByEndTime(existingServices, startSecs, lastSecs, template1);

            if (insertService != null) {
                //System.out.println("lastSecs:" + lastSecs + "   startSecs:" + startSecs + "   found service:" + insertService.getDescription());
                int lastRunTripSeqNo = insertService.getNumberOfActions() - 2;
                int latestTripStartTime = ((ActionRunTrip) (insertService.getTripAction(lastRunTripSeqNo))).getRefTrip().getPlannedStopSecs();
                PlannedService plannedService = this.insertTrip(timeBlock, insertService, template1, template2, startSecs, latestTripStartTime, glueType);
                plannedService.setEditing(true);
                entityManager.merge(plannedService);
                services.add(plannedService);
                TripAction newAction = plannedService.getTripAction(lastRunTripSeqNo + 2);
                if (newAction instanceof ActionRunTrip) {
                    lastSecs = ((ActionRunTrip) newAction).getRefTrip().getPlannedStartSecs();
                } else {
                    lastSecs = startSecs;
                }
            } else {
                PlannedService plannedService = createService(timeBlock, template1, template2, startSecs, glueType);
                created++;
                plannedService.setDescription(String.format(ConfigBean.getConfig().getFormat("serviceName").getFormat(), existing + created));
                //System.out.println("lastSecs:" + lastSecs + "   startSecs:" + startSecs + "   new service:" + plannedService.getDescription());
                plannedService.setCreating(true);
                entityManager.persist(plannedService);
                services.add(plannedService);
                lastSecs = startSecs;
            }
        }

        this.plannedServiceController.updateState();
        JsfUtil.addSuccessMessage(uiText.get("TimetableGenerated"));

        return services;
    }

    //When generate trips, it need to adjust the start time to make the new trips not overlap at the other end with existing trips
    private boolean hasServicesFromPrevious(TimeBlock timeBlock, TTObject startObj) {
        int existing = 0;
        PlannedServiceFilter filter = new PlannedServiceFilter();
        filter.setValidFilter(true);
        filter.setDayTypeFilter(timeBlock.getDayType());
        filter.setMaxPlannedStartSecs(timeBlock.getStartSecs());
        filter.setMinPlannedStopSecs(timeBlock.getStartSecs());
        filter.setMaxPlannedStopSecs(timeBlock.getEndSecs());
        filter.setPlannedStopObj(startObj);
        existing = this.plannedServiceController.getFacade().count(filter);
        if (existing > 0) {
            return true;
        }
        return false;

    }

    private TimeBlock getPrevTimeBlock(TimeBlock timeBlock) {
        TimeBlockFilter filter = new TimeBlockFilter();
        filter.setDayType(timeBlock.getDayType());
        filter.setEndSecs(timeBlock.getStartSecs());
        return this.ejbFacade.findFirst(filter);
    }

    private PlannedService createService(TimeBlock timeBlock, BasicTrip template1, BasicTrip template2, int startSecs, ActionType glueType) {
        startSecs = startSecs - TRAIN_FORMATION_START_ACTION.getDefaultPlanSecs() - GLUE_WAIT_ACTION.getDefaultPlanSecs();
        PlannedService ps = plannedServiceController.createPlannedService(timeBlock.getDayType(), template1.getAreaObj());
        ps.setPlannedStartObj(template1.getPlannedStartObj());
        ps.setPlannedStartSecs(startSecs);
        ps.setTrainType(DEFAULT_TRAINTYPE);

        TripAction ta = createTrainFormationAction(ps, this.TRAIN_FORMATION_START_ACTION, template1.getPlannedStartObj(), 1);
        ps.addTripAction(ta);

        int tripStartSecs = startSecs + ta.getPlannedSecs();
        TripAction lastAction = ta;
        int seqNo = 1;
        boolean isTemplate1 = true;

        while (tripStartSecs < timeBlock.getEndSecs()) {
            seqNo++;
            TripAction glueAction = createGlueAction(ps, lastAction, seqNo, seqNo ==2? GLUE_WAIT_ACTION:glueType);
            ps.addTripAction(glueAction);
            lastAction = glueAction;
            tripStartSecs = tripStartSecs + lastAction.getPlannedSecs();

            TripAction runTripAction = createRuntripAction(ps, isTemplate1 ? template1 : template2, lastAction, tripStartSecs,
                    tripStartSecs + (isTemplate1 ? template1.getDurationSecs() : template2.getDurationSecs()), ++seqNo);
            isTemplate1 = !isTemplate1;

            ps.addTripAction(runTripAction);
            this.plannedServiceController.templateChanged(runTripAction);
            lastAction = runTripAction;
            tripStartSecs = tripStartSecs + runTripAction.getPlannedSecs();
        }

        //ps.setDurationSecs(lastAction.getTimeFromTripStart() + lastAction.getPlannedSecs());//needed, since only run trip action has valid time from trip start
        lastAction = createGlueAction(ps, lastAction, ++seqNo, GLUE_WAIT_ACTION);
        ps.addTripAction(lastAction);
        //ps.setDurationSecs(ps.getDurationSecs() + lastAction.getPlannedSecs());

        lastAction = createTrainFormationAction(ps, TRAIN_FORMATION_END_ACTION, lastAction.getTimetableObject(), ++seqNo);
        ps.addTripAction(lastAction);
        ps.updateActionTimes();

        lastAction = ps.getTripAction(ps.getNumberOfActions());
        ps.setDurationSecs(lastAction.getTimeFromTripStart() + lastAction.getPlannedSecs());
        ps.setPlannedStopSecs(startSecs + ps.getDurationSecs());
        ps.setPlannedStopObj(lastAction.getTimetableObject());
        ps.setPlannedState(DEFAULT_STATE);
        ps.setEditing(false);
        ps.setCreating(false);
        ps.setVersion(1);

        return ps;
    }

    private PlannedService insertTrip(TimeBlock timeBlock, PlannedService ps, BasicTrip template1, BasicTrip template2, int startSecs, int latestTripEndTime, ActionType glueType) {
        if (ps.getNumberOfActions() <= 3) {
            return ps;
        }

        int seqNo = ps.getNumberOfActions() - 2;
        TripAction lastAction = ps.getTripAction(seqNo);
        boolean isTemplate1 = true;
        boolean firstGlue = true;

        TripAction lastGlue = ps.getTripAction(ps.getNumberOfActions() - 1);
        TripAction lastFormation = ps.getTripAction(ps.getNumberOfActions());
        while (startSecs < timeBlock.getEndSecs()) {
            TripAction glueAction = createGlueAction(ps, lastAction, ++seqNo, glueType);
            if (firstGlue) {
                firstGlue = false;
                int firstGlueSecs = startSecs - latestTripEndTime;
                if (firstGlueSecs < glueType.getDefaultPlanSecs()) {
                    startSecs = startSecs + (glueType.getDefaultPlanSecs() - firstGlueSecs);
                    firstGlueSecs = glueType.getDefaultPlanSecs();
                    System.out.println("first glue changed:" + (glueType.getDefaultPlanSecs() - firstGlueSecs));
                }
                glueAction.setPlannedSecs(firstGlueSecs);
            } else {
                startSecs = startSecs + glueAction.getPlannedSecs();
            }

            ps.addTripAction(glueAction);

            // Create Action
            TripAction runTripAction = createRuntripAction(ps, isTemplate1 ? template1 : template2, glueAction, startSecs,
                    startSecs + (isTemplate1 ? template1.getDurationSecs() : template2.getDurationSecs()), ++seqNo);
            isTemplate1 = !isTemplate1;

            ps.addTripAction(runTripAction);
            this.plannedServiceController.templateChanged(runTripAction);
            lastAction = runTripAction;
            startSecs = startSecs + runTripAction.getPlannedSecs();
        }

        lastGlue.setSeqNo(++seqNo);
        lastGlue.setTimetableObject(lastAction.getTimetableObject2());
        lastFormation.setSeqNo(++seqNo);
        lastFormation.setTimetableObject(lastAction.getTimetableObject2());

        ps.updateActionTimes();
        ps.setDurationSecs(lastFormation.getTimeFromTripStart() + lastFormation.getPlannedSecs());
        ps.setPlannedStopSecs(ps.getPlannedStartSecs() + ps.getDurationSecs());
        ps.setPlannedStopObj(lastFormation.getTimetableObject());
        ps.setEditing(false);
        ps.setCreating(false);
        ps.increaseVersion();
        return ps;
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

    private TripAction createGlueAction(PlannedService ps, TripAction lastAction, Integer seqNo, ActionType glueType) {
        TTObject loc = lastAction.getTimetableObject();
        if (lastAction.hasSecondObject()) {
            loc = ((ActionRunTrip) lastAction).getTimetableObject2();
        }

        TripAction ta;

        ta = plannedServiceController.createNewTripAction(glueType, ps.getAreaObj(), ps.getDayTypeList(), loc, null, null);
        ta.setPlannedSecs(glueType.getDefaultPlanSecs());

        ta.setTrip(ps);
        ta.setSeqNo(seqNo);
        ta.setTimeFromTripStart(lastAction.getTimeFromTripStart() + lastAction.getPlannedSecs());

        ta.setEditing(false);
        ta.setCreating(false);
        return ta;
    }

    private TripAction createRuntripAction(PlannedService ps, BasicTrip template, TripAction lastAction, int startSecs, int stopSecs, int seqNo) {

        TripAction ta = plannedServiceController.createNewTripAction(RUN_COMMERCIAL_TRIP_ACTION, ps.getAreaObj(), ps.getDayTypeList(), template.getPlannedStartObj(),
                template.getPlannedStopObj(), template);
        ta.setTrip(ps);
        ta.setSeqNo(seqNo);
        ta.setTimeFromTripStart(lastAction.getTimeFromTripStart() + lastAction.getPlannedSecs());
        ta.setPlannedSecs(template.getDurationSecs());

        // Set TimedTrip variables
        ((TimedTrip) ((ActionRunTrip) ta).getRefTrip()).setPlannedStartSecs(startSecs);
        ((TimedTrip) ((ActionRunTrip) ta).getRefTrip()).setPlannedStopSecs(stopSecs);
        ((TimedTrip) ((ActionRunTrip) ta).getRefTrip()).setPlannedState(ps.getPlannedState());

        ((TimedTrip) ((ActionRunTrip) ta).getRefTrip()).updateActionTimes();
        ta.setEditing(false);
        ta.setCreating(false);

        return ta;
    }

    private boolean isOverlapping(Date start1, Date end1, Date start2, Date end2) {
        return start1.before(end2) && start2.before(end1);
    }

    @Override
    public String save(TimeBlock item) {

        if (item.getStartSecs() >= item.getEndSecs()) {
            JsfUtil.addErrorMessage(uiText.get("Error_invalidTimeSpan"));
            return null;
        }

        if (item.getDayType() == null) {
            JsfUtil.addErrorMessage(uiText.get("RequiredMessage_daytype"));
            return null;
        }

        //List<TimeBlock> timeBlocks = (List<TimeBlock>) this.getItems().getWrappedData();
        TimeBlockFilter filter = new TimeBlockFilter();
        filter.setDayType(item.getDayType());
        List<TimeBlock> timeBlocks = this.ejbFacade.findAll(filter);
        Iterator<TimeBlock> it = timeBlocks.iterator();
        boolean overlap = false;
        while (it.hasNext()) {
            TimeBlock timeBlock = it.next();

            if (!Objects.equals(timeBlock.getBlockId(), item.getBlockId()) && isOverlapping(item.getStartTime(), item.getEndTime(), timeBlock.getStartTime(), timeBlock.getEndTime())) {
                overlap = true;
                break;
            }
        }
        if (overlap) {
            JsfUtil.addErrorMessage(uiText.get("Error_timeblockOverlap"));
            return null;
        }

        if (item.isCreating()) {
            try {
                item.setEditing(false);
                item.setCreating(false);
                getFacade().create(item);
                JsfUtil.addSuccessMessage(uiText.get("TimeBlockCreated"));
                xmlMessageSender.sendTimeBlockMsg(item, Operation.CREATE);
            } catch (Exception e) {
                item.setEditing(true);
                item.setCreating(true);
                JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
                return null;
            }
        } else {
            try {
                item.setEditing(false);
                getFacade().edit(item);
                //recreateModel();
                JsfUtil.addSuccessMessage(uiText.get("TimeBlockUpdated"));
                xmlMessageSender.sendTimeBlockMsg(item, Operation.MODIFY);
            } catch (Exception e) {
                item.setEditing(true);
                JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
                return null;
            }
        }

        updateState(item);
        recreateModel();
        return null;
    }

    @Override
    public String destroy(TimeBlock item) {
        try {
            if (!item.isCreating()) {
                this.ejbFacade.remove(item);
                this.ejbFacade.evict(item);
                JsfUtil.addSuccessMessage(uiText.get("TimeBlockDeleted"));
                xmlMessageSender.sendTimeBlockMsg(item, Operation.DELETE);
            }

            recreateModel();

        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
        }
        return "List";
    }

    public TTObjectType getSelectedFromTTObjectType() {
        return selectedFromTTObjectType;
    }

    public void setSelectedFromTTObjectType(TTObjectType selectedFromTTObjectType) {
        this.selectedFromTTObjectType = selectedFromTTObjectType;
    }

    public TTObject getSelectedFromTTObject() {
        return selectedFromTTObject;
    }

    public void setSelectedFromTTObject(TTObject selectedFromTTObject) {
        this.selectedFromTTObject = selectedFromTTObject;
    }

    public TTObject getSelectedToTTObject() {
        return selectedToTTObject;
    }

    public void setSelectedToTTObject(TTObject selectedToTTObject) {
        this.selectedToTTObject = selectedToTTObject;
    }

    public TTObjectType getSelectedToTTObjectType() {
        return selectedToTTObjectType;
    }

    public void setSelectedToTTObjectType(TTObjectType selectedToTTObjectType) {
        this.selectedToTTObjectType = selectedToTTObjectType;
    }

    public void selectedFromTTObjectTypeChanged() {
        if (this.selectedFromTTObjectType != null && this.selectedFromTTObjectType.getTTObjects() != null && !this.selectedFromTTObjectType.getTTObjects().isEmpty()) {
            this.selectedFromTTObject = this.selectedFromTTObjectType.getTTObjects().get(0);
        } else {
            this.selectedFromTTObject = null;
        }

        filterChanged();
    }

    public void selectedToTTObjectTypeChanged() {
        if (this.selectedToTTObjectType != null && this.selectedToTTObjectType.getTTObjects() != null && !this.selectedToTTObjectType.getTTObjects().isEmpty()) {
            this.selectedToTTObject = this.selectedToTTObjectType.getTTObjects().get(0);
        } else {
            this.selectedToTTObject = null;
        }

        filterChanged();
    }

    public void filterChanged() {
        this.recreateTemplateModel();
        this.templatePagination = null;
    }

    protected void recreateTemplateModel() {
        templateItems = null;
    }

    public TripType getSubTripTypeFilter() {
        return subTripTypeFilter;
    }

    public void setSubTripTypeFilter(TripType subTripTypeFilter) {
        this.subTripTypeFilter = subTripTypeFilter;
    }

    public List<TTObjectType> getPossibleFromTTObjectTypes() {
        return ttobjectTypes;
    }

    public List<TTObjectType> getPossibleToTTObjectTypes() {
        return ttobjectTypes;
    }

    public List<TTObject> getPossibleFromLocations() {
        if (this.selectedFromTTObjectType != null && this.selectedFromTTObjectType.getTTObjTypeId() != 0) {
            return this.selectedFromTTObjectType.getTTObjects();
        } else {
            return null;
        }
    }

    public List<TTObject> getPossibleToLocations() {
        if (selectedToTTObjectType != null && this.selectedToTTObjectType.getTTObjTypeId() != 0) {
            return this.selectedToTTObjectType.getTTObjects();
        } else {
            return null;
        }
    }

    public TTObjectType getTtObjectTypeAll() {
        return ttObjectTypeAll;
    }

    public void setTtObjectTypeAll(TTObjectType ttObjectTypeAll) {
        this.ttObjectTypeAll = ttObjectTypeAll;
    }

    public void locationFilterChanged() {
        if (this.selectedFromTTObjectType != null && this.selectedFromTTObjectType.getTTObjTypeId() == 0) {
            this.selectedFromTTObject = null;
        }
        if (this.selectedToTTObjectType != null && this.selectedToTTObjectType.getTTObjTypeId() == 0) {
            this.selectedToTTObject = null;
        }
    }

    public Integer getNoOfTrains() {
        return noOfTrains;
    }

    public void setNoOfTrains(Integer noOfTrains) {
        this.noOfTrains = noOfTrains;
    }

    public DayType getDayTypeAll() {
        return new DayType(0, uiText.get("FilterAll"), uiText.get("FilterAll"));
    }

    public Schedule getSchedule() {
        //It is possible that the current filter is deleted by another session
        if (this.getFilter().getSchedule() == null) {
            ScheduleFilter filter = new ScheduleFilter();
            filter.setValid(true);
            this.ejbScheduleFacade.findFirst(filter);
            this.getFilter().setSchedule(this.ejbScheduleFacade.findFirst(filter));
            scheduleFilterChanged();
        }
        return this.getFilter().getSchedule();
    }

    public void setSchedule(Schedule schedule) {
        this.getFilter().setSchedule(schedule);
    }

    public int getSelectedTemplateSeqNo(BasicTrip basicTrip) {
        return getSelectedTripTemplates().indexOf(basicTrip) + 1;
    }

    public boolean isReverseSelectedTemplates() {
        return reverseSelectedTemplates;
    }

    public void setReverseSelectedTemplates(boolean reverseSelectedTemplates) {
        this.reverseSelectedTemplates = reverseSelectedTemplates;
    }

    public Map<String, Integer> getDays() {

        return days;
    }

    public Integer getSelectedTemplateView() {
        return selectedTemplateView;
    }

    public void setSelectedTemplateView(Integer selectedTemplateView) {
        this.selectedTemplateView = selectedTemplateView;
    }

    public String getStartLocation() {
        if (this.selectedTemplates.isEmpty()) {
            return null;
        } else if (!this.reverseSelectedTemplates) {
            return ((BasicTrip) (this.selectedTemplates.values().toArray()[0])).getPlannedStartObj().getDescription();
        } else {
            return ((BasicTrip) (this.selectedTemplates.values().toArray()[this.selectedTemplates.size() - 1])).getPlannedStartObj().getDescription();
        }
    }
}
