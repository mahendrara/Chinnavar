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
import schedule.entities.ActionTrainFormation;
import schedule.entities.ActionTrainFormation_;
import schedule.entities.MovementTrip;
import schedule.sessions.AbstractPredicate;

/**
 *
 * @author Jia Li
 */
class ActionTrainFormationFilter extends AbstractPredicate<ActionTrainFormation> {

    private MovementTrip refTrip;
    private boolean refTripValid = false;

    @Override
    @SuppressWarnings("unchecked")
    public void addPredicate(CriteriaBuilder qb, CriteriaQuery cq, Root<ActionTrainFormation> p, boolean useOrdering) {
        // Currently we only have one logic implneted in this class...
        List<Predicate> predicates = new ArrayList<>();

        if (refTripValid) {
            Path<MovementTrip> refTripPath = p.get(ActionTrainFormation_.refTrip);
            predicates.add(qb.equal(refTripPath, this.refTrip));
        }
        cq.where(qb.and(predicates.toArray(new Predicate[predicates.size()])));
    }

    public void setRefTripFilter(MovementTrip refTrip) {
        this.refTrip = refTrip;
        this.refTripValid = true;
    }
}
