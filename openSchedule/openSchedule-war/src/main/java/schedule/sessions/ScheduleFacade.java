/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.sessions;

import schedule.entities.Schedule;
import schedule.entities.ScheduleTemplate;
import java.util.Calendar;
import javax.ejb.Stateless;
import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author EBIScreen
 */
@Stateless
public class ScheduleFacade extends AbstractFacade<Schedule> {

    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ScheduleFacade() {
        super(Schedule.class);
    }

    public void createFromTemplate(Schedule entity, Calendar start, Calendar end) {
        ScheduleTemplate template = entity.getScheduleTemplate();
        template.cloneDayCodesTo(entity);
        //create oneToMany: schedule<->DayInSchedule; ManyToOne: DayInSchedule->DayType from template
        entity.generateDaysInSchedule(start, end);
        super.create(entity);
        //We use cascadeType for the bidirectional oneToMany relationship to replace these persist
    }
    public void evict(Schedule schedule) {
        Cache cache = this.em.getEntityManagerFactory().getCache();
        cache.evict(Schedule.class, schedule.getScheduleId());
    }

    @Override
    public void edit(Schedule schedule) {
        //Schedule scheduleInDb = this.find(schedule.getScheduleid());
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        start.setTime(schedule.getStartTime());
        end.setTime(schedule.getEndTime());

        schedule.generateDaysInSchedule(start, end);
        super.edit(schedule);
     }
    
    public void evit(Schedule schedule) {
        Cache cache = this.em.getEntityManagerFactory().getCache();
        cache.evict(Schedule.class, schedule.getScheduleId());
    }
}
