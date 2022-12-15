package schedule.uiclasses;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import schedule.uiclasses.util.JsfUtil;

import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import schedule.entities.LocationGroup;
import schedule.entities.TTHierarchy;
import schedule.entities.TTHierarchyType;
import schedule.entities.TTObject;
import schedule.entities.TTObjectState;
import schedule.entities.TTObjectType;
import schedule.sessions.LocationGroupFacade;
import schedule.uiclasses.util.UiText;

@Named("TTLocationGroupsController")
@SessionScoped
public class TTLocationGroupsController extends FilterController<LocationGroup, LocationGroupFilter> implements Serializable {

    @Inject
    private schedule.sessions.LocationGroupFacade ejbFacade;
    @Inject
    private schedule.sessions.TTObjectFacade ejbTTObjectFacade;
    @Inject
    private schedule.sessions.TTObjectTypeFacade ejbTTObjectTypeFacade;
    @Inject
    private schedule.sessions.TTObjectStateFacade ejbTTObjectStateFacade;
    @Inject
    private schedule.sessions.TTHierarchyFacade ejbHierarchyFacade;
    @Inject
    private schedule.sessions.TTHierarchyTypeFacade ejbHierarchyTypeFacade;
    @Inject
    private UiText uiText;

    // Filter selections in UI
    private TTObjectType objectTypeAll = null;
    private TTObjectState objectStateAll = null;
    private Collection<TTObjectState> ttObjectStates;

    // Filter selections in View UI
    private List<TTObjectType> ttObjectTypes;
    private TTObjectType selectedObjectType = null;
    private TTObject selectedObject = null;
    private Integer selectedPriority = 0;

    public TTLocationGroupsController() {
        super(new LocationGroupFilter());
        // Filter 'all'-item
        objectStateAll = new TTObjectState(0);
        objectTypeAll = new TTObjectType(0);
        this.ttObjectTypes = new ArrayList<>();
    }

    @PostConstruct
    public void init() {
        // Generate items for ObjectType-menu
        ttObjectStates = ejbTTObjectStateFacade.findAll();

        List<TTHierarchyType> allowedHierarchyTypes = ejbHierarchyTypeFacade
                .findAllByParentType(new TTObjectType(TTObjectType.TTObjectTypeEnum.LOCATION_GROUP.getDBCode()));

        for (TTHierarchyType hierarchyType : allowedHierarchyTypes) {
            this.ttObjectTypes.add(hierarchyType.getChildType());
        }

        if (!this.ttObjectTypes.isEmpty()) {
            this.selectedObjectType = this.ttObjectTypes.get(0);
        }
    }

    @Override
    protected LocationGroupFacade getFacade() {
        return ejbFacade;
    }

    @Override
    public void clearFilters() {
        getFilter().setObjectStateFilter(null);
    }

    @Override
    public String destroy(LocationGroup group) {
        try {
            // Do not try to delete item which is not stored yet
            if (!group.isCreating()) {
                getFacade().remove(group);
                JsfUtil.addSuccessMessage(uiText.get("LocationGroupDeleted"));
            }
            recreateModel();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
        }
        
        return "List";
    }

    @Override
    public String save(LocationGroup group) {
        if (!isUniqueGroupName(group)) {
            JsfUtil.addErrorMessage(uiText.get("Error_LocationGroupNameMustBeUnique"));
            return "List";
        }

        try {
            if (group.isCreating()) {
                getFacade().create(group);
                JsfUtil.addSuccessMessage(uiText.get("LocationGroupCreated"));
            } else {
                getFacade().edit(group);
                JsfUtil.addSuccessMessage(uiText.get("LocationGroupUpdated"));
            }

            group.setCreating(false);
            group.setEditing(false);
            recreateModel();

            return "List";

        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
            return null;
        }
    }

    @Override
    public LocationGroup constructNewItem() {
        // Create new item and fill data to it
        LocationGroup current = new LocationGroup();
        TTObjectType ot = ejbTTObjectTypeFacade.find(TTObjectType.TTObjectTypeEnum.LOCATION_GROUP.getDBCode());
        current.setTTObjectType(ot);
        current.setTTObjectState(new TTObjectState(1));
        current.setEditing(true);
        current.setCreating(true);
        
        return current;
    }

    private boolean isUniqueGroupName(LocationGroup group) {
        for (LocationGroup g : ejbFacade.findAll()) {
            if (group.getName().equals(g.getName())
                    && !Objects.equals(g.getTTObjId(), group.getTTObjId())) {
                return false;
            }
        }

        return true;
    }

    public TTObjectState getObjectStateAll() {
        objectStateAll.setDescription(uiText.get("FilterAll"));
        return this.objectStateAll;
    }

    public TTObjectType getObjectTypeAll() {
        objectTypeAll.setDescription(uiText.get("FilterAll"));
        return this.objectTypeAll;
    }

    public TTObjectState getSelectedTTObjectState() {
        return getFilter().getObjectStateFilter();
    }

    public void setSelectedTTObjectState(TTObjectState selectedTTObjectState) {
        getFilter().setObjectStateFilter(selectedTTObjectState);
    }

    public Collection<TTObjectState> getTtObjectStates() {
        return ttObjectStates;
    }

    public void setTtObjectTypes(Collection<TTObjectState> ttObjectStates) {
        this.ttObjectStates = ttObjectStates;
    }

    // To create View child-list
    public List<TTObject> getSelectedChildObjects() {
        return getSelected().getChildObjects();
    }

    // Filter selection
    public TTObjectType getSelectedTTObjectType() {
        return getFilter().getObjectTypeFilter();
    }

    public void setSelectedTTObjectType(TTObjectType selectedTTObjectType) {
        getFilter().setObjectTypeFilter(selectedTTObjectType);
    }

    // For View filter
    public Set<TTObjectType> getChildTTObjectTypes() {
        Set<TTObjectType> ttobjTypes = new HashSet<>();
        for (TTObject o : getSelected().getChildObjects()) {
            ttobjTypes.add(o.getTTObjectType());
        }
        return ttobjTypes;
    }

    public void removeSelectedChild(TTObject parent, TTObject child) {
        this.ejbHierarchyFacade.removeChild(parent, child);

        JsfUtil.addSuccessMessage(uiText.get("LocationGroupChildRemoved"));
        
        updateState(parent);
        recreateModel();
    }

    public void addNewChild() {
        TTObject parent = getSelected();
        TTObject child = this.selectedObject;
        
        TTHierarchy newGroup = new TTHierarchy();
        newGroup.setEditing(true);
        newGroup.setCreating(true);

        TTHierarchyTypeFilter filter = new TTHierarchyTypeFilter();
        filter.setParentObjectType(parent.getTTObjectType());
        filter.setChildObjectType(child.getTTObjectType());

        TTHierarchyType hierarchyType = this.ejbHierarchyTypeFacade.findFirst(filter);
        newGroup.setHierarchyType(hierarchyType);
        newGroup.setParentObject(parent);
        newGroup.setChildObject(child);
        newGroup.setSeqNo(this.selectedPriority);

        this.ejbHierarchyFacade.addChild(newGroup);
        
        JsfUtil.addSuccessMessage(uiText.get("LocationGroupChildAdded"));

        updateState(parent);
        recreateModel();
    }

    public void updateState(TTObject parent) {
        this.ejbFacade.evict((LocationGroup) parent);
        this.ejbHierarchyFacade.evictAll();
        this.ejbHierarchyTypeFacade.evictAll();

        // Be cunning and update selected item!
        parent = this.ejbFacade.find(parent.getTTObjId());
        prepareView((LocationGroup) parent);
    }

    public List<TTObjectType> getObjectTypes() {
        return this.ttObjectTypes;
    }

    public TTObjectType getSelectedObjectType() {
        return selectedObjectType;
    }

    public void setSelectedObjectType(TTObjectType newType) {
        this.selectedObjectType = newType;
    }

    public TTObject getSelectedObject() {
        return selectedObject;
    }

    public void setSelectedObject(TTObject newObject) {
        this.selectedObject = newObject;
    }

    public Integer getSelectedPriority() {
        return selectedPriority;
    }

    public void setSelectedPriority(Integer newPriority) {
        this.selectedPriority = newPriority;
    }

    public List<TTObject> getTtObjects() {
        List<TTObject> objects = this.ejbTTObjectFacade.findByType(this.selectedObjectType);
        if (!objects.isEmpty()) {
            if( this.selectedObjectType.getTTObjTypeId() 
                    == TTObjectType.TTObjectTypeEnum.LOCATION_GROUP.getDBCode() )
            {
                objects.remove(getSelected());
            }
            
            this.selectedObject = objects.get(0);
            return objects;
        } else {
            return new ArrayList<>();
        }
    }

    public List<Integer> getPriorities() {
        Integer childrenAmount = getSelected().getChildCount() + 1;
        List<Integer> priorities = new ArrayList<>(childrenAmount);
        for(int i = 0; i < childrenAmount; i++)
        {
            priorities.add(i+1);
        }
        
        if (!priorities.isEmpty()) 
        {
            this.selectedPriority = priorities.get(0);
            return priorities;
        } else {
            return new ArrayList<>();
        }
    }
    
    public void selectedObjectTypeChanged() {
        List<TTObject> objects = this.ejbTTObjectFacade.findByType(this.selectedObjectType);
        if (!objects.isEmpty()) {
            this.selectedObject = this.ejbTTObjectFacade.findByType(this.selectedObjectType).get(0);
            selectedObjectChanged();
        } else {
            this.selectedObject = null;
        }
    }

    public void selectedObjectChanged() {
        // TODO :: Do we even need this?
    }
    
    public boolean isAddChildObjectAllowed() {
        if (getSelected() == null ||
            this.selectedObject == null) {
            return false;
        }

        // Forbid adding itself as a child.
        if(getSelected().getTTObjId().equals(this.selectedObject.getTTObjId()))
        {
            return false;
        }
        
        List<TTObject> list = (List<TTObject>) getSelected().getChildObjects();
        if (list == null || list.isEmpty()) {
            return true;
        } else {
            TTObject e = (TTObject) list.get(0);
            if (e != null && e.isCreating()) {
                return false;
            } else {
                Iterator<TTObject> iterator = list.iterator();
                while (iterator.hasNext()) {
                    TTObject item = (TTObject) iterator.next();
                    if (item.isEditing() ||
                        item.getTTObjId().equals(this.selectedObject.getTTObjId())) 
                    {
                        return false;
                    }
                }
            }
        }

        return true;
    }
    
    public Integer getChildPriority(TTObject child)
    {
        TTHierarchyFilter filter = new TTHierarchyFilter();
        filter.setParentObject(getSelected());
        filter.setChildObject(child);
        
        return this.ejbHierarchyFacade.findFirst(filter).getSeqNo();
    }
}
