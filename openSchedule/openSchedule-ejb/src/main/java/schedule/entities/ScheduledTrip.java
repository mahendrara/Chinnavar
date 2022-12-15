/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.entities;

import java.util.Date;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import schedule.entities.util.TimeHelper;

/**
 *
 * @author EBIScreen
 */
@Entity
@DiscriminatorValue(Trip.SCHEDULED_TRIP)
@NamedQueries({
    @NamedQuery(name = "ScheduledTrip.deleteNonArchiveNonActive", query = "DELETE FROM ScheduledTrip s WHERE s.day.scheduledDayCode <= :dayCode1 and s.day.scheduledDayCode >= :dayCode2 and s.day.active = false and s.day.archived = false"),
    @NamedQuery(name = "ScheduledTrip.deleteBySetInvalid", query = "UPDATE ScheduledTrip s set s.valid = false WHERE s.day.scheduledDayCode <= :dayCode1 and s.day.scheduledDayCode >= :dayCode2 and s.day.active = true")})
public class ScheduledTrip extends TimedTrip {

    @ManyToOne(optional = false)
    @JoinColumn(name = "scheduleddaycode", referencedColumnName = "scheduleddaycode")
    private ScheduledDay day;

    @JoinColumn(name = "sourceTrip")
    @ManyToOne
    private PlannedTrip sourceTrip;

    public PlannedTrip getSourceTrip() {
        return sourceTrip;
    }

    public void setSourceTrip(PlannedTrip sourceTrip) {
        this.sourceTrip = sourceTrip;
    }

    public ScheduledDay getDay() {
        return day;
    }

    public void setDay(ScheduledDay day) {
        this.day = day;
    }

    public ScheduledTrip() {

    }

    public void FormFromPlanned(PlannedTrip source, ScheduledDay day) {
        this.setSourceTrip(source);

        this.setDescription(source.getDescription()); // todo ?? set day code also to name
        this.setConsumed(false);
        this.setUtcTimes(true);
        this.setTimesAreValid(source.getTimesAreValid());
        this.setDurationSecs(source.getDurationSecs());
        this.setOrigoSecs(source.getOrigoSecs());

        //this.setTripTemplate(source.getTripTemplate());
        this.setDay(day);

        //newTrip.connectToCloned(this.triptype);
        source.CopyObjects(this);
        //this.setTripTemplate(source.getTripTemplate());
        //source.getTripTemplate().addTimedTrip(this);
        //this.setPlannedStartSecs(source.getPlannedStartSecs() - source.getPlannedStartObj().getUtcDiff());
        //this.setPlannedStopSecs(source.getPlannedStopSecs() - source.getPlannedStopObj().getUtcDiff());
        this.setPlannedStartSecs(TimeHelper.getUtcSecsFromLocalSecs(day.getDateOfDay(), source.getPlannedStartSecs(), getPlannedStartObj().getTimeZone()));
        this.setPlannedStopSecs(TimeHelper.getUtcSecsFromLocalSecs(day.getDateOfDay(), source.getPlannedStopSecs(), getPlannedStopObj().getTimeZone()));
        this.setTripDescriber(source.getTripDescriber());        
//CopyActionList(this); // todo laterwhen we have actions onplanned ???
    }

        @Override
    public Integer getDayForActionStartTime(TripAction tripAction) {
        if (!isActionTimesValid()) {
            updateActionTimes();
        }

        if (this.getPlannedStartSecs() != null) {
            Integer startSecs = getPlannedStartSecs() + tripAction.getTimeFromTripStart();
            return TimeHelper.getDayFor(this.getUtcTimes(), day.getDateOfDay(), startSecs, tripAction.getTimetableObject().getTimeZone());
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
            return TimeHelper.getDayFor(this.getUtcTimes(), day.getDateOfDay(), stopSecs, tripAction.getTimetableObject().getTimeZone());
        } else {
            return 0;
        }
    }

    @Override
    public Date getActionStartTime(TripAction tripAction) {
        if (this.getTripTemplate() != null) {
            if (tripAction.getTimeFromTripStart() != null && this.getPlannedStartSecs() != null) {
                Integer totalSecs = this.getPlannedStartSecs() /*+ this.getOrigoSecs()*/ + tripAction.getTimeFromTripStart();
                return TimeHelper.getUtcTimeFrom(this.getUtcTimes(), day.getDateOfDay(), totalSecs, tripAction.getTimetableObject().getTimeZone());
            }
        }

        return null;
    }

    @Override
    public Date getActionEndTime(TripAction tripAction) {
        if (this.getTripTemplate() != null) {
            if (tripAction.getTimeFromTripStart() != null && this.getPlannedStartSecs() != null) {
                Integer totalSecs = getPlannedStartSecs() + tripAction.getTimeFromTripStart() + tripAction.getPlannedSecs();
                return TimeHelper.getUtcTimeFrom(this.getUtcTimes(), day.getDateOfDay(), totalSecs, tripAction.getTimetableObject().getTimeZone());
            }
        }
        return null;
    }
    
    @Override
    public Date getPlannedStartTime() {
        return TimeHelper.getUtcTimeFrom(this.getUtcTimes(), day.getDateOfDay(), this.getPlannedStartSecs(), this.getPlannedStartObj().getTimeZone());
    }

    @Override
    public Date getPlannedStopTime() {
        return TimeHelper.getUtcTimeFrom(this.getUtcTimes(), day.getDateOfDay(), this.getPlannedStopSecs(), this.getPlannedStopObj().getTimeZone());
    }

    

    @Override
    public Integer getDayForPlannedStartTime() {
        return TimeHelper.getDayFor(this.getUtcTimes(), day.getDateOfDay(), getPlannedStartSecs(), this.getPlannedStartObj().getTimeZone());
    }

    @Override
    public Integer getDayForPlannedStopTime() {
        return TimeHelper.getDayFor(this.getUtcTimes(), day.getDateOfDay(), getPlannedStopSecs(), this.getPlannedStopObj().getTimeZone());
    }
}
