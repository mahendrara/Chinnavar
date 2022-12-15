/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.entities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
@DiscriminatorValue(TripAction.ACTION_FULL_TRIP_DRIVE_DUTY)
public class ActionFullTripDriveDuty extends ActionDriveDuty {

    @JoinColumn(name = "ttobjid2", referencedColumnName = "ttobjid")
    @ManyToOne(optional = false)
    private TTObject timetableObject2;

    @OneToOne
    @JoinColumn(name = "reftripid")
    private TimedTrip timedTrip;

    public enum ActionFullTripDriveDutyTypeEnum {

        DRIVER(1),
        PASSENGER(2);

        private final int value;

        ActionFullTripDriveDutyTypeEnum(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

        public static ActionFullTripDriveDutyTypeEnum parse(int value) {
            ActionFullTripDriveDutyTypeEnum actionType = null; // Default
            for (ActionFullTripDriveDutyTypeEnum item : ActionFullTripDriveDutyTypeEnum.values()) {
                if (item.getValue() == value) {
                    actionType = item;
                    break;
                }
            }
            return actionType;
        }
    }

    public ActionFullTripDriveDuty() {

    }

    @Override
    public TripAction createDerivedInstance() {
        return new ActionFullTripDriveDuty();
    }

    @Override
    public boolean hasSecondObject() {
        return true;
    }

    @Override
    public TTObject getTimetableObject2() {
        return timetableObject2;
    }

    public void setTimetableObject2(TTObject timetableObject2) {
        this.timetableObject2 = timetableObject2;
    }

    public TimedTrip getRefTrip() {
        return this.timedTrip;
    }

    public void setRefTrip(TimedTrip refTrip) {
        this.timedTrip = refTrip;
    }

    /*@Override
     public MainActionType getClassForActionType() {
     return MainActionType.RUN_TRIP;
     }*/
    public void cloneDataToNonPersisted(PlannedTrip plannedTrip) {

        plannedTrip.setConsumed(false);
        plannedTrip.setTimesAreValid(false);
        plannedTrip.setAreaObj(this.getTrip().getAreaObj());
        //plannedTrip.setDayType(this.getTrip().getDayType());
        plannedTrip.setDayTypeList(this.getTrip().getDayTypeList());
    }

    @Override
    public void cloneDataToNonPersited(TripAction target) {
        super.cloneDataToNonPersited(target);
        ((ActionFullTripDriveDuty) target).setRefTrip(this.getRefTrip());
    }

    @Override
    public void connectToCloned(TripAction tripAction) {
        this.setTimetableObject(tripAction.getTimetableObject());
        if (this.hasSecondObject()) {
            if (tripAction instanceof ActionFullTripDriveDuty) {
                ((ActionFullTripDriveDuty) this).setTimetableObject2(((ActionFullTripDriveDuty) tripAction).getTimetableObject2());
            }
        }
    }
}
