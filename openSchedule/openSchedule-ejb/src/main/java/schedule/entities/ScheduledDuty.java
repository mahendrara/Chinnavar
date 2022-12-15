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

@Entity
@NamedQueries({
    @NamedQuery(name = "ScheduledDuty.findByScheduledDay", query = "SELECT s FROM ScheduledDuty s WHERE s.day.scheduledDayCode <= :dayCode1 and s.day.scheduledDayCode >= :dayCode2"),
    @NamedQuery(name = "ScheduledDuty.findByDayTypes", query = "SELECT p FROM ScheduledDuty p join p.dayTypeList d WHERE d.dayTypeId in :a")})
@DiscriminatorValue(Trip.SCHEDULED_DUTY)
public class ScheduledDuty extends Duty {

    @ManyToOne(optional = false)
    @JoinColumn(name = "scheduleddaycode", referencedColumnName = "scheduleddaycode")
    private ScheduledDay day;

    @JoinColumn(name = "sourceTrip")
    @ManyToOne
    private PlannedDuty sourceTrip;

    public PlannedDuty getSourceTrip() {
        return sourceTrip;
    }

    public void setSourceTrip(PlannedDuty sourceTrip) {
        this.sourceTrip = sourceTrip;
    }

    public ScheduledDay getDay() {
        return day;
    }

    public void setDay(ScheduledDay day) {
        this.day = day;
    }

    /*void CopyFromPlanned(PlannedService source) {
        
     source.CopyFromPlanned(source);
     }*/
    @Override
    public String toString() {
        return "schedule.entities.ScheduledDuty[tripid=" + super.getTripId() + "]";
    }

    @Override
    public String getTripClassType() {
        return "ScheduledDuty Entity";
    }

    public Integer getDayForPlannedStopTime() {
        if (this.getPlannedStopObj() != null) {
            return TimeHelper.getDayFor(this.getUtcTimes(), day.getDateOfDay(), this.getPlannedStopSecs(), this.getPlannedStopObj().getTimeZone());
        } else {
            return 0;
        }
    }

    public Integer getDayForPlannedStartTime() {
        if (this.getPlannedStartObj() != null) {
            return TimeHelper.getDayFor(this.getUtcTimes(), day.getDateOfDay(), this.getPlannedStartSecs(), this.getPlannedStartObj().getTimeZone());
        } else {
            return 0;
        }

    }

    public Date getPlannedStartTime() {
        if (getPlannedStartObj() != null) {
            return TimeHelper.getUtcTimeFrom(this.getUtcTimes(), day.getDateOfDay(), this.getPlannedStartSecs(), getPlannedStartObj().getTimeZone());
        } else {
            return new Date();
        }
    }

    public Date getPlannedStopTime() {
        if (getPlannedStopObj() != null) {
            return TimeHelper.getUtcTimeFrom(this.getUtcTimes(), day.getDateOfDay(), this.getPlannedStopSecs(), getPlannedStopObj().getTimeZone());
        } else {
            return new Date();
        }
    }
    /*public void FormFromPlanned(PlannedDuty source, ScheduledDay day) {

        this.setSourceTrip(source);

        this.setDescription(source.getDescription()); // todo ?? set day code also to name
        this.setConsumed(false);
        this.setUtcTimes(true);
        this.setTimesAreValid(source.getTimesAreValid());

       // this.setPlannedStartSecs(source.getPlannedStartSecs() - source.getPlannedStartObj().getUtcDiff());
        //this.setPlannedStopSecs(source.getPlannedStopSecs() - source.getPlannedStopObj().getUtcDiff());
        this.setPlannedStartSecs(TimeHelper.getUtcSecsFromLocalSecs(day.getDateOfDay(), source.getPlannedStartSecs(), getPlannedStartObj().getTimeZone()));
        this.setPlannedStopSecs(TimeHelper.getUtcSecsFromLocalSecs(day.getDateOfDay(), source.getPlannedStopSecs(), getPlannedStopObj().getTimeZone()));

        this.setOrigoSecs(source.getOrigoSecs());
        this.setDurationSecs(source.getDurationSecs());

        this.setDay(day);
        this.setTripType(source.getTripType());

        //newTrip.connectToCloned(this.triptype);
        source.CopyObjects(this);
        source.CopyActionList(this);
    }*/
}
