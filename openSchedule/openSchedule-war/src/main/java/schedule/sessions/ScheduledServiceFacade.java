/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.sessions;

import java.util.ArrayList;
import java.util.List;
import schedule.entities.ScheduledService;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;


/**
 *
 * @author EBIScreen
 */
@Stateless
public class ScheduledServiceFacade extends AbstractFacade<ScheduledService> {

    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager em;
    
    @Inject
    protected MainActionTypeFacade ejbActionMainTypeFacade;
    @Inject
    protected ActionTypeFacade ejbActionTypeFacade;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ScheduledServiceFacade() {
        super(ScheduledService.class);
    }

    public void evict(ScheduledService scheduledService) {
        Cache cache = this.getEntityManager().getEntityManagerFactory().getCache();
        cache.evict(ScheduledService.class, scheduledService.getTripId());
    }

    @SuppressWarnings("unchecked")
    public List<ScheduledService> findScheduledServiceRange(int startDayCode, int endDayCode) {

        List<ScheduledService> list = getEntityManager().createNamedQuery("ScheduledService.findByScheduledDay").setParameter("dayCode1", endDayCode).setParameter("dayCode2", startDayCode).getResultList();
        //em.createNamedQuery("ScheduledService.deleteByScheduledDay").setParameter("dayCode1", endDayCode).setParameter("dayCode2", startDayCode).executeUpdate();

        return list;

    }

    /*public void deleteScheduledServiceRange(int startDayCode, int endDayCode) {
        em.createNamedQuery("ScheduledService.deleteNonArchiveNonActive").setParameter("dayCode1", endDayCode).setParameter("dayCode2", startDayCode).executeUpdate();
        em.createNamedQuery("ScheduledService.deleteBySetInvalid").setParameter("dayCode1", endDayCode).setParameter("dayCode2", startDayCode).executeUpdate();
    }*/
    @SuppressWarnings("unchecked")
    public List<ScheduledService> findInvalidByDayTypes(List<Integer> dayTypes) {
        return (dayTypes == null || dayTypes.isEmpty()) ? new ArrayList<ScheduledService>() : getEntityManager().createNamedQuery("ScheduledService.findInvalidByDayType").setParameter("a", dayTypes).getResultList();
    }

    @SuppressWarnings("unchecked")
    public boolean isValidExistByDayType(List<Integer> dayTypes) {

        if (dayTypes == null || dayTypes.isEmpty()) {
            return false;
        } else {
            return (Long) getEntityManager().createNamedQuery("ScheduledService.countValidByDayType").setParameter("a", dayTypes).getSingleResult() > 0;
        }
    }
}
