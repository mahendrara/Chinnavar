/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.uiclasses;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import schedule.entities.TTHierarchyType;
import schedule.entities.TTHierarchyType_;
import schedule.entities.TTObjectType;
import schedule.sessions.AbstractPredicate;

/**
 *
 * @author Jia Li
 */
public class TTHierarchyTypeFilter extends AbstractPredicate<TTHierarchyType> {
//    private TTHierarchyEnum hierarchyType;
    //private String hierarchyType;
    private TTObjectType parentObjectType;
    private TTObjectType childObjectType;
  
    public TTHierarchyTypeFilter() {
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void addPredicate(CriteriaBuilder qb, CriteriaQuery cq, Root<TTHierarchyType> p, boolean useOrdering) {

        List<Predicate> predicates = new ArrayList<>();
//        if (hierarchyType != null ) {
////            Path<TTHierarchyEnum> path = p.get(TTHierarchyType_.hierarchyType);
//            Path<String> path =  p.get(TTHierarchyType_.hierarchyType);
//            predicates.add(qb.equal(path, hierarchyType));
//        } 
      
        if (parentObjectType != null) {
            Path<TTObjectType> path = p.get(TTHierarchyType_.parentType);
            predicates.add(qb.equal(path, parentObjectType));
        }
        
        if (childObjectType != null) {
            Path<TTObjectType> path = p.get(TTHierarchyType_.childType);
            predicates.add(qb.equal(path, childObjectType));
        }
        
        cq.where(qb.and(predicates.toArray(new Predicate[predicates.size()])));
        if (useOrdering) {
            cq.orderBy(qb.asc(p.get(TTHierarchyType_.hierarchyId)));

        }
    }

//    public TTHierarchyEnum getHierarchyType() {
//        return hierarchyType;
//    }
//
//    public void setHierarchyType(TTHierarchyEnum hierarchyType) {
//        this.hierarchyType = hierarchyType;
//    }

//    public String getHierarchyType() {
//        return hierarchyType;
//    }
//
//    public void setHierarchyType(String hierarchyType) {
//        this.hierarchyType = hierarchyType;
//    }
    
    public TTObjectType getParentObjectType() {
        return parentObjectType;
    }

    public void setParentObjectType(TTObjectType parentObjectType) {
        this.parentObjectType = parentObjectType;
    }

    public TTObjectType getChildObjectType() {
        return childObjectType;
    }

    public void setChildObjectType(TTObjectType childObjectType) {
        this.childObjectType = childObjectType;
    }
}

   
