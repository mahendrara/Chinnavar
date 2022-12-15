/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.uiclasses;

import java.util.ArrayList;
import java.util.List;
import schedule.entities.DayType;
import schedule.entities.TTArea;
import schedule.entities.TTObject;
import schedule.sessions.AbstractPredicate;

import javax.persistence.criteria.*;

import schedule.entities.Schedule;
import schedule.entities.SchedulingState;
import schedule.entities.ServiceTrip;
import schedule.entities.ServiceTrip_;
import schedule.entities.TrainType;
import schedule.entities.Trip_;

//import javax.persistence.metamodel.Metamodel;
//import javax.persistence.metamodel.Type;
/**
 *
 * @author EBIScreen
 * @param <T>
 */
public class ServiceFilter<T extends ServiceTrip> extends AbstractPredicate<T> {

    protected DayType dayType = null;
    protected TTObject areaObj = null;
    protected TrainType trainType = null;
    protected Boolean valid = null;
    protected SchedulingState plannedState = null;
    protected boolean scheduleFilter = false;
    protected Schedule schedule = null;
    protected TTObject plannedStartObj = null;
    protected TTObject plannedStopObj = null;
    protected Integer minPlannedStopSecs = null;
    protected Integer maxPlannedStopSecs = null;
    protected Integer minPlannedStartSecs = null;
    protected Integer maxPlannedStartSecs = null;
    protected boolean sortByStopSecs = false;

    @Override
    @SuppressWarnings("unchecked")
    public void addPredicate(CriteriaBuilder qb, CriteriaQuery cq, Root<T> p, boolean useOrdering) {

        List<Predicate> predicates = new ArrayList<>();

        if (scheduleFilter) {
            if (schedule != null) {
                if (!schedule.getDayTypes().isEmpty()) {
                    ListJoin<T, DayType> daytypeJoin = p.join(Trip_.dayTypeList);
                    Predicate pred = daytypeJoin.in(schedule.getDayTypes());
                    //Path<DayType> path = p.get(Trip_.dayType);
                    //Predicate pred = path.in(schedule.getDayTypes());
                    predicates.add(pred);
                    cq.distinct(true);
                } else {
                    Path<List<DayType>> dayTypePath = p.get(String.valueOf(Trip_.dayTypeList));
                    predicates.add(qb.isNull(dayTypePath));
                }
            } else {
                Path<List<DayType>> dayTypePath = p.get(String.valueOf(Trip_.dayTypeList));
                predicates.add(qb.isNull(dayTypePath));
            }
        }

        if (dayType != null && dayType.getDayTypeId() != null) {
            //Path<DayType> dayTypePath = p.get(Trip_.dayType);
            //predicates.add(qb.equal(dayTypePath, dayType));
            //Predicate pred = qb.equal(dayTypePath, dayType);

            ListJoin<T, DayType> daytypeJoin = p.join(Trip_.dayTypeList);
            Predicate pred1 = daytypeJoin.in(dayType);
            predicates.add(pred1);
        }

        if (areaObj != null && areaObj.getTTObjId() > 0) {
            Path<TTArea> path = p.get(Trip_.areaObj);
            predicates.add(qb.equal(path, areaObj));
        }

        if (trainType != null && trainType.getTrainTypeId() > 0) {
            Path<TrainType> path = p.get(Trip_.trainType);
            if (trainType.getTrainSubTypes().isEmpty()) {
                predicates.add(qb.equal(path, trainType));
            } else {
                predicates.add(path.in(trainType.getTrainSubTypes()));
            }
        }

        if (valid != null) {
            Path<Boolean> path = p.get(Trip_.valid);
            predicates.add(qb.equal(path, valid));
        }

        if (plannedState != null && plannedState.getSchedulingStateId() > 0) {
            Path<SchedulingState> path = p.get(Trip_.plannedState);
            predicates.add(qb.equal(path, this.plannedState));
        }

        if (plannedStartObj != null) {
            Path<TTObject> path = p.get(Trip_.plannedStartObj);
            predicates.add(qb.equal(path, this.plannedStartObj));
        }

        if (plannedStopObj != null) {
            Path<TTObject> path = p.get(Trip_.plannedStopObj);
            predicates.add(qb.equal(path, this.plannedStopObj));
        }

        if (minPlannedStopSecs != null) {
            Path<Integer> path = p.get(ServiceTrip_.plannedStopSecs);
            predicates.add(qb.greaterThanOrEqualTo(path, this.minPlannedStopSecs));
        }

        if (maxPlannedStopSecs != null) {
            Path<Integer> path = p.get(ServiceTrip_.plannedStopSecs);
            predicates.add(qb.lessThanOrEqualTo(path, this.maxPlannedStopSecs));
        }
        
        if (minPlannedStartSecs != null) {
            Path<Integer> path = p.get(ServiceTrip_.plannedStartSecs);
            predicates.add(qb.greaterThanOrEqualTo(path, this.minPlannedStartSecs));
        }

        if (maxPlannedStartSecs != null) {
            Path<Integer> path = p.get(ServiceTrip_.plannedStartSecs);
            predicates.add(qb.lessThanOrEqualTo(path, this.maxPlannedStartSecs));
        }

        cq.where(qb.and(predicates.toArray(new Predicate[predicates.size()])));
        if (this.sortByStopSecs) {
            cq.orderBy(qb.asc(p.get(ServiceTrip_.plannedStopSecs)));
        } else if (useOrdering) {
            cq.orderBy(qb.asc(p.get(ServiceTrip_.plannedStartSecs)));
        }
    }

    // Setting Schedulefilter resets daytype, as daytype is schedule
    // dependant
    public void setScheduleFilter(Schedule schedule) {
        this.scheduleFilter = true;
        this.schedule = schedule;
        //dayType = null;
    }

    public Schedule getScheduleFilter() {
        return schedule;
    }

    public void setAreaFilter(TTObject areaObj) {
        this.areaObj = areaObj;
    }

    public TTObject getAreaFilter() {
        return areaObj;
    }

    public void setDayTypeFilter(DayType dayType) {
        this.dayType = dayType;
    }

    public DayType getDayTypeFilter() {
        return dayType;
    }

    public void setTrainTypeFilter(TrainType trainType) {
        this.trainType = trainType;
    }

    public TrainType getTrainTypeFilter() {
        return trainType;
    }

    public void setValidFilter(Boolean valid) {
        this.valid = valid;
    }

    public void setPlannedStateFilter(SchedulingState plannedState) {
        this.plannedState = plannedState;
    }

    public SchedulingState getPlannedStateFilter() {
        return plannedState;
    }

    public TTObject getPlannedStartObj() {
        return plannedStartObj;
    }

    public void setPlannedStartObj(TTObject plannedStartObj) {
        this.plannedStartObj = plannedStartObj;
    }

    public TTObject getPlannedStopObj() {
        return plannedStopObj;
    }

    public void setPlannedStopObj(TTObject plannedStopObj) {
        this.plannedStopObj = plannedStopObj;
    }

    public void setMinPlannedStopSecs(Integer minPlannedStopSecs) {
        this.minPlannedStopSecs = minPlannedStopSecs;
    }

    public void setMaxPlannedStopSecs(Integer maxPlannedStopSecs) {
        this.maxPlannedStopSecs = maxPlannedStopSecs;
    }

    public void setSortByStopSecs(boolean sortByStopSecs) {
        this.sortByStopSecs = sortByStopSecs;
    }

    public void setMinPlannedStartSecs(Integer minPlannedStartSecs) {
        this.minPlannedStartSecs = minPlannedStartSecs;
    }

    public void setMaxPlannedStartSecs(Integer maxPlannedStartSecs) {
        this.maxPlannedStartSecs = maxPlannedStartSecs;
    }
}
