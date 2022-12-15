/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.uiclasses;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.*;

import org.jfree.data.time.Day;
import schedule.entities.DayType;
import schedule.entities.PlannedDuty;
import schedule.entities.PlannedDuty_;
import schedule.entities.Schedule;
import schedule.entities.TTArea;
import schedule.sessions.AbstractPredicate;
import schedule.entities.Trip_;

/**
 *
 * @author Jia Li
 */
public class PlannedDutyFilter extends AbstractPredicate<PlannedDuty> {

    private TTArea areaFilter;
    private DayType dayType;
    private List<DayType> dayTypeList;
    private boolean scheduleFilter = false;
    private Schedule schedule = null;
    private String description = null;
    private Boolean valid;

    public void setAreaFilter(TTArea ttArea) {
        this.areaFilter = ttArea;
    }

    public void setDayType(DayType dayType) {
        this.dayType = dayType;
    }

    public DayType getDayType() {
        return dayType;
    }

    public List<DayType> getDayTypeList() { return this.dayTypeList; }

    public void setDayTypeList(List<DayType> dayTypeList) { this.dayTypeList = dayTypeList; }
    
    public void setSchedule(Schedule schedule) {
        this.scheduleFilter = true;
        this.schedule = schedule;
        //dayType = null;
    }

    public Schedule getSchedule() {
        return schedule;
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setValid(Boolean valid) {
        this.valid = valid;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void addPredicate(CriteriaBuilder qb, CriteriaQuery cq, Root<PlannedDuty> p, boolean useOrdering) {
        List<Predicate> predicates = new ArrayList<>();

        if (scheduleFilter) {
            if (schedule != null) {
                if (!schedule.getDayTypes().isEmpty()) {
//                    Path<DayType> path = p.get(Trip_.dayType);
//                    Predicate pred = path.in(schedule.getDayTypes());
                    ListJoin<PlannedDuty, DayType> daytypeJoin = p.join(Trip_.dayTypeList);
                    Predicate pred = daytypeJoin.in(schedule.getDayTypes());
                    predicates.add(pred);
                    cq.distinct(true);
                } else {
                    //Path<DayType> dayTypePath = p.get(Trip_.dayType);
                    Path<List<DayType>> dayTypePath = p.get(String.valueOf(Trip_.dayTypeList));
                    predicates.add(qb.isNull(dayTypePath));
                }
            } else {
                //Path<DayType> dayTypePath = p.get(Trip_.dayType);
                Path<List<DayType>> dayTypePath = p.get(String.valueOf(Trip_.dayTypeList));
                predicates.add(qb.isNull(dayTypePath));
            }
        }
        
        if (areaFilter != null) {
            Path<TTArea> areaPath = p.get(Trip_.areaObj);
            predicates.add(qb.equal(areaPath, this.areaFilter));
        }
        if(dayType != null) {
            //Path<DayType> dayTypePath = p.get(Trip_.dayType);
            Path<List<DayType>> dayTypePath = p.get(String.valueOf(Trip_.dayTypeList));
            predicates.add(qb.isMember(dayType, dayTypePath));
        }
        if(dayTypeList != null && !dayTypeList.isEmpty()) {
//            Expression<List<DayType>> daytypes = p.get(String.valueOf(Trip_.dayTypeList));
//            for(DayType d : dayTypeList) {
//                predicates.add(qb.isMember(d, daytypes));
//            }

//            ListJoin<PlannedDuty, DayType> dayTypeJoin = p.join(Trip_.dayTypeList);
//            predicates.add(dayTypeJoin.in(this.dayTypeList));

            for(DayType dayType: dayTypeList) {
                predicates.add(qb.isMember(dayType, p.get("dayTypeList")));
            }
        }

        if (description != null) {
            Path<String> path = p.get(Trip_.description);
            predicates.add(qb.equal(path, description));
        }
        if(valid != null) {
            Path<Boolean> path = p.get(Trip_.valid);
            predicates.add(qb.equal(path, valid));
        }
        cq.where(qb.and(predicates.toArray(new Predicate[predicates.size()])));
        if (useOrdering) {
            cq.orderBy(qb.asc(p.get(PlannedDuty_.plannedStartSecs)));
        }
    }
}
