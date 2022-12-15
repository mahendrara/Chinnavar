package schedule.uiclasses;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import schedule.entities.TrainConsist;
import schedule.entities.TrainType;
import schedule.uiclasses.util.JsfUtil;
import schedule.uiclasses.util.PaginationHelper;
import schedule.sessions.TrainConsistFacade;

import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.inject.Inject;
import javax.inject.Named;
import schedule.entities.ConsistState;
import schedule.entities.TTObject;
import schedule.entities.TTObjectType;
import schedule.messages.Operation;
import schedule.messages.XmlMessageSender;
import schedule.sessions.ConsistStateFacade;
import schedule.sessions.TTObjectFacade;
import schedule.sessions.TrainTypeFacade;
import schedule.uiclasses.util.UiText;

// @TODO: Needs to extend BaseController
@SuppressWarnings("unchecked")
@Named("trainConsistController")
@SessionScoped
public class TrainConsistController implements Serializable {

    private DataModel items = null;
    @Inject
    private schedule.sessions.TrainConsistFacade ejbFacade;
    @Inject
    private TrainTypeFacade ejbTrainTypeFacade;
    @Inject
    private schedule.sessions.TTObjectTypeFacade ejbTTObjectTypeFacade;
    @Inject
    private TTObjectFacade ejbTTObjectFacade;
    @Inject
    private ConsistStateFacade ejbConsistStateFacade;
    @Inject
    private UiText uiText;
    @Inject
    private XmlMessageSender xmlMessageSender;
    
    private PaginationHelper pagination;
    private final int itemsPerPage = 50;

    // Private selections in UI
    private Collection<TTObject> areaObjs;
    private Collection<ConsistState> consistStates;

    //@Inject
    //private TrainTypeController trainTypeController;
    public TrainConsistController() {
    }

    @PostConstruct
    public void init() {
        // Generate items for AreaObject-menu
        TTObjectType ot = ejbTTObjectTypeFacade.find(TTObjectType.TTObjectTypeEnum.AREA_LINE.getDBCode());
        areaObjs = ejbTTObjectFacade.findAll(ot);
        consistStates = ejbConsistStateFacade.findAll();
    }

    /*
    public TrainConsist getSelected() {
        if (current == null) {
            current = new TrainConsist();
            selectedItemIndex = -1;
        }
        return current;
    }
     */
    private TrainConsistFacade getFacade() {
        return ejbFacade;
    }

    /*public void setTraintypeController(TrainTypeController trainTypeController) {
        this.trainTypeController = trainTypeController;
    }*/
 /*
    public TrainType getSelectedTrainType()
    {
        TrainType trainType;
        if(current != null && current.getTrainType() != null)
            trainType = current.getTrainType();
        else
            trainType = new TrainType();
        return trainType;
    }
     */
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

    public String prepareList() {
        recreateModel();
        return "List";
    }

    /*
    public String prepareView() {
        current = (TrainConsist)getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return null;
    }

    public String prepareCreate() {
        current = new TrainConsist();
        selectedItemIndex = -1;
        return "Create";
    }
     */
    public String create(TrainConsist train) {
        getFacade().create(train);
        train.setCreating(false);
        train.setEditing(false);
        JsfUtil.addSuccessMessage(uiText.get("TrainConsistCreated"));
        
        // Send information to ATR that it needs to reload & send all train consists to TSUI
        // This is because TSUI needs to know train consists statuses
        xmlMessageSender.sendTrainConsistsModifiedMsg(train, Operation.CREATE);

        return null;
    }

    public String edit(TrainConsist train) {
        getFacade().edit(train);
        train.setEditing(false);
        JsfUtil.addSuccessMessage(uiText.get("TrainConsistUpdated"));

        // Send information to ATR that it needs to reload & send all train consists to TSUI
        // This is because TSUI needs to know train consists statuses
        xmlMessageSender.sendTrainConsistsModifiedMsg(train, Operation.MODIFY);

        return null;
    }

    public String destroy(TrainConsist train) {
        try {
            // Do not try to delete item which is not stored yet
            if (!train.isCreating()) {
                getFacade().remove(train);
                JsfUtil.addSuccessMessage(uiText.get("TrainConsistDeleted"));
                
                // Send information to ATR that it needs to reload & send all train consists to TSUI
                // This is because TSUI needs to know train consists statuses
                xmlMessageSender.sendTrainConsistsModifiedMsg(train, Operation.DELETE);

            }
            recreateModel();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
        }
        return "List";
    }

    public String save(TrainConsist train) {
        try {
            if (train.isCreating()) {
                return create(train);
            } else {
                return edit(train);
            }
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
            return null;
        }
    }

    public String cancel(TrainConsist train) {
        train.setEditing(false);
        return "List";
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

    public boolean isAddAllowed() {
        // TODO: Authority check here
        List<TrainConsist> array = (List<TrainConsist>) getItems().getWrappedData();
        /*if ((array != null) && (array.isEmpty() == false)) {
            if (array.get(0).isCreating()) // new template is always at index 0
            {
                return false;
            }
        }*/
        if (array == null || array.isEmpty()) {
            return true;
        } else {

            TrainConsist e = (TrainConsist) array.get(0);
            //return (e == null || !e.isCreating());
            if (e != null && e.isCreating()) {
                return false;
            } else {
                Iterator<TrainConsist> iterator = array.iterator();
                while (iterator.hasNext()) {
                    TrainConsist item = (TrainConsist) iterator.next();
                    if (item.isEditing()) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    public String addNew() {
        // Create new item and fill data to it
        TrainConsist current = new TrainConsist();
        current.setEditing(true);
        current.setCreating(true);

        //Must be set
        current.setSystemGen(false); // Manually created

        // Add new item to first of list
        List<TrainConsist> oldArray = (List<TrainConsist>) getItems().getWrappedData();
        oldArray.add(0, current);
        getItems().setWrappedData(oldArray);
        return null;
    }

    public String activateEdit(TrainConsist train) {
        train.setEditing(true);
        return null;
    }

    public Collection<TTObject> getAreaObjs() {
        return areaObjs;
    }

    public void setAreaObjs(Collection<TTObject> areaObjs) {
        this.areaObjs = areaObjs;
    }

    public List<TrainType> getTrainTypes() {
        TrainTypeFilter filter = new TrainTypeFilter();
        filter.setCanBeConsist(true);
        List<TrainType> trainTypes = this.ejbTrainTypeFacade.findAll(filter);

        return trainTypes;
    }

    public Collection<ConsistState> getConsistStates() {
        return consistStates;
    }

    public void setConsistStates(Collection<ConsistState> consistStates) {
        this.consistStates = consistStates;
    }
}
