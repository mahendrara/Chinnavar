/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
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
@DiscriminatorValue(Trip.PLANNED_SERVICE)
@NamedQueries(
{
    //@NamedQuery(name = "PlannedService.findByDayType", query = "SELECT p FROM PlannedService p WHERE p.dayType.dayTypeId in :a"),
        @NamedQuery(name = "PlannedService.findByDayTypeList", query = "SELECT distinct p FROM PlannedService p WHERE p.dayTypeList in :a")
})
public class PlannedService extends ServiceTrip
{

    void cloneDataToScheduledService( ScheduledService newTrip )
    {

        newTrip.setDescription( this.getDescription() );
        newTrip.setUtcTimes( true ); // use utc times
        newTrip.setTimesAreValid( this.getTimesAreValid() );

        newTrip.connectToCloned( this.getTripType(), this.getTrainType(), this.getDayTypeList() );
        CopyObjects( newTrip );
        CopyActionList( newTrip );

        newTrip.setPlannedStartSecs( TimeHelper.getUtcSecsFromLocalSecs( newTrip.getDay().getDateOfDay(), this.getPlannedStartSecs(), getPlannedStartObj().getTimeZone() ) );
        newTrip.setPlannedStopSecs( TimeHelper.getUtcSecsFromLocalSecs( newTrip.getDay().getDateOfDay(), this.getPlannedStopSecs(), getPlannedStopObj().getTimeZone() ) );
    }

    public PlannedService()
    {

    }

    public List<PlannedTrip> getPlannedTrips()
    {
        List<PlannedTrip> plannedTrips = new ArrayList<>();
        Iterator<TripAction> ite = this.getActionsIterator();

        while( ite.hasNext() )
        {
            TripAction tripAction = ite.next();
            if( tripAction.getClass() == ActionRunTrip.class )
            {
                ActionRunTrip runTrip = (ActionRunTrip) tripAction;
                plannedTrips.add( (PlannedTrip) runTrip.getRefTrip() );
            }
        }
        
        return plannedTrips;
    }
    
    public void FormFromPlanned( PlannedService source, Integer startSecs )
    {
        if( this.getDescription() == null )
        {
            this.setDescription( "Clone of " + source.getDescription() ); // todo ?? set day code also to name
        }
        this.setConsumed( source.getConsumed() );
        this.setUtcTimes( source.getUtcTimes() );
        this.setTimesAreValid( source.getTimesAreValid() );

        this.setOrigoSecs( source.getOrigoSecs() );
        this.setDurationSecs( source.getDurationSecs() );

        this.setTripType( source.getTripType() );

        this.setValid( source.isValid() );
        source.CopyObjects( this );
        source.CopyActionList( this );

        this.setPlannedStartSecs( startSecs );
        this.setPlannedStopSecs( startSecs + source.getDurationSecs() );
        this.setActionsFromTemplate( source.getActionsFromTemplate() );
    }

    @Override
    public Integer getDayForPlannedStopTime()
    {
        //needed for add new service
        if( this.getPlannedStopObj() != null )
        {
            return TimeHelper.getDayFor( this.getUtcTimes(), null, this.getPlannedStopSecs(), this.getPlannedStopObj().getTimeZone() );
        } else
        {
            return 0;
        }
    }

    @Override
    public Integer getDayForPlannedStartTime()
    {
        if( this.getPlannedStartObj() != null )
        {
            return TimeHelper.getDayFor( this.getUtcTimes(), null, this.getPlannedStartSecs(), this.getPlannedStartObj() != null ? getPlannedStartObj().getTimeZone() : this.getAreaObj().getTimeZone() );
        } else
        {
            return 0;
        }
    }

    @Override
    public void setPlannedStartTime( Date time )
    {
        this.setPlannedStartSecs( this.safeLongtoInteger( TimeHelper.getLocalSecsFrom( this.getUtcTimes(), time, this.getPlannedStartObj() != null ? getPlannedStartObj().getTimeZone() : this.getAreaObj().getTimeZone() ) ) );
    }

    @Override
    public void setPlannedStopTime( Date time )
    {
        this.setPlannedStopSecs( this.safeLongtoInteger( TimeHelper.getLocalSecsFrom( this.getUtcTimes(), time, this.getPlannedStopObj() != null ? getPlannedStopObj().getTimeZone() : this.getAreaObj().getTimeZone() ) ) );
    }

    @Override
    public Date getPlannedStartTime()
    {
        if( getPlannedStartObj() != null )
        {
            return TimeHelper.getUtcTimeFrom( this.getUtcTimes(), null, this.getPlannedStartSecs(), this.getPlannedStartObj() != null ? getPlannedStartObj().getTimeZone() : this.getAreaObj().getTimeZone() );
        } else
        {
            return new Date();
        }
    }

    @Override
    public Date getPlannedStopTime()
    {
        if( getPlannedStopObj() != null )
        {
            return TimeHelper.getUtcTimeFrom( this.getUtcTimes(), null, this.getPlannedStopSecs(), this.getPlannedStopObj() != null ? getPlannedStopObj().getTimeZone() : this.getAreaObj().getTimeZone() );
        } else
        {
            return new Date();
        }
    }
}
