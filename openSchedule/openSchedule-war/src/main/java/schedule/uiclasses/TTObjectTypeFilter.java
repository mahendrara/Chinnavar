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
import schedule.entities.TTObjectType;
import schedule.entities.TTObjectType_;
import schedule.sessions.AbstractPredicate;

/**
 *
 * @author Jia Li
 */
public class TTObjectTypeFilter extends AbstractPredicate<TTObjectType> {

    private List<Integer> startTTObjectTypes;
    private boolean startTTObjectTypeFilter = false;

    public void setStartTTObjectTypeFilter() {
        startTTObjectTypeFilter = true;
        this.startTTObjectTypes = new ArrayList<>();
        for (int i = 1; i < 11; i++) {
            startTTObjectTypes.add(i);
        }
        startTTObjectTypes.add(26);
        startTTObjectTypes.add(28);
        startTTObjectTypes.add(29);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addPredicate(CriteriaBuilder qb, CriteriaQuery cq, Root<TTObjectType> p, boolean useOrdering) {
        List<Predicate> predicates = new ArrayList<>();
        if (startTTObjectTypeFilter) {
            Path<Integer> objtypePath = p.get(TTObjectType_.ttObjTypeId);
            predicates.add(objtypePath.in(startTTObjectTypes));
        }
        cq.where(qb.and(predicates.toArray(new Predicate[predicates.size()])));

        if (useOrdering) {
            cq.orderBy(qb.asc(p.get(TTObjectType_.ttObjTypeId)));
        }
    }
}
