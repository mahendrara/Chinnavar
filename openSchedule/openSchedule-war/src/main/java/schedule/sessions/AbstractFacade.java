/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule.sessions;

import java.util.List;
import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;

/**
 *
 * @author EBIScreen
 * @param <T>
 */
@SuppressWarnings("unchecked")
public abstract class AbstractFacade<T> {
    private final Class<T> entityClass;
    protected boolean exactClass;
    protected Class exactClassType;
    

    public AbstractFacade(Class<T> entityClass) {
        this.entityClass = entityClass;
        exactClass = false;
    }
    public void SetExactSuperClassFilter(Class filterClass) {
        exactClassType = filterClass;
        exactClass = true;
    } 
    public void ClearExactSuperClassFilter() {
        exactClass = false;
    }
    protected abstract EntityManager getEntityManager();

    public void create(T entity) {
        getEntityManager().persist(entity);
    }

    public void edit(T entity) {
        getEntityManager().merge(entity);
    }

    public void remove(T entity) {
        getEntityManager().remove(getEntityManager().merge(entity));
    }

    public T find(Object id) {
        return getEntityManager().find(entityClass, id);
    }

    public T findFirst(AbstractPredicate<T> pred) {
        List<T> l = findAll(pred);
        if (l.isEmpty())
            return null;
        
        return l.get(0);
    }
    
    public List<T> findAll() {

        javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();

        if(exactClass)
        {
            //javax.persistence.criteria.Root<T> p = cq.from(entityClass);
            //cq.where(p.type().in(exactClassType));
            cq.select(cq.from(exactClassType));
        }
        else
        {
            cq.select(cq.from(entityClass));
            //javax.persistence.criteria.Predicate pred = getEntityManager().getCriteriaBuilder().in;
        }
        return getEntityManager().createQuery(cq).getResultList();

    }
    public List<T> findAll(AbstractPredicate<T> pred) {

        javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();

        javax.persistence.criteria.Root<T> p = cq.from(entityClass);
        pred.addPredicate(cb,cq, p,true);

        //return getEntityManager().createQuery(cq).getResultList();
        List<T> result = getEntityManager().createQuery(cq).getResultList();
        return result;
    }

    public List<T> findRange(int[] range) {
        javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        
        if(exactClass) {
            cq.select(cq.from(exactClassType));
        }
        else {
            cq.select(cq.from(entityClass));
        }
        
        javax.persistence.Query q = getEntityManager().createQuery(cq);
        q.setMaxResults(range[1] - range[0]);
        q.setFirstResult(range[0]);
        return q.getResultList();
    }
    public List<T> findRange(int[] range,AbstractPredicate<T> pred) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        javax.persistence.criteria.CriteriaQuery cq = cb.createQuery();

        javax.persistence.criteria.Root<T> rt;
        if(exactClass) {
            rt = cq.from(exactClassType);
        }
        else {
            rt = cq.from(entityClass);
        }
        cq.select(rt);
        pred.addPredicate(cb,cq, rt,true);

        javax.persistence.Query q = getEntityManager().createQuery(cq);
        q.setMaxResults(range[1] - range[0]);
        q.setFirstResult(range[0]);
        return q.getResultList();
    }
    public int count() {
        javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        javax.persistence.criteria.Root<T> rt;
        if(exactClass) {
            rt = cq.from(exactClassType);
        }
        else {
            rt = cq.from(entityClass);
        }
        cq.select(getEntityManager().getCriteriaBuilder().count(rt));
        javax.persistence.Query q = getEntityManager().createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }
    public int count(AbstractPredicate<T> pred) {
        javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();

        javax.persistence.criteria.Root<T> p = cq.from(entityClass);

        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();

        cq.select(cb.count(p));

        pred.addPredicate(cb,cq, p,false);

        javax.persistence.Query q = getEntityManager().createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }
    
    /**
     * Clears cache, that all references would be updated
     */
    public void evictAll() {
        Cache cache = getEntityManager().getEntityManagerFactory().getCache();
        cache.evict(entityClass);
    }
}
