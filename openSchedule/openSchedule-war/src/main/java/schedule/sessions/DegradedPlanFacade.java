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
import schedule.entities.DegradedPlan;
import schedule.entities.PlannedService;

/**
 *
 * @author Jia Li
 */
@Stateless
public class DegradedPlanFacade extends AbstractFacade<DegradedPlan> {

    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public DegradedPlanFacade() {
        super(DegradedPlan.class);
    }
    
    public void evict(DegradedPlan degradedPlan) {
        Cache cache = this.em.getEntityManagerFactory().getCache();
        cache.evict(DegradedPlan.class, degradedPlan.getPlanId());
    }
}
