/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.entities;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 *
 * @author Jia Li
 */
@Entity
@DiscriminatorValue(Trip.MOVEMENT_TRIP_GROUP)
public class MovementTripGroup extends Trip {

    @OneToOne(mappedBy = "refTrip")
    private ActionTrainMoving actionTrainMoving;

    public ActionTrainMoving getActionTrainMoving() {
        return actionTrainMoving;
    }

    public void setActionTrainMoving(ActionTrainMoving actionTrainMoving) {
        this.actionTrainMoving = actionTrainMoving;
    }

    @Override
    public List<TripAction> getTripActions() {
        Collections.sort(tripActions);
				//sort(tripActions);
        return tripActions;
    }
    
    @Override
    public void removeTripAction(TripAction tripAction) {
        Iterator<TripAction> iterator = this.getTripActions().iterator();//sorted list
        while (iterator.hasNext()) {
            TripAction temp = iterator.next();
            if (tripAction == temp) {
                temp.setTrip(null);
                iterator.remove();
            } else if (temp.getSeqNo() != null && temp.getSeqNo() > tripAction.getSeqNo()) {
                temp.setSeqNo(temp.getSeqNo() - 1);
                //temp.setTimeFromTripStart(temp.getTimeFromTripStart() - tripAction.getPlannedSecs());
                //temp.setMinTimeFromTripStart(temp.getMinTimeFromTripStart() - tripAction.getPlannedSecs());
            }
        }

    }
}
