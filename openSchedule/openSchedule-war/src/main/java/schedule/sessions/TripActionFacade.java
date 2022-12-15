/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule.sessions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import schedule.entities.ScheduledService;
import schedule.entities.Trip;
import schedule.entities.TripAction;
import schedule.uiclasses.TripActionFilter;

/**
 *
 * @author Jia Li
 */
@Stateless
public class TripActionFacade extends AbstractFacade<TripAction> {
    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public TripActionFacade() {
        super(TripAction.class);
    }
    
    public TripAction find(Trip trip, Integer seqNo) {
        TripActionFilter filter = new TripActionFilter();
        filter.SetTripFilter(trip);
        filter.setSeqNoFilter(seqNo);
        List<TripAction> tripActions = super.findAll(filter);
        if(tripActions != null && tripActions.size() > 0)
            return tripActions.get(0);
        else
            return null;
    }
    
    public void deleteTripActionsRange(List<ScheduledService> scheduledServices) {
        List<Integer> ids = new ArrayList<>();
        Iterator it = scheduledServices.iterator();
        while(it.hasNext()) {
            ids.add(((ScheduledService)it.next()).getTripId());
        }
        if(!ids.isEmpty())
            em.createNamedQuery("TripAction.deleteByTrips").setParameter("a", ids).executeUpdate();
    }
}
