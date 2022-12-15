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
import schedule.entities.TripUserType;
import schedule.entities.TripUserType_;
import schedule.sessions.AbstractPredicate;

/**
 *
 * @author Jia Li
 */
public class TripUserTypeFilter extends AbstractPredicate<TripUserType> {

    private Integer id;

    @Override
    public void addPredicate(CriteriaBuilder qb, CriteriaQuery cq, Root<TripUserType> p, boolean useOrdering) {
        List<Predicate> predicates = new ArrayList<>();
        if (id != null) {
            Path<Integer> path = p.get(TripUserType_.tripUserTypeId);

            //cq.where(qb.equal(objtypePath, this.tripType));
            predicates.add(qb.equal(path, id));
        }
        cq.where(qb.and(predicates.toArray(new Predicate[predicates.size()])));
        
    }

    public void setTripUserTypeId(Integer id) {
        this.id = id; //.getValue();
    }
}
