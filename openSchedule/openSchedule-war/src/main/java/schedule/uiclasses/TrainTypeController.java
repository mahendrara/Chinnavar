package schedule.uiclasses;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import schedule.uiclasses.util.JsfUtil;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import schedule.entities.BasicTrip;
import schedule.entities.TTObject;
import schedule.entities.TrainType;
import schedule.entities.TrainTypeProperty;
import schedule.messages.Operation;
import schedule.messages.XmlMessageSender;
import schedule.sessions.BaseTripFacade;
import schedule.sessions.ShortTurnFacade;
import schedule.sessions.SpeedProfileFacade;
import schedule.sessions.TTObjectFacade;
import schedule.sessions.TrainConsistFacade;
import schedule.sessions.TrainTypeFacade;
import schedule.sessions.TrainTypePropertyFacade;
import schedule.sessions.TripFacade;
import schedule.uiclasses.util.UiText;

@Named("TrainTypeController")
@SessionScoped
public class TrainTypeController extends BaseController<TrainType> implements Serializable {

    @Inject
    private TrainTypeFacade ejbFacade;
    
    @Inject
    private TrainTypePropertyFacade ejbTrainTypePropertyFacade;
    
    @Inject
    private TTObjectFacade ejbTTObjectFacade;
    
    @Inject
    private TripFacade ejbTripFacade;
    
    @Inject
    private BaseTripFacade ejbBaseTripFacade;
    
    @Inject
    private ShortTurnFacade ejbShortTurnFacade;
    
    @Inject
    private TrainConsistFacade ejbTrainConsistFacade;
    
    @Inject
    private SpeedProfileFacade ejbSpeedProfileFacade;
    
    @Inject
    private UiText uiText;
    
    @Inject
    private XmlMessageSender xmlMessageSender;
    
    private HashMap<String,TrainType.DriveTimeEstEnum> driveTimeEst;

    public TrainTypeController() {
    }

    @PostConstruct
    public void init() {
        driveTimeEst = new HashMap<>();
        driveTimeEst.put(uiText.get("DriveTimeEstimation_OnlyStatistics"),TrainType.DriveTimeEstEnum.onlystatistics);
        driveTimeEst.put(uiText.get("DriveTimeEstimation_validStatistics"), TrainType.DriveTimeEstEnum.validstatistics);
        driveTimeEst.put(uiText.get("DriveTimeEstimation_noStatistics"),TrainType.DriveTimeEstEnum.nostatistics);
    }

    @Override
    protected TrainTypeFacade getFacade() {
        return ejbFacade;
    }

    @Override
    public String save(TrainType tt) {
        try {
            if (tt.isCreating()) {
                // Since this is a new item, fields must be updated that new TrainType would not
                // contain empty data
                tt.setUsername("tms.traintypes." + tt.getDescription().toLowerCase());
                
                if(tt.getDerivedFrom() != null)
                    tt.setDefaultlength(tt.getDerivedFrom().getDefaultlength());

                tt.setEditing(false);
                tt.setCreating(false);

                getFacade().create(tt);
                JsfUtil.addSuccessMessage(uiText.get("TrainTypeCreated"));

                // Send information to ATR that it needs to reload & send all types (train) to TSUI
                // This is because TSUI needs to know new trains color
                xmlMessageSender.sendTypesModifiedMsg(tt, Operation.CREATE);
            } else {
                tt.setCreating(false);
                tt.setEditing(false);
                getFacade().edit(tt);
                JsfUtil.addSuccessMessage(uiText.get("TrainTypeUpdated"));

                // Send information to ATR that it needs to reload & send all types (train) to TSUI
                // This is because TSUI needs to know new trains color
                xmlMessageSender.sendTypesModifiedMsg(tt, Operation.MODIFY);
            }

            getFacade().evictAll();
            this.ejbShortTurnFacade.evictAll();
            this.ejbTrainConsistFacade.evictAll();
            this.ejbBaseTripFacade.evictAll();
            
            recreateModel();
            return null;

        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
            return null;
        }
    }

    @Override
    public String destroy(TrainType tt) {
        try {
            // Do not try to delete item which is not stored yet
            if (!tt.isCreating()) {
                if(tt.getTrainSubTypes() != null && !tt.getTrainSubTypes().isEmpty()){
                    JsfUtil.addErrorMessage(uiText.get("TrainTypeHasChildren"));
                    return "List";
                }else {
                    if(ejbBaseTripFacade.isTrainTypeExist(tt)) {
                        JsfUtil.addErrorMessage(uiText.get("TrainTypeUsedByTrips"));
                        return "List";
                    }else if(ejbShortTurnFacade.isTrainTypeExist(tt)) {
                        JsfUtil.addErrorMessage(uiText.get("TrainTypeUsedByShortTurns"));
                        return "List";
                    }else if(ejbTrainConsistFacade.isTrainTypeExist(tt)) {
                        JsfUtil.addErrorMessage(uiText.get("TrainTypeUsedByConsists"));
                        return "List";
                    }
                    
                    this.ejbSpeedProfileFacade.removeAllRelatedSpeedProfiles( tt );
                    
                    getFacade().remove(tt);
                    // Send information to ATR that it needs to reload & send all types (train) to TSUI
                    // This is because TSUI needs to know new trains color
                    xmlMessageSender.sendTypesModifiedMsg(tt, Operation.DELETE);
                }
                
                JsfUtil.addSuccessMessage(uiText.get("TrainTypeDeleted"));
            }
            
            recreateModel();
            
            this.ejbShortTurnFacade.evictAll();
            this.ejbTrainConsistFacade.evictAll();
            this.ejbBaseTripFacade.evictAll();
            this.ejbFacade.evictAll();
            
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
        }
        
        return "List";
    }

    @Override
    public TrainType constructNewItem() {
        // Create new item and fill data to it
        TrainType current = new TrainType(0);
        current.setCanBeChild(true);
        
        TrainType parent = this.getTrainTypes(current).get(0);
        if( parent != null ) 
        {
            current.setObiftypeid(parent.getObiftypeid());
            current.setOnboardRadio(parent.isOnboardRadio());
            current.setGpsPosCapable(parent.isGpsPosCapable());
            current.setMovingBlockCapable(parent.isMovingBlockCapable());
            current.setUseAsVehicle(parent.isUseAsVehicle());
            current.setAssumeTraction(parent.isAssumeTraction());
            current.setDynamicConsist(parent.isDynamicConsist());
            current.setDefaultlength(parent.getDefaultlength());
            current.setCanBeConsist(parent.isCanBeConsist());
            current.setHasSpeedProfile(parent.isHasSpeedProfile());
            current.setSimulate((parent.getSimulate()));
            
            current.getTrainTypeProperties().clear();
            for (TrainTypeProperty property : parent.getTrainTypeProperties()) {
                current.setCreateProp(property.getPropid());
                addNewTrainTypeProperty(current, false);
                current.getTrainTypeProperties().get(0).setSvalue(property.getSvalue());
                current.getTrainTypeProperties().get(0).setIvalue(property.getIvalue());
            }
        }
        
        current.setGlobalDefault( false );
        current.setEditing(true);
        current.setCreating(true);

        return current;
    }

    public String addNewTrainTypeProperty(TrainType tt, boolean editable) {
        TrainTypeProperty ttp = new TrainTypeProperty();
        
        ttp.setCreating(editable);
        ttp.setEditing(editable);
        ttp.setPropid(tt.getCreateProp());
        ttp.setTraintypeid(tt);
        tt.getTrainTypeProperties().add(0, ttp);
        
        return null;
    }

    //get all parent Ids
     private String getParentId(TrainType trainType)throws Exception{  
        if(trainType != null){  
            int currentId = trainType.getTrainTypeId();
            String returnParentId ="";
            if(trainType.getDerivedFrom()!=null) {
                returnParentId= getParentId(trainType.getDerivedFrom());  
            }
                
            return returnParentId+" " +currentId + " ";  
        }else{  
            return "";  
        }  
    }  
     
    // For selection
    public List<TrainType> getTrainTypes(TrainType tt) {
        List<TrainType> list = getFacade().findAll();
        list.remove(tt);
        try {
            Iterator<TrainType> it = list.iterator();
            while(it.hasNext()) {
                TrainType trainType = it.next();
                String parentIds = getParentId(trainType);
                if(parentIds.contains(tt.getTrainTypeId().toString()))
                    it.remove();
            }
            
        } catch (Exception ex) {
            Logger.getLogger(TrainTypeController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }

    public void parentChanged(TrainType tt) {
        if(tt.isCanBeChild() == true) {
            TrainType parent = tt.getDerivedFrom();
            if(parent != null) {
                tt.setOnboardRadio(parent.isOnboardRadio());
                tt.setObiftypeid(parent.getObiftypeid());
                tt.setMovingBlockCapable(parent.isMovingBlockCapable());
                tt.setGpsPosCapable(parent.isGpsPosCapable());
                tt.setUseAsVehicle(parent.isUseAsVehicle());
                tt.setAssumeTraction(parent.isAssumeTraction());
                tt.setDynamicConsist(parent.isDynamicConsist());
                tt.setDefaultlength(parent.getDefaultlength());
                //tt.setCanBeChild(parent.isCanBeChild());
                tt.setCanBeConsist(parent.isCanBeConsist());
                tt.setSimulate(parent.getSimulate());
                tt.setHasSpeedProfile(parent.isHasSpeedProfile());

                tt.getTrainTypeProperties().clear();
                for (TrainTypeProperty property : parent.getTrainTypeProperties()) {
                    tt.setCreateProp(property.getPropid());
                        addNewTrainTypeProperty(tt, false);
                    tt.getTrainTypeProperties().get(0).setSvalue(property.getSvalue());
                    tt.getTrainTypeProperties().get(0).setIvalue(property.getIvalue());
                }
            }
        }
    }
    
    public void canBeChildChanged(TrainType tt) {
            tt.setDerivedFrom(null);
    }

    public String activateTrainTypePropertyEdit(TrainTypeProperty ttp) {
        ttp.setEditing(true);
        return null;
    }

    public String saveTrainTypeProperty(TrainTypeProperty ttp, TrainType tt) {
        try {
            if (ttp.isCreating()) {
                JsfUtil.addSuccessMessage(uiText.get("TrainTypePropertyCreated"));
            } else {
                ejbTrainTypePropertyFacade.edit(ttp);
                JsfUtil.addSuccessMessage(uiText.get("TrainTypePropertyUpdated"));
                // Send information to ATR that it needs to reload & send all types (train) to TSUI
                // This is because TSUI needs to know new trains color
                xmlMessageSender.sendTypesModifiedMsg(tt, Operation.MODIFY);
            }

            ttp.setCreating(false);
            ttp.setEditing(false);
            //recreateModel();
            return "List";

        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
            return null;
        }
    }

    public String cancelTrainTypeProperty(TrainTypeProperty ttp) {
        recreateModel();
        return null;
    }

    public String destroyTrainTypeProperty(TrainTypeProperty ttp, TrainType tt) {
        try {
            // Do not try to delete item which is not stored yet
            if(tt.isCreating()) {
                tt.getTrainTypeProperties().remove(ttp);
            }else if (!ttp.isCreating()) {
                tt.getTrainTypeProperties().remove(ttp);
                ejbTrainTypePropertyFacade.remove(ttp);
                getFacade().edit(tt);
                JsfUtil.addSuccessMessage(uiText.get("TrainTypePropertyDeleted"));
                // Send information to ATR that it needs to reload & send all types (train) to TSUI
                // This is because TSUI needs to know new trains color
                xmlMessageSender.sendTypesModifiedMsg(tt, Operation.MODIFY);
            }
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
        }
        
        recreateModel();
        return "List";
    }

    public List<TTObject> getTTObjects() {
        return ejbTTObjectFacade.findAll();
    }

    public List<BasicTrip> getTrips() {
        return ejbTripFacade.findAll();
    }

    public boolean isAddPropertyAllowed(TrainType trainType) {
        if (trainType.getUnsetTrainProperties().size() > 0) {

            List<TrainTypeProperty> array = trainType.getTrainTypeProperties();
            if (array != null && !array.isEmpty()) {
                TrainTypeProperty trainTypeProperty = array.get(0);
                if (trainTypeProperty != null && trainTypeProperty.isCreating()) {
                    return false;
                } else {
                    Iterator<TrainTypeProperty> iterator = array.iterator();
                    while (iterator.hasNext()) {
                        trainTypeProperty = (TrainTypeProperty) iterator.next();
                        if (trainTypeProperty.isEditing()) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }
    
    public HashMap<String, TrainType.DriveTimeEstEnum> getDriveTimeEst() {
        return this.driveTimeEst;
    }
    
    public String getDriveTimeEstKey( TrainType.DriveTimeEstEnum value) {
        for (Entry<String, TrainType.DriveTimeEstEnum> entry : driveTimeEst.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
      return null;
    }
}
