/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.uiclasses;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import schedule.entities.ScheduledDuty;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import schedule.entities.Schedule;
import schedule.entities.ScheduledDay;
import schedule.entities.TTArea;
import schedule.sessions.AbstractFacade;
import schedule.sessions.ScheduledDayFacade;
import schedule.uiclasses.util.UiText;

@Named("scheduledDutyController")
@SessionScoped
public class ScheduledDutyController extends FilterController<ScheduledDuty, ScheduledDutyFilter> implements Serializable {

    @Inject
    private schedule.sessions.ScheduledDutyFacade ejbFacade;
    @Inject
    private schedule.sessions.TTAreaFacade ejbTTAreaFacade;
    @Inject
    private ScheduledDayFacade ejbScheduledDayFacade;
    @Inject
    private UiText uiText;

    private List<TTArea> areaFilterList; //"All" is included compared with areas
    //selected Area Filter
    private TTArea selectedArea;
    private List<TTArea> areas;

    public ScheduledDutyController() {
        super(new ScheduledDutyFilter());
    }

    public ScheduledDutyController(ScheduledDutyFilter filter) {
        super(filter);
    }

    public ScheduledDutyController(ScheduledDutyFilter filter, int itemsPerPage) {
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
        Calendar cal = Calendar.getInstance();
        ScheduledDay day = ejbScheduledDayFacade.find(cal);
        if (day == null) {
            // Dummy filter that not all schedules are searched
            day = new ScheduledDay(Schedule.convertToDayCode(cal), 0, 0, 0, false, 0);
        }
        getFilter().setScheduledDay(day);
    }

    public Date getDateFilter() {
        Date d = getFilter().getScheduledDay().getDateOfDay();
        return d;
    }

    public void setDateFilter(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        ScheduledDay day = this.ejbScheduledDayFacade.find(cal);
        if (day == null) {
            // Dummy item that there is some filter
            day = new ScheduledDay(Schedule.convertToDayCode(cal), 0, 0, 0, false, 0);
        }

        getFilter().setScheduledDay(day);
    }
    
    public void filterChanged() {
        recreateModel();
        this.pagination = null;
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
    protected AbstractFacade<ScheduledDuty> getFacade() {
        return this.ejbFacade;
    }

    @Override
    public ScheduledDuty constructNewItem() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String save(ScheduledDuty item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String destroy(ScheduledDuty item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
