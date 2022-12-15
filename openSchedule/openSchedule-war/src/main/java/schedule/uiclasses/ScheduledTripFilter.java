/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.uiclasses;

import java.util.ArrayList;
import java.util.List;
import schedule.entities.ScheduledTrip_;
import schedule.entities.ScheduledTrip;
import schedule.entities.TTArea;
import schedule.sessions.AbstractPredicate;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import schedule.entities.BasicTrip;
import schedule.entities.PlannedTrip;
import schedule.entities.ScheduledDay;
import schedule.entities.TTObject;
import schedule.entities.TrainType;
import schedule.entities.Trip_;
import schedule.entities.TimedTrip_;

public class ScheduledTripFilter extends AbstractPredicate<ScheduledTrip> {

    private ScheduledDay scheduledDay = null;
    private TTArea owner = null;
    private BasicTrip basicTrip;
    private TrainType trainType;
    private TTObject startTTObject;
    private Boolean valid;
    private PlannedTrip sourceTrip;

    //private boolean useOrder;
    public void setSelectedStartTTObjectFilter(TTObject startTTObject) {
        this.startTTObject = startTTObject;
    }

    public void setTripTemplateFilter(BasicTrip basicTrip) {
        this.basicTrip = basicTrip;
    }

    public void setTTAreaFilter(TTArea ttArea) {
        this.owner = ttArea;
    }

    public void setScheduledDayFilter(ScheduledDay scheduledDay) {
        this.scheduledDay = scheduledDay;
    }

    public void setTrainTypeFilter(TrainType trainType) {
        this.trainType = trainType;
    }

    public void setValidFilter(Boolean valid) {
        this.valid = valid;
    }

    public void setSourceTripFilter(PlannedTrip sourceTrip) {
        this.sourceTrip = sourceTrip;
    }
    /*public void setUseOrder(boolean useOrder) {
     this.useOrder = useOrder;
     }*/
    @Override
    @SuppressWarnings("unchecked")
    public void addPredicate(CriteriaBuilder qb, CriteriaQuery cq, Root<ScheduledTrip> p, boolean useOrdering) {
        List<Predicate> predicates = new ArrayList<>();

        if (scheduledDay != null) {
            Path<ScheduledDay> path = p.get(ScheduledTrip_.day);
            predicates.add(qb.equal(path, scheduledDay));
        }

        if (owner != null) {
            Path<TTArea> ownerPath = p.get(Trip_.areaObj);
            predicates.add(qb.equal(ownerPath, this.owner));

        }

        if (basicTrip != null) {
            Path<BasicTrip> path = p.get(TimedTrip_.tripTemplate);
            predicates.add(qb.equal(path, this.basicTrip));
        }

        if (trainType != null) {
            Path<TrainType> path = p.get(Trip_.trainType);
            predicates.add(qb.equal(path, trainType));
        }

        /*if (stationFilter) {
            Path<TTObject> startObjPath = p.get(ScheduledTrip_.plannedStartObj);
            Path<Integer> startIDPath = startObjPath.get(TTObject_.ttObjId);
            //predicates.add(startObjPath.in(station.getChildObjects()));
            ArrayList<Integer> startLocationIDs = new ArrayList<>();

            if (station.getChildCount() > 0) {
                List<TTObject> objects = station.getChildObjects();
                for (TTObject obj : objects) {
                    startLocationIDs.add(obj.getTTObjId());
                }
            }
            predicates.add(startIDPath.in(startLocationIDs));
        }*/
        if(startTTObject != null) {
            Path<TTObject> startObjPath = p.get(Trip_.plannedStartObj);
            predicates.add(qb.equal(startObjPath, this.startTTObject));
        }

        if (valid != null) {
            Path<Boolean> path = p.get(Trip_.valid);
            predicates.add(qb.equal(path, valid));
        }

         if (sourceTrip != null) {
            Path<PlannedTrip> path = p.get(ScheduledTrip_.sourceTrip);
            predicates.add(qb.equal(path, sourceTrip));
        }
        cq.where(qb.and(predicates.toArray(new Predicate[predicates.size()])));

        if (useOrdering) {
            cq.orderBy(qb.asc(p.get(TimedTrip_.plannedStartSecs)));
        }
    }

}
