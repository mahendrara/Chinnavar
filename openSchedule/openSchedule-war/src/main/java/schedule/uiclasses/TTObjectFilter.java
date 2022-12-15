/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.uiclasses;

import java.util.ArrayList;
import java.util.List;
import schedule.entities.TTObject;
import schedule.entities.TTObjectType;
import schedule.entities.TTObject_;
import schedule.sessions.AbstractPredicate;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import schedule.entities.ActionType_;
import schedule.entities.MainActionType;

//import javax.persistence.metamodel.Metamodel;
//import javax.persistence.metamodel.Type;
/**
 *
 * @author EBIScreen
 */
public class TTObjectFilter extends AbstractPredicate<TTObject> {

    protected TTObjectType typeFilter;
    protected Character classFilter;
    protected String scheduleName;
  
    protected List<TTObjectType> excludingFilters;
    
    public TTObjectFilter() {
        excludingFilters = new ArrayList<>();
    }
    @Override
    @SuppressWarnings("unchecked")
    public void addPredicate(CriteriaBuilder qb, CriteriaQuery cq, Root<TTObject> p, boolean useOrdering) {

        List<Predicate> predicates = new ArrayList<>();

        // Do not add null or 0 id
        if (typeFilter != null && typeFilter.getTTObjTypeId() != 0) {
            Path<TTObjectType> path = p.get(TTObject_.ttObjectType);
            predicates.add(qb.equal(path, this.typeFilter));
        }
        
        if (classFilter != null) {
            Path<Character> path = p.get(TTObject_.objectClass);
            predicates.add(qb.equal(path, this.classFilter));
        }
        if (scheduleName != null) {
            Path<String> path = p.get(TTObject_.scheduleName);
            predicates.add(qb.equal(path, this.scheduleName));
        }
        
        // And then use excluding filter
        for (TTObjectType i: excludingFilters) {
            Path<TTObjectType> path = p.get(TTObject_.ttObjectType);
            predicates.add(qb.notEqual(path, i));
        }      
        
        cq.where(qb.and(predicates.toArray(new Predicate[predicates.size()])));

        if (useOrdering) {
            cq.orderBy(qb.asc(p.get(TTObject_.description)));
        }
        

    }

    public void setObjectTypeFilter(TTObjectType objtype) {
        this.typeFilter = objtype;
    }
    public TTObjectType getObjectTypeFilter() {
        return typeFilter;
    }
    
    void addExcludingFilter(TTObjectType ttObjectType) {
        excludingFilters.add(ttObjectType);
    }

    void addExcludingFilter(List<TTObjectType> filterExcludors) {
        for (TTObjectType i : filterExcludors)
            addExcludingFilter(i);
    }

    public void setObjectClassFilter(Character objectClass) {
        this.classFilter = objectClass;
    }

    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }
}
