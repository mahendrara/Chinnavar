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
import schedule.entities.MovementTrip;

/**
 *
 * @author Jia Li
 */
@Stateless
public class MovementTripFacade extends AbstractFacade<MovementTrip> {

    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager em;

    public MovementTripFacade() {
        super(MovementTrip.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
    public void cloneTrip(MovementTrip source,MovementTrip newMovementTrip){
        source.cloneDataToClonedTrip(newMovementTrip);
        newMovementTrip.setVersion(1);
        create(newMovementTrip);
    }

    public void evict(MovementTrip movementTrip) {
        Cache cache = this.em.getEntityManagerFactory().getCache();
        cache.evict(MovementTrip.class, movementTrip.getTripId());
    }
}
