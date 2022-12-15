/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.sessions;

import java.util.List;
import schedule.entities.BasicTrip;
import javax.ejb.Stateless;
import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import schedule.entities.DayType;

/**
 *
 * @author EBIScreen
 */
@Stateless
public class TripFacade extends AbstractFacade<BasicTrip> {

    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public TripFacade() {
        super(BasicTrip.class);
        //exactClass = false;
    }

    /*public void persistActionForTrip(TripAction tripAction) {
        getEntityManager().persist(tripAction);
    }*/
    public void cloneTrip(BasicTrip source, BasicTrip newTrip) {

        //EntityTransaction tx = em.getTransaction();
        //tx.begin();
        source.cloneDataToClonedTrip(newTrip);
        newTrip.setVersion(1);
        create(newTrip);

        //newTrip.persistActions(this);
        //persistActions(newTrip);
        //tx.commit();
    }

    /*public boolean hasSchedulesInTripsByDaytype(DayType dayType) {
        if (dayType == null) return false;
        
        String query = "select count (*) from trips where (objectclass = 'M' or objectclass = 'S') and daytypeid = " + dayType.getDayTypeId();
        Object o = getEntityManager().createNativeQuery(query).getSingleResult();
                
        return (o != null && ((Long)o > 0)) ;
    }*/
    public boolean isValidExistByDayType(DayType dayType) {

        if (dayType == null) {
            return false;
        } else {
            String query = "select count (*) from trips where valid=true and daytypeid = " + dayType.getDayTypeId();
            Object o = getEntityManager().createNativeQuery(query).getSingleResult();

            return (o != null && ((Long) o > 0));
        }
    }

    public void evict(BasicTrip basicTrip) {
        Cache cache = this.em.getEntityManagerFactory().getCache();
        cache.evict(BasicTrip.class, basicTrip.getTripId());
    }
}
