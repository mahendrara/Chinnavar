/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.sessions;

import schedule.entities.TrainType;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import schedule.uiclasses.TrainTypeFilter;

/**
 *
 * @author jiali
 */
@Stateless
public class TrainTypeFacade extends AbstractFacade<TrainType> {
    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public TrainTypeFacade() {
        super(TrainType.class);
    }
    

    public TrainType find(String description) {
        TrainTypeFilter filter = new TrainTypeFilter();
        filter.setDescription(description);
        return findFirst(filter);
    }

    
}
