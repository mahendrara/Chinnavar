/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.uiclasses;

import java.util.ArrayList;
import java.util.List;
import schedule.sessions.AbstractPredicate;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import schedule.entities.TTHierarchy;
import schedule.entities.TTHierarchyType;
import schedule.entities.TTHierarchy_;
import schedule.entities.TTObject;

//import javax.persistence.metamodel.Metamodel;
//import javax.persistence.metamodel.Type;
/**
 *
 * @author EBIScreen
 */
public class TTHierarchyFilter extends AbstractPredicate<TTHierarchy> {
    private List<TTHierarchyType> hierarchyTypes;
    private TTHierarchyType hierarchyType;
    private TTObject parentObject;
    private TTObject childObject;
  
    public TTHierarchyFilter() {
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void addPredicate(CriteriaBuilder qb, CriteriaQuery cq, Root<TTHierarchy> p, boolean useOrdering) {

        List<Predicate> predicates = new ArrayList<>();
        if (hierarchyTypes != null) {
            Path<TTHierarchyType> path = p.get(TTHierarchy_.hierarchyType);
            predicates.add(path.in(hierarchyTypes));
        }else if( hierarchyType != null) {
            Path<TTHierarchyType> path = p.get(TTHierarchy_.hierarchyType);
            predicates.add(qb.equal(path, hierarchyType));
        }
        
        if(parentObject != null) {
            Path<TTObject> path = p.get(TTHierarchy_.parentObject);
            predicates.add(qb.equal(path, parentObject));
        }
        
        if(childObject != null) {
            Path<TTObject> path = p.get(TTHierarchy_.childObject);
            predicates.add(qb.equal(path, childObject));
        }
        
        cq.where(qb.and(predicates.toArray(new Predicate[predicates.size()])));
        if (useOrdering) {
            cq.orderBy(qb.asc(p.get(TTHierarchy_.seqNo)));

        }
    }

    public List<TTHierarchyType> getHierarchyTypes() {
        return hierarchyTypes;
    }

    public void setHierarchyTypes(List<TTHierarchyType> hierarchyTypes) {
        this.hierarchyTypes = hierarchyTypes;
    }

    public TTHierarchyType getHierarchyType() {
        return hierarchyType;
    }

    public void setHierarchyType(TTHierarchyType hierarchyType) {
        this.hierarchyType = hierarchyType;
    }

    public TTObject getParentObject() {
        return parentObject;
    }

    public void setParentObject(TTObject parentObject) {
        this.parentObject = parentObject;
    }

    public TTObject getChildObject() {
        return childObject;
    }

    public void setChildObject(TTObject childObject) {
        this.childObject = childObject;
    }
}
