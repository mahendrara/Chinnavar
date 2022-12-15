/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.entities;

import javax.persistence.Entity;
import schedule.entities.util.TimeHelper;

/**
 *
 * @author Jia Li
 */
@Entity
public abstract class TripActionBase extends TripAction {

    public Integer getActionStartDay(TimedTrip trip) {
        Integer plannedStartSecs = trip.getPlannedStartSecs();
        ScheduledDay scheduledDay = null;
        if (trip instanceof ScheduledTrip) {
            scheduledDay = ((ScheduledTrip) trip).getDay();
        }

        if (plannedStartSecs != null) {
            return TimeHelper.getDayFor(trip.getUtcTimes(), scheduledDay != null ? scheduledDay.getDateOfDay() : null, getTimeFromTripStart() + plannedStartSecs, getTimetableObject() != null ? this.getTimetableObject().getTimeZone() : this.getTrip().getAreaObj().getTimeZone());
        }

        return 0;
    }

    /*public void setActionStartDay(Integer day) {
        // Get possible service start time
        Integer plannedStartSecs = 0;
        if (this.getTrip() instanceof TimedTrip) {
            plannedStartSecs = ((TimedTrip) this.getTrip()).getPlannedStartSecs();
        }

        // This is absolute seconds from 00:00:00
        Integer timeFromStart = getTimeFromTripStart();
        Integer totalSecs = plannedStartSecs + timeFromStart;

        totalSecs = totalSecs % (3600 * 24) + day * 3600 * 24;

        // And make it relative
        this.setTimeFromTripStart(totalSecs - plannedStartSecs);
    }*/
 /*used for basic trip's action*/
 /*public Integer getDayForStartTime() {
        TimedTrip timedTrip = (TimedTrip) getTrip();
        ScheduledDay scheduledDay = null;
        if (timedTrip.isActionTimesValid() == false) {
            timedTrip.updateActionTimes();
        }

        if (timedTrip instanceof ScheduledTrip) {
            scheduledDay = ((ScheduledTrip) timedTrip).getDay();
        }

        if (timedTrip.getPlannedStartSecs() != null) {
            return TimeHelper.getDayFor(timedTrip.getUtcTimes(), scheduledDay, getTimeFromTripStart() + timedTrip.getPlannedStartSecs(), timedTrip.getPlannedStartObj().getTimeZone());
        } else {
            return 0;
        }
    }*/

 /*used for basic trip's action*/
    public Integer getActionEndDay(TimedTrip trip) {
        ScheduledDay scheduledDay = null;

        if (trip instanceof ScheduledTrip) {
            scheduledDay = ((ScheduledTrip) trip).getDay();
        }

        if (trip.getPlannedStartSecs() != null) {
            return TimeHelper.getDayFor(trip.getUtcTimes(), scheduledDay != null ? scheduledDay.getDateOfDay() : null, getTimeFromTripStart() + getPlannedSecs() + trip.getPlannedStartSecs(), getTimetableObject() != null ? getTimetableObject().getTimeZone() : this.getTrip().getAreaObj().getTimeZone());
        } else {
            return 0;
        }
    }

    @Override
    public abstract TripAction createDerivedInstance();
}
