/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.entities;

import java.util.Date;
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
@DiscriminatorValue(TripAction.ACTION_RUN_TRIP)
public class ActionRunTrip extends ServiceActionBase {

    private static final long serialVersionUID = 1L;
    @JoinColumn(name = "ttobjid2", referencedColumnName = "ttobjid")
    @ManyToOne(optional = false)
    private TTObject timetableObject2;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "reftripid")
    private TimedTrip refTrip;
    
    public enum ActionRunTripTypeEnum {

        RUN_COMMERCIAL_TRIP(1),
        RUN_NONCOMMERCIAL_TRIP(2),
        TURN_AND_RUN_COMMERCIAL_TRIP(3),
        TURN_AND_RUN_NONCOMMERCIAL_TRIP(4);

        private final int value;

        ActionRunTripTypeEnum(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

        public static ActionRunTripTypeEnum parse(int value) {
            ActionRunTripTypeEnum actionType = null; // Default
            for (ActionRunTripTypeEnum item : ActionRunTripTypeEnum.values()) {
                if (item.getValue() == value) {
                    actionType = item;
                    break;
                }
            }
            return actionType;
        }
    }

    /*@JoinColumn(name = "reftripid", referencedColumnName = "tripid")
     @ManyToOne(optional = false)
     private Trip refTrip;*/
    public ActionRunTrip() {

    }

    /*@Override
     public String getMainObjectName(){
     return this.refTrip.getDescription();
     }*/
    @Override
    public TripAction createDerivedInstance() {
        return new ActionRunTrip();
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
        return this.refTrip;
    }

    public void setRefTrip(TimedTrip refTrip) {
        this.refTrip = refTrip;
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
        ((ActionRunTrip) target).setRefTrip(this.getRefTrip());
    }

    public Date getArgumentPlannedTime() {
        if (this.getRefTrip() instanceof PlannedTrip) {
            PlannedTrip timedTrip = (PlannedTrip) this.getRefTrip();

            if (timedTrip.getUtcTimes()) {
                return new Date(timedTrip.getPlannedStartSecs() * 1000L);
            } else {
                //return new Date((timedTrip.getPlannedStartSecs() - timedTrip.getPlannedStartObj().getUtcDiff()) * 1000L);
                return timedTrip.getPlannedStartTime();
            }
        }

        return null;
    }

    @Override
    public void connectToCloned(TripAction tripAction) {
        this.setTimetableObject(tripAction.getTimetableObject());
        if (this.hasSecondObject()) {
            if (tripAction instanceof ActionRunTrip) {
                ((ActionRunTrip) this).setTimetableObject2(((ActionRunTrip) tripAction).getTimetableObject2());
            }
        }
    }

}
