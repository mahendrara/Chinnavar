package com.schedule.model;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.json.JsonObject;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
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
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.schedule.base.model.EditableEntity;
import com.schedule.base.model.SchedulingState;
import com.schedule.base.model.TripType;
import com.schedule.converter.JsonConverter;

/**
 *
 * @author EBIScreen
 */
@Entity
@Table(name = "trips")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "objectclass", discriminatorType = DiscriminatorType.CHAR)
public abstract class Trip extends EditableEntity implements Serializable {

    /*
     * Discriminator Values Definitions
     * All classes extends  Trip (directly or indirectly) should take one of
     * "@DiscriminatorValue" as below:
     */
    public static final String BASIC_TRIP = "T";
    public static final String PLANNED_SERVICE = "P";
    public static final String PLANNED_TRIP = "F";
    public static final String SCHEDULED_SERVICE = "S";
    public static final String SCHEDULED_TRIP = "A";

    //Used in openschedule-c++, defined here for consistence
    public static final String PLANNED_DUTY = "L";
    public static final String SCHEDULED_DUTY = "C";
    public static final String MOVEMENT_TRIP_TEMPLATE = "N";
    public static final String MOVEMENT_TRIP = "M";
    public static final String MOVEMENT_TRIP_GROUP = "B";

    public enum regulationType {

        CHANGE_FREELY, FIXED_TIMETABLE_MODE, FIXED_ALWAYS
    }

    private static final long serialVersionUID = 1L;
    
    @Basic(optional = false)
    @Column(name = "description")
    private String description;
    @Basic(optional = false)
    @Column(name = "consumed")
    private Boolean consumed;
    @Basic(optional = false)
    @Column(name = "utctimes")
    private Boolean utcTimes;
    @Column(name = "starttime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;
    @Column(name = "endtime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;
    @Column(name = "timesarevalid")
    private Boolean timesAreValid;
    @Id
    @Basic(optional = false)
    @Column(name = "tripid")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer tripId;
    @JoinColumn(name = "triptypeid", referencedColumnName = "triptypeid")
    @ManyToOne(optional = false)
    private TripType tripType;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy(value = "seqNo")
    protected List<TripAction> tripActions;

    @JoinColumn(name = "dayTypeId", referencedColumnName = "dayTypeId")
    @ManyToOne(optional = false)
    private DayType dayType;

    @JoinColumn(name = "plannedstartobj", referencedColumnName = "ttobjid")
    @ManyToOne(optional = false)
    private TTObject plannedStartObj;

    @Column(name = "durationsecs")
    private Integer durationSecs;

    @Column(name = "origosecs")
    private Integer origoSecs;

    @Column(name = "actionsfromtemplate")
    private Boolean actionsFromTemplate;

    @JoinColumn(name = "plannedstopobj", referencedColumnName = "ttobjid")
    @ManyToOne(optional = false)
    private TTObject plannedStopObj;

    @JoinColumn(name = "areaobj", referencedColumnName = "ttobjid")
    @ManyToOne(optional = false)
    private TTArea areaObj;

    @JoinColumn(name = "trainTypeId", referencedColumnName = "trainTypeId")
    @ManyToOne(optional = false)
    private TrainType trainType;

    @Column(name = "valid")
    private boolean valid;

    @JoinColumn(name = "plannedstate", referencedColumnName = "schedulingStateId")
    @ManyToOne(optional = false)
    private SchedulingState plannedState;

    @JoinColumn(name = "currentstate", referencedColumnName = "schedulingStateId")
    @ManyToOne(optional = false)
    private SchedulingState currentState;

    @JoinColumn(name = "usertype", referencedColumnName = "tripUserTypeId")
    @ManyToOne(optional = false)
    private TripUserType tripUserType;

    @Column(name = "version")
    private Integer version = 1;
    
    @Convert(converter=JsonConverter.class)
    @Column(name = "routing")
    private JsonObject routing;

    @Transient
    private boolean actionTimesValid = false;

    public Boolean getActionsFromTemplate() {
        if (actionsFromTemplate == null) {
            return false;
        } else {
            return actionsFromTemplate;
        }
    }

    public void setActionsFromTemplate(Boolean actionsFromTemplate) {
        this.actionsFromTemplate = actionsFromTemplate;
    }

    public Integer getDurationSecs() {
        return durationSecs;
    }

    private String secsToDuration(Integer seconds) {
        //jsf will convert it to localtime, so we need to do that trick here.
        // could be longer than 1 day
        if (seconds != null) {
            Integer temp = Math.abs(seconds);

            temp = temp % (3600 * 24);
            int hour = temp / 3600;
            int min = temp % 3600 / 60;
            int secs = temp % 60;
            StringBuilder str = new StringBuilder();
            if (hour < 10) {
                str.append("0");
            }
            str.append(hour).append(":");
            if (min < 10) {
                str.append("0");
            }
            str.append(min).append(":");
            if (secs < 10) {
                str.append("0");
            }
            str.append(secs);

            if (seconds >= 0) {
                return str.toString();
            } else {
                return "-" + str.toString();
            }
        } else {
            return null;
        }
    }

    public Integer getDayForPlannedDuration() {
        if (durationSecs != null) {
            return this.durationSecs / 3600 / 24;
        } else {
            return 0;
        }
    }

    public String getTimeForPlannedDuration() {
        return this.secsToDuration(this.durationSecs);
    }

    public void setDurationSecs(Integer durationSecs) {
        this.durationSecs = durationSecs;
    }

    public Integer getOrigoSecs() {
        return origoSecs;
    }

    public String getOrigo() {
        if (this.origoSecs != null) {
            return this.secsToDuration(Math.abs(this.origoSecs));
        } else {
            return "";
        }
    }

    public void setOrigo(String origo) throws ParseException {
        String[] splits = origo.split(":");
        String hour = splits[0].startsWith("0") ? splits[0].substring(1) : splits[0];
        String minute = splits[1].startsWith("0") ? splits[1].substring(1) : splits[1];
        String second = splits[2].startsWith("0") ? splits[2].substring(1) : splits[2];
        this.origoSecs = 0 - Integer.valueOf(hour) * 3600 - Integer.valueOf(minute) * 60 - Integer.valueOf(second);
    }

    public void setOrigoSecs(Integer origoSecs) {
        this.origoSecs = origoSecs;
    }

    public TTObject getPlannedStartObj() {
        return plannedStartObj;
    }

    public void setPlannedStartObj(TTObject plannedStartObj) {
        this.plannedStartObj = plannedStartObj;
    }

    public TTObject getPlannedStopObj() {
        return plannedStopObj;
    }

    public void setPlannedStopObj(TTObject plannedStopObj) {
        this.plannedStopObj = plannedStopObj;
    }

    public TTArea getAreaObj() {
        return areaObj;
    }

    public void setAreaObj(TTArea areaObj) {
        this.areaObj = areaObj;
    }

    public Trip() {
        this.tripActions = new ArrayList<>();
        actionsFromTemplate = true;
    }

    public Trip(Integer tripId) {
        this.tripActions = new ArrayList<>();
        this.tripId = tripId;
        actionsFromTemplate = true;
    }

    public Trip(Integer tripId, String description, boolean consumed, boolean utcTimes) {
        this.tripId = tripId;
        this.description = description;
        this.consumed = consumed;
        this.utcTimes = utcTimes;
        this.tripActions = new ArrayList<>();
        actionsFromTemplate = true;
    }

    public void cloneDataToClonedTrip(Trip newTrip) {
        if (newTrip.description == null) {
            newTrip.description = "Clone of " + this.description;
        }
        newTrip.consumed = this.consumed;
        newTrip.utcTimes = this.utcTimes;
        newTrip.timesAreValid = this.timesAreValid;
        newTrip.durationSecs = this.durationSecs;
        newTrip.actionsFromTemplate = this.actionsFromTemplate;
        newTrip.origoSecs = this.origoSecs;
        newTrip.valid = this.valid;

        newTrip.connectToCloned(this.tripType, this.trainType, this.dayType);
        CopyObjects(newTrip);
        CopyActionList(newTrip);
    }

    public void cloneDataToClonedTrip(Trip newTrip, Integer startSeqNo, Integer endSeqNo) {
        newTrip.consumed = this.consumed;
        newTrip.utcTimes = this.utcTimes;
        newTrip.timesAreValid = this.timesAreValid;
        //newTrip.durationSecs = this.durationSecs;
        newTrip.durationSecs = this.getTripAction(endSeqNo).getTimeFromTripStart() + this.getTripAction(endSeqNo).getPlannedSecs() - this.getTripAction(startSeqNo).getTimeFromTripStart();
        newTrip.actionsFromTemplate = this.actionsFromTemplate;
        newTrip.origoSecs = this.origoSecs;
        newTrip.valid = this.valid;

        newTrip.connectToCloned(this.tripType, this.trainType, this.dayType);
        CopyObjects(newTrip);
        CopyActionList(newTrip, startSeqNo, endSeqNo);
        newTrip.setPlannedStartObj(getTripAction(startSeqNo).getTimetableObject());
        //This might be getTimetableObject2
        TripAction lastTripAction = newTrip.getTripAction(newTrip.getNumberOfActions());
        newTrip.setPlannedStopObj(lastTripAction.hasSecondObject() ? lastTripAction.getTimetableObject2() : lastTripAction.getTimetableObject());
        newTrip.setDescription(newTrip.getPlannedStartObj().getDescription() + " - " + newTrip.getPlannedStopObj().getDescription());
    }

    void CopyObjects(Trip newTrip) {
        newTrip.setPlannedStopObj(this.getPlannedStopObj());
        newTrip.setPlannedStartObj(this.getPlannedStartObj());
        newTrip.setAreaObj(this.getAreaObj());
        newTrip.setTrainType(this.getTrainType());
        newTrip.setDayType(this.getDayType());
        newTrip.setTripType(this.getTripType());
        newTrip.setPlannedState(this.getPlannedState());
        newTrip.setCurrentState(this.getCurrentState());
        newTrip.setTripUserType(this.getTripUserType());
        newTrip.setRouting(this.getRouting());
    }

    public void CopyActionList(Trip newTrip) {

        Iterator<TripAction> ite = this.tripActions.iterator();
        while (ite.hasNext()) {
            TripAction curAction = ite.next();
            TripAction cloned = curAction.createDerivedInstance();

            curAction.cloneDataToNonPersited(cloned);

            newTrip.addTripAction(cloned, newTrip.getNumberOfActions());
        }
    }

    public void CopyActionList(Trip newTrip, Integer startSeqNo, Integer endSeqNo) {

        Iterator<TripAction> ite = this.tripActions.iterator();
        Integer index = 0;
        while (ite.hasNext()) {
            TripAction curAction = ite.next();
            if (curAction.getSeqNo() >= startSeqNo && curAction.getSeqNo() <= endSeqNo) {
                TripAction cloned = curAction.createDerivedInstance();
                curAction.cloneDataToNonPersited(cloned);
                cloned.setSeqNo(index + 1);
                newTrip.addTripAction(cloned, index++);
            }
        }
    }

    public void connectToCloned(TripType tripType, TrainType trainType, DayType dayType) {
        this.tripType = tripType;
        this.trainType = trainType;
        this.dayType = dayType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getConsumed() {
        return consumed;
    }

    public void setConsumed(Boolean consumed) {
        this.consumed = consumed;
    }

    public Boolean getUtcTimes() {
        return utcTimes;
    }

    public void setUtcTimes(Boolean utcTimes) {
        this.utcTimes = utcTimes;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Boolean getTimesAreValid() {
        return timesAreValid;
    }

    public void setTimesAreValid(Boolean timesAreValid) {
        this.timesAreValid = timesAreValid;
    }

    public Integer getTripId() {
        return tripId;
    }

    public void setTripId(Integer tripId) {
        this.tripId = tripId;
    }

    public TripType getTripType() {
        return tripType;
    }

    public void setTripType(TripType tripType) {
        this.tripType = tripType;
    }

    public List<TripAction> getTripActions() {
        if (!actionTimesValid) {
            updateActionTimes();
        }
        Collections.sort(tripActions);
        return tripActions;
    }

    protected void sort(List<TripAction> tripActions) {
        TripAction temp;

        int n = tripActions.size();

        for (int i = 0; i < n; i++) {
            for (int j = 1; j < (n - i); j++) {

                if (tripActions.get(j - 1).getSeqNo() > tripActions.get(j).getSeqNo()) {
                    //swap the elements!
                    temp = tripActions.get(j - 1);
                    tripActions.set(j - 1, tripActions.get(j));
                    tripActions.set(j, temp);
                }

            }
        }
    }

    public TripAction getTripAction(int seqNo) {
        if (this.tripActions != null && this.tripActions.size() > 0) {
            Iterator<TripAction> iterator = this.tripActions.iterator();
            while (iterator.hasNext()) {
                TripAction tripAction = iterator.next();
                if (tripAction.getSeqNo() != null && (tripAction.getSeqNo() == seqNo)) {
                    return tripAction;
                }
            }
        }
        return null;
    }

    public boolean isActionTimesValid() {
        return actionTimesValid;
    }

    public void setActionTimesValid(boolean actionTimesValid) {
        this.actionTimesValid = actionTimesValid;
    }

    public TripAction FindFirstActionInStation(TTStation station) {

        if (tripActions != null) {
            Iterator<TripAction> ite = this.tripActions.iterator();

            while (ite.hasNext()) {
                TripAction curAction = ite.next();

                if (station.isParentOf(curAction.getTimetableObject())) {
                    return curAction;
                }
            }
        }
        return null;
    }

    public void updateActionTimes() {
        if (tripActions != null && tripActions.size() > 0) {
            sort(tripActions);
            Iterator<TripAction> ite = tripActions.iterator();

            int totalSecs = this.origoSecs;
            int totalMin = this.origoSecs;
            while (ite.hasNext()) {
                TripAction curAction = ite.next();

                curAction.setTimeFromTripStart(totalSecs);
                curAction.setMinTimeFromTripStart(totalMin);

                if (curAction.getPlannedSecs() != null) {
                    totalSecs += curAction.getPlannedSecs();
                }
                if (curAction.getMinSecs() != null) {
                    totalMin += curAction.getMinSecs();
                }
            }
            actionTimesValid = true;
        }
    }

    public Iterator<TripAction> getActionsIterator() {

        return this.tripActions.iterator();
    }

    public void setTripActions(List<TripAction> t) {
        this.tripActions = t;
    }

    public void addTripAction(TripAction action, int index) {
        this.tripActions.add(index, action);
        action.setTrip(this);
    }

    public void addTripAction(TripAction action) {
        this.tripActions.add(action);
        action.setTrip(this);
    }

    public void removeTripAction(TripAction tripAction) {
        this.tripActions.remove(tripAction);
        tripAction.setTrip(null);
    }

    public int getNumberOfActions() {
        return tripActions == null ? 0 : this.tripActions.size();
    }

    public String getTripClassType() {
        return "Trip Entity";
    }

    public DayType getDayType() {
        return dayType;
    }

    public void setDayType(DayType dayType) {
        this.dayType = dayType;
    }

    public TrainType getTrainType() {
        return trainType;
    }

    public void setTrainType(TrainType trainType) {
        this.trainType = trainType;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public SchedulingState getPlannedState() {
        return plannedState;
    }

    public void setPlannedState(SchedulingState plannedState) {
        this.plannedState = plannedState;
    }

    public SchedulingState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(SchedulingState currentState) {
        this.currentState = currentState;
    }

    public TripUserType getTripUserType() {
        return tripUserType;
    }

    public void setTripUserType(TripUserType tripUserState) {
        this.tripUserType = tripUserState;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public void increaseVersion() {
        if (this.version == null) {
            this.version = 1;
        }
        this.version++;
    }

    public JsonObject getRouting() {
        return routing;
    }

    public void setRouting(JsonObject routing) {
        this.routing = routing;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (tripId != null ? tripId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Trip)) {
            return false;
        }
        Trip other = (Trip) object;
        if ((this.tripId == null && other.tripId != null) || (this.tripId != null && !this.tripId.equals(other.tripId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "schedule.entities.Trip[tripid=" + tripId + "]";
    }

    /**
     * Calculates Trip's duration. Sets duration to sum of trip's tripactions
     * plannedSecs
     *
     * @return new duration
     */
    public int recalculateTripDuration() {
        durationSecs = 0;
        Iterator<TripAction> iterator = tripActions.iterator();
        while (iterator.hasNext()) {
            TripAction tripAction = iterator.next();
            durationSecs += tripAction.getPlannedSecs();
        }
        return durationSecs;
    }

    public Integer getDefaultMinSecs() {
        Integer minSecs = 0;

        Iterator<TripAction> iterator = this.getTripActions().iterator();
        while (iterator.hasNext()) {
            minSecs += iterator.next().getMinSecs();
        }
        return minSecs;

    }
}

