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
import schedule.entities.ActionMovementTrip;
import schedule.entities.ActionMovementTrip_;
import schedule.entities.MovementTrip;
import schedule.entities.Trip;
import schedule.sessions.AbstractPredicate;

/**
 *
 * @author Jia Li
 */
public class ActionMovementTripFilter extends AbstractPredicate<ActionMovementTrip> {

    private Trip refTrip;
    private boolean refTripValid = false;

    @Override
    @SuppressWarnings("unchecked")
    public void addPredicate(CriteriaBuilder qb, CriteriaQuery cq, Root<ActionMovementTrip> p, boolean useOrdering) {
        List<Predicate> predicates = new ArrayList<>();

        if (refTripValid) {
            Path<MovementTrip> refTripPath = p.get(ActionMovementTrip_.refTrip);
            predicates.add(qb.equal(refTripPath, this.refTrip));
        }
        cq.where(qb.and(predicates.toArray(new Predicate[predicates.size()])));
    }

    public void setRefTripFilter(Trip refTrip) {
        this.refTrip = refTrip;
        this.refTripValid = true;
    }

}
