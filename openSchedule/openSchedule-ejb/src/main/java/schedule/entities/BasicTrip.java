/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.entities;

import java.util.Iterator;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue(Trip.BASIC_TRIP)
public class BasicTrip extends Trip {
    
    @JoinColumn(name = "sourceTrip")
    @ManyToOne
    private BasicTrip sourceTrip;

    public BasicTrip getSourceTrip() {
        return sourceTrip;
    }

    public void setSourceTrip(BasicTrip sourceTrip) {
        this.sourceTrip = sourceTrip;
    }

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
                    temp.setSeqNo(temp.getSeqNo() - 1);
                }
            }
            
            updateActionTimes();
        }

    }
    
    @Override
    public void setValid(boolean valid) {
        super.setValid(valid);
        for(TripAction tripAction : this.getTripActions()) {
            if(tripAction instanceof ActionTrainMoving) {
                if(((ActionTrainMoving)tripAction).getRefTrip()!=null) {
                    ((ActionTrainMoving)tripAction).getRefTrip().setValid(valid);
                }
            }
        }
    }
    
    @Override
    public void cloneDataToClonedTrip(Trip newTrip)  {
        super.cloneDataToClonedTrip(newTrip);
        ((BasicTrip)newTrip).setSourceTrip(this);
    }
    
    @Override
    public void cloneDataToClonedTrip(Trip newTrip, Integer startSeqNo, Integer endSeqNo) {
        super.cloneDataToClonedTrip(newTrip, startSeqNo, endSeqNo);
        ((BasicTrip)newTrip).setSourceTrip(this);
    }
}
