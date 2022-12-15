/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule.sessions;

import schedule.entities.MovementTripGroup;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author EBIScreen
 */
@Stateless
public class MovementTripGroupFacade extends AbstractFacade<MovementTripGroup> {
    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public MovementTripGroupFacade() {
        super(MovementTripGroup.class);
    }
}
