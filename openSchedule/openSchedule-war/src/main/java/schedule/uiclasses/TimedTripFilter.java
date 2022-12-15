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
import schedule.entities.BasicTrip;
import schedule.entities.TimedTrip;
import schedule.entities.TimedTrip_;
import schedule.sessions.AbstractPredicate;

/**
 *
 * @author spirttin
 */
public class TimedTripFilter extends AbstractPredicate<TimedTrip> {
    private BasicTrip tripTemplate = null;
    
    public void setTripTemplate(BasicTrip tripTemplate) {
        this.tripTemplate = tripTemplate;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void addPredicate(CriteriaBuilder qb, CriteriaQuery cq, Root<TimedTrip> p, boolean useOrdering) {
        List<Predicate> predicates = new ArrayList<>();
        if(tripTemplate != null)
        {
            Path<BasicTrip> tripTemplatePath = p.get(TimedTrip_.tripTemplate);
            predicates.add(qb.equal(tripTemplatePath, this.tripTemplate));
         }
        cq.where(qb.and(predicates.toArray(new Predicate[predicates.size()])));
        
//        if(useOrdering)
//            cq.orderBy(qb.asc(p.get(Trip_.description)));
    }
}