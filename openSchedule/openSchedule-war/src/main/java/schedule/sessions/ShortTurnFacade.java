/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule.sessions;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import schedule.entities.ShortTurn;
import schedule.entities.TrainType;

/**
 *
 * @author spirttin
 */
@Stateless
public class ShortTurnFacade extends AbstractFacade<ShortTurn> {
    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ShortTurnFacade() {
        super(ShortTurn.class);
    }
    
    public boolean isTrainTypeExist(TrainType trainType) {
        if (trainType == null) {
            return false;
        } else {
            String query = "select exists(select 1 from shortturns where traintypeid = " + trainType.getTrainTypeId() + ")";
            Object o = getEntityManager().createNativeQuery(query).getSingleResult();

            return (o != null && o==Boolean.TRUE);
        }
    }
}
