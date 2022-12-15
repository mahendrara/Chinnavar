package schedule.uiclasses;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;
import schedule.entities.DayType;
import schedule.entities.Schedule;
import schedule.entities.TTArea;
import schedule.entities.TTObject;
import schedule.entities.TTObjectType;
import schedule.sessions.ScheduleFacade;
import schedule.sessions.TTAreaFacade;
import schedule.uiclasses.util.UiText;

/**
 *
 * @author Jia Li
 */
@Named("longTermFilterController")
@SessionScoped
public class LongTermFilterController implements Serializable {

    private List<TTArea> areaFilter; //"All" is included compared with areas
    private List<TTArea> areas;
    private TTArea selectedArea;

    //private List<schedule.entities.Schedule> schedules;
    private schedule.entities.Schedule selectedSchedule;

    //private List<DayType> dayTypes;
    private DayType selectedDayType;
    //private Station selectedBranch;
    //private TTStation selectedStation;
    //private TTStation stationAll;
    private TTObjectType selectedStartTTObjectType;
    private TTObject selectedStartTTObject;
    private TTObjectType startTTObjectTypeAll;
    private List<TTObjectType> ttobjectTypes;

    //private Calendar dateFilter;
    //private ScheduledDay selectedScheduledDay;
    @Inject
    private TTAreaFacade ejbTTAreaFacade;
    @Inject
    private ScheduleFacade ejbScheduleFacade;
    //@Inject
    //private TTObjectFacade ejbTTObjectFacade;

    @Inject
    private PlannedTripController plannedTripController;
    @Inject
    private schedule.sessions.TTObjectTypeFacade ejbTTObjectTypeFacade;
    @Inject
    private UiText uiText;

    //private String allDescription;
    //@Inject
    //private ScheduledDayFacade ejbScheduledDayFacade;
    public LongTermFilterController() {
        areaFilter = null;
        areas = null;
        selectedArea = null;

        //schedules = null;
        selectedSchedule = null;

        selectedDayType = null;
        //selectedBranch = null;

    }

    @PostConstruct
    public void init() {
        startTTObjectTypeAll = new TTObjectType();
        startTTObjectTypeAll.setTTObjTypeId(0);
        startTTObjectTypeAll.setDescription(uiText.get("FilterAll"));

        initAreas();
        selectedScheduleChanged();
        initStartLocation();
    }

    private void initAreas() {
        areaFilter = ejbTTAreaFacade.findAll();
        areas = ejbTTAreaFacade.findAll();
        if (areaFilter.size() != 1) {

            TTArea all = new TTArea(0, uiText.get("FilterAll"));
            areaFilter.add(0, all);
            selectedArea = all;
//            plannedServiceController.setSelectedArea(all);
            plannedTripController.setSelectedArea(all);
        } else if (areas.size() > 0) {
            selectedArea = areas.get(0);
//            plannedServiceController.setSelectedArea(areas.get(0));
            plannedTripController.setSelectedArea(areas.get(0));
        }

    }

    public void selectedScheduleChanged() {
        if (selectedSchedule == null) {
            ScheduleFilter scheduleFilter = new ScheduleFilter();
            scheduleFilter.setValid(Boolean.TRUE);
            List<Schedule> schedules = ejbScheduleFacade.findAll(scheduleFilter);
            if (schedules != null && schedules.size() > 0) {
                selectedSchedule = schedules.get(0);
            } else {
                selectedSchedule = null;
            }
        }

        initDayTypes();
    }

    private void initDayTypes() {
        if (selectedSchedule != null) {

            List<DayType> dayTypes = selectedSchedule.getDayTypes();
            if (dayTypes.size() > 0) {
                selectedDayType = dayTypes.get(0);
//                plannedServiceController.setSelectedDayType(selectedDayType);
//                plannedServiceController.selectedDayTypeChanged();
            } else {
                selectedDayType = null;
            }
        } else {
            selectedDayType = null;
        }
        plannedTripController.setSelectedDayType(selectedDayType);
        plannedTripController.selectedDayTypeChanged();
    }

    private void initStartLocation() {
        //selectedStartTTObjectType = this.ejbTTObjectTypeFacade.findAll().get(0);
        TTObjectTypeFilter filter = new TTObjectTypeFilter();
        filter.setStartTTObjectTypeFilter();
        this.ttobjectTypes = this.ejbTTObjectTypeFacade.findAll(filter);
        this.selectedStartTTObjectType = startTTObjectTypeAll;
        selectedStartTTObject = new TTArea(0, "");
        plannedTripController.setSelectedStartTTObject(selectedStartTTObject);
        plannedTripController.selectedStartTTObjectChanged();
    }

    /*public Date getDateFilter() {
     if (selectedScheduledDay != null) {
     return selectedScheduledDay.getDateOfDay();
     } else {
     return null;
     }
     }

     public Calendar getCalendarFilter() {
     Calendar calendar = Calendar.getInstance();
     calendar.setTime(getDateFilter());
     return calendar;
     }

     public void setDateFilter(Date filterDateAsDate) {
     if (filterDateAsDate != null) {
     Calendar calendar = Calendar.getInstance();
     calendar.setTime(filterDateAsDate);
     this.selectedScheduledDay = ejbScheduledDayFacade.find(calendar);
     }

     //this.dayCodeFilter = Schedule.convertToDayCode(this.dateFilter);
     }
     */
    public TTArea getSelectedArea() {
        return selectedArea;
    }

    public void setSelectedArea(TTArea selectedArea) {
        //if (this.selectedArea != selectedArea) {
        this.selectedArea = selectedArea;
        /*plannedServiceController.setSelectedArea(selectedArea);
         //plannedServiceController.selectedAreaChanged();

         plannedTripController.setSelectedArea(selectedArea);
         //plannedTripController.selectedAreaChanged();*/
        //}
    }

    /*public void selectedAreaChanged(ValueChangeEvent e) {
        this.selectedArea = (TTArea) (e.getNewValue());
//        plannedServiceController.setSelectedArea(selectedArea);
//        plannedServiceController.selectedAreaChanged();

        plannedTripController.setSelectedArea(selectedArea);
        plannedTripController.selectedAreaChanged();
    }*/
    public void selectedAreaChanged() {
        plannedTripController.setSelectedArea(selectedArea);
        plannedTripController.selectedAreaChanged();
    }

    public List<TTArea> getAreas() {
        return areas;
    }

    /*public void setAreas(List<TTArea> areas) {
     this.areas = areas;
     }*/
    public List<TTArea> getAreaFilter() {
        return areaFilter;
    }

    /*public ScheduledDay getSelectedScheduledDay() {
     return selectedScheduledDay;
     }
     public void setSelectedScheduledDay(ScheduledDay selectedScheduledDay) {
     this.selectedScheduledDay = selectedScheduledDay;
     }*/
    public List<Schedule> getSchedules() {
        ScheduleFilter scheduleFilter = new ScheduleFilter();
        scheduleFilter.setValid(Boolean.TRUE);
        List<Schedule> schedules = ejbScheduleFacade.findAll(scheduleFilter);

        if (schedules == null) {
            schedules = new ArrayList<>();
        }
        return schedules;
    }

    public Schedule getSelectedSchedule() {
        if(selectedSchedule==null) {
            selectedScheduleChanged();
        }

        return selectedSchedule;
    }

    public void setSelectedSchedule(Schedule selectedSchedule) {
        //if (this.selectedSchedule.getScheduleid() != selectedSchedule.getScheduleid()) {
        this.selectedSchedule = selectedSchedule;
        // for ajax usage
        /*initDayTypes();
         plannedServiceController.setSelectedDayType(selectedDayType);
         plannedServiceController.selectedDayTypeChanged();
         plannedTripController.setSelectedDayType(selectedDayType);
         plannedTripController.selectedDayTypeChanged();*/
        //}
    }

    /*public void selectedScheduleChanged(ValueChangeEvent e) {
        this.selectedSchedule = (Schedule) (e.getNewValue());
        initDayTypes();
//        plannedServiceController.setSelectedDayType(selectedDayType);
//        plannedServiceController.selectedDayTypeChanged();
        plannedTripController.setSelectedDayType(selectedDayType);
        plannedTripController.selectedDayTypeChanged();
    }*/
    public List<DayType> getDayTypes() {
        if (selectedSchedule != null) {
            return selectedSchedule.getDayTypes();
        } else {
            return null;
        }
    }

    /*public void setDayTypes(List<DayType> dayTypes) {
     //dayTypes are initialized by schedule change
     this.dayTypes = dayTypes;
     }*/
    public DayType getSelectedDayType() {
        return this.selectedDayType;
    }

    public void setSelectedDayType(DayType selectedDayType) {
        if (selectedSchedule != null && selectedSchedule.FindDayType(selectedDayType.getDayTypeId()) != null) {
            this.selectedDayType = selectedDayType;
            /*plannedServiceController.setSelectedDayType(selectedDayType);
             //plannedServiceController.selectedDayTypeChanged();//This is needed when we put DayType to non-ajax update
             plannedTripController.setSelectedDayType(selectedDayType);
             //plannedTripController.selectedDayTypeChanged();//This is needed when we put DayType to non-ajax update*/
        }
    }

    /*public void selectedDayTypeChanged(ValueChangeEvent e) {
        DayType selectedDayType1 = (DayType) (e.getNewValue());
        if (selectedSchedule != null && selectedSchedule.FindDayType(selectedDayType1.getDayTypeId()) != null) {
            this.selectedDayType = selectedDayType1;
//            plannedServiceController.setSelectedDayType(selectedDayType1);
//            plannedServiceController.selectedDayTypeChanged();//This is needed when we put DayType to non-ajax update
            plannedTripController.setSelectedDayType(selectedDayType1);
            plannedTripController.selectedDayTypeChanged();//This is needed when we put DayType to non-ajax update
        }
    }*/
    public void selectedDayTypeChanged() {

        plannedTripController.setSelectedDayType(selectedDayType);
        plannedTripController.selectedDayTypeChanged();//This is needed when we put DayType to non-ajax update
    }


    /*public List<Station> getBranchFilter() {
     List<Station> branchFilter = getBranches();
     Station all = new Station(0, allDescription);
     this.selectedBranch = all;
     branchFilter.add(0, all);

     return branchFilter;
     }

     public List<Station> getBranches() {
     List<Station> branches = new ArrayList();
     if (this.selectedArea == null || this.selectedArea.getTTObjId() == 0) {
     for (TTArea area : this.areas) {

     if (area.getChildCount() > 0) {
     List<TTObject> children = area.getChildObjects();
     for (TTObject obj : children) {
     if (obj.getObjectType() == TTObject.TTObjectType.BRANCH_STATION) {
     branches.add((Station) obj);
     }
     }
     }
     }
     } else {

     if (this.selectedArea.getChildCount() > 0) {
     List<TTObject> children = this.selectedArea.getChildObjects();
     for (TTObject obj : children) {
     if (obj.getObjectType() == TTObject.TTObjectType.BRANCH_STATION) {
     branches.add((Station) obj);
     }
     }
     }
     }
     return branches;
     }*/

 /*public Station getSelectedBranch() {
     return selectedBranch;
     }

     public void setSelectedBranch(Station selectedBranch) {
     if (this.selectedBranch != selectedBranch) {
     this.selectedBranch = selectedBranch;

     plannedServiceController.setSelectedBranch(selectedBranch);
     plannedServiceController.selectedBranchChanged();

     plannedTripController.setSelectedBranch(selectedBranch);
     plannedTripController.selectedBranchChanged();
     }
     }*/
 /*public List<TTStation> getStationFilter() {
     List<TTStation> stationFilter = new ArrayList();

     //TTStation all = new TTStation(0, uiText.get("FilterAll"));
     this.selectedStation = stationAll;

     //stationFilter.add(0, stationAll);
     if (selectedArea == null || this.selectedArea.getTTObjId() == 0) {
     for (TTArea area : areas) {
     List<TTObject> children = area.getChildObjects();
     if (children != null && children.size() > 0) {
     for (TTObject obj : children) {
     if (obj.getTTObjectType().getTTObjTypeId() == TTObjectType.TTObjectTypeEnum.STATION.getDBCode() && obj.getChildCount() > 0) {
     stationFilter.add((TTStation) obj);
     }
     }
     }
     }

     } else {
     List<TTObject> children = this.selectedArea.getChildObjects();
     if (children != null && children.size() > 0) {
     for (TTObject obj : children) {
     if (obj.getTTObjectType().getTTObjTypeId() == TTObjectType.TTObjectTypeEnum.STATION.getDBCode() && obj.getChildCount() > 0) {
     stationFilter.add((TTStation) obj);
     }
     }
     }
     }

     //        if (this.selectedBranch == null || this.selectedBranch.getTTObjId() == 0) {
     //            if (this.selectedArea.getTTObjId() == 0) {
     //                List<Station> branches = getBranches();
     //                if (branches != null) {
     //                    for (Station branch : branches) {
     //                        List<TTObject> children = branch.getChildObjects();
     //                        if (children != null && children.size() > 0) {
     //                            for (TTObject obj : children) {
     //                                if (obj.getObjectType() == TTObject.TTObjectType.STATION) {
     //                                    stationFilter.add((Station) obj);
     //                                }
     //                            }
     //                        }
     //                    }
     //                }
     //            }
     //        } else {
     //
     //            if (this.selectedBranch.getChildCount() > 0) {
     //                List<TTObject> children = this.selectedBranch.getChildObjects();
     //                for (TTObject obj : children) {
     //                    if (obj.getObjectType() == TTObject.TTObjectType.STATION) {//&& obj.getChildCount() > 0??
     //                        stationFilter.add((Station) obj);
     //                    }
     //                }
     //            }
     //        }
     return stationFilter;
     }

     public TTStation getSelectedStation() {
     return selectedStation;
     }

     public void setSelectedStation(TTStation selectedStation) {
     //if (this.selectedStation != selectedStation) {
     this.selectedStation = selectedStation;

     plannedServiceController.setSelectedStation(selectedStation);
     plannedServiceController.selectedStationChanged();

     plannedTripController.setSelectedStation(selectedStation);
     plannedTripController.selectedStationChanged();
     //}

     }

     public TTStation getStationAll() {
     stationAll.setDescription(uiText.get("FilterAll"));
     return this.stationAll;
     }*/
    public List<TTObjectType> getPossibleStartTTObjectTypes() {
        return this.ttobjectTypes;
    }

    public TTObjectType getSelectedStartTTObjectType() {
        return selectedStartTTObjectType;
    }

    public void setSelectedStartTTObjectType(TTObjectType selectedStartTTObjectType) {
        this.selectedStartTTObjectType = selectedStartTTObjectType;
    }

    public void selectedStartTTObjectTypeChanged(ValueChangeEvent e) {
        this.selectedStartTTObjectType = (TTObjectType) (e.getNewValue());
        if (selectedStartTTObjectType != null && selectedStartTTObjectType.getTTObjTypeId() != 0) {
            if (selectedStartTTObjectType.getTTObjects().isEmpty() == false) {
                selectedStartTTObject = selectedStartTTObjectType.getTTObjects().get(0);
            } else {
                selectedStartTTObject = null;
            }
        } else {
            //used when all is selected
            selectedStartTTObject = new TTArea(0, "");
        }

        plannedTripController.setSelectedStartTTObject(selectedStartTTObject);
        plannedTripController.selectedStartTTObjectChanged();
    }

    public void selectedStartTTObjectTypeChanged() {
        if (selectedStartTTObjectType != null && selectedStartTTObjectType.getTTObjTypeId() != 0) {
            if (selectedStartTTObjectType.getTTObjects().isEmpty() == false) {
                selectedStartTTObject = selectedStartTTObjectType.getTTObjects().get(0);
            } else {
                selectedStartTTObject = null;
            }
        } else {
            //used when all is selected
            selectedStartTTObject = new TTArea(0, "");
        }

        plannedTripController.setSelectedStartTTObject(selectedStartTTObject);
        plannedTripController.selectedStartTTObjectChanged();
    }

    /*public void selectedStartTTObjectChanged(ValueChangeEvent e) {
        if (!ttobjectChanged) {
            this.selectedStartTTObject = (TTObject) (e.getNewValue());
            plannedTripController.setSelectedStartTTObject(selectedStartTTObject);
            plannedTripController.selectedStartTTObjectChanged();
        }
    }*/
    public void selectedStartTTObjectChanged() {
        plannedTripController.setSelectedStartTTObject(selectedStartTTObject);
        plannedTripController.selectedStartTTObjectChanged();
    }

    public TTObject getSelectedStartTTObject() {
        return selectedStartTTObject;
    }

    public void setSelectedStartTTObject(TTObject selectedStartTTObject) {
        this.selectedStartTTObject = selectedStartTTObject;
    }

    public List<TTObject> getPossibleStartTTObjects() {
        if (isPossibleStartTTObjectsAvailable()) {
            return this.selectedStartTTObjectType.getTTObjects();
        } else {
            return null;
        }
    }

    public TTObjectType getStartTTObjectTypeAll() {
        startTTObjectTypeAll.setDescription(uiText.get("FilterAll"));
        return startTTObjectTypeAll;
    }

    public boolean isPossibleStartTTObjectsAvailable() {
        return selectedStartTTObjectType != null && selectedStartTTObjectType.getTTObjects() != null && (selectedStartTTObjectType.getTTObjects().isEmpty() == false);
    }
}
