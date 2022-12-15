/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.uiclasses;

/**
 *
 * @author Jia Li
 */
import java.util.ArrayList;
import java.util.List;
import schedule.sessions.AbstractPredicate;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import schedule.entities.TTObjectType;
import schedule.entities.TTProperty;
import schedule.entities.TTProperty_;

/**
 *
 * @author EBIScreen
 */
public class TTPropertyFilter extends AbstractPredicate<TTProperty> {
    private TTObjectType type;

    /*private Trip refTrip;
    private boolean refTripValid = false;*/

    @Override
    @SuppressWarnings("unchecked")
    public void addPredicate(CriteriaBuilder qb, CriteriaQuery cq, Root<TTProperty> p, boolean useOrdering) {

        // Currently we only have one logic implneted in this class...
        List<Predicate> predicates = new ArrayList<>();

        if (type != null) {
            Path<TTObjectType> typePath = p.get(TTProperty_.ttobjtypeid);
            predicates.add(qb.equal(typePath, type));
        }
        cq.where(qb.and(predicates.toArray(new Predicate[predicates.size()])));
    }

    public void setTTObjectTypeFilter(TTObjectType type) {
        this.type = type;
    }

}
