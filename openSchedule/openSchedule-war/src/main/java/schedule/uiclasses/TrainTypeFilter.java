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
import schedule.entities.TrainType;
import schedule.entities.TrainType_;
import schedule.sessions.AbstractPredicate;

/**
 *
 * @author Jia Li
 */
public class TrainTypeFilter extends AbstractPredicate<TrainType>{

    private TrainType derivedFrom = null;
    private String description = null;
    private Boolean canBeConsist = null;
    
    public void setDerivedFrom(TrainType derivedFrom) {
        this.derivedFrom = derivedFrom; //.getValue();
    }
    
    public void setCanBeConsist(Boolean canBeConsist) {
        this.canBeConsist = canBeConsist;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void addPredicate(CriteriaBuilder qb, CriteriaQuery cq, Root<TrainType> p, boolean useOrdering) {
        List<Predicate> predicates = new ArrayList<>();
        if (derivedFrom != null) {
            Path<TrainType> path = p.get(TrainType_.derivedFrom);
            predicates.add(qb.equal(path, derivedFrom));
        } 
        if (description != null) {
            Path<String> path = p.get(TrainType_.description);
            predicates.add(qb.equal(path, description));            
        }
        if (canBeConsist != null) {
            Path<Boolean> path = p.get(TrainType_.canBeConsist);
            predicates.add(qb.equal(path, canBeConsist));  
        }
        
        cq.where(qb.and(predicates.toArray(new Predicate[predicates.size()])));
        if(useOrdering)
            cq.orderBy(qb.asc(p.get(TrainType_.description)));
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
}
