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

import org.jfree.data.time.Day;
import schedule.entities.DayType;
import schedule.entities.ScheduledDay;
import schedule.entities.ScheduledDuty;
import schedule.entities.ScheduledDuty_;
import schedule.entities.TTArea;
import schedule.sessions.AbstractPredicate;
import schedule.entities.Trip_;

/**
 *
 * @author Jia Li
 */
public class ScheduledDutyFilter extends AbstractPredicate<ScheduledDuty> {

    private TTArea areaFilter;
    private ScheduledDay scheduledDay;
    private String description = null;
    private Boolean valid;
    private DayType dayType;
    private List<DayType> dayTypeList;

    public void setAreaFilter(TTArea ttArea) {
        this.areaFilter = ttArea;
    }

    public void setScheduledDay(ScheduledDay scheduledDayCode) {
        this.scheduledDay = scheduledDayCode;
    }

    public ScheduledDay getScheduledDay() {
        return scheduledDay;
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
    
    public void setDayType(DayType dayType) {
        this.dayType = dayType;
    }

    public void setDayTypeList(List<DayType> dayTypeList) {
        this.dayTypeList = dayTypeList;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addPredicate(CriteriaBuilder qb, CriteriaQuery cq, Root<ScheduledDuty> p, boolean useOrdering) {
        List<Predicate> predicates = new ArrayList<>();

        if (scheduledDay != null) {
            Path<ScheduledDay> path = p.get(ScheduledDuty_.day);
            //cq.where(qb.equal(path, this.templateId));
            predicates.add(qb.equal(path, scheduledDay));
        }
        if (areaFilter != null) {
            Path<TTArea> areaPath = p.get(Trip_.areaObj);
            predicates.add(qb.equal(areaPath, this.areaFilter));
        }
        
        if (description != null) {
            Path<String> path = p.get(Trip_.description);
            predicates.add(qb.equal(path, description));
        }
        
        if(valid != null) {
            Path<Boolean> path = p.get(Trip_.valid);
            predicates.add(qb.equal(path, valid));
        }
        
//        if(dayType != null) {
//            Path<DayType> path = p.get("dayTypeList");
//            predicates.add(qb.isMember(dayType, path));
//        }

        if(dayTypeList != null && !dayTypeList.isEmpty()) {
            for(DayType dayType: dayTypeList) {
                predicates.add(qb.isMember(dayType, p.get("dayTypeList")));
            }
        }
        cq.where(qb.and(predicates.toArray(new Predicate[predicates.size()])));
        if (useOrdering) {
            cq.orderBy(qb.asc(p.get(ScheduledDuty_.plannedStartSecs)));
        }
    }
}
