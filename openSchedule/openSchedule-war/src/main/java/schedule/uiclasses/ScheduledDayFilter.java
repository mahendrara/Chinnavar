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
import schedule.entities.ScheduledDay;
import schedule.entities.ScheduledDay_;
import schedule.sessions.AbstractPredicate;

/**
 *
 * @author Jia Li
 */
public class ScheduledDayFilter extends AbstractPredicate<ScheduledDay> {

    private boolean archived;
    private boolean archivedFilter;

    private int scheduledDayCode;
    private boolean scheduledDayCodeFilter;

    private int startScheduledDayCode;
    private boolean startScheduledDayCodeFilter;

    private int endScheduledDayCode;
    private boolean endScheduledDayCodeFilter;

    private boolean active;
    private boolean activeFilter;

    public void setScheduledDayCode(int scheduledDayCode) {
        this.scheduledDayCodeFilter = true;
        this.scheduledDayCode = scheduledDayCode;
    }

    public void setArchived(boolean archived) {
        archivedFilter = true;
        this.archived = archived;
    }

    public void setActive(boolean active) {
        this.active = active;
        this.activeFilter = true;
    }

    public int getStartScheduledDayCode() {
        return startScheduledDayCode;
    }

    public void setStartScheduledDayCode(int startScheduledDayCode) {
        this.startScheduledDayCode = startScheduledDayCode;
        startScheduledDayCodeFilter = true;
    }

    public int getEndScheduledDayCode() {
        return endScheduledDayCode;
    }

    public void setEndScheduledDayCode(int endScheduledDayCode) {
        this.endScheduledDayCode = endScheduledDayCode;
        endScheduledDayCodeFilter = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addPredicate(CriteriaBuilder qb, CriteriaQuery cq, Root<ScheduledDay> p, boolean useOrdering) {
        List<Predicate> predicates = new ArrayList<>();
        //Path<Integer> path = p.get("archived");
        //cq.where(qb.equal(path, archived));

        if (archivedFilter) {
            Path<Boolean> path = p.get(ScheduledDay_.archived);
            //cq.where(qb.equal(path, this.templateId));
            predicates.add(qb.equal(path, archived));
        }
        if (scheduledDayCodeFilter) {
            Path<Integer> path = p.get(ScheduledDay_.scheduledDayCode);
            predicates.add(qb.equal(path, scheduledDayCode));
        } else {
            if (startScheduledDayCodeFilter) {
                Path<Integer> path = p.get(ScheduledDay_.scheduledDayCode);
                predicates.add(qb.greaterThanOrEqualTo(path, startScheduledDayCode));
            }
            if (endScheduledDayCodeFilter) {
                Path<Integer> path = p.get(ScheduledDay_.scheduledDayCode);
                predicates.add(qb.lessThanOrEqualTo(path, endScheduledDayCode));
            }
        }
        if (activeFilter) {
            Path<Boolean> path = p.get(ScheduledDay_.active);
            //cq.where(qb.equal(path, this.templateId));
            predicates.add(qb.equal(path, active));
        }

        cq.where(qb.and(predicates.toArray(new Predicate[predicates.size()])));

        if (useOrdering) {
            cq.orderBy(qb.desc(p.get(ScheduledDay_.scheduledDayCode)));
        }
    }
}
