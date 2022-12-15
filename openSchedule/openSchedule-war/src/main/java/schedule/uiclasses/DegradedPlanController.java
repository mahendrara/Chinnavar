package schedule.uiclasses;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import schedule.entities.DegradedPlan;
import schedule.entities.DegradedPlanGroup;
import schedule.entities.ScheduledService;
import schedule.entities.SchedulingState;
import schedule.entities.TTArea;
import schedule.entities.TimedTrip;
import schedule.entities.TrainType;
import schedule.messages.Operation;
import schedule.messages.XmlMessageSender;
import schedule.sessions.AbstractFacade;
import schedule.sessions.DegradedPlanFacade;
import schedule.sessions.DegradedPlanGroupFacade;
import schedule.sessions.ScheduledServiceFacade;
import schedule.sessions.ScheduledTripFacade;
import schedule.sessions.SchedulingStateFacade;
import schedule.sessions.TTAreaFacade;
import schedule.sessions.TrainTypeFacade;
import schedule.uiclasses.util.JsfUtil;
import schedule.uiclasses.util.UiText;

/**
 *
 * @author Jia Li
 */
@Named("degradedPlanController")
@SessionScoped
public class DegradedPlanController extends FilterController<ScheduledService, ScheduledServiceFilter> implements Serializable {

    @Inject
    protected XmlMessageSender xmlMessageSender;
    @Inject
    private DegradedPlanGroupFacade ejbDegradedPlanGroupFacade;
    @Inject
    private DegradedPlanFacade ejbDegradedPlanFacade;
    @Inject
    private ScheduledServiceFacade ejbDegradedServiceFacade;
    @Inject
    private TrainTypeFacade ejbTrainTypeFacade;
    @Inject
    private TTAreaFacade ejbTTAreaFacade;
    @Inject
    private SchedulingStateFacade ejbSchedulingStateFacade;
    @Inject
    private ScheduledTripFacade ejbScheduledTripFacade;
    @Inject
    protected UiText uiText;

    private DegradedPlan degradedPlan;
    private DegradedPlanGroup degradedPlanGroup;

    public DegradedPlanController() {
        super(new ScheduledServiceFilter());
    }

    @PostConstruct
    protected void init() {
        this.getFilter().setValidFilter(Boolean.TRUE);

        List<DegradedPlanGroup> groups = getDegradedPlanGroups();
        if (groups != null && !groups.isEmpty()) {
            degradedPlanGroup = groups.get(0);
            degradedPlan = degradedPlanGroup.getDegradedPlans().isEmpty() ? null : degradedPlanGroup.getDegradedPlans().get(0);
            degradedPlanChanged();
        }
    }

    public List<DegradedPlan> getDegradedPlans() {
        return degradedPlanGroup.getDegradedPlans();
    }

    public List<DegradedPlanGroup> getDegradedPlanGroups() {
        return ejbDegradedPlanGroupFacade.findAll();
    }

    public DegradedPlan getDegradedPlan() {
        return degradedPlan;
    }

    public void setDegradedPlan(DegradedPlan degradedPlan) {
        this.degradedPlan = degradedPlan;
    }

    public DegradedPlanGroup getDegradedPlanGroup() {
        return degradedPlanGroup;
    }

    public void setDegradedPlanGroup(DegradedPlanGroup degradedPlanGroup) {
        this.degradedPlanGroup = degradedPlanGroup;
    }

    public List<TrainType> getTrainTypes() {
        // Always create a new list, since it can be changed 
        List<TrainType> trainTypes = ejbTrainTypeFacade.findAll();
        Iterator<TrainType> iterator = trainTypes.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getTrainSubTypes().size() > 0) {
                iterator.remove();
            }
        }

        return trainTypes;
    }

    public List<TTArea> getAreas() {
        return ejbTTAreaFacade.findAll();
    }

    public List<SchedulingState> getSchedulingStates() {
        return ejbSchedulingStateFacade.findAll();
    }
    
    public TrainType getTrainTypeAll() {
        return new TrainType(0, uiText.get("FilterAll"), "");
    }
    
    public TTArea getAreaAll() {
        return new TTArea(0, uiText.get("FilterAll"));
    }

    public SchedulingState getSchedulingStateAll() {
        return new SchedulingState(0, uiText.get("FilterAll"));
    }

    public void degradedPlanGroupChanged() {
        degradedPlan = degradedPlanGroup.getDegradedPlans().isEmpty() ? null : degradedPlanGroup.getDegradedPlans().get(0);
        degradedPlanChanged();
    }

    public void degradedPlanChanged() {
        if (degradedPlan != null) {
            List<Integer> ids = new ArrayList<>();
            Iterator<ScheduledService> iterator = degradedPlan.getDegradedServices().iterator();
            while (iterator.hasNext()) {
                ids.add(iterator.next().getTripId());
            }
            if(ids.isEmpty()) {
                ids.add(0); // ids cannot be null to use the filter
            }
            this.getFilter().setIds(ids);
                
        }
        recreateModel();
    }

    public List<TimedTrip> getTimedTripsForSelected() {
        ScheduledService item = getSelected();
        List<TimedTrip> timedTrips = new ArrayList<>();
        for(TimedTrip timedTrip: item.getTimedTrips()) {
            if(!timedTrip.getServiceAction().isCreating())
            timedTrips.add(timedTrip);
        }
        return timedTrips;
    }
    public void filterChanged() {
        recreateModel();
    }
    

    protected void updateState() {
        ejbDegradedPlanFacade.evictAll();
        this.ejbDegradedServiceFacade.evictAll();
        //getFacade().evictAll();
        ejbScheduledTripFacade.evictAll();
        //ejbTripFacade.evictAll();
    }
    @Override
    public String save(ScheduledService item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String destroy(ScheduledService item) {
        try {
            //Many to many relationship. Removal of degraded plan will not remove degraded service from database
            degradedPlan.removeDegradedService(item);
            ejbDegradedPlanFacade.edit(degradedPlan);
            degradedPlanChanged();
            
            List<DegradedPlan> degradedPlans = this.ejbDegradedPlanFacade.findAll();
            boolean used= false;
            for(DegradedPlan plan : degradedPlans) {
                for(ScheduledService degradedService : plan.getDegradedServices()) {
                    if(Objects.equals(degradedService.getTripId(), item.getTripId())) {
                        used = true;
                        break;
                    }
                }
                if(used)
                    break;
            }
            
            updateState();
            JsfUtil.addSuccessMessage(uiText.get("ServiceDeleted"));
            
            if(!used) {
                this.ejbDegradedServiceFacade.remove(item);
                xmlMessageSender.sendServiceChangeMsg(item, Operation.DELETE);
            }
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
        }
        recreateModel();
        return "List";
    }

    @Override
    protected AbstractFacade<ScheduledService> getFacade() {
        return ejbDegradedServiceFacade;
    }

    @Override
    public ScheduledService constructNewItem() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
