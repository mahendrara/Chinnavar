/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.uiclasses;

import schedule.sessions.AbstractPredicate;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import schedule.entities.Employee;
import schedule.entities.TTObjectState;
import schedule.entities.TTObject_;

//import javax.persistence.metamodel.Metamodel;
//import javax.persistence.metamodel.Type;
/**
 *
 * @author spirttin
 */
public class EmployeeFilter extends AbstractPredicate<Employee> {

    protected TTObjectState stateFilter;

    @Override
    @SuppressWarnings("unchecked")
    public void addPredicate(CriteriaBuilder qb, CriteriaQuery cq, Root<Employee> p, boolean useOrdering) {
        if (stateFilter != null && stateFilter.getStateId() != 0) {
            Path<TTObjectState> objstatePath = p.get(TTObject_.ttObjectState);
            cq.where(qb.and(qb.equal(objstatePath, this.stateFilter)));
        }
        
        if (useOrdering) {
            cq.orderBy(qb.asc(p.get(TTObject_.scheduleName)));
        }
    }

    public void setObjectStateFilter(TTObjectState ttObjectState) {
        this.stateFilter = ttObjectState;
    }
    public TTObjectState getObjectStateFilter() {
        return this.stateFilter;
    }
}

