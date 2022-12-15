/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.sessions;

import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import schedule.entities.BasicTrip;
import schedule.entities.TimedTrip;
import schedule.uiclasses.TimedTripFilter;

/**
 *
 * @author spirttin
 */
@Stateless
public class TimedTripFacade extends AbstractFacade<TimedTrip> {
    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager em;


    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public TimedTripFacade() {
        super(TimedTrip.class);
    }

    
    /**
     * Gets every TimedTrip from DB which has uses 'trip'
     * IE TimedTrip('triptemplate') is pointing to BasicTrip('tripid')
     * @param trip
     * @return 
     */
    public List<TimedTrip> getTimedTripsForTemplate(BasicTrip trip) {
        TimedTripFilter filter = new TimedTripFilter();
        filter.setTripTemplate(trip);
        return findAll(filter);
    }
    
    public Long countByTripTemplate(Integer templateId) {
        return (Long) em.createNamedQuery("TimedTrip.countByTripTemplate").setParameter("templateId", templateId).getSingleResult();
    }
    
    public void evict(TimedTrip timedTrip) {
        Cache cache = this.em.getEntityManagerFactory().getCache();
        cache.evict(BasicTrip.class, timedTrip.getTripId());
    }
}
