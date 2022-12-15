/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;


@Entity
abstract public class ServiceTrip extends Trip {

    @Column(name = "plannedstartsecs")
    private Integer plannedStartSecs;
    @Column(name = "plannedstopsecs")
    private Integer plannedStopSecs;

    @Transient
    private String serviceDutyDescription;

    public Integer getPlannedStartSecs() {
        return plannedStartSecs;
    }

    public Integer getPlannedStopSecs() {
        return plannedStopSecs;
    }

    public abstract Integer getDayForPlannedStopTime();
//    {
//        ScheduledDay day = null;
//        if (this instanceof ScheduledService) {
//            day = ((ScheduledService) this).getDay();
//        }
//        return TimeHelper.getDayFor(this.getUtcTimes(), day, plannedStartSecs, this.getPlannedStartObj().getTimeZone());
//        /*if (plannedStopSecs != null) {
//            if (this.getUtcTimes()) {
//                return (this.plannedStopSecs + this.getPlannedStopObj().getUtcDiff()) / 3600 / 24;
//            } else {
//                return this.plannedStopSecs / 3600 / 24;
//            }
//        } else {
//            return 0;
//        }*/
//    }

    public abstract Integer getDayForPlannedStartTime();
//    {
//        ScheduledDay day = null;
//        if (this instanceof ScheduledService) {
//            day = ((ScheduledService) this).getDay();
//        }
//        return TimeHelper.getDayFor(this.getUtcTimes(), day, plannedStartSecs, this.getPlannedStartObj().getTimeZone());
//        /*if (plannedStartSecs != null) {
//            if (this.getUtcTimes()) {
//                return (this.plannedStartSecs + this.getPlannedStartObj().getUtcDiff()) / 3600 / 24;
//            } else {
//                return this.plannedStartSecs / 3600 / 24;
//            }
//        } else {
//            return 0;
//        }*/
//    }

    public void setPlannedStartSecs(Integer plannedStartSecs) {
        this.plannedStartSecs = plannedStartSecs;
    }

    public void setPlannedStopSecs(Integer plannedStopSecs) {
        this.plannedStopSecs = plannedStopSecs;
    }

    public void updatePlannedEndTime() {
        Iterator<TripAction> iterator = this.getTripActions().iterator();
        Integer endSecs = this.getPlannedStartSecs();
        while (iterator.hasNext()) {
            endSecs += iterator.next().getPlannedSecs();
        }
        this.setPlannedStopSecs(endSecs);
    }

    @Override
    public void removeTripAction(TripAction tripAction) {
        //this.getTripActions().remove(tripAction);
        if (tripAction.isCreating() == false) {
            Iterator<TripAction> iterator = this.getTripActions().iterator();//sorted list
            while (iterator.hasNext()) {
                TripAction temp = iterator.next();
                if (tripAction.getActionId() == temp.getActionId()) {
                    /*if (temp instanceof ActionRunTrip) {
                        TimedTrip refTrip = (TimedTrip) ((ActionRunTrip) temp).getRefTrip();
                        if (refTrip != null) {
                            //refTrip.setServiceAction(null);
                            //refTrip.getTripTemplate().removeTimedTrip(refTrip);
                            //refTrip.setTripTemplate(null);
                            //BasicTrip template = refTrip.getTripTemplate();
                            //template.removeTimedTrip(refTrip);
                            //((ActionRunTrip) temp).setRefTrip(null);
                        }
                    }*/
                    //temp.setTrip(null);
                    /*if (tripAction instanceof ActionRunTrip) {
                        TimedTrip timedTrip = ((ActionRunTrip) tripAction).getRefTrip();
                        if (timedTrip.getFullTripDriveDutyAction() != null) {
                            TripAction fullTripDriveDutyAction = timedTrip.getFullTripDriveDutyAction();
                            fullTripDriveDutyAction.getTrip().removeTripAction(fullTripDriveDutyAction);
                            ((Duty) fullTripDriveDutyAction.getTrip()).actionChanged();
                            //TODO: cascade or facade to save duty??
                        }
                    }*/
                    iterator.remove();
                } else if (temp.getSeqNo() != null && temp.getSeqNo() > tripAction.getSeqNo()) {
                    temp.setSeqNo(temp.getSeqNo() - 1);
                    temp.setTimeFromTripStart(temp.getTimeFromTripStart() - tripAction.getPlannedSecs());
                    //temp.setMinTimeFromTripStart(temp.getMinTimeFromTripStart() - tripAction.getPlannedSecs());
                }
            }
        }
        /*tripAction.setTrip(null);
         if (tripAction.getRefTrip() != null) {
         ((TimedTrip) (tripAction.getRefTrip())).setServiceAction(null);
         tripAction.setRefTrip(null);
         }*/
    }

    public ServiceTrip() {

    }

    /*public Date getPlannedStopSecsAsDate() {
        if (plannedStopSecs != null && this.getPlannedStartObj() != null) {
            this.getPlannedStopObj().getTTObjId();
            return this.getUtcTimes() ? new Date(this.getPlannedStopSecs() * 1000L) : new Date((this.getPlannedStopSecs() - this.getPlannedStopObj().getUtcDiff()) * 1000L);
        } else {
            return null;
        }
    }

    public void setPlannedStartSecsAsDate(Date startTime) {
        if (startTime != null) {
            long start = startTime.getTime() / 1000L;
            if (this.getUtcTimes() == false) {
                start += this.getPlannedStartObj().getUtcDiff();
            }
            this.plannedStartSecs = this.safeLongtoInteger(start);
        }
    }
    
    public Date getPlannedStartSecsAsDate() {

        if (plannedStartSecs != null && this.getPlannedStartObj() != null) {
            this.getPlannedStartObj().getTTObjId();
            return this.getUtcTimes() ? new Date(this.getPlannedStartSecs() * 1000L) : new Date((this.getPlannedStartSecs() - this.getPlannedStartObj().getUtcDiff()) * 1000L);
        } else {
            return null;
        }
    }
    
    /*public void setPlannedStopSecsAsDate(Date stopTime) {
        if (stopTime != null) {
            long stop = stopTime.getTime() / 1000L;
            if (this.getUtcTimes() == false) {
                stop += this.getPlannedStopObj().getUtcDiff();
            }
            this.plannedStopSecs = this.safeLongtoInteger(stop);
        }
    }*/
    public abstract void setPlannedStartTime(Date time);

    /*{
        this.plannedStartSecs = this.safeLongtoInteger(getSecsFrom(time, getPlannedStartObj().getTimeZone()));
    }*/
    public abstract void setPlannedStopTime(Date time);

    /*{
        this.plannedStopSecs = this.safeLongtoInteger(getSecsFrom(time, getPlannedStopObj().getTimeZone()));
    }*/
    public abstract Date getPlannedStartTime();/* {
        return TimeHelper.getUtcTimeFrom(this.getUtcTimes(), day, plannedStartSecs, getPlannedStartObj().getTimeZone());
    }*/

    public abstract Date getPlannedStopTime();/* {
        return TimeHelper.getUtcTimeFrom(this.getUtcTimes(), plannedStopSecs, getPlannedStopObj().getTimeZone());
    }*/

    protected Integer safeLongtoInteger(Long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(l + " cannot be converted to Integer!");
        }
        return (Integer.valueOf("" + l));
    }

    public List<TimedTrip> getTimedTrips() {
        List<TimedTrip> timedTrips = new ArrayList<>();
        Iterator<TripAction> ite = this.getActionsIterator();

        while (ite.hasNext()) {
            TripAction tripAction = ite.next();
            if (tripAction.getClass() == ActionRunTrip.class) {
                ActionRunTrip runTrip = (ActionRunTrip) tripAction;
                timedTrips.add((TimedTrip) runTrip.getRefTrip());
            }
        }
        return timedTrips;
    }

    public void validateTimedTrips(boolean valid) {
        Iterator<TripAction> ite = this.getActionsIterator();

        while (ite.hasNext()) {
            TripAction tripAction = ite.next();
            if (tripAction.getClass() == ActionRunTrip.class) {
                ActionRunTrip runTrip = (ActionRunTrip) tripAction;
                ((TimedTrip) runTrip.getRefTrip()).setValid(valid);
            }
        }
    }

    public void updateTripTimes() {
        if (!isActionTimesValid()) {
            updateActionTimes();
        }

        if (tripActions != null && tripActions.size() > 0) {
            //Collections.sort(tripActions);
						sort(tripActions);
            Iterator<TripAction> ite = tripActions.iterator();

            while (ite.hasNext()) {
                TripAction curAction = ite.next();
                if (curAction instanceof ActionRunTrip) {
                    TimedTrip timedTrip = ((ActionRunTrip) curAction).getRefTrip();
                    if (timedTrip != null) {
                        timedTrip.setPlannedStartSecs(plannedStartSecs + curAction.getTimeFromTripStart());
                        if (timedTrip.getTripTemplate() != null) {
                            timedTrip.setDurationSecs(timedTrip.getTripTemplate().getDurationSecs());
                        } else {
                            Integer lastSeqNo = timedTrip.getTripActions() != null ? timedTrip.getTripActions().size() : 0;
                            if (lastSeqNo > 0) {
                                timedTrip.setDurationSecs(timedTrip.getTripAction(lastSeqNo).getTimeFromTripStart() + timedTrip.getTripAction(lastSeqNo).getPlannedSecs());
                            } else {
                                timedTrip.setDurationSecs(0);
                            }
                        }
                        timedTrip.setPlannedStopSecs(timedTrip.getPlannedStartSecs() + timedTrip.getDurationSecs());
                    }
                }
            }
        }

    }

    /*public Date getNewRunTripStartTime() {
        if(this.getNumberOfActions()>3) {
            TripAction lastRunTripAction = this.getTripAction(this.getNumberOfActions()-2);
            if(lastRunTripAction instanceof ActionRunTrip)
                return ((ActionRunTrip)lastRunTripAction).getActionEndTime();
        }
        return this.getStartTime();
    }*/
    public String getServiceDutyDescription() {
        return serviceDutyDescription;
    }

    public void setServiceDutyDescription(String dutyDescription) {
        this.serviceDutyDescription = dutyDescription;
    }
}
