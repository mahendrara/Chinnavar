/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule.sessions;

import schedule.entities.TrainConsist;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import schedule.entities.TrainType;

/**
 *
 * @author EBIScreen
 */
@Stateless
public class TrainConsistFacade extends AbstractFacade<TrainConsist> {
    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public TrainConsistFacade() {
        super(TrainConsist.class);
    
    }
    public boolean isTrainTypeExist(TrainType trainType) {
        if (trainType == null) {
            return false;
        } else {
            String query = "select exists(select 1 from trainconsists where traintypeid = " + trainType.getTrainTypeId() + ")";
            Object o = getEntityManager().createNativeQuery(query).getSingleResult();
            
            return (o != null && o==Boolean.TRUE);
        }
    }
}
