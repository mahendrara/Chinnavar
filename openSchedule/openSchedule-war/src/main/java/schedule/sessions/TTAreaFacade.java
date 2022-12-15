/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule.sessions;

import schedule.entities.TTArea;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author EBIScreen
 */
@Stateless
public class TTAreaFacade extends AbstractFacade<TTArea> {
    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public TTAreaFacade() {
        super(TTArea.class);
    }
    /*public int GetValidObjectsFor(Tripaction parent,List<TTArea> possibleObjects){

        // We should in the future build direct query,
        // but for now lets just get all and let Tripaction decide..
        List<TTObject> allPossible = findAll();

        Iterator<TTObject> ite = allPossible.iterator();
        while(ite.hasNext())
        {
            TTObject checkObject = ite.next();

            if(parent.isAllowedObject(checkObject))
            {
                possibleObjects.add(checkObject);
            }
            else
            {
                 //allowedActions.add(checkAction);
            }


        }
        return allPossible.size();
    }*/

}
