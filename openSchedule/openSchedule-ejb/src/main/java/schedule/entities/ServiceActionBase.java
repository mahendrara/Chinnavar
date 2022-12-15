/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.entities;

import java.util.Date;
import javax.persistence.Entity;
import schedule.entities.util.TimeHelper;

/**
 *
 * @author Jia Li
 */
@Entity
public abstract class ServiceActionBase extends TripAction {

    public Integer getActionStartDay() {
        Integer plannedStartSecs = ((ServiceTrip) this.getTrip()).getPlannedStartSecs();
        ScheduledDay scheduledDay = null;
        if (this.getTrip() instanceof ScheduledService) {
            scheduledDay = ((ScheduledService) this.getTrip()).getDay();
        }

        if (plannedStartSecs != null) {
            return TimeHelper.getDayFor(getTrip().getUtcTimes(), scheduledDay != null ? scheduledDay.getDateOfDay() : null, getTimeFromTripStart() + plannedStartSecs, this.getTimetableObject() != null ? this.getTimetableObject().getTimeZone() : this.getTrip().getAreaObj().getTimeZone());
        }

        return 0;
    }

    public void setActionStartDay(Integer day) {
        // Get possible service start time
        Integer plannedStartSecs = 0;
        if (this.getTrip() instanceof ServiceTrip) {
            plannedStartSecs = ((ServiceTrip) this.getTrip()).getPlannedStartSecs();
        }

        // This is absolute seconds from 00:00:00
        Integer timeFromStart = getTimeFromTripStart();
        Integer totalSecs = plannedStartSecs + timeFromStart;

        totalSecs = totalSecs % (3600 * 24) + day * 3600 * 24;

        // And make it relative
        this.setTimeFromTripStart(totalSecs - plannedStartSecs);
    }

    public Date getActionStartTime() {

        ScheduledDay scheduledDay = null;
        if (this.getTrip() instanceof ScheduledService) {
            ScheduledService service = (ScheduledService) this.getTrip();
            scheduledDay = service.getDay();
        }

        Integer secs = getActionStartSecs();
        if (secs != null) {
            TTObject ttObject = this.getTimetableObject() != null ? getTimetableObject() : getTrip().getAreaObj();
            return TimeHelper.getUtcTimeFrom(getTrip().getUtcTimes(), scheduledDay != null ? scheduledDay.getDateOfDay() : null, secs, ttObject.getTimeZone());
        }

        return null;
    }

    public void setActionStartTime(Date actionStartTime) {
        // Get possible service start time
        Integer plannedStartSecs = ((ServiceTrip) getTrip()).getPlannedStartSecs();

        ScheduledDay scheduledDay = null;
        if (actionStartTime != null && getTrip() != null) {
            int day = (plannedStartSecs + this.getTimeFromTripStart()) / (3600 * 24);
            TTObject ttObject = this.getTimetableObject() != null ? this.getTimetableObject() : getTrip().getAreaObj();
            if (getTrip() instanceof ScheduledService) {
                scheduledDay = ((ScheduledService) getTrip()).getDay();
                Integer startSecs = safeLongtoInteger(TimeHelper.getUTCSecsFrom(getTrip().getUtcTimes(), scheduledDay.getDateOfDay(), actionStartTime, ttObject.getTimeZone()));
                this.setTimeFromTripStart(startSecs + day * 3600 * 24 - plannedStartSecs % (3600 * 24));
            } else {
                Integer startSecs = safeLongtoInteger(TimeHelper.getLocalSecsFrom(getTrip().getUtcTimes(), actionStartTime, ttObject.getTimeZone()));
                this.setTimeFromTripStart(startSecs + day * 3600 * 24 - plannedStartSecs % (3600 * 24));
            }
        }
    }

    public Date getActionEndTime() {
        Integer secs = getActionEndSecs();

        TTObject ttObject = this.getTimetableObject() != null ? getTimetableObject() : getTrip().getAreaObj();
        ScheduledDay scheduledDay = null;
        if (getTrip() instanceof ScheduledService) {
            scheduledDay = ((ScheduledService) getTrip()).getDay();
        }

        return TimeHelper.getUtcTimeFrom(getTrip().getUtcTimes(), scheduledDay != null ? scheduledDay.getDateOfDay() : null, secs, ttObject.getTimeZone());
    }

    public Integer getActionEndSecs() {
        Integer secs = getActionStartSecs();
        if (secs != null) {
            secs += getPlannedSecs();
        }

        return secs;
    }

    private Integer safeLongtoInteger(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(l + " cannot be converted to Integer!");
        }
        return (Integer.valueOf("" + l));
    }

    public Integer getActionStartSecs() {
        if (getTrip() != null && getTrip() instanceof ServiceTrip) {
            if (((ServiceTrip) getTrip()).getPlannedStartSecs() != null && this.getTimeFromTripStart() != null) {
                Integer totalSecs = ((ServiceTrip) getTrip()).getPlannedStartSecs() + this.getTimeFromTripStart();
                return totalSecs;
            }
        }

        return null;
    }

    public Integer getActionEndDay() {
        Integer plannedStartSecs = ((ServiceTrip) this.getTrip()).getPlannedStartSecs();
        ScheduledDay scheduledDay = null;

        if (getTrip() instanceof ScheduledService) {
            scheduledDay = ((ScheduledService) this.getTrip()).getDay();
        }

        if (plannedStartSecs != null) {
            return TimeHelper.getDayFor(getTrip().getUtcTimes(), scheduledDay != null ? scheduledDay.getDateOfDay() : null, getTimeFromTripStart() + plannedStartSecs + this.getPlannedSecs(), this.getTimetableObject2() != null ? this.getTimetableObject2().getTimeZone() : this.getTrip().getAreaObj().getTimeZone());
        }

        return 0;
    }

    @Override
    public abstract TripAction createDerivedInstance();
}
