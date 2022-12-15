/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule.entities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Represents a actual template which is only a collection of
 * user group items to be added on a new user 
 * @author spirttin
 */
@Entity
@DiscriminatorValue("T")
public class UserTemplate extends BaseUser {

    public UserTemplate() {
        setType('T');
    }
}