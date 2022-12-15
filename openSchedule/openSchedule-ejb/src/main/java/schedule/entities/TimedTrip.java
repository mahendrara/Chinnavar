/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.entities;

import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.Transient;

/**
 *
 * @author EBIScreen
 */
@Entity
@NamedQueries({
    @NamedQuery(name = "TimedTrip.countByTripTemplate", query = "SELECT COUNT(s) FROM TimedTrip s  WHERE s.valid = true and s.tripTemplate.tripId=:templateId")})
public abstract class TimedTrip extends Trip {

    @Column(name = "plannedstartsecs")
    private Integer plannedStartSecs;
    @Column(name = "plannedstopsecs")
    private Integer plannedStopSecs;
    @Column(name = "regstart")
    Trip.regulationType regStart;

    @Column(name = "tripdescriber")
    private String tripDescriber;
     
    @OneToOne(mappedBy = "refTrip")
    private ActionRunTrip serviceAction;

    @ManyToOne
    @JoinColumn(name = "triptemplate", referencedColumnName = "tripid")
    private BasicTrip tripTemplate;

    //@OneToOne(mappedBy = "timedTrip", cascade = CascadeType.ALL, orphanRemoval = true)
    @OneToOne(mappedBy = "timedTrip"/*, fetch= FetchType.EAGER*/)
    private ActionFullTripDriveDuty fullTripDriveDutyAction;

    //Only support one duty now
    @Transient
    private String fullTripDriveDutyDescription;

    public TimedTrip() {
    }
    
    @PostLoad
    public void init() {
        fullTripDriveDutyDescription = fullTripDriveDutyAction != null ? fullTripDriveDutyAction.getTrip().getDescription() : null;
        //if(fullTripDriveDutyAction != null)
         //   System.out.println(this.getTripId() + ": " + fullTripDriveDutyAction.getActionId());
    }

    public void setTripTemplate(BasicTrip tripTemplate) {
        this.tripTemplate = tripTemplate;

        // Automatic update to actionsFromTemplate variable
        setActionsFromTemplate(tripTemplate != null);
    }

    public BasicTrip getTripTemplate() {
        return tripTemplate;
    }

    public Integer getTripTemplateID() {
        if (tripTemplate != null) {
            return tripTemplate.getTripId();
        }
        return null;

    }

    public TripAction getServiceAction() {
        return serviceAction;
    }

    public void setServiceAction(TripAction serviceAction) {
        this.serviceAction = (ActionRunTrip) serviceAction;
    }

    public void setFullTripDriveDutyAction(ActionFullTripDriveDuty duty) {
        this.fullTripDriveDutyAction = duty;
    }

    public ActionFullTripDriveDuty getFullTripDriveDutyAction() {
        return this.fullTripDriveDutyAction;
    }

    public regulationType getRegStart() {
        return regStart;
    }

    public void setRegStart(regulationType regStart) {
        this.regStart = regStart;
    }

    public Integer getPlannedStartSecs() {
        return plannedStartSecs;
    }
    public Integer getPlannedStopSecs() {
        return plannedStopSecs;
    }
     public String getTripDescriber() {
        return tripDescriber;
    }
    public void setTripDescriber(String newDescriber) {
        this.tripDescriber = newDescriber;
    }
    /*public Date getPlannedStopSecsAsDate() {
        if (plannedStopSecs != null) {
            return this.getUtcTimes() ? new Date(getPlannedStopSecs() * 1000L) : new Date((this.getPlannedStopSecs() - this.getPlannedStopObj().getUtcDiff()) * 1000L);
        } else {
            return null;
        }
    }*/
 /*public Integer getDayForPlannedStartSecs() {
        if (plannedStartSecs != null) {
            if (this.getUtcTimes()) {
                return (this.plannedStartSecs + this.getPlannedStartObj().getUtcDiff()) / 3600 / 24;
            } else {
                return this.plannedStartSecs / 3600 / 24;
            }
        } else {
            return 0;
        }
    }

    public Integer getDayForPlannedStopSecs() {
        if (plannedStopSecs != null) {
            if (this.getUtcTimes()) {
                return (this.plannedStopSecs + this.getPlannedStopObj().getUtcDiff()) / 3600 / 24;
            } else {
                return this.plannedStopSecs / 3600 / 24;
            }
        } else {
            return 0;
        }
    }*/
    public abstract Integer getDayForActionStartTime(TripAction tripAction);/* {
        if (!isActionTimesValid()) {
            updateActionTimes();
        }

        if (this.plannedStartSecs != null) {
            Integer startSecs = plannedStartSecs + tripAction.getTimeFromTripStart();
            if (this.getUtcTimes()) {
                
                return (startSecs + tripAction.getTimetableObject().getUtcDiff()) / 3600 / 24;
            } else {
                return startSecs / 3600 / 24;
            }
        } else {
            return 0;
        }

    }*/

    public abstract Integer getDayForActionEndTime(TripAction tripAction);

    /*{
        if (!isActionTimesValid()) {
            updateActionTimes();
        }

        if (this.plannedStartSecs != null) {
            Integer stopSecs = plannedStartSecs + tripAction.getTimeFromTripStart() + tripAction.getPlannedSecs();
            if (this.getUtcTimes()) {
                return (stopSecs + tripAction.getTimetableObject().getUtcDiff()) / 3600 / 24;
            } else {
                return stopSecs / 3600 / 24;
            }
        } else {
            return 0;
        }
    }*/
//    public Integer getDayForTripStartTime(/*ServiceTrip service*/) {
//        if (this.plannedStartSecs != null) {
//            if (this.getUtcTimes()) {
//                return (this.plannedStartSecs + this.getPlannedStartObj().getUtcDiff()/*+ service.getOrigoSecs()*/) / 3600 / 24;
//            } else {
//                return (this.plannedStartSecs /*+ service.getOrigoSecs()*/) / 3600 / 24;
//            }
//        } else {
//            return 0;
//        }
//    }
//
//    public Integer getDayForTripEndTime(/*ServiceTrip service*/) {
//        if (this.plannedStopSecs != null) {
//            if (this.getUtcTimes()) {
//                return (this.plannedStopSecs + this.getPlannedStopObj().getUtcDiff()/* + service.getOrigoSecs()*/) / 3600 / 24;
//            } else {
//                return this.plannedStopSecs / 3600 / 24;
//            }
//        } else {
//            return 0;
//        }
//    }
    public abstract Date getActionStartTime(TripAction tripAction);
//    {
//        if (this.tripTemplate != null) {
//            //Integer plannedStartSecs = 0;
//            if (tripAction.getTimeFromTripStart() != null && this.plannedStartSecs != null) {
//
//                Integer totalSecs = this.plannedStartSecs /*+ this.getOrigoSecs()*/ + tripAction.getTimeFromTripStart();
//                if (this.getUtcTimes() == false) {
//                    totalSecs -= tripAction.getTimetableObject().getUtcDiff();
//                }
//
//                return new Date(totalSecs * 1000L);
//            }
//        }
//
//        return null;
//    }

    public abstract Date getActionEndTime(TripAction tripAction);
//    {
//        if (this.tripTemplate != null) {
//            if (tripAction.getTimeFromTripStart() != null && this.plannedStartSecs != null) {
//
//                Integer totalSecs = plannedStartSecs + tripAction.getTimeFromTripStart() + tripAction.getPlannedSecs();
//                if (this.getUtcTimes() == false) {
//                    totalSecs -= tripAction.getTimetableObject().getUtcDiff();
//                }
//                return new Date(totalSecs * 1000L);
//            }
//        }
//        return null;
//    }

    public void setPlannedStartSecs(Integer plannedStartSecs) {
        this.plannedStartSecs = plannedStartSecs;
    }

    public void setPlannedStopSecs(Integer plannedStopSecs) {
        this.plannedStopSecs = plannedStopSecs;
    }

    public Integer getActionStartSecs(TripAction action) {

        if (!isActionTimesValid()) {
            updateActionTimes();
        }

        int stopSecs = getActionStopSecs(action);
        return stopSecs -= action.getPlannedSecs();
    }

    public Integer getActionStopSecs(TripAction action) {

        if (!isActionTimesValid()) {
            updateActionTimes();
        }
        return getPlannedStartSecs() /*+ getOrigoSecs().intValue()*/ + action.getTimeFromTripStart();
    }

    @Override
    public List<TripAction> getTripActions() {

        if (this.getActionsFromTemplate() != null && this.getActionsFromTemplate() && this.getTripTemplate() != null) {
            return this.getTripTemplate().getTripActions();
        }
        return super.getTripActions();
    }

    @Override
    public TripAction FindFirstActionInStation(TTStation station) {

        if (this.getActionsFromTemplate() != null && this.getActionsFromTemplate() && this.getTripTemplate() != null) {
            return this.getTripTemplate().FindFirstActionInStation(station);
        }
        return super.FindFirstActionInStation(station);
    }

    @Override
    public void updateActionTimes() {

        if (this.getActionsFromTemplate() != null && this.getActionsFromTemplate() && this.getTripTemplate() != null) {
            if (this.getTripTemplate().isActionTimesValid() == false) {
                this.getTripTemplate().updateActionTimes();
            }
        }
        super.updateActionTimes();
    }

    @Override
    public boolean isActionTimesValid() {

        if (this.getActionsFromTemplate() != null && this.getActionsFromTemplate() && this.getTripTemplate() != null) {
            if (!this.getTripTemplate().isActionTimesValid()) {
                return false;
            }
        }
        return super.isActionTimesValid();
    }

    @Override
    public int getNumberOfActions() {
        return this.getTripActions().size();
    }

    @Override
    public void cloneDataToClonedTrip(Trip newTrip) {
        newTrip.setDescription(this.getDescription());
        newTrip.setConsumed(this.getConsumed());
        newTrip.setUtcTimes(this.getUtcTimes());
        newTrip.setTimesAreValid(this.getTimesAreValid());
        newTrip.setDurationSecs(this.getDurationSecs());
        newTrip.setActionsFromTemplate(this.getActionsFromTemplate());
        newTrip.setOrigoSecs(this.getOrigoSecs());

        newTrip.connectToCloned(this.getTripType(), this.getTrainType(), this.getDayTypeList());
        CopyObjects(newTrip);
        CopyActionList(newTrip);
    }

    public void cloneDataToClonedTrip(TimedTrip newTrip, Integer plannedStartSecs) {
        Integer diff = plannedStartSecs - this.plannedStartSecs;
        newTrip.setPlannedStartSecs(plannedStartSecs);
        newTrip.setPlannedStopSecs(this.plannedStopSecs + diff);
        this.cloneDataToClonedTrip(newTrip);
    }

    /*private ZonedDateTime getZonedDateTimeFrom(Integer seconds, String zone) {
         if (seconds != null && zone != null) {
            return ZonedDateTime.of(0, 0, 0, seconds / 3600, seconds / 60, seconds % 60, 0, ZoneId.of(zone));
            
        }else
             return null;
    }*/
    public abstract Date getPlannedStartTime();/* {
        return getUtcTimeFrom(this.plannedStartSecs, this.getPlannedStartObj().getTimeZone());
    }*/

    public abstract Date getPlannedStopTime();/* {
        return getUtcTimeFrom(this.plannedStopSecs, this.getPlannedStopObj().getTimeZone());
    }*/

    public abstract Integer getDayForPlannedStartTime();/* {
        return getDayFor(plannedStartSecs, this.getPlannedStartObj().getTimeZone());
    }*/

    public abstract Integer getDayForPlannedStopTime();/* {
        return getDayFor(plannedStopSecs, this.getPlannedStopObj().getTimeZone());
    }*/

    public String getFullTripDriveDutyDescription() {
        //if(fullTripDriveDutyAction != null)
        //    System.out.println(this.getTripId() + ": " + fullTripDriveDutyAction.getActionId());
        return this.fullTripDriveDutyDescription;
    }

    public void setFullTripDriveDutyDescription(String fullTripDriveDutyDescription) {
        this.fullTripDriveDutyDescription = fullTripDriveDutyDescription;
    }
}
