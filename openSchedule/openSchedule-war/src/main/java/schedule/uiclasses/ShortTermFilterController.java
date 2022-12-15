/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.uiclasses;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;
import schedule.entities.TTArea;
import schedule.entities.TTObject;
import schedule.entities.TTObjectType;
import schedule.entities.TrainType;
import schedule.sessions.TTAreaFacade;
import schedule.sessions.TrainTypeFacade;
import schedule.uiclasses.util.UiText;

/**
 *
 * @author Jia Li
 */
@Named("shortTermFilterController")
@SessionScoped
public class ShortTermFilterController implements Serializable {

    private List<TTArea> areaFilter; //"All" is included compared with areas
    private List<TTArea> areas;
    private TTArea selectedArea;

    //private TTStation selectedStation;
    private Calendar dateFilter;
    //private ScheduledDay selectedScheduledDay;

    private List<TrainType> trainTypeFilter = null; //"All" is included compared with areas
    //selected Area Filter
    private TrainType selectedTrainType;
    //private List<TrainType> trainTypes = null;
    //private TTStation stationAll;
    private TTObjectType selectedStartTTObjectType;
    private TTObject selectedStartTTObject;
    private TTObjectType startTTObjectTypeAll;
    private List<TTObjectType> ttobjectTypes;

    @Inject
    private TTAreaFacade ejbTTAreaFacade;
//    @Inject
//    private ScheduledServiceController scheduledServiceController;
    @Inject
    private ScheduledTripController scheduledTripController;
    @Inject
    private TrainTypeFacade ejbTrainTypeFacade;
    @Inject
    private schedule.sessions.TTObjectTypeFacade ejbTTObjectTypeFacade;
    @Inject
    private UiText uiText;
    
    private boolean ttobjectChanged = false;

    public ShortTermFilterController() {
        areaFilter = null;
        areas = null;
        selectedArea = null;
        //selectedStation = null;
    }

    @PostConstruct
    public void init() {
        /*stationAll = new TTStation();
         stationAll.setTTObjId(0);
         stationAll.setDescription(uiText.get("FilterAll"));
         this.selectedStation = stationAll;*/

        startTTObjectTypeAll = new TTObjectType();
        startTTObjectTypeAll.setTTObjTypeId(0);
        startTTObjectTypeAll.setDescription(uiText.get("FilterAll"));

        initAreas();
        initTrainTypes();
        initDate();
        initStartLocation();
    }

    private void initAreas() {
        areaFilter = ejbTTAreaFacade.findAll();
        areas = ejbTTAreaFacade.findAll();
        if (areaFilter.size() != 1) {
            TTArea all = new TTArea(0, uiText.get("FilterAll"));
            areaFilter.add(0, all);
            selectedArea = all;
//            scheduledServiceController.setSelectedArea(all);
            scheduledTripController.setSelectedArea(all);
        } else if (areas.size() > 0) {
            selectedArea = areas.get(0);
//            scheduledServiceController.setSelectedArea(areas.get(0));
            scheduledTripController.setSelectedArea(areas.get(0));
        }

    }

    private void initDate() {
        this.dateFilter = Calendar.getInstance();
//        this.scheduledServiceController.dateFilterChanged(dateFilter);
        this.scheduledTripController.dateFilterChanged(dateFilter);
    }

    private void initTrainTypes() {
        trainTypeFilter = ejbTrainTypeFacade.findAll();
        if (trainTypeFilter.size() != 1) {
            TrainType all = new TrainType();
            all.setTrainTypeId(0);
            all.setDescription(uiText.get("FilterAll"));
            trainTypeFilter.add(0, all);
        }

        //trainTypes = ejbTrainTypeFacade.findAll();
        if (selectedTrainType == null && trainTypeFilter.size() > 0) {
            selectedTrainType = trainTypeFilter.get(0);
//            scheduledServiceController.setSelectedTrainType(selectedTrainType);
            scheduledTripController.setSelectedTrainType(selectedTrainType);
        }
    }

    private void initStartLocation() {
        //selectedStartTTObjectType = this.ejbTTObjectTypeFacade.findAll().get(0);
        TTObjectTypeFilter filter = new TTObjectTypeFilter();
        filter.setStartTTObjectTypeFilter();
        this.ttobjectTypes = this.ejbTTObjectTypeFacade.findAll(filter);
        this.selectedStartTTObjectType = startTTObjectTypeAll;
        this.selectedStartTTObject = new TTArea(0, "");
        scheduledTripController.setSelectedStartTTObject(selectedStartTTObject);
        scheduledTripController.selectedStartTTObjectChanged();
    }

    public void dateFilterChanged(ValueChangeEvent event) {
        if (event.getNewValue() != null) {
            Date time = (Date) event.getNewValue();
            if (time.equals(this.dateFilter.getTime()) == false) {
                this.dateFilter.setTime(time);
//                scheduledServiceController.dateFilterChanged(this.dateFilter);
                scheduledTripController.dateFilterChanged(this.dateFilter);
            }
        }

        //recreateModel();
        //items = getPagination().createPageDataModel();
    }

    public Date getDateFilter() {
        return dateFilter.getTime();
    }

    public void setDateFilter(Date date) {
        //this.dateFilter.setTime(date);
    }

    public TTArea getSelectedArea() {
        return selectedArea;
    }

    public void setSelectedArea(TTArea selectedArea) {
        this.selectedArea = selectedArea;
        /*scheduledServiceController.setSelectedArea(selectedArea);
         scheduledTripController.setSelectedArea(selectedArea);
         scheduledServiceController.selectedAreaChanged();
         scheduledTripController.selectedAreaChanged();*/
    }

    public void selectedAreaChanged(ValueChangeEvent e) {
        this.selectedArea = (TTArea) (e.getNewValue());
//        scheduledServiceController.setSelectedArea(selectedArea);
//        scheduledServiceController.selectedAreaChanged();

        scheduledTripController.setSelectedArea(selectedArea);
        scheduledTripController.selectedAreaChanged();
    }

    public TrainType getSelecteTrainType() {
        return selectedTrainType;
    }

    public void setSelectedTrainType(TrainType selectedTrainType) {
        this.selectedTrainType = selectedTrainType;
        //scheduledServiceController.setSelectedTrainType(selectedTrainType);
        //scheduledTripController.setSelectedTrainType(selectedTrainType);
    }

    public List<TTArea> getAreas() {
        return areas;
    }

    public List<TTArea> getAreaFilter() {
        return areaFilter;
    }

    public List<TrainType> getTrainTypeFilter() {
        return this.trainTypeFilter;
    }

    public TrainType getSelectedTrainType() {
        return this.selectedTrainType;
    }

    /*public List<TTStation> getStationFilter() {
     List<TTStation> stationFilter = new ArrayList();
     //TTStation all = new TTStation(0, uiText.get("FilterAll"));
     this.selectedStation = stationAll;
     //stationFilter.add(0, all);
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

     return stationFilter;
     }

     public TTStation getSelectedStation() {
     return selectedStation;
     }

     public void setSelectedStation(TTStation selectedStation) {
     this.selectedStation = selectedStation;

     //scheduledTripController.setSelectedStation(selectedStation);
     //scheduledTripController.selectedStationChanged();
     }
    
     public void selectedStationChanged(ValueChangeEvent e) {
     this.selectedStation = (TTStation) (e.getNewValue());

     scheduledTripController.setSelectedStation(selectedStation);
     scheduledTripController.selectedStationChanged();
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
        scheduledTripController.setSelectedStartTTObject(selectedStartTTObject);
        scheduledTripController.selectedStartTTObjectChanged();
        ttobjectChanged = true;
    }

    public void selectedStartTTObjectChanged(ValueChangeEvent e) {
        if(!ttobjectChanged) {
            this.selectedStartTTObject = (TTObject) (e.getNewValue());
            scheduledTripController.setSelectedStartTTObject(selectedStartTTObject);
            scheduledTripController.selectedStartTTObjectChanged();
        }
    }

    public TTObject getSelectedStartTTObject() {
        return selectedStartTTObject;
    }

    public void setSelectedStartTTObject(TTObject selectedStartTTObject) {
        this.selectedStartTTObject = selectedStartTTObject;
    }

    public List<TTObject> getPossibleStartTTObjects() {
        if (isPossibleStartTTObjectsAvailable()) {
            ttobjectChanged = false;
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
        return selectedStartTTObjectType!= null && selectedStartTTObjectType.getTTObjects()!= null && (selectedStartTTObjectType.getTTObjects().isEmpty()==false);
    }
}
