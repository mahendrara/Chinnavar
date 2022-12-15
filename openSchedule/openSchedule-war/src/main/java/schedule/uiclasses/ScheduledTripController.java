package schedule.uiclasses;

import schedule.entities.ActionTrainMoving;
import schedule.entities.DayType;
import schedule.entities.TTArea;
import schedule.entities.TTObject;
import schedule.entities.TripAction;
import java.util.List;

import schedule.uiclasses.util.JsfUtil;
import schedule.uiclasses.util.PaginationHelper;
import schedule.sessions.ScheduledTripFacade;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import schedule.entities.ScheduledDay;
import schedule.entities.ScheduledTrip;
import schedule.entities.TrainType;
import schedule.uiclasses.util.UiText;

// @TODO: Needs to extend BaseController
@SuppressWarnings("unchecked")
@Named("scheduledTripController")
@SessionScoped
public class ScheduledTripController implements Serializable {

    private ScheduledTrip current;
    private DataModel items = null;
    @Inject
    private schedule.sessions.ScheduledTripFacade ejbFacade;
    @Inject
    private schedule.sessions.ScheduledDayFacade ejbScheduledDayFacade;
    @Inject
    private LanguageBean languageBean;
    @Inject
    private UiText uiText;
    //@Inject
    //private schedule.uiclasses.FilterController filterController;
    //@Inject
    //private schedule.sessions.TTAreaFacade ejbTTAreaFacade;

    private PaginationHelper pagination;
    private int selectedItemIndex;
    private DayType selectedDayType;
    private TrainType selectedTrainType;
    //private List<DayType> dayTypes;

    //private List<TTArea> areaFilterList = null; //"All" is included compared with areas
    //private List<TTArea> areas = null;
    private TTArea selectedArea;
    //private Station selectedBranch;
    private TTObject selectedStartTTObject;

    private Calendar dateFilter;
    private ScheduledDay selectedScheduledDay;

    private boolean utcTime;

    private final int itemsPerPage = 50;

    private StreamedContent tripChart = null;

    public ScheduledTripController() {
    }

    @PostConstruct
    public void init() {
        /*if (areaFilterList == null) {

         areaFilterList = ejbTTAreaFacade.findAll();
         if (areaFilterList.size() != 1) {
         areaFilterList.add(0, new TTArea(0, "All"));
         }
         }

         if (areas == null) {
         areas = ejbTTAreaFacade.findAll();
         }
         if (selectedArea == null && areaFilterList.size() > 0) {
         selectedArea = areaFilterList.get(0);
         }
         initDayTypes();*/

        /*if (filterController.getAreaFilter().size() > 0) {
         selectedArea = filterController.getAreaFilter().get(0);
         }*/
        this.dateFilter = Calendar.getInstance();
    }

    public void dateFilterChanged(Calendar dateFilter) {
        //if (dateFilter.getTime().equals(this.dateFilter.getTime()) == false) {
        this.dateFilter = dateFilter;

        this.selectedScheduledDay = this.ejbScheduledDayFacade.find(this.dateFilter);
        recreateModel();
        //items = getPagination().createPageDataModel();
        //}
    }
    /*private void initDayTypes() {
     if (usingSchedule != null) {
     dayTypes = usingSchedule.getDayTypes();

     if (dayTypes.size() > 0) {
     for (int i = 0; i < dayTypes.size(); i++) {
     if (dayTypes.get(i).getAbbr().equals("W")) {
     selectedDayType = dayTypes.get(i);
     break;
     }
     }
     if (selectedDayType == null) {
     selectedDayType = dayTypes.get(0);
     }
     }
     }
     recreateModel();
     }*/

    public boolean isUtcTime() {
        return utcTime;
    }

    public void setUtcTime(boolean utcTime) {
        this.utcTime = utcTime;
    }

    public TTArea getSelectedArea() {
        return selectedArea;
    }

    public void setSelectedArea(TTArea selectedArea) {
        this.selectedArea = selectedArea;
    }

    public void selectedAreaChanged() {
        recreateModel();
//        if (this.pagination != null) {
//            this.pagination.setFirstPage();
//        }
    }

    /*public List<TTArea> getAreas() {
     return areas;
     }

     public void setAreas(List<TTArea> areas) {
     this.areas = areas;
     }*/
    public void setSelectedTrainType(TrainType selectedTrainType) {
        this.selectedTrainType = selectedTrainType;
    }

    public void selectedTrainTypeChanged() {
        recreateModel();
//        if (this.pagination != null) {
//            this.pagination.setFirstPage();
//        }
    }

    public DayType getSelectedDayType() {
        return selectedDayType;
    }

    public void setSelectedDayType(DayType selectedDayType) {
        this.selectedDayType = selectedDayType;
    }

    public void selectedDayTypeChanged() {
        recreateModel();
//        if (this.pagination != null) {
//            this.pagination.setFirstPage();
//        }
    }

    /*public Station getSelectedBranch() {
     return selectedBranch;
     }

     public void setSelectedBranch(Station selectedBranch) {
     this.selectedBranch = selectedBranch;
     }*/
    public void selectedBranchChanged() {
        recreateModel();
//        if (this.pagination != null) {
//            this.pagination.setFirstPage();
//        }
    }

    public TTObject getSelectedStartTTObject() {
        return selectedStartTTObject;
    }

    public void setSelectedStartTTObject(TTObject selectedStartTTObject) {
        this.selectedStartTTObject = selectedStartTTObject;
    }

    public void selectedStartTTObjectChanged() {
        recreateModel();
        this.pagination = null;
//        if (this.pagination != null) {
//            this.pagination.setFirstPage();
//        }
    }

    /*public Station getStationTimetableFilter() {
     return stationTimetableFilter;
     }

     public void setStationTimetableFilter(Station stationTimetableFilter) {
     this.stationTimetableFilter = stationTimetableFilter;
     }*/
    /*public void changeDayTypeFilter() {
     recreateModel();
     items = getPagination().createPageDataModel();
     }*/
    /*public void changeDayTypeFilter(ValueChangeEvent event) {
     filterController.setSelectedDayType((DayType)event.getNewValue());
     recreateModel();
     //items = getPagination().createPageDataModel();
     }*/
    //used for local area filer
     /*public void changeAreaFilter(ValueChangeEvent event) {
     TTArea area = (TTArea)event.getNewValue();
     if (area == null) {
     area = new TTArea(0, "");
     }

     this.selectedArea = area;
        
     recreateModel();
     //items = getPagination().createPageDataModel();
     }*/
    /*public void changeScheduleFilter(ValueChangeEvent event) {
     filterController.setSelectedSchedule((Schedule)event.getNewValue());
     recreateModel();
     //items = getPagination().createPageDataModel();
     }*/
    /*public List<DayType> getDayTypes() {
     return filterController.getSelectedSchedule().getDayTypes();
     //return this.dayTypes;
     }*/
    /*public void changeBranchFilter() {
     areaFilterController.changeBranchFilter();
     recreateModel();
     }
     public void changeStationFilter() {
     areaFilterController.changeStationFilter();
     //recreateModel();
     }*/
    /*    
     public Station getStartBranchFilter() {
     return areaFilterController.getBranchStartInstance();
     }*/
    /*public void daycodeChanged(ValueChangeEvent e) {
     int newCode = Integer.parseInt(e.getNewValue().toString());
     setSelectedDayTypeID(newCode);
     }*/
    public ScheduledTrip getSelected() {
        if (current == null) {
            current = new ScheduledTrip();
            selectedItemIndex = -1;
        }
        return current;
    }

    public StreamedContent getTrainGraph() {
        //Chart
        try {
            ScheduledTrip tripToDraw = getSelected();

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

    private DefaultCategoryDataset createTripDataset(ScheduledTrip tripToDraw) {

        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        List<TripAction> allActions = tripToDraw.getTripActions();

        for (TripAction action : allActions) {
            if (action.getClass() != ActionTrainMoving.class) {
                if (action.getTimetableObject()
                        != null) {
                    TTObject object = action.getTimetableObject();
                    dataset.addValue(action.getTimeFromTripStart(), "PLANNED", action.getMainObjectName());
                }
            }
        }
        return dataset;
    }
    /*private XYDataset createTripDataset(){
        
     TimeSeries oneAndOnlyTrip 
     /*this.addedValidSchedules = 0;

     TaskSeries s1 = new TaskSeries("Scheduled");
     List<Schedule> all = getFacade().findAll();

     Iterator<Schedule> iteSchedules = all.iterator();

     while(iteSchedules.hasNext())
     {
     Schedule cur = iteSchedules.next();
     if(cur.getValid())
     {
     SimpleTimePeriod timespan = new SimpleTimePeriod(cur.getStarttime(),cur.getEndtime());
     s1.add(new Task(cur.getDescription(),timespan));
     this.addedValidSchedules++;
     }
     }
     TaskSeriesCollection collection = new TaskSeriesCollection();
     collection.add(s1);
     //collection.add(s2);
        
     return collection;
     }
     */

    private ScheduledTripFacade getFacade() {
        return ejbFacade;
    }

    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(this.itemsPerPage) {

                @Override
                public int getItemsCount() {
                    ScheduledTripFilter filter = new ScheduledTripFilter();
                    filter.setScheduledDayFilter(selectedScheduledDay);
                    if (selectedArea != null && selectedArea.getTTObjId() != 0) {
                        filter.setTTAreaFilter(selectedArea);
                    }
                    if (selectedTrainType != null && selectedTrainType.getTrainTypeId() != 0) {
                        filter.setTrainTypeFilter(selectedTrainType);
                    }
                    if (selectedStartTTObject == null || selectedStartTTObject.getTTObjId() != 0) {
                        filter.setSelectedStartTTObjectFilter(selectedStartTTObject);
                    }
                    filter.setValidFilter(Boolean.TRUE);
                    return getFacade().count(filter);
                }

                @Override
                public DataModel createPageDataModel() {
                    ScheduledTripFilter filter = new ScheduledTripFilter();
                    filter.setScheduledDayFilter(selectedScheduledDay);
                    if (selectedArea != null && selectedArea.getTTObjId() != 0) {
                        filter.setTTAreaFilter(selectedArea);
                    }

                    if (selectedTrainType != null && selectedTrainType.getTrainTypeId() != 0) {
                        filter.setTrainTypeFilter(selectedTrainType);
                    }
                    if (selectedStartTTObject == null || selectedStartTTObject.getTTObjId() != 0) {
                        filter.setSelectedStartTTObjectFilter(selectedStartTTObject);
                    }
                    //filter.setUseOrder(true);
                    filter.setValidFilter(Boolean.TRUE);
                    return new ListDataModel(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}, filter));
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        recreateModel();
        return "List";
    }

    public String prepareGraph() {
        return "ViewGraph";
    }

    public String prepareView() {
        current = (ScheduledTrip) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return null;//"View";
    }

    public String backToDetailsView() {
        return "View";
    }

    public String prepareCreate() {
        current = new ScheduledTrip();
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            getFacade().create(current);
            JsfUtil.addSuccessMessage(uiText.get("PlannedServiceCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        current = (ScheduledTrip) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String activateEdit(ScheduledTrip plannedTrip) {
        current = plannedTrip;
        plannedTrip.setEditing(true);
        return null;
    }

    public String update() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(uiText.get("PlannedServiceUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
            return null;
        }
    }

    public String save(ScheduledTrip plannedTrip) {
        try {
            if (plannedTrip.isCreating()) {
                create();
            } else {
                getFacade().edit(plannedTrip);
                plannedTrip.setEditing(false);
                recreateModel();
                JsfUtil.addSuccessMessage(uiText.get("TripUpdated"));
            }
            return null;
            //return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
            return null;
        }
    }

    public String cancel(ScheduledTrip plannedTrip) {
        try {
            plannedTrip.setEditing(false);
            recreateModel();
            return "List";
            //return "View";
        } catch (Exception e) {
            return null;
        }
    }

    public String destroy() {
        current = (ScheduledTrip) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreateModel();
        return "List";
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

    private void performDestroy() {
        try {
            getFacade().remove(current);
            JsfUtil.addSuccessMessage(uiText.get("PlannedServiceDeleted"));
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
        /*if (selectedArea == null && filterController.getAreaFilter().size() > 0) {
         selectedArea = filterController.getAreaFilter().get(0);
         }*/

        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }

    /*public List<TTArea> getAreaFilter() {
     return areaFilterList;
     }

     public void changeAreaFilter() {
     recreateModel();
     items = getPagination().createPageDataModel();
     }*/

    /*public int getStationItemsSearchCount() {
     if (this.items != null) {
     return this.items.getRowCount();
     }
     return 0;

     }*/
    /*public DataModel getStationItems() {
        
     // Temporary code tester...
     //        if(this.stationTimetableFilter==null)
     //        {
     //            this.stationTimetableFilter = (Station) ejbTTObjectFacade.find(new Integer(29)); // Liverpool street... 
     //        }
     this.stationTimetableFilter = areaFilterController.getStationFilterInstance();
                
     List<StationItem> retList = new ArrayList<>();
        
     if(this.items != null && this.stationTimetableFilter != null) {
            
     for(Object item : this.items) {
                
     ScheduledTrip trip = (ScheduledTrip) item;
     TripAction match = trip.FindFirstActionInStation(stationTimetableFilter);
     if(match!=null)
     {
     StationItem stationEvent = new StationItem(stationTimetableFilter);
     stationEvent.setTrip(trip);
     stationEvent.setActionOnStation(match);
     stationEvent.updateTimes();
     retList.add(stationEvent);
     }
     //                else
     //                {
     //                     StationItem stationEvent = new StationItem(stationTimetableFilter);
     //                     stationEvent.setTrip(trip);
     //                     retList.add(stationEvent);
     //                }
     }
     }
     Comparator<StationItem> sorter = new Comparator<StationItem>() {
            
     public int compare(StationItem o1,StationItem o2) {
     return o1.getStartSecs() - o2.getStartSecs();
     }
     };
     Collections.sort(retList,sorter);
        
     return new ListDataModel(retList);
     }*/

    /*public DataModel getAreaItems() {
     if (areaItems == null) {
     areaItems = new ListDataModel(ejbTTAreaFacade.findAll());
     }
     return areaItems;
     }*/
    /*public Date calcActionStartHour(TripAction action) {
        return calcActionStartHour(current, action);
    }

    public Date calcActionStartHour(ScheduledTrip plannedTrip, TripAction action) {
        if (plannedTrip != null && plannedTrip.getPlannedStartSecs() != null) {
            int totalSecs = plannedTrip.getActionStartSecs(action);
            if (plannedTrip.getUtcTimes() == false) {
                totalSecs -= action.getTimetableObject().getUtcDiff();
            }
            return new Date(totalSecs * 1000L);
        }
        return null;
    }

    public Date calcActionEndHour(TripAction action) {
        return calcActionEndHour(current, action);
    }

    public Date calcActionEndHour(ScheduledTrip plannedTrip, TripAction action) {
        if (plannedTrip != null && plannedTrip.getPlannedStopSecs() != null) {
            int totalSecs = plannedTrip.getActionStopSecs(action);
            if (plannedTrip.getUtcTimes() == false) {
                totalSecs -= action.getTimetableObject().getUtcDiff();
            }
            return new Date(totalSecs * 1000L);
        }
        return null;
    }*/

    /*public Date calcStationStartHour(StationItem stationItem) {

     return new Date(stationItem.getStartSecs() * 1000);
     }

     public Date calcStationEndHourString(StationItem stationItem) {

     return new Date(stationItem.getEndSecs() * 1000);
     }*/
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

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), true);

    }
}
