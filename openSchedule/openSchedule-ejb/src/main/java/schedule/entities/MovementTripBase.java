/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule.entities;

import java.util.Iterator;
import javax.persistence.Entity;

/**
 *
 * @author Jia Li
 */
@Entity
public class MovementTripBase extends Trip{
    
    @Override
    public void removeTripAction(TripAction tripAction) {

        if (tripAction.isCreating() == false) {
            Iterator<TripAction> iterator = this.getTripActions().iterator();//sorted list
            while (iterator.hasNext()) {
                TripAction temp = iterator.next();
                
                if (tripAction.getActionId() == temp.getActionId()) {
                    temp.setTrip(null);
                    iterator.remove();
                } else if (temp.getSeqNo() != null && temp.getSeqNo() > tripAction.getSeqNo()) {
                    if(this.getTripAction(temp.getSeqNo() - 1) == null)
                        temp.setSeqNo(temp.getSeqNo() - 1);
                    //temp.setTimeFromTripStart(temp.getTimeFromTripStart() - tripAction.getPlannedSecs());
                    //temp.setMinTimeFromTripStart(temp.getMinTimeFromTripStart() - tripAction.getPlannedSecs());
                }
                
            }
        }

    }
}