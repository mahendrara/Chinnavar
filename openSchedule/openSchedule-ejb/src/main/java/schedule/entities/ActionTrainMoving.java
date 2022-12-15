/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.entities;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

/**
 *
 * @author EBIScreen
 */
@Entity
@DiscriminatorValue(TripAction.ACTION_TRAIN_MOVING)
public class ActionTrainMoving extends TripActionBase {

    public enum ActionTrainMovingTypeEnum {
        TRAIN_MOVING_WITHOUT_SPEED(1),
        TRAIN_MOVING_PLANNED_SPEED(2);

        private final int value;

        ActionTrainMovingTypeEnum(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    private static final long serialVersionUID = 1L;
    @JoinColumn(name = "ttobjid2", referencedColumnName = "ttobjid")
    @ManyToOne(optional = false)
    private TTObject timetableObject2;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "reftripid")
    private Trip refTrip;//MovementTripGroup

    public ActionTrainMoving() {

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
    public String getMainObjectName() {
        return getTimetableObject().getDescription() + " -> " + this.getTimetableObject2().getDescription();
    }

    @Override
    public TripAction createDerivedInstance() {
        return new ActionTrainMoving();
    }

    @Override
    public void connectToCloned(TripAction tripAction) {
        this.setTimetableObject(tripAction.getTimetableObject());
        if (this.hasSecondObject()) {
            if (tripAction instanceof ActionTrainMoving) {
                this.setTimetableObject2(((ActionTrainMoving) tripAction).getTimetableObject2());
            }
        }
    }

    @Override
    public void cloneDataToNonPersited(TripAction target) {
        super.cloneDataToNonPersited(target);
        if (this.getRefTrip() != null && (target instanceof ActionTrainMoving)) {
            MovementTripGroup movementTripGroup;
            movementTripGroup = new MovementTripGroup();
            movementTripGroup.setDescription(this.getRefTrip().getDescription());
            this.getRefTrip().cloneDataToClonedTrip(movementTripGroup);
            ((ActionTrainMoving) target).setRefTrip(movementTripGroup);
        } else {
            ((ActionTrainMoving) target).setRefTrip(this.getRefTrip());
        }
    }

    public Trip getRefTrip() {
        return this.refTrip;
    }

    public void setRefTrip(Trip refTrip) {
        this.refTrip = refTrip;
    }
}
