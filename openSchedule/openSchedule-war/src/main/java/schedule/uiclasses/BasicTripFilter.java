/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule.uiclasses;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import schedule.entities.BasicTrip;
import schedule.entities.SchedulingState;
import schedule.entities.TTArea;
import schedule.entities.TTObject;
import schedule.entities.TripType;
import schedule.entities.TripType.TripMainType;
import schedule.entities.TripType_;
import schedule.entities.TripUserType;
import schedule.entities.Trip_;
import schedule.sessions.AbstractPredicate;

/**
 *
 * @author Jia Li
 */
public class BasicTripFilter extends AbstractPredicate<BasicTrip> {
    private TTArea area = null;
    private TTObject plannedStartObj = null;
    private TTObject plannedStopObj = null;
//    private TripType tripType = null;
    private SchedulingState plannedState = null;
    private Boolean valid = null;
    private TripType.TripMainType tripMainType = null;
    private TripType tripSubType = null;
    private TripType tripType = null;
    private String description = null;
    private TripUserType tripUserType = null;
    private Set<Integer> selectedIds = null;
    
    public void setAreaFilter(TTArea ttArea) {
        this.area = ttArea;
    }
    
    public void setPlannedStartObjFilter(TTObject plannedStartObj) {
        this.plannedStartObj = plannedStartObj;
    }
    
    public void setPlannedStopObjFilter(TTObject plannedStopObj) {
        this.plannedStopObj = plannedStopObj;
    }
    
    /*public void setTripTypeFilter(TripType tripType) {
        if (tripType != null)
            setTripTypeFilter(tripType.getTripType());
        else
            tripMainType = null;
    }*/
    
    
    public void setPlannedStateFilter(SchedulingState plannedState) {
        this.plannedState = plannedState;
    }

    public void setValidFilter(boolean b) {
        valid = b;
    }

    public void setTripMainTypeFilter(TripType.TripMainType tripMainType) {
        this.tripMainType = tripMainType;
    }

    public void setTripTypeFilter(TripType tripType) {
        this.tripType = tripType;
    }
    
    public void setSelectedIds(Set<Integer> selectedIds) {
        this.selectedIds = selectedIds;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void addPredicate(CriteriaBuilder qb, CriteriaQuery cq, Root<BasicTrip> p, boolean useOrdering) {
        List<Predicate> predicates = new ArrayList<>();
        
        if(selectedIds != null) {
            Path<Integer> idPath = p.get(Trip_.tripId);
            predicates.add(idPath.in(this.selectedIds));
        }
        
        if(area != null)
        {
            Path<TTArea> areaPath = p.get(Trip_.areaObj);
            
            predicates.add(qb.equal(areaPath, this.area));
         }
        
        if(this.plannedStartObj != null) {
            Path<TTObject> startObjPath = p.get(Trip_.plannedStartObj);
            predicates.add(qb.equal(startObjPath, this.plannedStartObj));
        }
        
        if(this.plannedStopObj != null) {
            Path<TTObject> startObjPath = p.get(Trip_.plannedStopObj);
            predicates.add(qb.equal(startObjPath, this.plannedStopObj));
        }
        
        if(this.tripMainType != null) {
            Path<TripType> tripTypePath = p.get(Trip_.tripType);
            Path<TripMainType> tripMainTypePath = tripTypePath.get(TripType_.tripType);
            predicates.add(qb.equal(tripMainTypePath, this.tripMainType));
        }
        
        if(tripSubType != null) {
            Path<TripType> tripTypePath = p.get(Trip_.tripType);
            predicates.add(qb.equal(tripTypePath, this.tripSubType));
        }
        
        if(tripType != null) {
            Path<TripType> tripTypePath = p.get(Trip_.tripType);
            predicates.add(qb.equal(tripTypePath, this.tripType));
        }
        
        if(plannedState != null) {
            Path<SchedulingState> plannedStatePath = p.get(Trip_.plannedState);
            predicates.add(qb.equal(plannedStatePath, this.plannedState));
        }
        if (valid != null) {
            Path<Boolean> plannedStatePath = p.get(Trip_.valid);
            predicates.add(qb.equal(plannedStatePath, this.valid));
            
        }
        if (description != null) {
            Path<String> descriptionPath = p.get(Trip_.description);
            predicates.add(qb.equal(descriptionPath, this.description));
        }
        if(tripUserType != null) {
            Path<TripUserType> tripUserTypePath = p.get(Trip_.tripUserType);
            predicates.add(qb.equal(tripUserTypePath, this.tripUserType));
        }
        
        cq.where(qb.and(predicates.toArray(new Predicate[predicates.size()])));
        
        if(useOrdering)
            cq.orderBy(qb.asc(p.get(Trip_.description)));
    }

    public void setDescriptionFilter(String desc) {
        description = desc;

    }

    void setTripUserTypeFilter(TripUserType tripUserType) {
        this.tripUserType = tripUserType;
    }

    public void setTripSubTypeFilter(TripType tripSubType) {
        this.tripSubType = tripSubType;
    }
}