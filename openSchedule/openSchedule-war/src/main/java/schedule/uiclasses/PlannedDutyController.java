/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.uiclasses;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import schedule.entities.PlannedDuty;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import schedule.entities.DayType;
import schedule.entities.Schedule;
import schedule.entities.TTArea;
import schedule.sessions.AbstractFacade;
import schedule.uiclasses.util.UiText;

@Named("plannedDutyController")
@SessionScoped
public class PlannedDutyController extends FilterController<PlannedDuty, PlannedDutyFilter> implements Serializable {

    @Inject
    private schedule.sessions.PlannedDutyFacade ejbFacade;
    @Inject
    private schedule.sessions.TTAreaFacade ejbTTAreaFacade;
    @Inject
    protected schedule.sessions.ScheduleFacade ejbScheduleFacade;
    @Inject
    LongTermFilterController longTermFilterController;
    @Inject
    private UiText uiText;

    private List<TTArea> areaFilterList; //"All" is included compared with areas
    //selected Area Filter
    private TTArea selectedArea;
    private List<TTArea> areas;

    public PlannedDutyController() {
        super(new PlannedDutyFilter());
    }

    public PlannedDutyController(PlannedDutyFilter filter) {
        super(filter);
    }

    public PlannedDutyController(PlannedDutyFilter filter, int itemsPerPage) {
        super(filter, itemsPerPage);
    }

    /*@Inject
    AreaFilterController areaFilterController;

    public AreaFilterController getAreaFilterController() {
        return areaFilterController;
    }

    public void setAreaFilterController(AreaFilterController areaFilterController) {
        this.areaFilterController = areaFilterController;
    }*/
    @PostConstruct
    public void init() {
        if (areaFilterList == null) {

            areaFilterList = ejbTTAreaFacade.findAll();
            if (areaFilterList.size() != 1) {
                areaFilterList.add(0, new TTArea(0, uiText.get("FilterAll")));
            }
        }

        if (areas == null) {
            areas = ejbTTAreaFacade.findAll();
        }
        if (selectedArea == null && areaFilterList.size() > 0) {
            selectedArea = areaFilterList.get(0);
            this.getFilter().setAreaFilter(selectedArea);
        }
        getFilter().setSchedule(longTermFilterController.getSelectedSchedule());
    }

    public void filterChanged() {
        recreateModel();
        this.pagination = null;
    }

    public void scheduleFilterChanged() {
//        if (getFilter().getSchedule() != null && getFilter().getSchedule().getValid() && getFilter().getSchedule().getDayTypes() != null) {
//            getFilter().setDayType(getFilter().getSchedule().getDayTypes().isEmpty() ? null : getFilter().getSchedule().getDayTypes().get(0));
//        } else {
            getFilter().setDayType(null);
//        }

        resetPagination();
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

    public List<Schedule> getSchedules() {
        ScheduleFilter scheduleFilter = new ScheduleFilter();
        scheduleFilter.setValid(Boolean.TRUE);
        List<Schedule> schedules = ejbScheduleFacade.findAll(scheduleFilter);

        if (schedules == null) {
            schedules = new ArrayList<>();
        }
        return schedules;
    }

    public List<DayType> getDayTypes() {
        if (getFilter().getSchedule() != null) {
            return getFilter().getSchedule().getDayTypes();
        } else {
            return null;
        }
    }

    public DayType getDayTypeAll() {
        return new DayType(0, uiText.get("FilterAll"), uiText.get("FilterAll"));
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

    public List<TTArea> getAreaFilter() {
        //areaItems = getAreaItems();
        //List<TTArea> areas = (List<TTArea>) areaItems.getWrappedData();
        return areaFilterList;
    }

    public void changeAreaFilter() {
        recreateModel();
        //items = getPagination().createPageDataModel();
    }

    @Override
    protected AbstractFacade<PlannedDuty> getFacade() {
        return this.ejbFacade;
    }

    @Override
    public PlannedDuty constructNewItem() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String save(PlannedDuty item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String destroy(PlannedDuty item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
