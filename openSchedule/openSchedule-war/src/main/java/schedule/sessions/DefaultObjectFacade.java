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
import schedule.entities.ActionType;
import schedule.entities.DefaultObject;

/**
 *
 * @author Jia Li
 */
@Stateless
public class DefaultObjectFacade extends AbstractFacade<DefaultObject> {

    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public DefaultObjectFacade() {
        super(DefaultObject.class);
    }

//    public String getScheduleName(String externalCode, Integer direction) {
//        List<Object> list = em.createNamedQuery("DefaultObject.findScheduleName").setParameter("extCode", externalCode).setParameter("dir", direction).getResultList();
//        if (list != null && list.size() > 0) {
//            return (String) list.get(0);
//        } else {
//            return null;
//        }
//    }

    public ActionType getDefaultAction(String prevScheduleName, String scheduleName) {
        List<Object> list = em.createNamedQuery("DefaultObject.findDefaultAction").setParameter("prevScheduleName", prevScheduleName).setParameter("scheduleName", scheduleName).getResultList();
        if (list != null && list.size() > 0) {
            return (ActionType) list.get(0);
        } else {
            System.out.println("DefaultObjectFacade.getDefaultAction null: " + prevScheduleName + " " + scheduleName);
            return null;
        }
    }
    
    public Integer getDefaultMovementId(String prevScheduleName, String scheduleName) {
        List<Object> list = em.createNamedQuery("DefaultObject.findDefaultMovementId").setParameter("prevScheduleName", prevScheduleName).setParameter("scheduleName", scheduleName).getResultList();
        if (list != null && list.size() > 0) {
            return (Integer) list.get(0);
        } else {
            System.out.println("DefaultObjectFacade.findDefaultMovementId null: " + prevScheduleName + " " + scheduleName);
            return null;
        }
    }
    
    public Integer getAlternativeMovementId(String prevScheduleName, String scheduleName) {
        List<Object> list = em.createNamedQuery("DefaultObject.findAlternativeMovementId").setParameter("prevScheduleName", prevScheduleName).setParameter("scheduleName", scheduleName).getResultList();
        if (list != null && list.size() > 0) {
            return (Integer) list.get(0);
        } else {
            System.out.println("DefaultObjectFacade.findAlternativeMovementId null: " + prevScheduleName + " " + scheduleName);
            return null;
        }
    }
}
