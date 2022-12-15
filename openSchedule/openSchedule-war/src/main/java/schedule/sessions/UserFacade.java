/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule.sessions;

import java.util.List;
import schedule.entities.BaseUser;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import schedule.uiclasses.UserFilter;

/**
 *
 * @author EBIScreen
 */
@Stateless
public class UserFacade extends AbstractFacade<BaseUser> {
    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public BaseUser findUserByUserName(String username) {
        UserFilter filter = new UserFilter('U'); // Access only to users
        filter.SetUserNameFilter(username);
        
        List<BaseUser> list = this.findAll(filter);
        if (list.isEmpty())
            return null;

        return list.get(0);    
    }
    
    public UserFacade() {
        super(BaseUser.class);
    }
}
