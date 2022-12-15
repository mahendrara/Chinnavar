/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.entities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 *
 * @author Jia Li
 */
@Entity
@DiscriminatorValue(TripAction.ACTION_SHUNTING_ROUTE_TRIP)
public class ActionShuntingRouteTrip extends MovementActionBase {

    private static final long serialVersionUID = 1L;

    public ActionShuntingRouteTrip() {
    }

    @Override
    public TripAction createDerivedInstance() {
        return new ActionShuntingRouteTrip();
    }
}
