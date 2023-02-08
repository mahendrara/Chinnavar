package com.schedule.model;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.schedule.base.model.ActionType;
import com.schedule.base.model.EditableEntity;

/**
 *
 * @author EBIScreen
 */
@Entity
@Table(name = "tripactions")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "objectclass", discriminatorType = DiscriminatorType.CHAR)
@NamedQueries({
    @NamedQuery(name = "TripAction.deleteByTrips", query = "DELETE FROM TripAction s WHERE s.trip.tripId in :a")})
public abstract class TripAction extends EditableEntity implements Serializable, Comparable<TripAction> {

    private static final long serialVersionUID = 1L;
    /*
    * Discriminator Values Definitions
    * All classes extends  Tripaction (directly or indirectly) should take one of
    * "@DiscriminatorValue" as below:
     */
    public static final String ACTION_CARGO_STOP = "C";
    public static final String ACTION_CREW = "D";
    public static final String ACTION_GLUE = "G";
    public static final String ACTION_PASS_OBJECT = "N";
    public static final String ACTION_PASSENGER_STOP = "P";
    public static final String ACTION_RUN_TRIP = "R";
    public static final String ACTION_SCHEDULING_STOP = "S";
    public static final String ACTION_TRAIN_FORMATION = "F";
    public static final String ACTION_TRAIN_INTERNAL = "I";
    public static final String ACTION_TRAIN_MOVING = "M";

    //Used in openschedule-c++. Define here for consistence
    public static final String ACTION_BOOK_DUTY = "B";
    public static final String ACTION_FULL_TRIP_DRIVE_DUTY = "U";
    public static final String ACTION_PARTIAL_TRIP_DRIVE_DUTY = "T";
    public static final String ACTION_MANDATORY_BREAK_DUTY = "Y";
    public static final String ACTION_OPTIONAL_BREAK_DUTY = "O";
    public static final String ACTION_ROUTE_TRIP = "E";
    public static final String ACTION_SHUNTING_ROUTE_TRIP = "Z";
    public static final String ACTION_CMD_TRIP = "K";
    public static final String ACTION_AUTOMATION_TRIP = "A";
    public static final String ACTION_MOVEMENT_TRIP = "H";

    @Override
    public int compareTo(TripAction o) {

        if (this.seqNo == null) {
            return -1;
        }
        if (o.seqNo == null) {
            return 1;
        } else {

            return seqNo > o.getSeqNo() ? 1
                    : seqNo < o.getSeqNo() ? -1
                    : 0;
        }
    }

    public enum MainActionTypeEnum {

        UNKNOWN(0),
        RUN_TRIP(1), //(R)service action
        PASSENGER_STOP(2), //(P)trip action
        CARGO_STOP(3), //(C)trip action
        SCHEDULING_STOP(4), //(S)trip action
        TRAIN_MOVING(5), //(M)trip action
        TRAIN_FORMATION(6), //(F)service action
        TRAIN_ACTION(7), //(I)
        CREW(8), //(D)
        PASS_OBJECT(9), //(N)trip action
        GLUE(10), //(G)service action
        CUSTOMER_INFO(11),
        MAINTENANCE(12),
        BOOK_DUTY(13), // (B)duty action
        FULL_TRIP_DRIVE_DUTY(14), //(U)duty action
        PARTIAL_TRIP_DRIVE_DUTY(15), // (T)duty action
        MANDATORY_BREAK_DUTY(16), //(Y)duty action
        OPTIONAL_BREAK_DUTY(17),
        ROUTE_ACTION(18),
        SINGLE_CMD_ACTION(19),
        START_AUTOMATON_ACTION(20),
        STOP_AUTOMATON_ACTION(21),
        SHUNTING_ROUTE_ACTION(22),
        MOVEMENT_TRIP_ACTION(23);

        private int value;

        MainActionTypeEnum(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

        public static MainActionTypeEnum parse(int value) {
            MainActionTypeEnum actionType = null; // Default
            for (MainActionTypeEnum item : MainActionTypeEnum.values()) {
                if (item.getValue() == value) {
                    actionType = item;
                    break;
                }
            }
            return actionType;
        }
    }

    @Basic(optional = false)
    @Column(name = "seqno")
    private Integer seqNo;

    @Column(name = "actionarg1")
    private Integer actionArg1;
    @Column(name = "actionarg2")
    private Integer actionArg2;

    @Column(name = "timesvalid")
    private Boolean timesValid;
    @Basic(optional = false)
    @Column(name = "plannedsecs")
    private Integer plannedSecs;
    @Basic(optional = false)
    @Column(name = "minsecs")
    private Integer minSecs;
    @Basic(optional = false)
    @Column(name = "consumed")
    private Boolean consumed;
    @Id
    @Basic(optional = false)
    @Column(name = "actionid")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer actionId;

    @JoinColumn(name = "atypeid", referencedColumnName = "atypeid")
    @ManyToOne(optional = false)
    private ActionType actionType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tripid", referencedColumnName = "tripid")
    private Trip trip;

    @JoinColumn(name = "ttobjid", referencedColumnName = "ttobjid")
    @ManyToOne(optional = false)
    private TTObject timetableObject;

    @Transient
    private Integer timeFromTripStart; // This is sum of earlier TripAction.plannedSecs
    @Transient
    private Integer minTimeFromTripStart; // This is sum of earlier TripAction.minSecs

    @Transient
    private TripAction clonedFrom;

    public TripAction getClonedFrom() {
        return clonedFrom;
    }

    public void setClonedFrom(TripAction clonedFrom) {
        this.clonedFrom = clonedFrom;
    }

    public Integer getMinTimeFromTripStart() {
        if (trip.isActionTimesValid() == false) {
            this.trip.updateActionTimes();
        }

        if (minTimeFromTripStart != null) {
            return minTimeFromTripStart;
        }

        return 0;
    }

    // Allowed to be called only from TRIP::UPDATEACTIONTIMES
    public void setMinTimeFromTripStart(Integer minTimeFromTripStart) {
        this.minTimeFromTripStart = minTimeFromTripStart;
    }

    public Integer getDayForMinEndTimeFromTripStart() {
        if (trip.isActionTimesValid() == false) {
            this.trip.updateActionTimes();
        }

        return (getMinSecs() + getMinTimeFromTripStart()) / (3600 * 24);
    }

    // Returns only up to 23:59:59
    public String getTimeForMinEndTimeFromTripStart() {
        return this.getDuration(getMinTimeFromTripStart() + getMinSecs());
    }

    public Integer getTimeFromTripStart() {
        if (trip.isActionTimesValid() == false) {
            this.trip.updateActionTimes();
        }

        if (timeFromTripStart != null) {
            return timeFromTripStart;
        } else {
            return 0;
        }
    }

    public String getTimeForEndTimeFromTripStart() {
        return this.getDuration(getTimeFromTripStart() + getPlannedSecs());
    }

    public Integer getDayForEndTimeFromTripStart() {
        return (getTimeFromTripStart() + getPlannedSecs()) / 3600 / 24;
    }

    public void setTimeFromTripStart(Integer timeFromTripStart) {
        this.timeFromTripStart = timeFromTripStart;
    }

    public TripAction() {
    }

    public TripAction(Integer actionId) {
        this.actionId = actionId;
    }

    public TripAction(Integer actionId, Integer seqNo, Integer plannedSecs, Integer minSecs, boolean consumed) {
        this.actionId = actionId;
        this.seqNo = seqNo;

        setPlannedSecs(plannedSecs);
        setMinSecs(minSecs);
        this.consumed = consumed;

    }

    public void cloneDataToNonPersited(TripAction target) {
        target.seqNo = this.seqNo;
        target.setPlannedSecs(plannedSecs);
        target.setMinSecs(minSecs);
        target.consumed = this.consumed;
        target.actionArg1 = this.actionArg1;
        target.actionArg2 = this.actionArg2;

        target.timesValid = this.timesValid;
        target.setEditing(this.isEditing());
        target.setCreating(this.isCreating());

        target.minTimeFromTripStart = this.minTimeFromTripStart;
        target.timeFromTripStart = this.timeFromTripStart;

        target.connectToCloned(this);
        target.setActionType(this.actionType);
        target.setClonedFrom(this);
    }

    // Overriden in actions, which inherit this
    public void connectToCloned(TripAction tripAction) {
        this.setTimetableObject(tripAction.getTimetableObject());
    }

    public void connectToCloned(Trip trip) {
        this.setTrip(trip);
        if (trip != null) {
            Iterator<TripAction> ite = trip.getTripActions().iterator();
            int i = -1;
            while (ite.hasNext()) {
                i++;
                TripAction checkAction = ite.next();
                if (checkAction.getActionId() == this.getActionId()) {
                    trip.getTripActions().set(i, checkAction);
                }
            }

        }
    }

    public boolean hasSecondObject() {
        return false;
    }

    public Integer getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(Integer seqNo) {
        this.seqNo = seqNo;
    }

    public Integer getActionArg1() {
        return actionArg1;
    }

    public void setActionArg1(Integer actionArg1) {
        this.actionArg1 = actionArg1;
    }

    public Integer getActionArg2() {
        return actionArg2;
    }

    public void setActionArg2(Integer actionArg2) {
        this.actionArg2 = actionArg2;
    }

    public Boolean getTimesValid() {
        return timesValid;
    }

    public void setTimesValid(Boolean timesValid) {
        this.timesValid = timesValid;
    }

    public Integer getPlannedSecs() {
        if (plannedSecs != null) {
            return plannedSecs;
        } else {
            return 0;
        }
    }

    public void setPlannedSecs(Integer plannedSecs) {
        this.plannedSecs = plannedSecs;
    }

    public String getDuration(Integer seconds) {
        if (seconds != null) {
            Integer temp = Math.abs(seconds);
            temp = temp % (3600 * 24);
            int hour = temp / 3600;
            int min = temp % 3600 / 60;
            int secs = temp % 60;

            String s = "";
            if (seconds < 0) {
                s = "-";
            }

            return String.format("%s%02d:%02d:%02d", s, hour, min, secs);
        } else {
            return null;
        }
    }

    public String getTimeForPlannedDuration() {
        return getDuration(this.plannedSecs);
    }

    public Integer getDayForPlannedDuration() {
        if (plannedSecs == null) {
            return 0;
        }

        return plannedSecs / (3600 * 24);
    }

    public void setDayForPlannedDuration(Integer day) {
        plannedSecs = Math.abs(plannedSecs % (3600 * 24)) + day * 3600 * 24;
    }

    public Integer getDayForMinDuration() {
        if (minSecs == null) {
            return 0;
        }

        return minSecs / (3600 * 24);
    }

    public void setDayForMinDuration(Integer day) {
        minSecs = Math.abs(minSecs % (3600 * 24)) + day * 3600 * 24;
    }

    @SuppressWarnings("deprecation")
    public Date getPlannedDuration() {
        Date plannedDuration = new Date();
        plannedDuration.setHours(Math.abs(plannedSecs) / 3600);
        plannedDuration.setMinutes(Math.abs(plannedSecs) % 3600 / 60);
        plannedDuration.setSeconds(Math.abs(plannedSecs) % 60);
        return plannedDuration;
    }

    @SuppressWarnings("deprecation")
    public void setPlannedDuration(Date plannedDuration) {
        int day = this.plannedSecs / (3600 * 24);
        plannedSecs = plannedDuration.getHours() * 3600 + plannedDuration.getMinutes() * 60 + plannedDuration.getSeconds() + day * 3600 * 24;
    }

    @SuppressWarnings("deprecation")
    public Date getMinDuration() {
        Date minDuration = new Date();
        minDuration.setHours(Math.abs(minSecs) / 3600);
        minDuration.setMinutes(Math.abs(minSecs) % 3600 / 60);
        minDuration.setSeconds(Math.abs(minSecs) % 60);
        return minDuration;
    }

    @SuppressWarnings("deprecation")
    public void setMinDuration(Date minDuration) {
        int day = this.minSecs / (3600 * 24);
        minSecs = minDuration.getHours() * 3600 + minDuration.getMinutes() * 60 + minDuration.getSeconds() + day * 3600 * 24;
    }

    public Integer getMinSecs() {
        if (minSecs != null) {
            return minSecs;
        }

        return 0;
    }

    public void setMinSecs(Integer minSecs) {
        this.minSecs = minSecs;
    }

    public String getTimeForMinDuration() {
        return this.getDuration(minSecs);
    }

    public Boolean getConsumed() {
        return consumed;
    }

    public void setConsumed(Boolean consumed) {
        this.consumed = consumed;
    }

    public Integer getActionId() {
        return actionId;
    }

    public void setActionId(Integer actionId) {
        this.actionId = actionId;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public Trip getTrip() {
        return this.trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    public TTObject getTimetableObject() {
        return timetableObject;
    }

    public TTObject getTimetableObject2() {
        return null;
    }

    public void setTimetableObject(TTObject ttObject) {
        this.timetableObject = ttObject;
    }

    public String getMainObjectName() {
        if (this.timetableObject != null) {
            return this.timetableObject.getDescription();
        } else {
            return "";
        }
    }

    public boolean isPlanedSecsEditable() {
        return this.actionType.isMainActionType(MainActionTypeEnum.RUN_TRIP) == false;
    }

    public boolean isMinSecsEditable() {
        return this.actionType.isMainActionType(MainActionTypeEnum.RUN_TRIP) == false;
    }

    abstract public TripAction createDerivedInstance();

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (actionId != null ? actionId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TripAction)) {
            return false;
        }
        TripAction other = (TripAction) object;
        if ((this.actionId == null && other.actionId != null) || (this.actionId != null && !this.actionId.equals(other.actionId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ScheduleEntities.Tripaction[actionid=" + actionId + "]";
    }
}
