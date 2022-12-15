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
import schedule.entities.TrainType;
import schedule.entities.Trip;

/**
 *
 * @author Jia Li
 */

@Stateless
public class BaseTripFacade extends AbstractFacade<Trip> {

    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public BaseTripFacade() {
        super(Trip.class);
        //exactClass = false;
    }
  
    public boolean isTrainTypeExist(TrainType trainType) {
        if (trainType == null) {
            return false;
        } else {
            String query = "select exists(select 1 from trips where traintypeid = " + trainType.getTrainTypeId() + ")";
            Object o = getEntityManager().createNativeQuery(query).getSingleResult();

            return (o != null && o==Boolean.TRUE);
        }
    }
}
