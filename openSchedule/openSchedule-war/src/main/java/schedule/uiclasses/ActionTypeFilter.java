/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule.uiclasses;

import java.util.ArrayList;
import java.util.List;
import schedule.entities.ActionType;
import schedule.entities.ActionType_;
import schedule.sessions.AbstractPredicate;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import schedule.entities.MainActionType;
import schedule.entities.MainActionType_;
import schedule.entities.TripAction.MainActionTypeEnum;
/**
 *
 * @author EBIScreen
 */
public class ActionTypeFilter extends AbstractPredicate<ActionType> {
    private Integer actionSubType;
    private MainActionTypeEnum mainActionType = null;

    @Override
    @SuppressWarnings("unchecked")
    public void addPredicate(CriteriaBuilder qb,CriteriaQuery cq,Root<ActionType> p,boolean useOrdering) {

        // Currently we only have one logic implneted in this class...
        
        List<Predicate> predicates = new ArrayList<>();
        if(mainActionType != null)
        {
            Path<MainActionType> objtypePath = p.get(ActionType_.mainActionType);
            Path<Integer> mainActionTypePath = objtypePath.get(MainActionType_.mactionTypeId);
            predicates.add(qb.equal(mainActionTypePath, mainActionType.getValue()));
            
            if(actionSubType != null) {
                Path<Integer> subTypePath = p.get(ActionType_.actionSubtype);
                predicates.add(qb.equal(subTypePath, actionSubType));
            }
        }
        cq.where(qb.and(predicates.toArray(new Predicate[predicates.size()])));
    }
    public void setTypeFilter(MainActionType objtype) {
        if (objtype != null)
            setTypeFilter(objtype.getMainActionTypeEnum());
        else
            mainActionType = null;
    }
    
    public void setTypeFilter(MainActionTypeEnum mainActionType) {
        this.mainActionType = mainActionType;
    }
    
    public void setActionSubTypeFilter(Integer actionSubType) {
        this.actionSubType = actionSubType;
    }
}
