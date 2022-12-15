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
import schedule.entities.DayType;

/**
 *
 * @author devel1
 */
@Stateless
public class DayTypeFacade extends AbstractFacade<DayType> {
    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public DayTypeFacade() {
        super(DayType.class);
    }
    
    public void evict(DayType dayType) {
        Cache cache = this.em.getEntityManagerFactory().getCache();
        cache.evict(DayType.class, dayType.getDayTypeId());
    }
}
