/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule.sessions;

import schedule.entities.ScheduledTrip;
import javax.ejb.Stateless;
import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author EBIScreen
 */
@Stateless
public class ScheduledTripFacade extends AbstractFacade<ScheduledTrip> {
    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ScheduledTripFacade() {
        super(ScheduledTrip.class);
    }
    
    public void deleteScheduledTripsRange(int startDayCode, int endDayCode) {
        em.createNamedQuery("ScheduledTrip.deleteNonArchiveNonActive").setParameter("dayCode1", endDayCode).setParameter("dayCode2", startDayCode).executeUpdate();
        em.createNamedQuery("ScheduledTrip.deleteBySetInvalid").setParameter("dayCode1", endDayCode).setParameter("dayCode2", startDayCode).executeUpdate();
    }
}
