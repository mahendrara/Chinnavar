/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule.entities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 *
 * @author spirttin
 */
@Entity
@DiscriminatorValue("U")
public class User extends BaseUser {

    public User() {
        setType('U');
    }
    
    
    public void setUserTemplate(BaseUser u) {
        setUserGroupCollection(u.getUserGroupCollection());
    }
    public BaseUser getUserTemplate() {
        return null;
    }
}