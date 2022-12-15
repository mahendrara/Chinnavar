package railml;

import eventlog.EventLogBean;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import schedule.entities.ActionType;
import schedule.entities.BasicTrip;
import schedule.entities.DayType;
import schedule.entities.PlannedService;
import schedule.entities.PlannedTrip;
import schedule.entities.Schedule;
import schedule.entities.ScheduleTemplate;
import schedule.entities.SchedulingState;
import schedule.entities.TTObject;
import schedule.entities.TrainType;
import schedule.entities.TripType;
import schedule.entities.TripUserType;
import schedule.sessions.ActionTypeFacade;
import schedule.sessions.DayTypeFacade;
import schedule.sessions.PlannedServiceFacade;
import schedule.sessions.ScheduleFacade;
import schedule.sessions.ScheduleTemplateFacade;
import schedule.sessions.SchedulingStateFacade;
import schedule.sessions.TTObjectFacade;
import schedule.sessions.TrainTypeFacade;
import schedule.sessions.TripFacade;
import schedule.sessions.TripTypeFacade;
import schedule.sessions.TripUserTypeFacade;
import schedule.uiclasses.BasicTripFilter;
import schedule.uiclasses.DayTypeFilter;
import schedule.uiclasses.PlannedServiceFilter;
import schedule.uiclasses.TripTypeFilter;
import schedule.uiclasses.TripUserTypeFilter;

/**
 * Gets/sets specified data from/to database.
 *
 * @author Pavel
 */
@Named("RailMlDataController")
@SessionScoped
@TransactionManagement(TransactionManagementType.BEAN)
public class RailMlDataController implements Serializable
{

    @Inject
    private ActionTypeFacade actionTypeFacade;

    @Inject
    private ScheduleFacade scheduleFacade;

    @Inject
    private SchedulingStateFacade schedulingStateFacade;

    @Inject
    private ScheduleTemplateFacade scheduleTemplateFacade;

    @Inject
    private DayTypeFacade dayTypeFacade;

    @Inject
    private PlannedServiceFacade plannedServiceFacade;

    @Inject
    private TTObjectFacade tTObjectFacade;

    @Inject
    private TrainTypeFacade trainTypeFacade;

    @Inject
    private TripFacade tripFacade;

    @Inject
    private TripTypeFacade tripTypeFacade;

    @Inject
    private TripUserTypeFacade tripUserTypeFacade;

    @Resource
    private UserTransaction userTransaction;

    /**
     * Gathers data from database and wraps into structure.
     *
     * @param ScheduleId
     *
     * @return ScheduleData if succeeded, otherwise null.
     */
    public ScheduleData getScheduleData( Integer ScheduleId )
    {
        ScheduleData railMlSchedule = new ScheduleData();

        Schedule schedule = this.getSchedule( ScheduleId );
        if( schedule == null )
        {
            return null;
        }
        railMlSchedule.setSchedule( schedule );

        List<DayType> dayTypes = this.getDayTypes( ScheduleId );
        if( dayTypes == null || dayTypes.isEmpty() )
        {
            return null;
        }
        railMlSchedule.setDayTypes( dayTypes );

        List<PlannedService> plannedServices = new ArrayList<>();
        for( DayType dayType : dayTypes )
        {
            List<PlannedService> plannedServicesForDayType = this.getPlannedServices( dayType );
            plannedServices.addAll( plannedServicesForDayType );
        }

        if( plannedServices.isEmpty() )
        {
            return null;
        }
        railMlSchedule.setPlannedServices( plannedServices );

        return railMlSchedule;
    }

    /**
     * Gets schedule by specified ID.
     *
     * @param ScheduleId
     *
     * @return Schedule if succeeded, otherwise null.
     */
    private Schedule getSchedule( Integer ScheduleId )
    {
        return this.scheduleFacade.find( ScheduleId );
    }

    /**
     * Imports provided schedule into database.
     *
     * @param schedule
     */
    private void setSchedule( Schedule schedule )
    {
        this.scheduleFacade.create( schedule );
    }

    /**
     * Gets list of DayType objects, related to schedule specified by ID.
     *
     * @param ScheduleId
     *
     * @return list of DayType of succeeded, otherwise null.
     */
    private List<DayType> getDayTypes( Integer ScheduleId )
    {
        DayTypeFilter filter = new DayTypeFilter();
        filter.setScheduleId( ScheduleId );

        return this.dayTypeFacade.findAll( filter );
    }

    /**
     * Imports provided list of DayType objects to database.
     *
     * @param dayTypes
     */
    private void setDayTypes( List<DayType> dayTypes )
    {
        for( DayType dayType : dayTypes )
        {
            this.dayTypeFacade.create( dayType );
        }
    }

    /**
     * Gets list of PlannedService objects, related to specific DayType.
     *
     * @param dayType
     *
     * @return list of PlannedService of succeeded, otherwise null.
     */
    private List<PlannedService> getPlannedServices( DayType dayType )
    {
        PlannedServiceFilter filter = new PlannedServiceFilter();
        filter.setDayTypeFilter( dayType );
        filter.setValidFilter( true );

        return this.plannedServiceFacade.findAll( filter );
    }

    /**
     * Imports provided list of PlannedService objects to database.
     *
     * @param plannedServices
     */
    private void setPlannedServices( List<PlannedService> plannedServices )
    {
        for( PlannedService plannedService : plannedServices )
        {
            this.plannedServiceFacade.create( plannedService );
        }
    }

    /**
     * Imports provided list of BasicTrip objects to database.
     *
     * @param templates
     */
    private void setTemplates( List<BasicTrip> templates )
    {
        for( BasicTrip template : templates )
        {
            this.tripFacade.create( template );
        }
    }

    /**
     * Un-wraps the data structure and imports obtained data into database.
     *
     * @param data
     *
     * @return true if succeeded, otherwise false.
     */
    public boolean setScheduleData(EventLogBean eventLog, ScheduleData data )
    {
        System.out.println( "setScheduleData(): " );

        try
        {
            userTransaction.begin();

            Schedule schedule = data.getSchedule();
            this.setSchedule( schedule );

            List<DayType> dayTypes = data.getDayTypes();
            this.setDayTypes( dayTypes );

            List<PlannedService> plannedServices = data.getPlannedServices();
            this.setPlannedServices( plannedServices );

            List<BasicTrip> templates = data.getTemplates();
            this.setTemplates( templates );
            //userTransaction.rollback(); // ATI Commnet : Development Tip : You can change below to this for safer testing
            userTransaction.commit();

            this.clearCacheAndUpdateAllReferences();

            return true;

        } catch( NotSupportedException | SystemException | RollbackException |
                 HeuristicMixedException | HeuristicRollbackException |
                 SecurityException | IllegalStateException ex )
        {
            eventLog.addEvent(RailMlDataController.class.getName(), ex.toString());
            Logger.getLogger( RailMlDataController.class.getName() ).log( Level.SEVERE, null, ex );
            return false;
        }
    }

    private void clearCacheAndUpdateAllReferences()
    {
        this.actionTypeFacade.evictAll();
        this.dayTypeFacade.evictAll();
        this.plannedServiceFacade.evictAll();
        this.scheduleFacade.evictAll();
        this.scheduleTemplateFacade.evictAll();
        this.schedulingStateFacade.evictAll();
        this.tTObjectFacade.evictAll();
        this.trainTypeFacade.evictAll();
        this.tripFacade.evictAll();
        this.tripTypeFacade.evictAll();
        this.tripUserTypeFacade.evictAll();
    }

    /**
     * Gets list of all ScheduleTemplate objects.
     *
     * @return list of ScheduleTemplate if succeeded, otherwise null.
     */
    public List<ScheduleTemplate> getScheduleTemplates()
    {
        return this.scheduleTemplateFacade.findAll();
    }

    /**
     * Gets ActionType object by specified ID.
     *
     * @return ActionType if succeeded, otherwise null.
     */
    public ActionType getActionType( Integer id )
    {
        return this.actionTypeFacade.find( id );
    }

    /**
     * Gets TTObject object by specified RailML ID. Removes all characters from
     * ID before search.
     *
     * @param objectId
     *
     * @return TTObject if succeeded, otherwise null.
     */
    public TTObject getTtObject( String objectId )
    {
        Integer id = Integer.valueOf( objectId.replaceAll( "[^\\d.]", "" ) );
        return this.tTObjectFacade.find( id );
    }

    /**
     * Gets TTArea/TTObject object by specified ID.
     *
     * @param objectId
     *
     * @return TTObject if succeeded, otherwise null.
     */
    public TTObject getArea( String objectId )
    {
        return this.tTObjectFacade.find( objectId );
    }

    /**
     * Gets TrainType object by specified RailML ID. Removes all characters from
     * ID before search.
     *
     * @param trainTypeId
     *
     * @return TrainType if succeeded, otherwise null.
     */
    public TrainType getTrainType( String trainTypeId )
    {
        Integer id = Integer.valueOf( trainTypeId.replaceAll( "[^\\d.]", "" ) );
        return this.trainTypeFacade.find( id );
    }

    /**
     * Gets SchedulingState object, related to "Planned to run" state.
     *
     * @return SchedulingState if succeeded, otherwise null.
     */
    public SchedulingState getSchedulingState()
    {
        int stateValue = SchedulingState.State.PLANNED_TO_RUN.getStateValue();
        return this.schedulingStateFacade.find( stateValue );
    }

    /**
     * Gets TripType object by specified RailML ID. Removes all characters from
     * ID before search.
     *
     * @param tripTypeId
     *
     * @return TripType if succeeded, otherwise null.
     */
    public TripType getTripType( String tripTypeId )
    {
        Integer id = Integer.valueOf( tripTypeId.replaceAll( "[^\\d.]", "" ) );
        return this.tripTypeFacade.find( id );
    }

    /**
     * Gets TripType object, related to attributes of specified PlannedTrip
     * object.
     *
     * @param plannedTrip
     *
     * @return TripType if succeeded, otherwise null.
     */
    public TripType getTripType( PlannedTrip plannedTrip )
    {
        TripTypeFilter tripTypeFilter = new TripTypeFilter();

        tripTypeFilter.setValid( true );
        tripTypeFilter.setToTripType( plannedTrip.getTripType().getTripType().getValue() );
        tripTypeFilter.setTripSubType( plannedTrip.getTripType().getTripSubType() );

        TripType tripType = this.tripTypeFacade.findFirst( tripTypeFilter );

        return tripType;
    }

    /**
     * Gets list BasicTrip objects, related to the specific parameters of
     * provided filter.
     *
     * @param filter
     *
     * @return list of BasicTrip if succeeded, otherwise null.
     */
    public List<BasicTrip> findAllRelatedTemplates( BasicTripFilter filter )
    {
        return this.tripFacade.findAll( filter );
    }

    /**
     * Gets TripUserType object by specified ID.
     *
     * @param id
     *
     * @return TripUserType if succeeded, otherwise null.
     */
    public TripUserType getTripUserType( int id )
    {
        TripUserTypeFilter filter = new TripUserTypeFilter();

        filter.setTripUserTypeId( id );

        return this.tripUserTypeFacade.findFirst( filter );
    }

}
