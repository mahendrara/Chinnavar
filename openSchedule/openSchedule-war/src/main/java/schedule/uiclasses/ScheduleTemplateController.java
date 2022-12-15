package schedule.uiclasses;

import schedule.entities.ScheduleTemplate;
import schedule.uiclasses.util.JsfUtil;
import schedule.uiclasses.util.PaginationHelper;
import schedule.sessions.ScheduleTemplateFacade;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import schedule.entities.DayType;
import schedule.entities.DefaultDayRule;
import schedule.sessions.DayTypeFacade;
import schedule.uiclasses.util.UiText;

// @TODO: Needs to extend BaseController
@SuppressWarnings("unchecked")
@Named("scheduleTemplateController")
@SessionScoped
public class ScheduleTemplateController implements Serializable {

    @Inject
    private schedule.sessions.ScheduleTemplateFacade ejbFacade;
    
    @Inject
    private DayTypeFacade ejbDayTypeFacade;

    @Inject
    protected UiText uiText;
    
    private ScheduleTemplate current;
    private DataModel items = null;
    
    private PaginationHelper pagination;
    private int selectedItemIndex;
    private final int itemsPerPage = 50;
    
    public ScheduleTemplateController() {
    }

    @PostConstruct
    public void init() {
    }

    public ScheduleTemplate getSelected() {
        if (current == null) {
            current = new ScheduleTemplate();
            selectedItemIndex = -1;
        }
        return current;
    }

    public ScheduleTemplateFacade getFacade() {
        return ejbFacade;
    }

    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(itemsPerPage) {

                @Override
                public int getItemsCount() {
                    return getFacade().count();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}));
                }
            };
        }
        return pagination;
    }

    /*public void onDaytypeDrop(DragDropEvent ddEvent) {
     Daytype daytype = ((Daytype) ddEvent.getData());
     int targetId = 0;
     targetId = (Integer) ddEvent.getComponent().getAttributes().get("targetId");
     if (targetId != 0) {
     for (DefaultDayrule temp : current.getDefaultDayrules()) {
     if (temp.getDefaultruleid() == targetId) {
     temp.setDaytype(daytype);
     break;
     }
     }
     }
     }*/
    private void updateState(ScheduleTemplate scheduleTemplate) {
        this.ejbFacade.evict(scheduleTemplate);
        //System.out.println("evit template");
        //scheduleTemplate = this.ejbFacade.find(scheduleTemplate.getScheduleId());
    }

    public boolean isAddAllowed() {
        List<ScheduleTemplate> list = (List<ScheduleTemplate>) getItems().getWrappedData();

        if (list == null || list.isEmpty()) {
            return true;
        } else {
            ScheduleTemplate e = (ScheduleTemplate) list.get(0);
            //return (e == null || !e.isCreating());
            if (e != null && e.isCreating()) {
                return false;
            } else {
                Iterator<ScheduleTemplate> iterator = list.iterator();
                while (iterator.hasNext()) {
                    ScheduleTemplate item = (ScheduleTemplate) iterator.next();
                    if (item.isEditing()) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    public boolean isAddDayTypeAllowed(ScheduleTemplate scheduleTemplate) {
        //return current == null || (current.isCreating() == false);
        //if(scheduleTemplate.isCreating())
        //return false;

        List<DayType> array = scheduleTemplate.getDayTypes();
        if (array != null && !array.isEmpty()) {
            DayType dayType = array.get(0);
            if (dayType != null && dayType.isCreating()) {
                return false;
            }else {
                Iterator<DayType> iterator = array.iterator();
                while (iterator.hasNext()) {
                    dayType = (DayType) iterator.next();
                    if (dayType.isEditing()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public String prepareList() {
        recreateModel();
        return "List";
    }

    public String prepareView() {
        current = (ScheduleTemplate) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return null;//"View";
    }

    public String prepareCreate() {
        current = new ScheduleTemplate();
        selectedItemIndex = -1;
        return "Create";
    }

    public String addNew() {
        current = new ScheduleTemplate();
        current.setEditing(true);
        current.setCreating(true);
        List<ScheduleTemplate> oldArray = (List<ScheduleTemplate>) getItems().getWrappedData();
        oldArray.add(0, current);
        //getFacade().create(current);
        getItems().setWrappedData(oldArray);
        selectedItemIndex = -1;
        return null;
    }

    public String addNewDayType(ScheduleTemplate scheduleTemplate) {
        DayType dayType = new DayType();
        dayType.setCreating(true);
        dayType.setEditing(true);
        dayType.setScheduleParent(scheduleTemplate);
        scheduleTemplate.getDayTypes().add(0, dayType);
        return null;
    }

    public String prepareEdit() {
        current = (ScheduleTemplate) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String activateEdit(ScheduleTemplate scheduleTemplate) {
        scheduleTemplate.setEditing(true);
        return null;
    }

    public String activateEditDayRule(ScheduleTemplate scheduleTemplate, DefaultDayRule dayrule) {
        if (scheduleTemplate != null) {
            for (int i = 0; i < scheduleTemplate.getDefaultDayRules().size(); i++) {
                if (scheduleTemplate.getDefaultDayRules().get(i).getDefaultRuleId() == dayrule.getDefaultRuleId()) {
                    scheduleTemplate.getDefaultDayRules().get(i).setEditing(true);
                    break;
                }
            }
        }
        return null;
    }

    public String activateEditDayType(ScheduleTemplate scheduleTemplate, DayType dayType) {
        current = scheduleTemplate;
        for (int i = 0; i < scheduleTemplate.getDayTypes().size(); i++) {
            if (scheduleTemplate.getDayTypes().get(i).getDayTypeId() == dayType.getDayTypeId()) {
                scheduleTemplate.getDayTypes().get(i).setEditing(true);
                break;
            }
        }
        //dayType.setEditing(true);
        return null;
    }

    public String update() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(uiText.get("ScheduleTemplateUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
            return null;
        }
    }

    public String save(ScheduleTemplate scheduleTemplate) {
        Iterator<DayType> iterator = scheduleTemplate.getDayTypes().iterator();

        DayType dayType;
        while (iterator.hasNext()) {
            dayType = iterator.next();
            dayType.setCreating(false);
            dayType.setEditing(false);
        }
        if (scheduleTemplate.isCreating()) {
            try {
                scheduleTemplate.setEditing(false);
                scheduleTemplate.setCreating(false);
                getFacade().create(scheduleTemplate);
                updateState(scheduleTemplate);
                JsfUtil.addSuccessMessage( uiText.get("ScheduleTemplateCreated") );
            } catch (Exception e) {
                scheduleTemplate.setEditing(true);
                scheduleTemplate.setCreating(true);
                iterator = scheduleTemplate.getDayTypes().iterator();

                while (iterator.hasNext()) {
                    dayType = iterator.next();
                    dayType.setCreating(true);
                    dayType.setEditing(true);
                }
                JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
                return null;
            }
        } else {
            try {
                scheduleTemplate.setEditing(false);
                getFacade().edit(scheduleTemplate);
                updateState(scheduleTemplate);
                JsfUtil.addSuccessMessage(uiText.get("ScheduleTemplateUpdated"));
            } catch (Exception e) {
                current.setEditing(true);
                //current = this.ejbFacade.find(current.getScheduleId());
                JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
                return null;
            }
        }
        //updateState(scheduleTemplate);
        recreateModel();
        return null;
    }

    public String saveDayRule(ScheduleTemplate scheduleTemplate, DefaultDayRule dayRule) {
        if (dayRule.getDayType() != null) {
            //ScheduleTemplate selectedTemplate = getFacade().find(dayrule.getScheduleParent().getScheduleid());
            try {
                dayRule.setEditing(false);
                getFacade().edit(scheduleTemplate);
                JsfUtil.addSuccessMessage(uiText.get("ScheduleTemplateUpdated"));
                //return "View";
            } catch (Exception e) {
                JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
                return null;
            }
            updateState(scheduleTemplate);
        }

        return null;
    }

    public String saveDayType(ScheduleTemplate scheduleTemplate, DayType dayType) {
        try {
            dayType.setEditing(false);

            if (dayType.isCreating()) {
                dayType.setCreating(false);
                getFacade().edit(scheduleTemplate);
                JsfUtil.addSuccessMessage(uiText.get("DayTypeCreated"));
            } else {
                getFacade().edit(scheduleTemplate);
                JsfUtil.addSuccessMessage(uiText.get("DayTypeUpdated"));
            }
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
            updateState(scheduleTemplate);
            recreateModel();
            return null;
        }
        updateState(scheduleTemplate);
        recreateModel();
        return null;
    }

    public String cancel(ScheduleTemplate scheduleTemplate) {
        try {
            scheduleTemplate.setEditing(false);

            //JsfUtil.addSuccessMessage(uiText.get("ScheduleUpdated"));
            return "List";
            //return "View";
        } catch (Exception e) {
            //JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
            return null;
        }
    }

    public String cancelDayRule(ScheduleTemplate scheduleTemplate, DefaultDayRule dayRule) {
        try {
            //for (int i = 0; i < scheduleTemplate.getDefaultDayRules().size(); i++) {
            //if (scheduleTemplate.getDefaultDayRules().get(i).getDefaultRuleId() == dayRule.getDefaultRuleId()) {
            //scheduleTemplate.getDefaultDayRules().get(i).setEditing(false);
            //break;
            // }
            //}
            dayRule.setEditing(false);
            recreateModel();

            //return "View";
        } catch (Exception e) {
            //JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
            return null;
        }
        return "";//"List";
    }

    public String cancelDayType(ScheduleTemplate scheduleTemplate, DayType dayType) {
        dayType.setEditing(false);
        current = this.ejbFacade.find(current.getScheduleId());
        //recreateModel();
        return null;
    }

    public String destroy() {
        current = (ScheduleTemplate) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        if (!current.isCreating()) {
            if (current.getSchedules() != null && current.getSchedules().size() > 0) {
                JsfUtil.addErrorMessage(uiText.get("Error_ScheduleTemplateScheduleExist"));
            } else {
                performDestroy();
            }
        }
        recreatePagination();
        recreateModel();
        return "List";
    }

    public String destroyDayType(ScheduleTemplate scheduleTemplate, DayType dayType) {
        try {
            if (dayType.isCreating()) {
                dayType.setScheduleParent(null);
            } else {
                current = this.ejbFacade.find(scheduleTemplate.getScheduleId());//needed, because if 1. add a new one, 2. delete an existing one ->error
                Iterator<DefaultDayRule> dayRules = current.getDefaultDayRules().iterator();
                DefaultDayRule dayRule;
                while (dayRules.hasNext()) {
                    dayRule = dayRules.next();
                    if (dayRule.getDayType() != null && (dayRule.getDayType().getDayTypeId().equals( dayType.getDayTypeId() ))) {
                        dayRule.setDayType(null);
                    }
                }

                dayType.setScheduleParent(null);
                
                current.remove(dayType);
                this.ejbFacade.edit(current);
                
                this.ejbDayTypeFacade.remove(dayType);
                
                JsfUtil.addSuccessMessage(uiText.get("DayTypeDeleted"));
            }

            //current = this.ejbFacade.find(scheduleTemplate.getScheduleId());
            updateState(scheduleTemplate);
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
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
            JsfUtil.addSuccessMessage(uiText.get("ScheduleTemplateDeleted"));
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

    private void recreatePagination() {
        pagination = null;
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

    /*public ScheduleTemplate getScheduleTemplate(java.lang.Integer id) {
        return ejbFacade.find(id);

    }

    @FacesConverter(forClass = ScheduleTemplate.class)
    public static class ScheduleTemplateControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ScheduleTemplateController controller = (ScheduleTemplateController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "scheduleTemplateController");
            return controller.getScheduleTemplate(getKey(value));
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
            if (object instanceof ScheduleTemplate) {
                ScheduleTemplate o = (ScheduleTemplate) object;
                return getStringKey(o.getScheduleId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + ScheduleTemplate.class.getName());
            }
        }

    }*/
}
