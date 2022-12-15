/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.uiclasses;

/**
 *
 * @author Jia Li
 */
import java.util.ArrayList;
import java.util.List;
import schedule.sessions.AbstractPredicate;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import schedule.entities.Trip;
import schedule.entities.TripAction;
import schedule.entities.TripAction_;

/**
 *
 * @author EBIScreen
 */
public class TripActionFilter extends AbstractPredicate<TripAction> {

    private Integer seqNo;
    private boolean seqNoValid = false;

    private Trip trip;
    private boolean tripValid = false;

    /*private Trip refTrip;
    private boolean refTripValid = false;*/

    @Override
    @SuppressWarnings("unchecked")
    public void addPredicate(CriteriaBuilder qb, CriteriaQuery cq, Root<TripAction> p, boolean useOrdering) {

        // Currently we only have one logic implneted in this class...
        List<Predicate> predicates = new ArrayList<>();

        if (tripValid) {
            Path<Trip> tripPath = p.get(TripAction_.trip);
            predicates.add(qb.equal(tripPath, trip));
        }

        if (seqNoValid) {
            Path<Integer> seqNoPath = p.get(TripAction_.seqNo);
            predicates.add(qb.equal(seqNoPath, seqNo));
        }

        /*if (refTripValid) {
            Path<Trip> refTripPath = p.get(TripAction_.refTrip);
            predicates.add(qb.equal(refTripPath, this.refTrip));
        }*/
        cq.where(qb.and(predicates.toArray(new Predicate[predicates.size()])));
    }

    public void SetTripFilter(Trip trip) {
        this.trip = trip;
        this.tripValid = true;
    }

    public void setSeqNoFilter(Integer seqNo) {
        this.seqNo = seqNo;
        this.seqNoValid = true;
    }

    /*public void setRefTripFilter(Trip refTrip) {
        this.refTrip = refTrip;
        this.refTripValid = true;
    }*/
}
