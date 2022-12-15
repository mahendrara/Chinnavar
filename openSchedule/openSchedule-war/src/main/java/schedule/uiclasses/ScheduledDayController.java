package schedule.uiclasses;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import schedule.entities.ScheduledDay;
import schedule.uiclasses.util.JsfUtil;
import schedule.uiclasses.util.PaginationHelper;
import schedule.sessions.ScheduledDayFacade;

import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
//import javax.faces.bean.ManagedBean;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import schedule.messages.Operation;
import schedule.messages.XmlMessageSender;
import schedule.uiclasses.util.UiText;

// @TODO: Needs to extend BaseController
@SuppressWarnings("unchecked")
@Named
@SessionScoped
public class ScheduledDayController implements Serializable {

    //private ScheduledDay manualSelection;
    private ScheduledDay current;
    private DataModel items = null;
    private boolean active;
    private boolean archive;
    //@Inject
    //private ScheduledServiceController scheduledServiceController;
    //@Inject
    //private FilterController filterController;
    @Inject
    private XmlMessageSender xmlMessageSender;

    @Inject
    private schedule.sessions.ScheduledDayFacade ejbFacade;

    //@Inject
    //private ScheduledServiceController scheduledServiceController;

    @Inject
    private UiText uiText;

    private PaginationHelper pagination;
    private int selectedItemIndex;
    private final int itemsPerPage = 50;
    private Calendar dateFilter;

    public ScheduledDayController() {
    }

    @PostConstruct
    public void init() {
        this.dateFilter = Calendar.getInstance();
        active = Boolean.TRUE;
    }

    public Date getDateFilter() {
        return dateFilter.getTime();
    }

    public void setDateFilter(Date dateFilter) {
        if (this.dateFilter.getTime().getTime() != dateFilter.getTime()) {
            this.dateFilter.setTime(dateFilter);
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
    
    public boolean isArchive(){
        return archive;
    }

    public void setArchive(boolean archive) {
        this.archive = archive;
    }
    
    public void activeFilterChanged() {
        recreateModel();
        this.pagination = null;
    }
    
    public void archiveFilterChanged() {
        recreateModel();
        this.pagination = null;
    }

    public ScheduledDay getSelected() {
        if (current == null) {
            current = new ScheduledDay();
            selectedItemIndex = -1;
        }
        return current;
    }

    /*public ScheduledDay getManualSelection() {
     //return manualSelection;
     return (ScheduledDay)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("selectedScheduledDay");
     }*/
    private ScheduledDayFacade getFacade() {
        return ejbFacade;
    }

    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(this.itemsPerPage) {

                @Override
                public int getItemsCount() {
                    ScheduledDayFilter filter = new ScheduledDayFilter();
                    filter.setArchived(archive);
                    filter.setActive(active);
                    return getFacade().count(filter);
                }

                @Override
                public DataModel createPageDataModel() {
                    ScheduledDayFilter filter = new ScheduledDayFilter();
                    filter.setArchived(archive);
                    filter.setActive(active);
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

    /*public String prepareView() {
     current = (ScheduledDay) getItems().getRowData();
     selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
     try {
     FacesContext.getCurrentInstance().getExternalContext().redirect("View.xhtml");
     } catch (IOException ex) {
     Logger.getLogger(PlannedServiceController.class.getName()).log(Level.SEVERE, null, ex);
     }
     return null;
     //return "View";
     }*/
    public String prepareCreate() {
        current = new ScheduledDay();
        selectedItemIndex = -1;
        return "Create";
    }

    public boolean isAddAllowed() {
        List<ScheduledDay> list = (List<ScheduledDay>) getItems().getWrappedData();
        if (list == null || list.isEmpty()) {
            return true;
        } else {

            ScheduledDay e = (ScheduledDay) list.get(0);
            if (e != null && e.isCreating()) {
                return false;
            } else {
                Iterator<ScheduledDay> iterator = list.iterator();
                while (iterator.hasNext()) {
                    ScheduledDay item = (ScheduledDay) iterator.next();
                    if (item.isEditing()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void selectForManualLoadDay() {

        current = (ScheduledDay) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        //manualSelection = current;

        /*if(current.getDateOfDay() != null){
         //FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("selectedScheduledDay", current);
         filterController.setSelectedScheduledDay(current);
         }*/
        //scheduledServiceController.setDateFilter(manualSelection.getDateOfDay());
    }

    public boolean isSelected(ScheduledDay item) {
        return current != null && item == current;
    }

    public String create() {
        try {

            ScheduledDay selectedScheduledDay = this.ejbFacade.find(this.dateFilter);
            if (selectedScheduledDay != null) {
                JsfUtil.addErrorMessage(uiText.get("Error_ScheduledDayExist"));
                return null;
            }

            int scheduledDayCode = schedule.entities.Schedule.convertToDayCode(dateFilter);
            current.setScheduledDayCode(scheduledDayCode);
            current.setStartYear(dateFilter.get(Calendar.YEAR));
            current.setStartMonth(dateFilter.get(Calendar.MONTH) + 1);
            current.setStartDay(dateFilter.get(Calendar.DAY_OF_MONTH));
            current.setEditing(false);
            current.setCreating(false);
            current.setVersion(1);
            getFacade().create(current);
            getFacade().evictAll();
            //This leads to WELD-001303 No active contexts error: next day is created when loaddaywidth=2, but services are not creaed for next day
            //scheduledServiceController.newDayCreated(current.getDateOfDay());

            xmlMessageSender.sendScheduledDayMsg(current, Operation.CREATE);
            JsfUtil.addSuccessMessage(uiText.get("ScheduleddayCreated"));
            return null;
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
            return null;
        }
    }

    public String addNew() {
        current = new ScheduledDay();
        current.setEditing(true);
        current.setCreating(true);
        current.setArchived(false);
        current.setActive(true);
        current.setSchedulingState(1); // todo enums..

        //current.setDurationSecs(0);
        List<ScheduledDay> oldArray = (List<ScheduledDay>) getItems().getWrappedData();
        oldArray.add(0, current);
        getItems().setWrappedData(oldArray);
        selectedItemIndex = -1;
        return null;
    }

    public String prepareEdit() {
        current = (ScheduledDay) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String activateEdit(ScheduledDay scheduledday) {
        //current = scheduleTemplate;
        scheduledday.setEditing(true);
        return null;
    }

    public String update() {
        try {
            current.increaseVersion();
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(uiText.get("ScheduledDayUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
            return null;
        }
    }

    public String save(ScheduledDay scheduledday) {
        System.out.println(scheduledday);
        current = (ScheduledDay) getItems().getRowData();
        if (current.isCreating()) {
            create();

            return null;
        } else {
            try {
                scheduledday.increaseVersion();
                getFacade().edit(scheduledday);
                scheduledday.setEditing(false);

                xmlMessageSender.sendScheduledDayMsg(current, Operation.MODIFY);
                recreateModel();
                JsfUtil.addSuccessMessage(uiText.get("ScheduleddayUpdated"));
                return null;
                //return "View";
            } catch (Exception e) {
                JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
                return null;
            }
        }
    }

    public String cancel() {
        try {
            current = (ScheduledDay) getItems().getRowData();
            current.setEditing(false);

            //JsfUtil.addSuccessMessage(uiText.get("ScheduleUpdated"));
            return "List";
            //return "View";
        } catch (Exception e) {
            //JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (ScheduledDay) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        if (current.isCreating()) {
            current = null;
        }
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
            JsfUtil.addSuccessMessage(uiText.get("ScheduleddayDeleted"));
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
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
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

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), true);
    }

    public ScheduledDay getScheduledDay(java.lang.Integer id) {
        return ejbFacade.find(id);
    }

    @FacesConverter(forClass = ScheduledDay.class)
    public static class ScheduledDayControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ScheduledDayController controller = (ScheduledDayController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "scheduledDayController");
            return controller.getScheduledDay(getKey(value));
        }

        java.lang.Integer getKey(String value) {
            java.lang.Integer key;
            key = Integer.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Integer value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof ScheduledDay) {
                ScheduledDay o = (ScheduledDay) object;
                return getStringKey(o.getScheduledDayCode());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + ScheduledDayController.class.getName());
            }
        }
    }

}
