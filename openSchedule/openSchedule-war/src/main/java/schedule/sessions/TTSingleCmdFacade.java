/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule.sessions;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import schedule.entities.TTSingleCmd;

/**
 *
 * @author Jia Li
 */
@Stateless
public class TTSingleCmdFacade extends AbstractFacade<TTSingleCmd> {
    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public TTSingleCmdFacade() {
        super(TTSingleCmd.class);
    }
    
}
