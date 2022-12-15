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
import schedule.entities.Schedule;
import schedule.entities.ScheduleBase_;
import schedule.sessions.AbstractPredicate;

/**
 *
 * @author Jia Li
 */
public class ScheduleFilter extends AbstractPredicate<Schedule>
{

    private Boolean valid;
    private boolean validFilter;

    private Integer scheduleId;
    private boolean scheduleIdFilter;

    @Override
    @SuppressWarnings("unchecked")
    public void addPredicate( CriteriaBuilder qb, CriteriaQuery cq, Root<Schedule> p, boolean useOrdering )
    {
        List<Predicate> predicates = new ArrayList<>();
        if( validFilter )
        {
            Path<Boolean> path = p.get( ScheduleBase_.valid );
            predicates.add( qb.equal( path, valid ) );
        }

        if( scheduleIdFilter )
        {
            Path<Integer> path = p.get( ScheduleBase_.scheduleid );
            predicates.add( qb.equal( path, scheduleId ) );
        }

        cq.where( qb.and( predicates.toArray( new Predicate[ predicates.size() ] ) ) );
    }

    public void setValid( Boolean valid )
    {
        this.valid = valid;
        this.validFilter = true;
    }

    public void setScheduleId( Integer scheduleId )
    {
        this.scheduleId = scheduleId;
        this.scheduleIdFilter = true;
    }
}
