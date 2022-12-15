/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule.entities;

/**
 *
 * @author Jia Li
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 *
 * @author Jia Li
 */
@Entity
@DiscriminatorValue(TripAction.ACTION_CMD_TRIP)
public class ActionCmdTrip extends MovementActionBase {

    private static final long serialVersionUID = 1L;
    
    public ActionCmdTrip() {
    }

    @Override
    public TripAction createDerivedInstance() {
        return new ActionRouteTrip();
    }
}
