/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.uiclasses;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import schedule.entities.LocationGroup;
import schedule.entities.TTObjectState;
import schedule.entities.TTObjectType;
import schedule.entities.TTObject_;
import schedule.sessions.AbstractPredicate;

/**
 *
 * @author Pavel
 */
public class LocationGroupFilter extends AbstractPredicate<LocationGroup> {

    protected TTObjectState stateFilter;
    protected TTObjectType typeFilter;

    @Override
    @SuppressWarnings("unchecked")
    public void addPredicate(CriteriaBuilder qb, CriteriaQuery cq, Root<LocationGroup> p, boolean useOrdering) {
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
    
    public void setObjectTypeFilter(TTObjectType objtype) {
        this.typeFilter = objtype;
    }
    
    public TTObjectType getObjectTypeFilter() {
        return typeFilter;
    }
}
