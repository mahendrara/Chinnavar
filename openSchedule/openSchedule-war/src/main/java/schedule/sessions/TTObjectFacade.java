/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.sessions;

import schedule.entities.TTObject;
import schedule.entities.TTObjectType;
import schedule.uiclasses.TTObjectFilter;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author EBIScreen
 */
@Stateless
public class TTObjectFacade extends AbstractFacade<TTObject> {

    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public TTObjectFacade() {
        super(TTObject.class);
    }

    /**
     * Find all TTObjects of given object type
     *
     * @param objectType
     * @return
     */
    public List<TTObject> findAll(TTObjectType objectType) {
        TTObjectFilter filter = new TTObjectFilter();
        filter.setObjectTypeFilter(objectType);
        return findAll(filter);
    }

    public List<TTObject> findByType(TTObjectType objectType) {
        return getEntityManager().createNamedQuery("TTObject.findAllByType")
                .setParameter("objecttype", objectType).getResultList();
    }

    /**
     * Find all TTObjects of given object class
     *
     * @param objectClass
     * @return
     */
    public List<TTObject> findAll(Character objectClass) {
        TTObjectFilter filter = new TTObjectFilter();
        filter.setObjectClassFilter(objectClass);
        return findAll(filter);
    }

    public TTObject find(String scheduleName) {
        TTObjectFilter filter = new TTObjectFilter();
        filter.setScheduleName(scheduleName);
        return findFirst(filter);
    }
}
