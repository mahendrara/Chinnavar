/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.uiclasses;

import java.util.ArrayList;
import java.util.List;
import schedule.entities.ScheduledService_;
import schedule.entities.ScheduledService;
import schedule.entities.ScheduledDay;
import schedule.entities.TTArea;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import schedule.entities.PlannedService;
import schedule.entities.SchedulingState;
import schedule.entities.ServiceTrip_;
import schedule.entities.TrainType;
import schedule.entities.Trip_;

/**
 *
 * @author EBIScreen
 */
//public class ScheduledServiceFilter<F extends ScheduledService> extends ServiceFilter<F> {
public  class ScheduledServiceFilter extends ServiceFilter<ScheduledService>  {

    private ScheduledDay scheduledDay;
    private PlannedService sourceTrip;
    private SchedulingState currentState = null;
    private List<Integer> ids;

    @SuppressWarnings("unchecked")
    public void addPredicate(CriteriaBuilder qb, CriteriaQuery cq, Root<ScheduledService> p, boolean useOrdering) {

        List<Predicate> predicates = new ArrayList<>();

        if (scheduledDay != null) {
            Path<ScheduledDay> path = p.get(ScheduledService_.day);
            //cq.where(qb.equal(path, this.templateId));
            predicates.add(qb.equal(path, scheduledDay));
        }
        if (sourceTrip != null) {
            Path<PlannedService> path = p.get(ScheduledService_.sourceTrip);
            predicates.add(qb.equal(path, sourceTrip));
        }
        if (currentState != null) {
            Path<SchedulingState> path = p.get(Trip_.currentState);
            predicates.add(qb.equal(path, this.currentState));
        }

        // Copypaste from ServiceFilter
        if (areaObj != null) {
            Path<TTArea> path = p.get(Trip_.areaObj);
            predicates.add(qb.equal(path, areaObj));
        }
        if (trainType != null) {
            Path<TrainType> path = p.get(Trip_.trainType);
            if (trainType.getTrainSubTypes().isEmpty()) {
                predicates.add(qb.equal(path, trainType));
            } else {
                predicates.add(path.in(trainType.getTrainSubTypes()));
            }
        }
        if (plannedState != null) {
            Path<SchedulingState> path = p.get(Trip_.plannedState);
            predicates.add(qb.equal(path, this.plannedState));
        }

        // Filter by validity type if it is set
        if (valid != null) {
            Path<Boolean> path = p.get(Trip_.valid);
            predicates.add(qb.equal(path, true));
        }
        
        if (ids != null) {
            Path<Integer> path = p.get(Trip_.tripId);
            predicates.add(path.in(ids));
        }

        cq.where(qb.and(predicates.toArray(new Predicate[predicates.size()])));
        if (useOrdering) {
            cq.orderBy(qb.asc(p.get(ServiceTrip_.plannedStartSecs)));
        }
    }

    public void setScheduledDayFilter(ScheduledDay scheduledDayCode) {
        this.scheduledDay = scheduledDayCode;
    }

    public ScheduledDay getScheduledDayFilter() {
        return scheduledDay;
    }

    public void setSourceTripFilter(PlannedService sourceTrip) {
        this.sourceTrip = sourceTrip;
    }

    public void setCurrentStateFilter(SchedulingState currentState) {
        this.currentState = currentState;
    }

    public SchedulingState getCurrentStateFilter() {
        return currentState;
    }
    public void setIds(List<Integer> ids) {
        this.ids = ids;
    }
}
