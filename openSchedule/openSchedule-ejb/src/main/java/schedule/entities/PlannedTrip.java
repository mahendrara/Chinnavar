/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.entities;

import java.util.Date;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import schedule.entities.util.TimeHelper;

/**
 *
 * @author EBIScreen
 */
@Entity
@DiscriminatorValue(Trip.PLANNED_TRIP)
@NamedQueries(
{
        @NamedQuery(name = "PlannedTrip.findByDayTypeList", query = "SELECT distinct p FROM PlannedTrip p WHERE p.dayTypeList in :a")
})
public class PlannedTrip extends TimedTrip {

    public PlannedTrip() {

    }

    @Override
    public Integer getDayForActionStartTime(TripAction tripAction) {
        if (!isActionTimesValid()) {
            updateActionTimes();
        }

        if (this.getPlannedStartSecs() != null) {
            Integer startSecs = getPlannedStartSecs() + tripAction.getTimeFromTripStart();
            return TimeHelper.getDayFor(this.getUtcTimes(), null, startSecs, tripAction.getTimetableObject().getTimeZone());
        } else {
            return 0;
        }

    }

    @Override
    public Integer getDayForActionEndTime(TripAction tripAction) {
        if (!isActionTimesValid()) {
            updateActionTimes();
        }

        if (this.getPlannedStartSecs() != null) {
            Integer stopSecs = this.getPlannedStartSecs() + tripAction.getTimeFromTripStart() + tripAction.getPlannedSecs();
            return TimeHelper.getDayFor(this.getUtcTimes(), null, stopSecs, tripAction.getTimetableObject().getTimeZone());
        } else {
            return 0;
        }
    }

    @Override
    public Date getActionStartTime(TripAction tripAction) {
        //if (this.getTripTemplate() != null) {
            if (tripAction.getTimeFromTripStart() != null && this.getPlannedStartSecs() != null) {
                Integer totalSecs = this.getPlannedStartSecs() /*+ this.getOrigoSecs()*/ + tripAction.getTimeFromTripStart();
                return TimeHelper.getUtcTimeFrom(this.getUtcTimes(), null, totalSecs, tripAction.getTimetableObject().getTimeZone());
            }
        //}

        return null;
    }

    @Override
    public Date getActionEndTime(TripAction tripAction) {
        //if (this.getTripTemplate() != null) {
            if (tripAction.getTimeFromTripStart() != null && this.getPlannedStartSecs() != null) {
                Integer totalSecs = getPlannedStartSecs() + tripAction.getTimeFromTripStart() + tripAction.getPlannedSecs();
                return TimeHelper.getUtcTimeFrom(this.getUtcTimes(), null, totalSecs, tripAction.getTimetableObject().getTimeZone());
            }
        //}
        return null;
    }
    
    @Override
    public Date getPlannedStartTime() {
        return TimeHelper.getUtcTimeFrom(this.getUtcTimes(), null, this.getPlannedStartSecs(), this.getPlannedStartObj().getTimeZone());
    }

    @Override
    public Date getPlannedStopTime() {
        return TimeHelper.getUtcTimeFrom(this.getUtcTimes(), null, this.getPlannedStopSecs(), this.getPlannedStopObj().getTimeZone());
    }

    

    @Override
    public Integer getDayForPlannedStartTime() {
        return TimeHelper.getDayFor(this.getUtcTimes(), null, getPlannedStartSecs(), this.getPlannedStartObj().getTimeZone());
    }

    @Override
    public Integer getDayForPlannedStopTime() {
        return TimeHelper.getDayFor(this.getUtcTimes(), null, getPlannedStopSecs(), this.getPlannedStopObj().getTimeZone());
    }
}
