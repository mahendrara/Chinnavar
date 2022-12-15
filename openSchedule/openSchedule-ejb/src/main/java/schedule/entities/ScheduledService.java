/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.entities;

//import SessionClasses.ScheduledTripFacade;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
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
@NamedQueries({
    //@NamedQuery(name = "ScheduledService.findInvalidByDayType", query = "SELECT s FROM ScheduledService s  WHERE s.valid = false and s.dayTypeList.dayTypeId in :a"),
    @NamedQuery(name = "ScheduledService.findInvalidByDayType", query = "SELECT s FROM ScheduledService s  join s.dayTypeList d WHERE s.valid = false and d.dayTypeId in :a"),
    //@NamedQuery(name = "ScheduledService.countValidByDayType", query = "SELECT COUNT(s) FROM ScheduledService s  WHERE s.valid = true and s.dayType.dayTypeId in :a"),
    @NamedQuery(name = "ScheduledService.countValidByDayType", query = "SELECT COUNT(s) FROM ScheduledService s join s.dayTypeList d WHERE s.valid = true and d.dayTypeId in :a"),
    @NamedQuery(name = "ScheduledService.findByScheduledDay", query = "SELECT s FROM ScheduledService s WHERE s.day.scheduledDayCode <= :dayCode1 and s.day.scheduledDayCode >= :dayCode2"),
    @NamedQuery(name = "ScheduledService.deleteNonArchiveNonActive", query = "DELETE FROM ScheduledService s WHERE s.day.scheduledDayCode <= :dayCode1 and s.day.scheduledDayCode >= :dayCode2 and s.day.active = false and s.day.archived = false"),
    @NamedQuery(name = "ScheduledService.deleteBySetInvalid", query = "UPDATE ScheduledService s set s.valid = false WHERE s.day.scheduledDayCode <= :dayCode1 and s.day.scheduledDayCode >= :dayCode2 and s.day.active = true"),
    //@NamedQuery(name = "ScheduledService.deleteByDayType", query = "DELETE FROM ScheduledService p WHERE p.dayType.dayTypeId in :a")
})
@DiscriminatorValue(Trip.SCHEDULED_SERVICE)
public class ScheduledService extends ServiceTrip {

    @ManyToOne(optional = false)
    @JoinColumn(name = "scheduleddaycode", referencedColumnName = "scheduleddaycode")
    private ScheduledDay day;

    @JoinColumn(name = "sourceTrip")
    @ManyToOne
    private PlannedService sourceTrip;

    public PlannedService getSourceTrip() {
        return sourceTrip;
    }

    public void setSourceTrip(PlannedService sourceTrip) {
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
    public ScheduledService() {

    }

    /*@Override
    public String toString() {
        return "schedule.entities.ScheduledService[tripid=" + super.getTripId() + "]";
    }

    @Override
    public String getTripClassType() {
        return "ScheduledService Entity";
    }*/
    public void FormFromPlanned(PlannedService source, ScheduledDay day) {

        this.setSourceTrip(source);

        this.setDescription(source.getDescription()); // todo ?? set day code also to name
        this.setConsumed(false);
        this.setUtcTimes(true);
        this.setTimesAreValid(source.getTimesAreValid());

        this.setOrigoSecs(source.getOrigoSecs());
        this.setDurationSecs(source.getDurationSecs());

        this.setDay(day);
        this.setTripType(source.getTripType());

        this.setValid(source.isValid());
        this.setVersion(1);
        this.setActionsFromTemplate(source.getActionsFromTemplate());
        //newTrip.connectToCloned(this.triptype);
        source.CopyObjects(this);
        source.CopyActionList(this);

        //this.setPlannedStartSecs(source.getPlannedStartSecs() - source.getPlannedStartObj().getUtcDiff());
        //this.setPlannedStopSecs(source.getPlannedStopSecs() - source.getPlannedStopObj().getUtcDiff());
        this.setPlannedStartSecs(TimeHelper.getUtcSecsFromLocalSecs(day.getDateOfDay(), source.getPlannedStartSecs(), getPlannedStartObj().getTimeZone()));
        this.setPlannedStopSecs(TimeHelper.getUtcSecsFromLocalSecs(day.getDateOfDay(), source.getPlannedStopSecs(), getPlannedStopObj().getTimeZone()));
    }

    public List<ScheduledTrip> getScheduledTrips() {
        List<ScheduledTrip> scheduledTrips = new ArrayList<>();
        Iterator<TripAction> ite = this.getActionsIterator();

        while (ite.hasNext()) {
            TripAction tripAction = ite.next();
            if (tripAction.getClass() == ActionRunTrip.class) {
                ActionRunTrip runTrip = (ActionRunTrip) tripAction;
                scheduledTrips.add((ScheduledTrip) runTrip.getRefTrip());
            }
        }
        return scheduledTrips;
    }

    @Override
    public Integer getDayForPlannedStopTime() {
        if (this.getPlannedStopObj() != null) {
            return TimeHelper.getDayFor(this.getUtcTimes(), day.getDateOfDay(), this.getPlannedStopSecs(), this.getPlannedStopObj() != null ? getPlannedStopObj().getTimeZone() : this.getAreaObj().getTimeZone());
        } else {
            return 0;
        }
    }

    @Override
    public Integer getDayForPlannedStartTime() {
        if (this.getPlannedStartObj() != null) {
            return TimeHelper.getDayFor(this.getUtcTimes(), day.getDateOfDay(), this.getPlannedStartSecs(), this.getPlannedStartObj() != null ? getPlannedStartObj().getTimeZone() : this.getAreaObj().getTimeZone());
        } else {
            return 0;
        }

    }

    @Override
    public void setPlannedStartTime(Date time) {
        this.setPlannedStartSecs(this.safeLongtoInteger(TimeHelper.getUTCSecsFrom(this.getUtcTimes(), day.getDateOfDay(), time, this.getPlannedStartObj() != null ? getPlannedStartObj().getTimeZone() : this.getAreaObj().getTimeZone())));
    }

    @Override
    public void setPlannedStopTime(Date time) {
        this.setPlannedStopSecs(this.safeLongtoInteger(TimeHelper.getUTCSecsFrom(this.getUtcTimes(), day.getDateOfDay(), time, this.getPlannedStopObj() != null ? getPlannedStopObj().getTimeZone() : this.getAreaObj().getTimeZone())));
    }

    @Override
    public Date getPlannedStartTime() {
        if (getPlannedStartObj() != null) {
            return TimeHelper.getUtcTimeFrom(this.getUtcTimes(), day.getDateOfDay(), this.getPlannedStartSecs(), this.getPlannedStartObj() != null ? getPlannedStartObj().getTimeZone() : this.getAreaObj().getTimeZone());
        } else {
            return new Date();
        }
    }

    @Override
    public Date getPlannedStopTime() {
        if (getPlannedStopObj() != null) {
            return TimeHelper.getUtcTimeFrom(this.getUtcTimes(), day.getDateOfDay(), this.getPlannedStopSecs(), this.getPlannedStopObj() != null ? getPlannedStopObj().getTimeZone() : this.getAreaObj().getTimeZone());
        } else {
            return new Date();
        }
    }
    /*@Override
    public void removeTripAction(TripAction tripAction) {
        //this.getTripActions().remove(tripAction);
        
        if (tripAction.isCreating() == false) {
            Iterator<TripAction> iterator = this.getTripActions().iterator();//sorted list
            while (iterator.hasNext()) {
                TripAction temp = iterator.next();
                if (tripAction.getActionId() == temp.getActionId()) {
                    iterator.remove();
                } else if (temp.getSeqNo()!= null && temp.getSeqNo()>tripAction.getSeqNo()) {
                    temp.setSeqNo(temp.getSeqNo() - 1);
                    temp.setTimeFromTripStart(temp.getTimeFromTripStart() - tripAction.getPlannedSecs());
                    //temp.setMinTimeFromTripStart(temp.getMinTimeFromTripStart() - tripAction.getPlannedSecs());
                }
            }
        }
        tripAction.setTrip(null);
        if (tripAction.getRefTrip() != null) {
            ((PlannedTrip) (tripAction.getRefTrip())).setServiceAction(null);
            tripAction.setRefTrip(null);
        }
    }*/
 /*public void createScheduledTripsFromPlannedTrips(ScheduledTripFacade facade,ScheduledDay day,boolean persist) {
     // Maybe add later after fist pesist ???
     Iterator<Tripaction> ite = getActionsIterator();
       
     while(ite.hasNext())
     {
     Tripaction newlycreatedAction = ite.next();
     if(newlycreatedAction.getClass()==ActionRunTrip.class)
     {
     ActionRunTrip runTrip = (ActionRunTrip) newlycreatedAction;
     PlannedTrip planTrip = (PlannedTrip) runTrip.getRefTrip();
                
     if(planTrip!=null)
     {
     ScheduledTrip schedTrip = new ScheduledTrip(); 
     facade.cloneToScheduled(schedTrip,planTrip,day);
                    
     ((ActionRunTrip)newlycreatedAction).setRefTrip(schedTrip);
     //facade.create(schedTrip);
     if(persist)
     {
     facade.persistTrip(schedTrip);
     //schedTrip 
     }
                    
     }
                
     }
     }
     }*/
}
