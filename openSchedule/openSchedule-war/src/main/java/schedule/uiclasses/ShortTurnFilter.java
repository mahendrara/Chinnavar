/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.uiclasses;

import java.util.ArrayList;
import java.util.List;
import schedule.sessions.AbstractPredicate;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import schedule.entities.ShortTurn;
import schedule.entities.ShortTurn_;
import schedule.entities.TrainType;

//import javax.persistence.metamodel.Metamodel;
//import javax.persistence.metamodel.Type;
/**
 *
 * @author spirttin
 */
public class ShortTurnFilter extends AbstractPredicate<ShortTurn> {

    protected TrainType trainType;

    @Override
    @SuppressWarnings("unchecked")
    public void addPredicate(CriteriaBuilder qb, CriteriaQuery cq, Root<ShortTurn> p, boolean useOrdering) {

        // Currently we only have one logic implneted in this class...
        if (trainType != null && trainType.getTrainTypeId() != 0) {
            Path<TrainType> trainTypePath = p.get(ShortTurn_.trainTypeId);
            
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(qb.equal(trainTypePath, trainType));
            for(TrainType subType : trainType.getTrainSubTypes()) {
                predicates.add(qb.equal(trainTypePath, subType));
            }            
            
           cq.where(qb.or(predicates.toArray(new Predicate[predicates.size()])));
         }
        
        if (useOrdering) {
            cq.orderBy(qb.asc(p.get(ShortTurn_.fromId)));
        }
    }

    public void setTrainType(TrainType trainType) {
        this.trainType = trainType;
    }
    public TrainType getTrainType() {
        return this.trainType;
    }
}

