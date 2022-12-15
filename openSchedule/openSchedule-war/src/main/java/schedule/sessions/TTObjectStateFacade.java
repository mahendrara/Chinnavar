/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule.sessions;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import schedule.entities.TTObjectState;

/**
 *
 * @author spirttin
 */
@Stateless
public class TTObjectStateFacade extends AbstractFacade<TTObjectState> {
    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public TTObjectStateFacade() {
        super(TTObjectState.class);
    }
    
}
