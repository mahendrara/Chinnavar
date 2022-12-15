/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule.sessions;

import javax.ejb.Stateless;
import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import schedule.entities.MovementTripTemplate;

/**
 *
 * @author Jia Li
 */
@Stateless
public class MovementTripTemplateFacade extends AbstractFacade<MovementTripTemplate> {
    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager em;
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    public MovementTripTemplateFacade() {
        super(MovementTripTemplate.class);
        //exactClass = false;
    }
    
    public void cloneTrip(MovementTripTemplate source,MovementTripTemplate newMovementTripTemplate){
        source.cloneDataToClonedTrip(newMovementTripTemplate);
        newMovementTripTemplate.setVersion(1);
        create(newMovementTripTemplate);
    }
    
    public void evict(MovementTripTemplate tripTemplate) {
        Cache cache = this.em.getEntityManagerFactory().getCache();
        cache.evict(MovementTripTemplate.class, tripTemplate.getTripId());
    }

}
