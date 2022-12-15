/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.sessions;

import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import schedule.entities.TTHierarchyType;
import schedule.entities.TTObjectType;

/**
 *
 * @author Jia Li
 */
@Stateless
public class TTHierarchyTypeFacade extends AbstractFacade<TTHierarchyType> {

    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public TTHierarchyTypeFacade() {
        super(TTHierarchyType.class);
    }

    public List<TTHierarchyType> findAllByParentType(TTObjectType parentType) {
        return getEntityManager().createNamedQuery("TTHierarchyType.findAllByParentType")
                .setParameter("parenttype", parentType).getResultList();
    }
}
