/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.sessions;

import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import schedule.entities.TimeBlock;

/**
 *
 * @author Jia Li
 */
@Stateless
public class TimeBlockFacade extends AbstractFacade<TimeBlock> {

    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public TimeBlockFacade() {
        super(TimeBlock.class);
    }
    public void evict(TimeBlock timeBlock) {
        Cache cache = this.em.getEntityManagerFactory().getCache();
        cache.evict(TimeBlock.class, timeBlock.getBlockId());
    }
    
     @SuppressWarnings("unchecked")
    public List<TimeBlock> findByDayTypes(List<Integer> ids) {
        if (!ids.isEmpty()) {
            List<TimeBlock> timeBlocks = em.createNamedQuery("TimeBlock.findByDayTypes").setParameter("a", ids).getResultList();
            return timeBlocks;
        }else
            return new ArrayList<>();
    }
    
//     @SuppressWarnings("unchecked")
//    public void deleteByDayTypes(List<Integer> ids) {
//        if (!ids.isEmpty()) {
//            em.createNamedQuery("TimeBlock.deleteByDayType").setParameter("a", ids).executeUpdate();
//        }
//    }
}
