/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.uiclasses;

import java.util.ArrayList;
import java.util.List;
import schedule.entities.TripType_;
import schedule.sessions.AbstractPredicate;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import schedule.entities.TripType.TripMainType;
import schedule.entities.TripType;

//import javax.persistence.metamodel.Metamodel;
//import javax.persistence.metamodel.Type;
/**
 *
 * @author Jia Li
 */
public class TripTypeFilter extends AbstractPredicate<TripType> {

    private TripMainType tripType;
    private boolean tripTypeFilter = false;
    
    private boolean templateFilter = false;
    private boolean template;
    
    private boolean validFilter = false;
    private boolean valid;
    
    private boolean onlySubTripTypeFilter = false;
    private boolean onlyMainTripTypeFilter = false;
    
    private boolean tripSubTypeFilter = false;
    private Integer tripSubType;
    
    private boolean externalIdFilter = false;
    private String externalId;
    
    private boolean toTripTypeFilter = false;
    private Integer toTripType;
    
    private boolean toTripAndExternalIdNotNullFilter = false;
    
    @Override
    @SuppressWarnings("unchecked")
    public void addPredicate(CriteriaBuilder qb, CriteriaQuery cq, Root<TripType> p, boolean useOrdering) {

        List<Predicate> predicates = new ArrayList<>();
        if (tripTypeFilter) {
            Path<TripMainType> path = p.get(TripType_.tripType);

            //cq.where(qb.equal(objtypePath, this.tripType));
            predicates.add(qb.equal(path, tripType));
        } 
        
        if(templateFilter) {
            Path<Boolean> path = p.get(TripType_.template);
            predicates.add(qb.equal(path, template));
        }
        
        if(validFilter) {
            Path<Boolean> path = p.get(TripType_.valid);
            predicates.add(qb.equal(path, valid));
        }
        
        if(onlySubTripTypeFilter) {
            Path<Integer> path = p.get(TripType_.tripSubType);
            predicates.add(qb.notEqual(path, 0));
        } 
        
        if(this.tripSubTypeFilter) {
            Path<Integer> path = p.get(TripType_.tripSubType);
            predicates.add(qb.equal(path, this.tripSubType));
        }
        
        if(onlyMainTripTypeFilter) {
            Path<Integer> path = p.get(TripType_.tripSubType);
            predicates.add(qb.equal(path, 0));
        }
        
        if(externalIdFilter) {
            Path<String> path = p.get(TripType_.externalId);
            predicates.add(qb.equal(path, this.externalId));
        }
        
        if(toTripTypeFilter) {
            Path<Integer> path = p.get(TripType_.toTripType);
            predicates.add(qb.equal(path, this.toTripType));
        }
        
        if(toTripAndExternalIdNotNullFilter) {
            Path<Integer> path = p.get( TripType_.toTripType );
            predicates.add( qb.isNotNull( path ) );
            
            Path<String> path2 = p.get( TripType_.externalId );
            predicates.add( qb.isNotNull( path2 ) );
            predicates.add( qb.notEqual( path2, "" ) );
        }
        
        cq.where(qb.and(predicates.toArray(new Predicate[predicates.size()])));
        if(useOrdering)
            cq.orderBy(qb.asc(p.get(TripType_.tripSubType)));
    }

    public void setTripType(TripMainType objtype) {
        this.tripType = objtype; //.getValue();
        this.tripTypeFilter = true;
    }
    
    /*public TripMainType getTypeFilter() {
        TripMainType[] types = TripMainType.values();
        if (tripType >= 0 && tripType < types.length) {
            return types[tripType];
        }
        return TripMainType.TRIPTYPE_UNKNOWN;
    }*/
    
    public void setTemplate(boolean template) {
        this.templateFilter = true;
        this.template = template;
    }
    
     public void setValid(boolean valid) {
        this.validFilter = true;
        this.valid = valid;
    }

    public void setOnlySubTripTypeFilter(boolean onlySubTripTypeFilter) {
        if(onlySubTripTypeFilter)
            onlyMainTripTypeFilter = false;
        this.onlySubTripTypeFilter = onlySubTripTypeFilter;
    }
    
    public void setOnlyMainTripTypeFilter(boolean onlyMainTripTypeFilter) {
        if(onlyMainTripTypeFilter)
            onlySubTripTypeFilter = false;
        this.onlyMainTripTypeFilter = onlyMainTripTypeFilter;
    }
    
    public void setTripSubType(Integer subType) {
        this.tripSubTypeFilter = true;
        this.tripSubType = subType;
    }
    
    public void setExternalId(String externalId) {
        this.externalIdFilter = true;
        this.externalId = externalId;
    }
    
    public void setToTripType(Integer toTripType) {
        this.toTripTypeFilter = true;
        this.toTripType = toTripType;
    }
    
    public void setToTripAndExternalIdNotNull() {
        this.toTripAndExternalIdNotNullFilter = true;
    }
}
