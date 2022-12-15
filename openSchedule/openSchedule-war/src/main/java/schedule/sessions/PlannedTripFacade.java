/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule.sessions;

import schedule.entities.DayType;
import schedule.entities.PlannedService;
import schedule.entities.PlannedTrip;
import javax.ejb.Stateless;
import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author EBIScreen
 */
@Stateless
public class PlannedTripFacade extends AbstractFacade<PlannedTrip> {
    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public PlannedTripFacade() {
        super(PlannedTrip.class);
    }
    
//    public void save(PlannedTrip plannedTrip) {
//        em.persist(plannedTrip);
//    }
    
    /*public List<PlannedTrip> findAll(DayType parent,TTArea owner,Station start,Station end) {

         PlannedTripFilter filter = new PlannedTripFilter();

         filter.SetFilter(parent,owner,start,end);

         return findAll(filter);
    }

    public List<PlannedTrip> findRange(DayType parent,TTArea owner,Station start,Station end,int[] range) {
        
        PlannedTripFilter filter = new PlannedTripFilter();

        filter.SetFilter(parent,owner,start,end);
        return findRange(range,filter);
    }

    public int count(DayType parent,TTArea owner,Station start,Station end) {
        
        PlannedTripFilter filter = new PlannedTripFilter();

        filter.SetFilter(parent,owner,start,end);
        return count(filter);
    }*/

    public List<PlannedTrip> findByDayTypeList(List<DayType> dts) {
        if (!dts.isEmpty()) {
            return getEntityManager().createNamedQuery("PlannedTrip.findByDayTypeList").setParameter("a", dts).getResultList();
        } else {
            return new ArrayList<>();
        }
    }

}
