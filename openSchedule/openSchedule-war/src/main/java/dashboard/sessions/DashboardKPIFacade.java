/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dashboard.sessions;

import dashboard.entities.DashboardKPI;
import schedule.sessions.AbstractFacade;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author jiali
 */
@Stateless
public class DashboardKPIFacade extends AbstractFacade<DashboardKPI> {
    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public DashboardKPIFacade() {
        super(DashboardKPI.class);
    }
    
}
