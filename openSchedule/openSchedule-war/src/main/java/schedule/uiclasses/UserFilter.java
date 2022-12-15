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
import schedule.entities.BaseUser;
import schedule.entities.BaseUser_;
import schedule.sessions.AbstractPredicate;

/**
 *
 * @author spirttin
 */
public class UserFilter extends AbstractPredicate<BaseUser> {
    private String userName;
    private Character type;
    
    public UserFilter() {
    }
    
    public UserFilter(Character type) {
        this.type = type;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void addPredicate(CriteriaBuilder qb,CriteriaQuery cq,Root<BaseUser> p,boolean useOrdering) {

        List<Predicate> predicates = new ArrayList<>();
        if(userName != null) {
             Path<String> path = p.get(BaseUser_.username);
            
            predicates.add(qb.equal(path, this.userName));
        }
        if(type != null) {
             Path<Character> path = p.get(BaseUser_.type);
            
            predicates.add(qb.equal(path, this.type));
        }
            
        cq.where(qb.and(predicates.toArray(new Predicate[predicates.size()])));
        
        if(useOrdering)
            cq.orderBy(qb.asc(p.get(BaseUser_.username)));
    }
    public void SetUserNameFilter(String userName) {
        this.userName = userName;
    }
    public void SetTypeFilter(Character type) {
        this.type = type;
    }
    
}
