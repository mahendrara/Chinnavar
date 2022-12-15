/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.uiclasses;

import schedule.entities.DayType;
import schedule.entities.PlannedTrip;
import schedule.entities.TTArea;
import schedule.sessions.AbstractPredicate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.*;

import schedule.entities.BasicTrip;
import schedule.entities.TimedTrip_;
import schedule.entities.TTObject;
import schedule.entities.TrainType;
import schedule.entities.Trip_;

/**
 *
 * @author EBIScreen
 */
public class PlannedTripFilter extends AbstractPredicate<PlannedTrip> {

    boolean dayTypeFilter = false;
    private DayType dayType;

    private TTArea area;

    //private Station branch;
    //private boolean branchFilter;
    private TTObject startTTObject;

    private BasicTrip basicTrip;

    private TrainType trainType;

    private Boolean valid;
    private TTObject startObj;
    private TTObject stopObj;
    private boolean sortByStopSecs = false;
    private Integer minPlannedStopSecs = null;
    private Integer maxPlannedStopSecs = null;

    public void setTripTemplateFilter(BasicTrip basicTrip) {
        this.basicTrip = basicTrip;
    }

    public void setValidFilter(Boolean valid) {
        this.valid = valid;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addPredicate(CriteriaBuilder qb, CriteriaQuery cq, Root<PlannedTrip> p, boolean useOrdering) {
        List<Predicate> predicates = new ArrayList<>();

        if (dayTypeFilter) {
            //Path<DayType> dayTypePath = p.get(Trip_.dayType);
            //cq.where(qb.equal(path, this.templateId));
            //predicates.add(qb.equal(dayTypePath, dayType));
            ListJoin<PlannedTrip, DayType> daytypeJoin = p.join(Trip_.dayTypeList);
            Predicate pred1 = qb.equal(daytypeJoin, dayType);
            predicates.add(pred1);
        }
        if (area != null) {
            Path<TTArea> path = p.get(Trip_.areaObj);
            predicates.add(qb.equal(path, area));
        }

        if (basicTrip != null) {
            Path<BasicTrip> path = p.get(TimedTrip_.tripTemplate);
            predicates.add(qb.equal(path, this.basicTrip));
        }

        if (trainType != null) {
            Path<TrainType> path = p.get(Trip_.trainType);
            predicates.add(qb.equal(path, trainType));
        }

        if (valid != null) {
            Path<Boolean> path = p.get(Trip_.valid);
            predicates.add(qb.equal(path, valid));
        }

        if (startObj != null) {
            Path<TTObject> path = p.get(Trip_.plannedStartObj);
            predicates.add(qb.equal(path, startObj));
        }

        if (stopObj != null) {
            Path<TTObject> path = p.get(Trip_.plannedStopObj);
            predicates.add(qb.equal(path, stopObj));
        }
        /*if (branchFilter) {
         Path<TTObject> startBranchPath = p.get(PlannedTrip_.plannedStartObj);
         //            Path<Integer> startBranchIDPath = startBranchPath.get(TTObject_.ttObjId);
         //             ArrayList<Integer> startBranchIDs = new ArrayList<>();
         //             for(TTObject obj : area.getChildObjects())
         //             startBranchIDs.add(obj.getTTObjId());
         predicates.add(startBranchPath.in(area.getChildObjects()));

         if (stationFilter) {
         Path<TTObject> startStationPath = p.get(PlannedTrip_.plannedStartObj);
         predicates.add(startStationPath.in(branch.getChildObjects()));
         }
         }*/
 /*if (stationFilter) {
            Path<TTObject> startObjPath = p.get(PlannedTrip_.plannedStartObj);
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

        if (startTTObject != null) {
            Path<TTObject> startObjPath = p.get(Trip_.plannedStartObj);
            predicates.add(qb.equal(startObjPath, this.startTTObject));
        }

        cq.where(qb.and(predicates.toArray(new Predicate[predicates.size()])));

        if (minPlannedStopSecs != null) {
            Path<Integer> path = p.get(TimedTrip_.plannedStopSecs);
            predicates.add(qb.greaterThanOrEqualTo(path, this.minPlannedStopSecs));
        }

        if (maxPlannedStopSecs != null) {
            Path<Integer> path = p.get(TimedTrip_.plannedStopSecs);
            predicates.add(qb.lessThanOrEqualTo(path, this.maxPlannedStopSecs));
        }
        if (sortByStopSecs) {
            cq.orderBy(qb.asc(p.get(TimedTrip_.plannedStopSecs)));
        } else if (useOrdering) {
            cq.orderBy(qb.asc(p.get(TimedTrip_.plannedStartSecs)));
        }
    }

    public void setAreaFilter(TTArea ttArea) {
        this.area = ttArea;
    }

    public void setDayTypeFilter(DayType dayType) {
        dayTypeFilter = true;
        this.dayType = dayType;
    }

    public void setSelectedStartTTObjectFilter(TTObject startTTObject) {
        this.startTTObject = startTTObject;
    }

    public void setTrainTypeFilter(TrainType trainType) {
        this.trainType = trainType;
    }

    /*public void setBranchFilter(Station branch) {
     this.branch = branch;
     this.branchFilter = true;
     }*/

    public void setStartObj(TTObject startObj) {
        this.startObj = startObj;
    }

    public void setStopObj(TTObject stopObj) {
        this.stopObj = stopObj;
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

    public TTObject getStartObj() {
        return startObj;
    }

    public TTObject getStopObj() {
        return stopObj;
    }
}
