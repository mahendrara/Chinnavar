/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule.sessions;

import java.util.Calendar;
import javax.ejb.Stateless;
import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import schedule.entities.Schedule;
import schedule.entities.ScheduledDay;

/**
 *
 * @author Jia Li
 */
@Stateless
public class ScheduledDayFacade extends AbstractFacade<ScheduledDay> {
    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ScheduledDayFacade() {
        super(ScheduledDay.class);
    }
    public ScheduledDay find(Calendar filterDate) {
        return find(new Integer(Schedule.convertToDayCode(filterDate)));
    }
    
    public void evict(ScheduledDay scheduledDay) {
        Cache cache = this.em.getEntityManagerFactory().getCache();
        cache.evict(ScheduledDay.class, scheduledDay.getScheduledDayCode());
    }
}
