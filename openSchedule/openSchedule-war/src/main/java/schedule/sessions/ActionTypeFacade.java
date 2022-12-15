/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.sessions;

import schedule.entities.ActionType;
import schedule.uiclasses.ActionTypeFilter;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import schedule.entities.MainActionType;

/**
 *
 * @author EBIScreen
 */
@Stateless
public class ActionTypeFacade extends AbstractFacade<ActionType>
{

    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager()
    {
        return em;
    }

    public ActionTypeFacade()
    {
        super( ActionType.class );

    }

    /*public int GetValidActionsList(TripAction parent,List<ActionType> allowedActions){

        // We should in the future build direct query,
        // but for now lets just get all and let TripAction decide..
        List<ActionType> allPossible = findAll();

        Iterator<ActionType> ite = allPossible.iterator();
        while(ite.hasNext())
        {
            ActionType checkAction = ite.next();

            if(parent.isAllowedAction(checkAction))
            {
                allowedActions.add(checkAction);
            }
            else
            {
                 //allowedActions.add(checkAction);
            }


        }
        return allPossible.size();
    }*/
    
    public List<ActionType> findAll( MainActionType objectType )
    {

        ActionTypeFilter filter = new ActionTypeFilter();

        filter.setTypeFilter( objectType );

        return findAll( filter );
    }

    public List<ActionType> findAll( MainActionType objectType, Integer actionSubType )
    {

        ActionTypeFilter filter = new ActionTypeFilter();

        filter.setTypeFilter( objectType );
        filter.setActionSubTypeFilter( actionSubType );
        return findAll( filter );
    }

    public List<ActionType> findRange( MainActionType objectType, int[] range )
    {

        ActionTypeFilter filter = new ActionTypeFilter();

        filter.setTypeFilter( objectType );
        return findRange( range, filter );
    }

    public int count( MainActionType objectType )
    {

        ActionTypeFilter filter = new ActionTypeFilter();
        filter.setTypeFilter( objectType );
        return count( filter );
    }

}
