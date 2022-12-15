/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule.sessions;

import schedule.entities.TripType;
import schedule.uiclasses.TripTypeFilter;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import schedule.entities.TripType.TripMainType;

/**
 *
 * @author EBIScreen
 */
@Stateless
public class TripTypeFacade extends AbstractFacade<TripType> {
    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public TripTypeFacade() {
        super(TripType.class);

    }
//    public int GetValidActionsList(Tripaction parent,List<Triptype> allowedActions){
//
//        // We should in the future build direct query,
//        // but for now lets just get all and let Tripaction decide..
//        List<Triptype> allPossible = findAll();
//
//        Iterator<Triptype> ite = allPossible.iterator();
//        while(ite.hasNext())
//        {
//            TripType checkAction = ite.next();
//
//            if(parent.isAllowedAction(checkAction))
//            {
//                allowedActions.add(checkAction);
//            }
//            else
//            {
//                 //allowedActions.add(checkAction);
//            }
//
//
//        }
//        return allPossible.size();
//    }
      /*public List<TripType> findAll(TripMainType objectType) {

         TripTypeFilter filter = new TripTypeFilter();
         filter.setValid(true);

         filter.setTripType(objectType);

         return findAll(filter);
    }*/

    public List<TripType> findRange(TripMainType objectType,int[] range) {
        
        TripTypeFilter filter = new TripTypeFilter();

        filter.setTripType(objectType);
        return findRange(range,filter);
    }

    public int count(TripMainType objectType) {
        
        TripTypeFilter filter = new TripTypeFilter();
        filter.setTripType(objectType);
        return count(filter);
    }

    public List<TripType> findAll(boolean tempalte, boolean valid) {
        TripTypeFilter filter = new TripTypeFilter();
        filter.setTemplate(tempalte);
        filter.setValid(valid);
        return findAll(filter);
    }
}
