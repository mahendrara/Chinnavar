/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.sessions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import schedule.entities.ScheduledDuty;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author EBIScreen
 */
@Stateless
public class ScheduledDutyFacade extends AbstractFacade<ScheduledDuty> {

    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ScheduledDutyFacade() {
        super(ScheduledDuty.class);
        exactClass = false;
    }

    @SuppressWarnings("unchecked")
    public List<ScheduledDuty> findByDayCodes(int startDayCode, int endDayCode) {
        List<ScheduledDuty> scheduledDuties = em.createNamedQuery("ScheduledDuty.findByScheduledDay").setParameter("dayCode1", endDayCode).setParameter("dayCode2", startDayCode).getResultList();
        return scheduledDuties;
    }
    
    @SuppressWarnings("unchecked")
     public List<ScheduledDuty> findByDayTypes(List<Integer> ids) {
         if (!ids.isEmpty()) {
            List<ScheduledDuty> scheduledDuties = em.createNamedQuery("ScheduledDuty.findByDayTypes").setParameter("a", ids).getResultList();
            return scheduledDuties;
         }else
             return new ArrayList<>();
     }
    
    public void delete(List<ScheduledDuty> scheduledDuties){
        Iterator<ScheduledDuty> it = scheduledDuties.iterator();
        while (it.hasNext()) {
            this.remove(it.next());
        }
    }
    
//    @SuppressWarnings("unchecked")
//    public void deleteScheduledDutyRange(int startDayCode, int endDayCode) {
//        List<ScheduledDuty> scheduledDuties = em.createNamedQuery("ScheduledDuty.findByScheduledDay").setParameter("dayCode1", endDayCode).setParameter("dayCode2", startDayCode).getResultList();
//        Iterator<ScheduledDuty> it = scheduledDuties.iterator();
//        while (it.hasNext()) {
//            this.remove(it.next());
//        }
//    }
//
//    @SuppressWarnings("unchecked")
//    public void deleteByDayTypes(List<Integer> ids) {
//        if (!ids.isEmpty()) {
//            //em.createNamedQuery("ScheduledDuty.deleteByDayTypes").setParameter("a", ids).executeUpdate();
//            List<ScheduledDuty> scheduledDuties = em.createNamedQuery("ScheduledDuty.findByDayTypes").setParameter("a", ids).getResultList();
//            Iterator<ScheduledDuty> it = scheduledDuties.iterator();
//            while (it.hasNext()) {
//                this.remove(it.next());
//            }
//        }
//    }
//
//    public void deleteByDayType(Integer id) {
//        ScheduledDuty scheduledDuty = this.find(id);
//        if (scheduledDuty != null) {
//            this.remove(scheduledDuty);
//        }
//        //em.createNamedQuery("ScheduledDuty.deleteByDayType").setParameter("a", id).executeUpdate();
//    }
}
