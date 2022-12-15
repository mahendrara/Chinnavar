package schedule.uiclasses;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import schedule.entities.TTObject;
import schedule.uiclasses.util.JsfUtil;

import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import schedule.entities.TTHierarchy;
import schedule.entities.TTHierarchyType;
import schedule.entities.TTObjectType;
import schedule.sessions.TTHierarchyFacade;
import schedule.sessions.TTHierarchyTypeFacade;
import schedule.uiclasses.util.UiText;

@Named("TTObjectAltController")
@SessionScoped
public class TTObjectAltController extends FilterController<TTHierarchy, TTHierarchyFilter> implements Serializable {

    @Inject
    private TTHierarchyFacade ejbFacade;

    @Inject
    private TTHierarchyTypeFacade ejbHierarchyTypeFacade;

    @Inject
    private UiText uiText;

    private HashMap<TTObjectType, List<TTObjectType>> parentChildrenTypes;
    private TTObjectType selectedParentObjectType;
    private TTObjectType selectedChildObjectType;
    private TTObject selectedParentObject;
    private TTObject selectedChildObject;

    public TTObjectAltController() {
    }

    @PostConstruct
    public void init() {
        parentChildrenTypes = new HashMap<>();
        // Generate items for TTObject-menus
        //ttObjects = ejbTTObjectFacade.findAll('L'); // Give L as location object to filter
        TTHierarchyTypeFilter hierarchyTypeFilter = new TTHierarchyTypeFilter();
        //hierarchyTypeFilter.setHierarchyType("alternativeto");
        //hierarchyTypeFilter.setHierarchyType("alternativeto");

        List<TTHierarchyType> hierarchyTypes = ejbHierarchyTypeFacade.findAll(hierarchyTypeFilter);
        TTHierarchyFilter filter = new TTHierarchyFilter();
        List<TTHierarchyType> list = new ArrayList<>();
        //filter.setHierarchyTypes(hierarchyTypes);
        //super.setFilter(filter);

        if (hierarchyTypes != null) {
            for (int i = 0; i < hierarchyTypes.size(); i++) {
                TTHierarchyType hierarchyType = hierarchyTypes.get(i);
                if (hierarchyType.getHierarchyType().equals("alternativeto")) {
                    list.add(hierarchyType);
                    if (i == 0) {
                        selectedParentObjectType = hierarchyType.getParentType();
                        if (!selectedParentObjectType.getTTObjects().isEmpty()) {
                            selectedParentObject = selectedParentObjectType.getTTObjects().get(0);
                        }
                        selectedChildObjectType = hierarchyType.getChildType();
                        if (!selectedChildObjectType.getTTObjects().isEmpty()) {
                            selectedChildObject = selectedChildObjectType.getTTObjects().get(0);
                        }
                    }
                    if (parentChildrenTypes.containsKey(hierarchyType.getParentType()) == false) {
                        List<TTObjectType> children = new ArrayList<>();
                        children.add(hierarchyType.getChildType());
                        parentChildrenTypes.put(hierarchyType.getParentType(), children);
                    } else {
                        parentChildrenTypes.get(hierarchyType.getParentType()).add(hierarchyType.getChildType());
                    }
                }
            }
        }
        filter.setHierarchyTypes(list);
        super.setFilter(filter);
    }

    @Override
    protected TTHierarchyFacade getFacade() {
        return ejbFacade;
    }

    @Override
    public void clearFilters() {
//        getFilter().setAltTTObject(null);
//        getFilter().setOrgTTObject(null);
    }

    @Override
    public String destroy(TTHierarchy alt) {
        try {
            // Do not try to delete item which is not stored yet
            if (!alt.isCreating()) {
                getFacade().remove(alt);
                JsfUtil.addSuccessMessage(uiText.get("TTObjectAltDeleted"));
            }
            recreateModel();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
        }
        return "List";
    }

    @Override
    public String save(TTHierarchy alt) {
        try {
            if(alt.getParentObject().getTTObjId() == alt.getChildObject().getTTObjId()) {
                 JsfUtil.addErrorMessage(uiText.get("SameAlternativeObject"));
                 return null;
            }
                
            if (alt.isCreating()) {
                getFacade().create(alt);
                JsfUtil.addSuccessMessage(uiText.get("TTObjectAltCreated"));
            } else {
                getFacade().edit(alt);
                JsfUtil.addSuccessMessage(uiText.get("TTObjectAltUpdated"));
            }

            alt.setCreating(false);
            alt.setEditing(false);
            recreateModel();
            return "List";

        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
            return "List";
        }
    }

    @Override
    public TTHierarchy constructNewItem() {
        // Create new item and fill data to it
        TTHierarchy current = new TTHierarchy();
        current.setEditing(true);
        current.setCreating(true);
        TTHierarchyTypeFilter filter = new TTHierarchyTypeFilter();
        filter.setParentObjectType(selectedParentObjectType);
        filter.setChildObjectType(selectedChildObjectType);
        TTHierarchyType hierarchyType = this.ejbHierarchyTypeFacade.findFirst(filter);
        current.setHierarchyType(hierarchyType);
        current.setParentObject(selectedParentObject);
        current.setChildObject(selectedChildObject);
        TTHierarchyFilter filter2 = new TTHierarchyFilter();
        filter2.setHierarchyType(hierarchyType);
        filter2.setParentObject(selectedParentObject);
        current.setSeqNo(this.ejbFacade.count(filter2) + 1);
        return current;
    }

    public Set<TTObjectType> getParentObjectTypes() {
        return parentChildrenTypes.keySet();
    }

    public List<TTObjectType> getChildObjectTypes() {
        if (selectedParentObjectType != null) {
            return this.parentChildrenTypes.get(selectedParentObjectType);
        } else {
            return new ArrayList<>();
        }
    }

    public TTObjectType getSelectedParentObjectType() {
        return selectedParentObjectType;
    }

    public void setSelectedParentObjectType(TTObjectType selectedParentObjectType) {
        this.selectedParentObjectType = selectedParentObjectType;
        this.selectedChildObjectType = this.parentChildrenTypes.get(selectedParentObjectType).get(0);
        //selectedParentObject = selectedParentObjectType.getTTObjects().isEmpty() ? null : selectedParentObjectType.getTTObjects().get(0);
    }

    public TTObjectType getSelectedChildObjectType() {
       return selectedChildObjectType;
    }

    public void setSelectedChildObjectType(TTObjectType selectedChildObjectType) {
        //selectedChildObject = selectedChildObjectType.getTTObjects().isEmpty() ? null : selectedChildObjectType.getTTObjects().get(0);
        this.selectedChildObjectType = selectedChildObjectType;
    }

    public TTObject getSelectedParentObject() {
        return selectedParentObject;
    }

    public void setSelectedParentObject(TTObject selectedParentObject) {
        this.selectedParentObject = selectedParentObject;
    }

    public TTObject getSelectedChildObject() {
        return selectedChildObject;
    }

    public void setSelectedChildObject(TTObject selectedChildObject) {
        this.selectedChildObject = selectedChildObject;
    }

}
