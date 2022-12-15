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
import schedule.entities.LocationGroup;
import schedule.entities.TTObject;

/**
 *
 * @author Pavel
 */
@Stateless
public class LocationGroupFacade extends AbstractFacade<LocationGroup> {
    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public LocationGroupFacade() {
        super(LocationGroup.class);
    }
    
    public void evict(LocationGroup employee) {
        Cache cache = this.em.getEntityManagerFactory().getCache();
        cache.evict(LocationGroup.class, employee.getTTObjId());
    }
}
