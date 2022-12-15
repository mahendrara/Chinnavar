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
import schedule.entities.DayType;
import schedule.entities.Schedule;
import schedule.entities.TimeBlock;
import schedule.entities.TimeBlock_;
import schedule.sessions.AbstractPredicate;

/**
 *
 * @author Jia Li
 */
public class TimeBlockFilter extends AbstractPredicate<TimeBlock> {

    private boolean scheduleFilter = false;
    private Schedule schedule = null;
    private DayType dayType = null;
    private Integer startSecs = null;
    private Integer endSecs = null;

    @SuppressWarnings("unchecked")
    @Override
    public void addPredicate(CriteriaBuilder qb, CriteriaQuery cq, Root<TimeBlock> p, boolean useOrdering) {

        List<Predicate> predicates = new ArrayList<>();

        if (scheduleFilter) {
            if (schedule != null) {
                if (!schedule.getDayTypes().isEmpty()) {
                    Path<DayType> path = p.get(TimeBlock_.dayType);
                    Predicate pred = path.in(schedule.getDayTypes());
                    predicates.add(pred);
                } else {
                    Path<DayType> dayTypePath = p.get(TimeBlock_.dayType);
                    predicates.add(qb.isNull(dayTypePath));
                }
            } else {
                Path<DayType> dayTypePath = p.get(TimeBlock_.dayType);
                predicates.add(qb.isNull(dayTypePath));
            }
        }

        if (dayType != null && dayType.getDayTypeId() != null) {
            Path<DayType> path = p.get(TimeBlock_.dayType);
            //cq.where(qb.equal(path, this.templateId));
            predicates.add(qb.equal(path, dayType));
        }
        
        if(startSecs != null) {
            Path<Integer> path = p.get(TimeBlock_.startSecs);
            predicates.add(qb.equal(path, this.startSecs));
        }
        
        if(endSecs != null) {
            Path<Integer> path = p.get(TimeBlock_.endSecs);
            predicates.add(qb.equal(path, this.endSecs));
        }

        cq.where(qb.and(predicates.toArray(new Predicate[predicates.size()])));
        if (useOrdering) {
            cq.orderBy(qb.asc(p.get(TimeBlock_.startSecs)));
        }
    }

    public void setDayType(DayType dayType) {
        this.dayType = dayType;
    }

    public DayType getDayType() {
        return dayType;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        scheduleFilter = true;
        this.schedule = schedule;
    }

    public Integer getStartSecs() {
        return startSecs;
    }

    public void setStartSecs(Integer startSecs) {
        this.startSecs = startSecs;
    }

    public Integer getEndSecs() {
        return endSecs;
    }

    public void setEndSecs(Integer endSecs) {
        this.endSecs = endSecs;
    }

}
