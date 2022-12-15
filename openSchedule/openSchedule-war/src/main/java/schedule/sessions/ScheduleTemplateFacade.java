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
import schedule.entities.ScheduleTemplate;

/**
 *
 * @author devel1
 */
@Stateless
public class ScheduleTemplateFacade extends AbstractFacade<ScheduleTemplate> {

    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager em;
    private final int defaultTemplateID = 1;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ScheduleTemplateFacade() {
        super(ScheduleTemplate.class);
    }

    @Override
    public void create(ScheduleTemplate scheduleTemplate) {
        ScheduleTemplate defaultTemplate = getEntityManager().find(ScheduleTemplate.class, defaultTemplateID);
        if (defaultTemplate != null) {
            defaultTemplate.cloneDayCodesTo(scheduleTemplate);
        } else {
            scheduleTemplate.createEmptyDayCodes(scheduleTemplate);
        }
        super.create(scheduleTemplate);
    }

    public void editDefaultDayrules(ScheduleTemplate scheduleTemplate) {
        ScheduleTemplate scheduleTemplateInDb = this.find(scheduleTemplate.getScheduleId());
        scheduleTemplateInDb.setDefaultDayRules(scheduleTemplate.getDefaultDayRules());
        super.edit(scheduleTemplateInDb);
    }

    public void evict(ScheduleTemplate scheduleTemplate) {
        Cache cache = this.em.getEntityManagerFactory().getCache();
        cache.evict(ScheduleTemplate.class, scheduleTemplate.getScheduleId());
    }

}
