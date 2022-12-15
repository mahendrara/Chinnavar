package schedule.uiclasses;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import schedule.entities.TTObject;
import schedule.uiclasses.util.JsfUtil;

import java.util.List;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import schedule.entities.MovementTripTemplate;
import schedule.entities.ShortTurn;
import schedule.entities.TTLocation;
import schedule.entities.TrainType;
import schedule.messages.Operation;
import schedule.messages.XmlMessageSender;
import schedule.sessions.MovementTripTemplateFacade;
import schedule.sessions.ShortTurnFacade;
import schedule.sessions.TTObjectFacade;
import schedule.sessions.TrainTypeFacade;
import schedule.uiclasses.util.UiText;

@Named("shortTurnController")
@SessionScoped
public class ShortTurnController extends FilterController<ShortTurn, ShortTurnFilter> implements Serializable {
    @Inject
    private ShortTurnFacade ejbFacade;
    @Inject
    private TTObjectFacade ejbTTObjectFacade;
    @Inject
    private MovementTripTemplateFacade ejbMovementTripTemplateFacade;
    @Inject
    private TrainTypeFacade ebjTrainTypeFacade;
    @Inject
    private UiText uiText;
    @Inject
    private XmlMessageSender xmlMessageSender;

    // All items for pulldown menus
    private List<TTObject> ttObjects;
    private List<TTObject> ttObjectsWithNull;
    
    // Private selections in UI
    private TrainType trainTypeAll = null;

    public ShortTurnController() {
        super(new ShortTurnFilter());
        trainTypeAll = new TrainType(0);
        ttObjectsWithNull = new ArrayList<>();
    }
    
    @PostConstruct
    public void init() {
        // Generate items for menus
        ttObjects = ejbTTObjectFacade.findAll('L'); // Give L as location object to filter
  
        // Create null object and insert it first
        TTObject ttObjectAll = new TTLocation();
        ttObjectsWithNull.clear();
        ttObjectsWithNull.add(ttObjectAll);
        for (TTObject item : ttObjects) {
            ttObjectsWithNull.add(item);
        }
    }

    @Override
    protected ShortTurnFacade getFacade() {
        return ejbFacade;
    }
    
    public void clearFilters() {
        getFilter().setTrainType(null);
    }
    
    @Override
    public String destroy(ShortTurn st) {
        try {
            // Do not try to delete item which is not stored yet
            if (!st.isCreating())
            {
                getFacade().remove(st);
                xmlMessageSender.sendShortTurnMsg(st, Operation.DELETE);
                JsfUtil.addSuccessMessage(uiText.get("ShortTurnDeleted"));
            }
            recreateModel();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
        }
        return "List";
    }
        
    private boolean findTrip(TTObject from, TTObject to, boolean allowSame) {
        // Items same and it is allowed
        if (allowSame && from.equals(to)) return true;
        
        // Search from trips
        MovementTripTemplateFilter filter = new MovementTripTemplateFilter();
        filter.setPlannedStartObjFilter(from);
        filter.setPlannedStopObjFilter(to);
        List<MovementTripTemplate> list = ejbMovementTripTemplateFacade.findAll(filter);
        
        return list.size() > 0;
    }
    
    @Override
    public String save(ShortTurn st) {
        
        // Do validation!
        if (findTrip(st.getFromId(), st.getToId(), false) == false)
        {
            JsfUtil.addWarnMessage(uiText.get("Warn_FromTo_NotExisting"));
        }
    
        if (findTrip(st.getFromId(), st.getLocationId(), true) == false)
        {
            JsfUtil.addWarnMessage(uiText.get("Warn_FromLocation_NotExisting"));
        }
        
        if (findTrip(st.getLocationId(), st.getDestinationId(), false) == false)
        {
            JsfUtil.addWarnMessage(uiText.get("Warn_LocationDestination_NotExisting"));
        }
        
        if (st.isCreating()) {
            try {
                st.setCreating(false);
                st.setEditing(false);

                getFacade().create(st);

                JsfUtil.addSuccessMessage(uiText.get("ShortTurnCreated"));
                xmlMessageSender.sendShortTurnMsg(st, Operation.CREATE);
            } catch (Exception e) {
                st.setCreating(true);
                st.setEditing(true);

                JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));

                return null;
            }
        } 
        else {
            try {
                st.setEditing(false);

                getFacade().edit(st);

                JsfUtil.addSuccessMessage(uiText.get("ShortTurnUpdated"));
                xmlMessageSender.sendShortTurnMsg(st, Operation.MODIFY);
            } catch (Exception e) {
                st.setEditing(true);

                JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));

                return null;
            }
        }

        recreateModel();
        return "List";
    }
    
    @Override
    public ShortTurn constructNewItem() {
        // Create new item and fill data to it
        ShortTurn current = new ShortTurn();
        current.setEditing(true);
        current.setCreating(true);
        current.setFromId(ttObjects.get(0));
        current.setToId(ttObjects.get(0));
        current.setDestinationId(ttObjects.get(0));
        current.setLocationId(ttObjects.get(0));
        current.setFromCurrent(true); // Always true since turnback is always similar
        return current;
    }
    
    // For filter
    public Collection<TTObject> getTtObjects() {
        return ttObjects;
    }
    // For filter
    public Collection<TTObject> getTtObjectsWithNull() {
        return ttObjectsWithNull;
    }    
    // For filter
    public TrainType getTrainTypeAll() {
        trainTypeAll.setDescription(uiText.get("FilterAll"));
        return this.trainTypeAll;
    }
    
    public TrainType getSelectedTrainType() {
        return getFilter().getTrainType();
    }
    public void setSelectedTrainType(TrainType selectedTrainType) {
        getFilter().setTrainType(selectedTrainType);
    }

    // For filter
    public List<TrainType> getTrainTypes() {
        TrainTypeFilter filter = new TrainTypeFilter();
        filter.setCanBeConsist(true);
        List<TrainType> trainTypes = this.ebjTrainTypeFacade.findAll(filter);

        return trainTypes;
    }
}
