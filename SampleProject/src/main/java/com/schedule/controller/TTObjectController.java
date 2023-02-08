package com.schedule.controller;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.schedule.base.controller.FilterController;
import com.schedule.facede.TTObjectFacade;
import com.schedule.facede.TTObjectTypeFacade;
import com.schedule.model.TTObject;
import com.schedule.model.TTObjectType;
import com.schedule.util.UiText;

@Named("TTObjectController")
@SessionScoped
public class TTObjectController extends FilterController<TTObject, TTObjectFilter> implements Serializable {
    @Inject
    private com.schedule.facede.TTObjectFacade ejbFacade;
    @Inject
    private TTObjectTypeFacade ejbTTObjectTypeFacade;
    @Inject
    private UiText uiText;

    private List<TTObjectType> ttObjectTypes;
    TTObjectType objectTypeAll;

    private final List<TTObjectType> filterExcludors;

    public TTObjectController() {
        super(new TTObjectFilter());

        // Create 'All' filter
        objectTypeAll = new TTObjectType(0);

        // Define all objecttypes which are not wanted to be shown in TTObject page
        filterExcludors = new ArrayList<>();
        filterExcludors.add(new TTObjectType(22));
    
        // And update filter
        getFilter().addExcludingFilter(filterExcludors);
    }

    @PostConstruct
    public void init() {
        // Get all objecttypes which are wanted to be shown
        ttObjectTypes = ejbTTObjectTypeFacade.findAll();
        for (TTObjectType i : filterExcludors) {
            ttObjectTypes.remove(i);
        }
    }

    // For List&View filter - AllType
    public TTObjectType getObjectTypeAll() {
        objectTypeAll.setDescription(uiText.get("FilterAll"));
        return this.objectTypeAll;
    }

    // For List filter
    public List<TTObjectType> getTtObjectTypes() {
        return ttObjectTypes;
    }

    // For View filter
    public Set<TTObjectType> getChildTTObjectTypes() {
        Set<TTObjectType> ttobjTypes = new HashSet<>();
        for (TTObject o: getSelected().getChildObjects()) {
            ttobjTypes.add(o.getTTObjectType());
        }
        return ttobjTypes;  
    }

    // Filter selection
    public TTObjectType getSelectedTTObjectType() {
        return getFilter().getObjectTypeFilter();
    }

    // Filter selection
    public void setSelectedTTObjectType(TTObjectType selectedTTObjectType) {
        getFilter().setObjectTypeFilter(selectedTTObjectType);
    }

    // To create View child-list
    public List<TTObject> getSelectedChildObjects() {
        List<TTObject> ttObjects = new ArrayList<>();
        for (TTObject o: getSelected().getChildObjects()) {
            // Add if filter does not exists is 0 (all) or exact match
            if (getFilter().getObjectTypeFilter() == null ||
                    getFilter().getObjectTypeFilter().getTTObjTypeId() == 0 || 
                    o.getTTObjectType().equals(getFilter().getObjectTypeFilter()))
                ttObjects.add(o);
        }
        return ttObjects;
    }
   
    @Override
    protected TTObjectFacade getFacade() {
        return ejbFacade;
    }

    @Override
    protected void clearFilters() {
        getFilter().setObjectTypeFilter(null);
    }
    
    @Override
    public TTObject constructNewItem() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String save(TTObject item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String destroy(TTObject item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

