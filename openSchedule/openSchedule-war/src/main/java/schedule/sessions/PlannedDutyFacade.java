/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.sessions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import schedule.entities.DayType;
import schedule.entities.PlannedDuty;


/**
 *
 * @author jia
 */
@Stateless
public class PlannedDutyFacade extends AbstractFacade<PlannedDuty> {

    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager em;
    
    @Override
    protected EntityManager getEntityManager() {
       return em;
    }
    
//    @SuppressWarnings("unchecked")
//    public void deleteByDayTypes(List<Integer> ids) {
//        if (!ids.isEmpty()) {
//            List<PlannedDuty> scheduledDuties = em.createNamedQuery("PlannedDuty.findByDayTypes").setParameter("a", ids).getResultList();
//            Iterator<PlannedDuty> it = scheduledDuties.iterator();
//            while (it.hasNext()) {
//                this.remove(it.next());
//            }
//        }
//    }
    
     @SuppressWarnings("unchecked")
    public List<PlannedDuty> findByDayTypes(List<Integer> ids) {
        if (!ids.isEmpty()) {
            List<PlannedDuty> plannedDuties = getEntityManager().createNamedQuery("PlannedDuty.findByDayTypes").setParameter("a", ids).getResultList();
            //Iterator<PlannedDuty> it = scheduledDuties.iterator();
            //while (it.hasNext()) {
            //    this.remove(it.next());
            //}
            return plannedDuties;
        }else
            return new ArrayList<>();
    }

    public List<PlannedDuty> findByDayTypesList(List<DayType> dts) {
        if (!dts.isEmpty()) {
            List<PlannedDuty> plannedDuties = getEntityManager().createNamedQuery("PlannedDuty.findByDayTypesList").setParameter("a", dts).getResultList();
            //Iterator<PlannedDuty> it = scheduledDuties.iterator();
            //while (it.hasNext()) {
            //    this.remove(it.next());
            //}
            return plannedDuties;
        }else
            return new ArrayList<>();
    }
    
    public void delete(List<PlannedDuty> plannedDuties){
        Iterator<PlannedDuty> it = plannedDuties.iterator();
        while (it.hasNext()) {
            this.remove(it.next());
        }
    }
    
//    public void deleteByDayType(Integer id) {
//        PlannedDuty plannedDuty = this.find(id);
//        if (plannedDuty != null) {
//            this.remove(plannedDuty);
//        }
//    }
    
    public PlannedDutyFacade() {
        super(PlannedDuty.class);
    }
}
