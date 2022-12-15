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
import schedule.entities.MovementTripTemplate;
import schedule.entities.TTArea;
import schedule.entities.TTObject;
import schedule.entities.TripType;
import schedule.entities.Trip_;
import schedule.sessions.AbstractPredicate;

/**
 *
 * @author Jia Li
 */
public class MovementTripTemplateFilter extends AbstractPredicate<MovementTripTemplate>{
    private TTArea area = null;
    private TTObject plannedStartObj = null;
    private TTObject plannedStopObj = null;
    private TripType tripType = null;
    
    public void setAreaFilter(TTArea ttArea) {
        this.area = ttArea;
    }
    
    public void setPlannedStartObjFilter(TTObject plannedStartObj) {
        this.plannedStartObj = plannedStartObj;
    }
    public void setPlannedStopObjFilter(TTObject plannedStopObj) {
        this.plannedStopObj = plannedStopObj;
    }
    
    public void setTripTypeFilter(TripType tripType) {
        this.tripType = tripType;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void addPredicate(CriteriaBuilder qb, CriteriaQuery cq, Root<MovementTripTemplate> p, boolean useOrdering) {
        List<Predicate> predicates = new ArrayList<>();
        if(area != null)
        {
            Path<TTArea> areaPath = p.get(Trip_.areaObj);
            
            predicates.add(qb.equal(areaPath, this.area));
         }
        
        if(this.plannedStartObj != null) {
            Path<TTObject> startObjPath = p.get(Trip_.plannedStartObj);
            predicates.add(qb.equal(startObjPath, this.plannedStartObj));
        }
        if(plannedStopObj != null) {
            Path<TTObject> stopObjPath = p.get(Trip_.plannedStopObj);
            predicates.add(qb.equal(stopObjPath, this.plannedStopObj));
        }
        
        if(this.tripType != null) {
            Path<TripType> tripTypePath = p.get(Trip_.tripType);
            predicates.add(qb.equal(tripTypePath, this.tripType));
        }
        
        cq.where(qb.and(predicates.toArray(new Predicate[predicates.size()])));
        
        if(useOrdering)
            cq.orderBy(qb.asc(p.get(Trip_.description)));
    }
}
