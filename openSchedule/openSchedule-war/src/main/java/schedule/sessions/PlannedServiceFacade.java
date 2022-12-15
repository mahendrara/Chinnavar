/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.sessions;

import java.util.ArrayList;
import java.util.List;

import schedule.entities.DayType;
import schedule.entities.PlannedService;
import javax.ejb.Stateless;
import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author EBIScreen
 */
@Stateless
public class PlannedServiceFacade extends AbstractFacade<PlannedService> {

    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public PlannedServiceFacade() {
        super(PlannedService.class);
    }

    public List<PlannedService> findByDayType(List<Integer> ids) {
        if (!ids.isEmpty()) {
            return getEntityManager().createNamedQuery("PlannedService.findByDayType").setParameter("a", ids).getResultList();
        } else {
            return new ArrayList<>();
        }
    }

    public List<PlannedService> findByDayTypeList(List<DayType> dts) {
        if (!dts.isEmpty()) {
            return getEntityManager().createNamedQuery("PlannedService.findByDayTypeList").setParameter("a", dts).getResultList();
        } else {
            return new ArrayList<>();
        }
    }

    public void evict(PlannedService plannedService) {
        Cache cache = this.getEntityManager().getEntityManagerFactory().getCache();
        cache.evict(PlannedService.class, plannedService.getTripId());
    }
}
