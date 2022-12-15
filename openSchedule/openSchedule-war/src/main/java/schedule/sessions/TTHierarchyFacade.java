/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.sessions;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import schedule.entities.TTHierarchy;
import schedule.entities.TTObject;

/**
 *
 * @author Jia Li
 */
@Stateless
public class TTHierarchyFacade extends AbstractFacade<TTHierarchy> {

    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public TTHierarchyFacade() {
        super(TTHierarchy.class);
    }

    public void addChild(TTHierarchy newHierarchy) {
        em.persist(newHierarchy);
    }

    public void removeChild(TTObject parent, TTObject child) {
        getEntityManager().createNamedQuery("TTHierarchy.deleteChild")
                .setParameter("parentid", parent.getTTObjId())
                .setParameter("childid", child.getTTObjId()).executeUpdate();
    }
}
