/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.entities;

/**
 *
 * @author Jia Li
 */
import java.util.Date;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import schedule.entities.util.TimeHelper;

@Entity
@NamedQueries({
    //@NamedQuery(name = "PlannedDuty.findByDayTypes", query = "SELECT p FROM PlannedDuty p WHERE p.dayType.dayTypeId in :a")
    @NamedQuery(name = "PlannedDuty.findByDayTypes", query = "SELECT p FROM PlannedDuty p WHERE p.dayTypeList = :a"),
    @NamedQuery(name = "PlannedDuty.findByDayTypesList", query = "SELECT distinct p FROM PlannedDuty p WHERE p.dayTypeList in :a")
})
@DiscriminatorValue(Trip.PLANNED_DUTY)
public class PlannedDuty extends Duty {

    void cloneDataToScheduledDuty(ScheduledDuty newScheduledDuty) {

        newScheduledDuty.setDescription(this.getDescription());
        //newTrip.consumed = this.consumed;
        newScheduledDuty.setUtcTimes(true);
        newScheduledDuty.setTimesAreValid(this.getTimesAreValid());

        newScheduledDuty.connectToCloned(this.getTripType(), this.getTrainType(), this.getDayTypeList());
        CopyObjects(newScheduledDuty);
        CopyActionList(newScheduledDuty);

        //newScheduledDuty.setPlannedStartSecs(this.getPlannedStartSecs() - this.getPlannedStartObj().getUtcDiff());
        //newScheduledDuty.setPlannedStopSecs(this.getPlannedStopSecs() - this.getPlannedStopObj().getUtcDiff());
        newScheduledDuty.setPlannedStartSecs(TimeHelper.getUtcSecsFromLocalSecs(newScheduledDuty.getDay().getDateOfDay(), this.getPlannedStartSecs(), getPlannedStartObj().getTimeZone()));
        newScheduledDuty.setPlannedStopSecs(TimeHelper.getUtcSecsFromLocalSecs(newScheduledDuty.getDay().getDateOfDay(), this.getPlannedStopSecs(), getPlannedStopObj().getTimeZone()));
    }

    public PlannedDuty() {

    }

    @Override
    public String toString() {
        return "schedule.entities.PlannedDuty[tripid=" + super.getTripId() + "]";
    }

    @Override
    public String getTripClassType() {
        return "PlannedDuty Entity";
    }
    
    public Integer getDayForPlannedStopTime() {
        if (this.getPlannedStopObj() != null) {
            return TimeHelper.getDayFor(this.getUtcTimes(), null, this.getPlannedStopSecs(), this.getPlannedStopObj().getTimeZone());
        } else {
            return 0;
        }
    }

    public Integer getDayForPlannedStartTime() {
        if (this.getPlannedStartObj() != null) {
            return TimeHelper.getDayFor(this.getUtcTimes(), null, this.getPlannedStartSecs(), this.getPlannedStartObj().getTimeZone());
        } else {
            return 0;
        }

    }

    public Date getPlannedStartTime() {
        if (getPlannedStartObj() != null) {
            return TimeHelper.getUtcTimeFrom(this.getUtcTimes(), null, this.getPlannedStartSecs(), getPlannedStartObj().getTimeZone());
        } else {
            return new Date();
        }
    }

    public Date getPlannedStopTime() {
        if (getPlannedStopObj() != null) {
            return TimeHelper.getUtcTimeFrom(this.getUtcTimes(), null, this.getPlannedStopSecs(), getPlannedStopObj().getTimeZone());
        } else {
            return new Date();
        }
    }
}
