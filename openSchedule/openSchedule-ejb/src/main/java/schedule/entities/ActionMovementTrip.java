/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule.entities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 *
 * @author Jia Li
 */
@Entity
@DiscriminatorValue(TripAction.ACTION_MOVEMENT_TRIP)
public class ActionMovementTrip extends MovementGroupActionBase {
    
    private static final long serialVersionUID = 1L;

    public enum ActionMovementTripTypeEnum {
        DEFAULT(1),
        ALTERNATIVE(2);
        
        private final int value;

        ActionMovementTripTypeEnum(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return this.value;
        }
    }
    
    @JoinColumn(name = "ttobjid2", referencedColumnName = "ttobjid")
    @ManyToOne(optional = false)
    private TTObject timetableObject2;
    
    @ManyToOne
    @JoinColumn(name = "reftripid")
    private MovementTrip refTrip;

    public ActionMovementTrip() {

    }
    
    @Override
    public TripAction createDerivedInstance() {
        return new ActionMovementTrip();
    }
    
    @Override
    public TTObject getTimetableObject2() {
        return timetableObject2;
    }

    public void setTimetableObject2(TTObject timetableObject2) {
        this.timetableObject2 = timetableObject2;
    }

    @Override
    public boolean hasSecondObject() {
        return true;
    }
    
    @Override
    public void cloneDataToNonPersited(TripAction target) {
        super.cloneDataToNonPersited(target);
        ((ActionMovementTrip) target).setRefTrip(this.getRefTrip());
    }
    
    public MovementTrip getRefTrip() {
        return refTrip;
    }

    public void setRefTrip(MovementTrip refTrip) {
        this.refTrip = refTrip;
    }
}
