package railml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.json.Json;
import javax.json.JsonObject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;
import org.railml.schemas._2011.EArrivalDepartureTimes;
import org.railml.schemas._2011.EBlock;
import org.railml.schemas._2011.EBlockPartSequence;
import org.railml.schemas._2011.EFormation;
import org.railml.schemas._2011.EFormationTT;
import org.railml.schemas._2011.EOcp;
import org.railml.schemas._2011.EOcpTT;
import org.railml.schemas._2011.EOcpsTT;
import org.railml.schemas._2011.EOperatingPeriod;
import org.railml.schemas._2011.EOperatingPeriodRef;
import org.railml.schemas._2011.ETimetablePeriod;
import org.railml.schemas._2011.ETrainPart;
import org.railml.schemas._2011.Railml;
import org.railml.schemas._2011.TBlockPart;
import org.railml.schemas._2011.TBlockPartRef;
import org.railml.schemas._2011.TCirculation;
import org.railml.schemas._2011.TFormation;
import railml.exceptions.RailMlBufferException;
import railml.exceptions.RailMlConversionException;
import railml.exceptions.RailMlDatabaseLinkingException;
import railml.exceptions.RailMlFormatCastingException;
import railml.exceptions.RailMlNullAttributeException;
import railml.exceptions.RailMlNullElementException;
import railml.exceptions.RailMlServiceExportException;
import railml.exceptions.RailMlServiceImportException;
import schedule.entities.ActionCargoStop;
import schedule.entities.ActionGlue;
import schedule.entities.ActionPassObject;
import schedule.entities.ActionPassengerStop;
import schedule.entities.ActionRunTrip;
import schedule.entities.ActionSchedulingStop;
import schedule.entities.ActionTrainFormation;
import schedule.entities.ActionTrainMoving;
import schedule.entities.ActionType;
import schedule.entities.BasicTrip;
import schedule.entities.DayInSchedule;
import schedule.entities.DayType;
import schedule.entities.PlannedService;
import schedule.entities.PlannedTrip;
import schedule.entities.Schedule;
import schedule.entities.ScheduleTemplate;
import schedule.entities.SchedulingState;
import schedule.entities.TTArea;
import schedule.entities.TTObject;
import schedule.entities.TrainType;
import schedule.entities.TripAction;
import schedule.entities.TripType;
import schedule.entities.TripUserType;
import schedule.uiclasses.BasicTripFilter;

/**
 *
 * @author Pavel
 */
@Named("RailMlConverter")
@SessionScoped
@TransactionManagement(TransactionManagementType.BEAN)
public class RailMlConverter implements Serializable
{

    private Map<String, EFormation> bufferedFormations;
    private Map<String, TrainType> bufferedTrainTypes;
    private Map<String, EOcp> bufferedOcps;
    private Map<String, TTObject> bufferedTtObjects;
    private Map<String, DayType> bufferedDayTypes;
    private List<BasicTrip> bufferedTemplates;

    @Inject
    private RailMlDataController dataController;

    public RailMlConverter()
    {
        this.clearBuffers();
    }

    /**
     * Casts provided InputStream to Railml structure object.
     *
     * @param inputStream
     *
     * @throws RailMlFormatCastingException if casting to object failed.
     *
     * @return Railml
     */
    public static Railml cast( InputStream inputStream ) throws RailMlFormatCastingException
    {
        try
        {
            JAXBContext jaxbContext = JAXBContext.newInstance( Railml.class );
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            return (Railml) jaxbUnmarshaller.unmarshal( inputStream );
        } catch( JAXBException exception )
        {
            throw new RailMlFormatCastingException( exception );
        }
    }

    /**
     * Casts provided Railml structure to InputStream.
     *
     * @param railml
     *
     * @throws RailMlFormatCastingException if casting to InputStream failed.
     *
     * @return InputStream
     */
    public static InputStream cast( Railml railml ) throws RailMlFormatCastingException
    {
        try
        {
            File file = File.createTempFile( "temporary", ".xml" );
            file.deleteOnExit();

            JAXBContext jaxbContext = JAXBContext.newInstance( Railml.class );
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, true );
            jaxbMarshaller.marshal( railml, file );

            return new FileInputStream( file );
        } catch( JAXBException | IOException exception )
        {
            throw new RailMlFormatCastingException( exception );
        }
    }

    /**
     * Converts provided ScheduleData structure to RailMl structure.
     *
     * @param railmlData
     * @param schedule
     *
     * @throws RailMlConversionException if any conversion step fails.
     */
    public void scheduledata_to_railml( Railml railmlData, ScheduleData schedule ) throws RailMlConversionException
    {
        this.clearBuffers();

        try
        {
            RailMlFacade railmlWrapper = new RailMlFacade( railmlData );

            ETimetablePeriod eTimetablePeriod = schedule_to_timetableperiod( schedule );
            railmlWrapper.GetTimetablePeriods().add( eTimetablePeriod );

            List<EOperatingPeriod> eOperatingPeriods = daytypes_to_operatingperiods( schedule );
            railmlWrapper.GetOperatingPeriods().addAll( eOperatingPeriods );

            plannedservices_to_railml( railmlWrapper, schedule );

            buffered_ocps_add_to_railml( railmlWrapper );
            buffered_formations_add_to_railml( railmlWrapper );

        } catch( NullPointerException | RailMlBufferException |
                 RailMlServiceExportException | DatatypeConfigurationException exception )
        {
            throw new RailMlConversionException( exception );
        }
    }

    /**
     * Converts provided RailMl structure to ScheduleData structure.
     *
     * @param importedFile
     *
     * @throws RailMlServiceImportException if no planned services were
     *                                      converted.
     * @throws RailMlConversionException    if any conversion step fails.
     *
     * @return ScheduleData
     */
    public ScheduleData scheduledata_from_railml( Railml importedFile ) throws RailMlServiceImportException, RailMlConversionException
    {
        this.clearBuffers();

        try
        {

            RailMlFacade railmlWrapper = new RailMlFacade( importedFile );
            ScheduleData scheduleData = new ScheduleData();

            ETimetablePeriod eTimetablePeriod = railmlWrapper.GetTimetablePeriod();
            Schedule schedule = schedule_from_timetableperiod( eTimetablePeriod );
            scheduleData.setSchedule( schedule );

            List<EOperatingPeriod> eOperatingPeriods = railmlWrapper.GetOperatingPeriods();
            for( EOperatingPeriod eOperatingPeriod : eOperatingPeriods )
            {
                DayType dayType = daytype_from_operatingperiod( eOperatingPeriod );

                daytype_with_schedule_linking( schedule, dayType );
                daytype_with_daysinschedule_linking( schedule, dayType, eOperatingPeriod.getBitMask() );

                scheduleData.addDayType( dayType );
            }

            List<EOcp> eOcps = railmlWrapper.GetOperatingControlPoints();
            ttobject_with_ocp_dblinking( eOcps );

            List<EFormation> eFormations = railmlWrapper.GetFormations();
            traintype_with_formation_dblinking( eFormations );

            List<EBlock> eBlocks = railmlWrapper.GetBlocks();
            List<PlannedService> plannedServices = plannedservices_from_blocks( eBlocks );
            scheduleData.setPlannedServices( plannedServices );

            scheduleData.setTemplates( this.bufferedTemplates );

            return scheduleData;
        } catch( NullPointerException exception )
        {
            throw new RailMlConversionException( exception );
        }
    }

    /**
     * Clears all buffered objects.
     */
    private void clearBuffers()
    {
        this.bufferedFormations = new HashMap<>();
        this.bufferedTrainTypes = new HashMap<>();
        this.bufferedOcps = new HashMap<>();
        this.bufferedTtObjects = new HashMap<>();
        this.bufferedDayTypes = new HashMap<>();
        this.bufferedTemplates = new ArrayList<>();
    }

    /**
     * Converts Schedule object to TimetablePeriod element.
     *
     * @param data
     *
     * @throws DatatypeConfigurationException if creation of XML calendar fails.
     *
     * @return ETimetablePeriod, empty if failed.
     */
    private ETimetablePeriod schedule_to_timetableperiod( ScheduleData data ) throws DatatypeConfigurationException
    {
        ETimetablePeriod eTimetablePeriod = new ETimetablePeriod();
        Schedule schedule = data.getSchedule();

        eTimetablePeriod.setId( "ttp" + schedule.getScheduleId() );
        eTimetablePeriod.setName( schedule.getDescription() );

        Date startTime = schedule.getStartTime();
        XMLGregorianCalendar xmlStartDate = DateTimeHelper.createXml( startTime );
        eTimetablePeriod.setStartDate( xmlStartDate );
        eTimetablePeriod.setStartTime( xmlStartDate );

        Date endTime = schedule.getEndTime();
        XMLGregorianCalendar xmlEndDate = DateTimeHelper.createXml( endTime );
        eTimetablePeriod.setEndDate( xmlEndDate );
        eTimetablePeriod.setEndTime( xmlEndDate );

        // Set reference to template.
        if( schedule.getScheduleTemplate() != null )
        {
            int templateHash = schedule.getScheduleTemplate().hashCode();
            String templateHashStr = Integer.toString( templateHash );
            eTimetablePeriod.setCode( templateHashStr );
        }

        return eTimetablePeriod;
    }

    /**
     * Converts TimetablePeriod element to Schedule object.
     *
     * @param eTimetablePeriod
     *
     * @return Schedule, empty if failed.
     */
    private Schedule schedule_from_timetableperiod( ETimetablePeriod eTimetablePeriod )
    {
        Schedule schedule = new Schedule();

        Calendar startDate = eTimetablePeriod.getStartDate().toGregorianCalendar();
        Calendar endDate = eTimetablePeriod.getEndDate().toGregorianCalendar();

        Date startTime = DateTimeHelper.toDate( eTimetablePeriod.getStartDate(), eTimetablePeriod.getStartTime() );
        Date endTime = DateTimeHelper.toDate( eTimetablePeriod.getEndDate(), eTimetablePeriod.getEndTime() );

        schedule.setStartTime( startTime );
        schedule.setEndTime( endTime );

        schedule.generateDaysInSchedule( startDate, endDate );
        schedule.setValid( true );
        schedule.setCreating( false );
        schedule.setEditing( false );
        String templateIDString = eTimetablePeriod.getCode();
        if(templateIDString!=null)
        {
            // let's not cacth here anything...            
            int templateHash = Integer.parseInt(templateIDString);
            
            for( ScheduleTemplate template : dataController.getScheduleTemplates() )
            {
                if(template.hashCode()==templateHash )
                {
                    schedule.setScheduleTemplate( template );
                    break;
                }
            }
        }
        DateFormat dateFormat = new SimpleDateFormat( "dd/MM/yy HH:mm" );
        String desc = "RailML: " + eTimetablePeriod.getName()
                      + " (imported at " + dateFormat.format( Calendar.getInstance().getTime() )
                      + ")";
        schedule.setDescription( desc );

        return schedule;
    }

    /**
     * Converts list of DayType objects to list of OperatingPeriod elements.
     *
     * @param data
     *
     * @return list of EOperatingPeriod.
     */
    private List<EOperatingPeriod> daytypes_to_operatingperiods( ScheduleData data )
    {
        List<EOperatingPeriod> eOperatingPeriods = new ArrayList<>();
        Schedule schedule = data.getSchedule();

        for( DayType dayType : schedule.getDayTypes() )
        {
            EOperatingPeriod eOperatingPeriod = new EOperatingPeriod();

            eOperatingPeriod.setId( "op" + dayType.getDayTypeId() );
            eOperatingPeriod.setName( dayType.getDescription() );
            eOperatingPeriod.setCode( dayType.getAbbr() );

            ETimetablePeriod eTimetablePeriod = new ETimetablePeriod();
            eTimetablePeriod.setId( "ttp" + schedule.getScheduleId() );
            eOperatingPeriod.setTimetablePeriodRef( eTimetablePeriod );

            String bitMask = "";
            for( DayInSchedule dayInSchedule : schedule.getDayInSchedules() )
            {
                bitMask += dayInSchedule.getDayType() == dayType ? "1" : "0";
            }
            eOperatingPeriod.setBitMask( bitMask );

            eOperatingPeriods.add( eOperatingPeriod );
        }

        return eOperatingPeriods;
    }

    /**
     * Converts EOperatingPeriod element to DayType object.
     *
     * @param eOperatingPeriod
     *
     * @return DayType object.
     */
    private DayType daytype_from_operatingperiod( EOperatingPeriod eOperatingPeriod )
    {
        DayType dayType = new DayType();

        dayType.setDescription( eOperatingPeriod.getName() );
        if( eOperatingPeriod.getCode() != null )
        {
            dayType.setAbbr( eOperatingPeriod.getCode() );
        } else
        {
            dayType.setAbbr( eOperatingPeriod.getName() );
        }

        this.bufferedDayTypes.put( eOperatingPeriod.getId(), dayType );

        return dayType;
    }

    /**
     * Creates EOperatingPeriodRef element on basis of DayType ID.
     *
     * @param dayType
     *
     * @return EOperatingPeriodRef element.
     */
    private EOperatingPeriodRef daytype_to_operatingperiodref( DayType dayType )
    {
        EOperatingPeriodRef eOperatingPeriodRef = new EOperatingPeriodRef();

        String dayTypeId = dayType.getDayTypeId().toString();

        EOperatingPeriod eOperatingPeriod = new EOperatingPeriod();
        eOperatingPeriod.setId( "op" + dayTypeId );
        eOperatingPeriodRef.setRef( eOperatingPeriod );

        return eOperatingPeriodRef;
    }

    /**
     * Obtains DayType ID from EOperatingPeriodRef element.
     *
     * @param eOperatingPeriodRef
     *
     * @return String
     */
    private String daytypeid_from_operatingperiodref( EOperatingPeriodRef eOperatingPeriodRef )
    {
        EOperatingPeriod eOperatingPeriod = (EOperatingPeriod) eOperatingPeriodRef.getRef();
        return eOperatingPeriod.getId();
    }

    /**
     * Links DayType object with Schedule object.
     *
     * @param schedule
     * @param dayType
     */
    private void daytype_with_schedule_linking( Schedule schedule, DayType dayType )
    {
        dayType.setScheduleParent( schedule );
    }

    /**
     * Links DayType object with certain DayInSchedule objects in Schedule.
     *
     * @param schedule
     * @param dayType
     * @param bitmask
     */
    private void daytype_with_daysinschedule_linking( Schedule schedule, DayType dayType, String bitmask )
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime( schedule.getStartTime() );

        for( char c : bitmask.toCharArray() )
        {
            if( c == '1' )
            {
                for( DayInSchedule dayInSchedule : schedule.getDayInSchedules() )
                {
                    if( dayInSchedule.getDayCodeNo() != DateTimeHelper.GetDayCode( calendar ) )
                    {
                        continue;
                    }

                    dayInSchedule.setDayType( dayType );
                }
            }

            calendar.add( Calendar.DATE, 1 );
        }
    }

    /**
     * Converts PlannedService objects to multiple RailML elements.
     *
     * @param railml
     * @param data
     *
     * @throws RailMlServiceExportException if no needed RailML elements were
     *                                      created.
     */
    private void plannedservices_to_railml( RailMlFacade railml, ScheduleData data ) throws RailMlServiceExportException
    {
        for( PlannedService plannedService : data.getPlannedServices() )
        {
            try
            {
                EBlock eBlock = plannedservice_to_block( plannedService );

                TCirculation tCirculation = block_to_circulation( eBlock );

                EFormation eFormation = traintype_to_formation( plannedService.getTrainType() );
                AddFormationToBufferIfNotExist( eFormation );

                List<TBlockPart> tBlockParts = new ArrayList<>();
                List<ETrainPart> eTrainParts = new ArrayList<>();

                int plannedStartSecs = plannedService.getPlannedStartSecs();
                for( TripAction tripAction : plannedService.getTripActions() )
                {
                    Integer id = tripAction.getActionId();
                    Integer dayTypeId = tripAction.getTrip().getDayTypeList().get(0).getDayTypeId();   // @Todo: check
                    Integer seqNo = tripAction.getSeqNo();

                    TBlockPart tBlockPart = blockpart_create( id, dayTypeId, eFormation );
                    tBlockParts.add( tBlockPart );

                    EBlockPartSequence eBlockPartSequence = blockpartsequence_create( id, seqNo );
                    eBlock.getBlockPartSequence().add( eBlockPartSequence );

                    ETrainPart eTrainPart = tripaction_to_trainpart( tripAction, plannedStartSecs );
                    eTrainParts.add( eTrainPart );
                }

                railml.GetBlockParts().addAll( tBlockParts );
                railml.GetTrainParts().addAll( eTrainParts );
                railml.GetCirculations().add( tCirculation );
                railml.GetBlocks().add( eBlock );

            } catch( NullPointerException | DatatypeConfigurationException exception )
            {
                Logger.getLogger( RailMlConverter.class.getName() ).log( Level.WARNING, null, exception );
            }
        }

        if( railml.GetBlocks().isEmpty()
            || railml.GetBlockParts().isEmpty()
            || railml.GetTrainParts().isEmpty() )
        {
            throw new RailMlServiceExportException( "List of exported planned services is empty." );
        }
    }

    /**
     * Adds buffered formations to RailML structure.
     *
     * @param railmldata
     *
     * @throws RailMlBufferException if buffer is empty.
     */
    private void buffered_formations_add_to_railml( RailMlFacade railmldata ) throws RailMlBufferException
    {
        if( this.bufferedFormations.isEmpty() )
        {
            throw new RailMlBufferException( "Formations buffer is empty." );
        }

        for( Map.Entry<String, EFormation> entry : this.bufferedFormations.entrySet() )
        {
            railmldata.GetFormations().add( entry.getValue() );
        }
    }

    /**
     * Adds buffered OCPs to RailML structure.
     *
     * @param railmldata
     *
     * @throws RailMlBufferException if buffer is empty.
     */
    private void buffered_ocps_add_to_railml( RailMlFacade railmldata ) throws RailMlBufferException
    {
        if( this.bufferedOcps.isEmpty() )
        {
            throw new RailMlBufferException( "Ocps buffer is empty." );
        }

        for( Map.Entry<String, EOcp> entry : this.bufferedOcps.entrySet() )
        {
            railmldata.GetOperatingControlPoints().add( entry.getValue() );
        }
    }

    /**
     * Converts TripAction to ETrainPart element. Supports conversion of Train
     * Formation and Glue Actions with no children actions and TripTemplate
     * actions with children actions.
     *
     * @param tripAction
     * @param plannedStartSecs
     *
     * @throws DatatypeConfigurationException if XML calendar creation fails.
     *
     * @return ETrainPart
     */
    private ETrainPart tripaction_to_trainpart( TripAction tripAction, int plannedStartSecs ) throws DatatypeConfigurationException
    {
        ETrainPart eTrainPart = new ETrainPart();

        eTrainPart.setId( "tp" + tripAction.getActionId() );
        eTrainPart.setLine( tripAction.getTrip().getAreaObj().getScheduleName() );

        String actionTypeId = tripAction.getActionType().getAtypeId().toString();
        eTrainPart.setCode( actionTypeId );

        String actionTypeDescription = tripAction.getActionType().getDescription();
        eTrainPart.setName( actionTypeDescription );

        List<EOcpTT> eOcpTTs = new ArrayList<>();
        if( tripAction instanceof ActionRunTrip )
        {
            PlannedTrip plannedTrip = (PlannedTrip) ((ActionRunTrip) tripAction).getRefTrip();
            eTrainPart.setName( plannedTrip.getDescription() );

            String templateTypeId = plannedTrip.getTripTemplate().getTripType().getTripTypeId().toString();
            eTrainPart.setRemarks( templateTypeId );

            String tripTypeId = plannedTrip.getTripType().getTripTypeId().toString();
            eTrainPart.setDescription( tripTypeId );

            DayType dayType = tripAction.getTrip().getDayTypeList().get(0); // @Todo: check
            EOperatingPeriodRef eOperatingPeriodRef = daytype_to_operatingperiodref( dayType );
            eTrainPart.setOperatingPeriodRef( eOperatingPeriodRef );

            TrainType trainType = tripAction.getTrip().getTrainType();
            EFormationTT eFormationTT = traintype_to_formationtt( trainType );
            eTrainPart.setFormationTT( eFormationTT );

            List<EOcpTT> _eOcpTTs = triptemplate_to_tocptts( plannedTrip );
            eOcpTTs.addAll( _eOcpTTs );
        } else
        {
            EOcpTT eOcpTT = tripaction_to_tocptt( tripAction, plannedStartSecs );
            eOcpTTs.add( eOcpTT );
        }

        EOcpsTT eOcpsTT = new EOcpsTT();
        eOcpsTT.getOcpTT().addAll( eOcpTTs );
        eTrainPart.setOcpsTT( eOcpsTT );

        return eTrainPart;
    }

    /**
     * Converts TripTemplate/PlannedTrip to the list of EOcpTT elements.
     *
     * @param plannedTrip
     *
     * @throws DatatypeConfigurationException if XML calendar creation fails.
     *
     * @return list of EOcpTT.
     */
    private List<EOcpTT> triptemplate_to_tocptts( PlannedTrip plannedTrip ) throws DatatypeConfigurationException
    {
        List<EOcpTT> eOcpTTs = new ArrayList<>();

        int plannedStartSecs = plannedTrip.getPlannedStartSecs();
        for( TripAction tripAction : plannedTrip.getTripActions() )
        {
            EOcpTT eOcpTT = tripaction_to_tocptt( tripAction, plannedStartSecs );
            eOcpTTs.add( eOcpTT );
        }

        return eOcpTTs;
    }

    /**
     * Converts TripAction object to EOcpTT element.
     *
     * @param tripAction
     * @param plannedStartSecs
     *
     * @throws DatatypeConfigurationException if XML calendar creation fails.
     *
     * @return EOcpTT
     */
    private EOcpTT tripaction_to_tocptt( TripAction tripAction, int plannedStartSecs ) throws DatatypeConfigurationException
    {
        EOcpTT eOcpTT = new EOcpTT();

        EOcp eOcp = ttobject_to_ocp( tripAction.getTimetableObject() );
        AddOcpToBufferIfNotExist( eOcp );
        eOcpTT.setOcpRef( eOcp );

        EArrivalDepartureTimes eArrivalDepartureTimes = tripaction_to_times( tripAction, plannedStartSecs );
        eOcpTT.getTimes().add( eArrivalDepartureTimes );

        Integer actionTypeId = tripAction.getActionType().getAtypeId();
        eOcpTT.setRemarks( actionTypeId.toString() );

        return eOcpTT;
    }

    /**
     * Adds formation to a buffer if it was not yet buffered.
     *
     * @param eFormation
     */
    private void AddFormationToBufferIfNotExist( EFormation eFormation )
    {
        String eFormationId = eFormation.getId();
        if( !this.bufferedFormations.containsKey( eFormationId ) )
        {
            this.bufferedFormations.put( eFormationId, eFormation );
        }
    }

    /**
     * Adds OCP to a buffer if it was not yet buffered.
     *
     * @param eOcp
     */
    private void AddOcpToBufferIfNotExist( EOcp eOcp )
    {
        String eOcpId = eOcp.getId();
        if( !this.bufferedOcps.containsKey( eOcpId ) )
        {
            this.bufferedOcps.put( eOcpId, eOcp );
        }
    }

    /**
     * Creates EArrivalDepartureTimes element on the basis of TripAction object
     * information.
     *
     * @param tripAction
     * @param plannedStartSecs
     *
     * @throws DatatypeConfigurationException if XML calendar creation fails.
     *
     * @return EArrivalDepartureTimes element.
     */
    private EArrivalDepartureTimes tripaction_to_times( TripAction tripAction, int plannedStartSecs ) throws DatatypeConfigurationException
    {
        EArrivalDepartureTimes eArrivalDepartureTimes = new EArrivalDepartureTimes();

        eArrivalDepartureTimes.setScope( "scheduled" );

        int seconds = plannedStartSecs + tripAction.getTimeFromTripStart();
        String timeZone = tripAction.getTimetableObject().getTimeZone();

        XMLGregorianCalendar arrival = DateTimeHelper.createXml( seconds, timeZone );
        eArrivalDepartureTimes.setArrival( arrival );
        eArrivalDepartureTimes.setArrivalDay( DateTimeHelper.GetDayFromSeconds( seconds ) );

        seconds = seconds + tripAction.getPlannedSecs();
        timeZone = tripAction.getTimetableObject2() != null ? tripAction.getTimetableObject2().getTimeZone() : tripAction.getTimetableObject().getTimeZone();

        XMLGregorianCalendar departure = DateTimeHelper.createXml( seconds, timeZone );
        eArrivalDepartureTimes.setDeparture( departure );
        eArrivalDepartureTimes.setDepartureDay( DateTimeHelper.GetDayFromSeconds( seconds ) );

        return eArrivalDepartureTimes;
    }

    /**
     * Creates stub TCirculation element with a reference to provided EBlock.
     * TCirculation is a stub object, which is not really used anywhere. It was
     * added to make the RailML file valid using RailVIVID.
     *
     * @param eBlock
     *
     * @return TCirculation element.
     */
    private TCirculation block_to_circulation( EBlock eBlock )
    {
        TCirculation tCirculation = new TCirculation();

        tCirculation.setBlockRef( eBlock );

        return tCirculation;
    }

    /**
     * Converts PlannedService to EBlock element.
     *
     * @param plannedService
     *
     * @return EBlock element.
     */
    private EBlock plannedservice_to_block( PlannedService plannedService )
    {
        EBlock eBlock = new EBlock();

        eBlock.setId( "b" + plannedService.getTripId() );
        eBlock.setName( plannedService.getDescription() );

        eBlock.setCode( plannedService.getTripType().getTripTypeId().toString() );
        eBlock.setDescription( plannedService.getTripType().getDescription() );

        return eBlock;
    }

    /**
     * Creates EFormationTT reference element from TrainType object.
     *
     * @param trainType
     *
     * @return EFormationTT element.
     */
    private EFormationTT traintype_to_formationtt( TrainType trainType )
    {
        EFormationTT eFormationTT = new EFormationTT();

        EFormation eFormation = traintype_to_formation( trainType );
        eFormationTT.setFormationRef( eFormation );

        return eFormationTT;
    }

    /**
     * Creates EFormation element from TrainType object.
     *
     * @param trainType
     *
     * @return EFormation element.
     */
    private EFormation traintype_to_formation( TrainType trainType )
    {
        EFormation eFormation = new EFormation();

        eFormation.setId( "f" + trainType.getTrainTypeId() );
        eFormation.setName( trainType.getDescription() );

        return eFormation;
    }

    /**
     * Converts TTObject object to EOcp element.
     *
     * @param object
     *
     * @return EOcp element.
     */
    private EOcp ttobject_to_ocp( TTObject object )
    {
        EOcp eOcp = new EOcp();

        Integer objectId = object.getTTObjId();
        eOcp.setId( "ocp" + objectId );
        eOcp.setName( object.getDescription() );
        eOcp.setCode( object.getScheduleName() );
        eOcp.setNumber( objectId.toString() );

        return eOcp;
    }

    /**
     * Creates EBlockPartSequence element on the basis of provided information.
     *
     * @param actionId
     * @param seqNo
     *
     * @return EBlockPartSequence element.
     */
    private EBlockPartSequence blockpartsequence_create( Integer actionId, Integer seqNo )
    {
        EBlockPartSequence eBlockPartSequence = new EBlockPartSequence();

        BigInteger SequenceNo = BigInteger.valueOf( seqNo );
        eBlockPartSequence.setSequence( SequenceNo );

        TBlockPart tBlockPart = new TBlockPart();
        tBlockPart.setId( "bp" + actionId );
        TBlockPartRef tBlockPartRef = new TBlockPartRef();
        tBlockPartRef.setRef( tBlockPart );
        eBlockPartSequence.getBlockPartRef().add( tBlockPartRef );

        return eBlockPartSequence;
    }

    /**
     * Creates TBlockPart element on the basis of provided information.
     *
     * @param tripId
     * @param dayTypeId
     * @param eFormation
     *
     * @return TBlockPart element.
     */
    private TBlockPart blockpart_create( Integer tripId, Integer dayTypeId, EFormation eFormation )
    {
        TBlockPart tBlockPart = new TBlockPart();

        tBlockPart.setId( "bp" + tripId );

        ETrainPart eTrainPart = new ETrainPart();
        eTrainPart.setId( "tp" + tripId );
        tBlockPart.setTrainPartRef( eTrainPart );

        EOperatingPeriod eOperatingPeriod = new EOperatingPeriod();
        eOperatingPeriod.setId( "op" + dayTypeId );
        tBlockPart.setOperatingPeriodRef( eOperatingPeriod );

        tBlockPart.setFormationRef( eFormation );
        tBlockPart.setMission( "timetable" );

        return tBlockPart;
    }

    /**
     * Converts list of EBlock elements to the list of PlannedService objects.
     *
     * @param eBlocks
     *
     * @return list of PlannedService objects.
     */
    private List<PlannedService> plannedservices_from_blocks( List<EBlock> eBlocks ) throws RailMlServiceImportException
    {
        List<PlannedService> plannedServices = new ArrayList<>();

        for( EBlock eBlock : eBlocks )
        {
            try
            {
                PlannedService plannedService = plannedservice_from_block( eBlock );
                plannedServices.add( plannedService );
            } catch( NullPointerException | RailMlDatabaseLinkingException |
                     RailMlNullElementException | RailMlServiceImportException |
                     RailMlNullAttributeException ex )
            {
                Logger.getLogger( RailMlConverter.class.getName() ).log( Level.SEVERE, null, ex );
            }
        }

        if( plannedServices.isEmpty() )
        {
            throw new RailMlServiceImportException( "List of imported planned services is empty." );
        }

        return plannedServices;
    }

    /**
     * Links the OCP objects with TTObject objects in database by EOcp:ID value.
     * Buffers TTObject under EOcp:ID value if link was successful. Buffered
     * objects are used for checking that all stations/platforms used in
     * imported service are exist in database.
     *
     * @param eOcps
     */
    private void ttobject_with_ocp_dblinking( List<EOcp> eOcps )
    {
        for( EOcp eOcp : eOcps )
        {
            TTObject tTObject = this.dataController.getTtObject( eOcp.getId() );
            if( tTObject == null )
            {
                System.out.println( "No related object found for " + eOcp.getId() );
                continue;
            }

            this.bufferedTtObjects.put( eOcp.getId(), tTObject );
        }
    }

    /**
     * Links the Formation objects with TrainType objects in database by
     * Formation:ID value. Buffers TrainType under Formation:ID value if link
     * was successful. Buffered objects are used for checking that all train
     * types used in imported service are exist in database.
     *
     * @param eFormations
     */
    private void traintype_with_formation_dblinking( List<EFormation> eFormations )
    {
        for( EFormation eFormation : eFormations )
        {
            TrainType trainType = this.dataController.getTrainType( eFormation.getId() );
            if( trainType == null )
            {
                System.out.println( "No related TrainType found for " + eFormation.getId() );
                continue;
            }

            this.bufferedTrainTypes.put( eFormation.getId(), trainType );
        }
    }

    /**
     * Converts EBlock element and its references to PlannedService object.
     *
     * @param eBlock
     *
     * @throws RailMlDatabaseLinkingException if object mentioned in RailML is
     *                                        not found in TMS database.
     * @throws RailMlNullElementException     if used RailML element is empty or
     *                                        null.
     * @throws RailMlServiceImportException   if planned service was not
     *                                        imported or no trip actions were
     *                                        found for it.
     * @throws RailMlNullAttributeException   if used RailML element attribute
     *                                        is empty or null.
     *
     * @return PlannedService object.
     */
    private PlannedService plannedservice_from_block( EBlock eBlock ) throws RailMlDatabaseLinkingException, RailMlNullElementException, RailMlServiceImportException, RailMlNullAttributeException
    {
        List<EBlockPartSequence> eBlockPartSequences = eBlock.getBlockPartSequence();
        if( eBlockPartSequences.isEmpty() )
        {
            throw new RailMlNullElementException( "List of EBlockPartSequence is null." );
        }

        PlannedService plannedService = plannedservice_construct( eBlock );
        List<TripAction> tripActions = new ArrayList<>();
        for( int eBpsNo = 0; eBpsNo < eBlockPartSequences.size(); eBpsNo++ )
        {
            EBlockPartSequence eBlockPartSequence = eBlockPartSequences.get( eBpsNo );

            List<TBlockPartRef> tBlockPartRefs = eBlockPartSequence.getBlockPartRef();
            if( tBlockPartRefs.isEmpty() )
            {
                throw new RailMlNullElementException( "List of TBlockPartRef is null." );
            }

            for( TBlockPartRef tBlockPartRef : tBlockPartRefs )
            {
                TBlockPart tBlockPart = (TBlockPart) tBlockPartRef.getRef();
                if( tBlockPart == null )
                {
                    throw new RailMlNullElementException( "TBlockPart is null." );
                }

                if( !traintype_exists_for_linking( tBlockPart ) )
                {
                    throw new RailMlServiceImportException( "Aborting importing planned service. No TrainType to link with." );
                }

                if( !daytype_exists_for_linking( tBlockPart ) )
                {
                    throw new RailMlServiceImportException( "Aborting importing planned service. No DayType to link with." );
                }

                ETrainPart eTrainPart = (ETrainPart) tBlockPart.getTrainPartRef();
                if( eTrainPart == null )
                {
                    throw new RailMlNullElementException( "ETrainPart is null." );
                }

                List<EOcpTT> eOcpTTs = RailMlFacade.GetEOcpTTs( eTrainPart );
                EOcpTT eOcpTT = eOcpTTs.get( 0 );

                if( !ttobject_exists_for_linking( eOcpTT ) )
                {
                    throw new RailMlServiceImportException( "Aborting importing planned service. No TtObject to link with." );
                }

                TripAction tripAction = null;
                if( eOcpTTs.size() == 1 )
                {
                    tripAction = tripaction_from_tocptt( eOcpTT );
                } else if( eOcpTTs.size() > 1 )
                {
                    tripAction = triptemplate_from_trainpart( eTrainPart );
                }

                tripAction.setTrip( plannedService );
                tripAction.setSeqNo( eBpsNo + 1 );
                tripActions.add( tripAction );

                Integer durationSecs = plannedService.getDurationSecs() + tripAction.getPlannedSecs();
                plannedService.setDurationSecs( durationSecs );

                if( eBpsNo == 0 )
                {
                    TTObject plannedStartObj = tripAction.getTimetableObject();
                    plannedService.setPlannedStartObj( plannedStartObj );

                    Integer plannedStartSecs = DateTimeHelper.GetStartSecs( eOcpTT );
                    plannedService.setPlannedStartSecs( plannedStartSecs );

                    String line = eTrainPart.getLine();
                    TTArea areaObj = (TTArea) this.dataController.getArea( line );
                    plannedService.setAreaObj( areaObj );

                    EFormation eFormation = (EFormation) tBlockPart.getFormationRef();
                    TrainType trainType = this.bufferedTrainTypes.get( eFormation.getId() );
                    plannedService.setTrainType( trainType );

                    EOperatingPeriod eOperatingPeriod = (EOperatingPeriod) tBlockPart.getOperatingPeriodRef();
                    DayType dayType = this.bufferedDayTypes.get( eOperatingPeriod.getId() );
                    plannedService.setDayTypeList( new ArrayList<>(Arrays.asList(dayType)) );  // @Todo: check

                } else if( eBpsNo == eBlockPartSequences.size() - 1 )
                {
                    TTObject plannedStopObj = tripAction.getTimetableObject();
                    plannedService.setPlannedStopObj( plannedStopObj );

                    Integer plannedStopSecs = plannedService.getPlannedStartSecs() + plannedService.getDurationSecs();
                    plannedService.setPlannedStopSecs( plannedStopSecs );
                }
            }
        }

        if( tripActions.isEmpty()
            || tripActions.size() <= 1 )
        {
            throw new RailMlServiceImportException( "Aborting importing planned service. No trip actions found." );
        }

        plannedService.setTripActions( tripActions );

        return plannedService;
    }

    /**
     * Creates PlannedService object from EBlock element information.
     *
     * @param eBlock
     *
     * @return PlannedService object.
     */
    private PlannedService plannedservice_construct( EBlock eBlock )
    {
        PlannedService plannedService = new PlannedService();

        String description = eBlock.getName();
        String format = ResourceBundle.getBundle( "Bundle" ).getString( "SERVICE_REGEXP_PATTERN" );
        int zeros = Character.getNumericValue( format.charAt( 3 ) ) - description.length();
        while( zeros > 0 )
        {
            description = "0" + description;
            zeros--;
        }
        plannedService.setDescription( description );

        plannedService.setPlannedState( null );
        plannedService.setDurationSecs( 0 );
        plannedService.setPlannedStartSecs( 0 );
        plannedService.setPlannedStopSecs( 0 );
        plannedService.setConsumed( false );
        plannedService.setUtcTimes( false );
        plannedService.setValid( true );
        plannedService.setTimesAreValid( false );
        plannedService.setOrigoSecs( 0 );
        plannedService.setActionsFromTemplate( false );

        JsonObject jsonObject = Json.createObjectBuilder().build();
        plannedService.setRouting( jsonObject );

        SchedulingState schedulingState = this.dataController.getSchedulingState();
        plannedService.setPlannedState( schedulingState );

        TripType tripType = triptype_from_description( eBlock.getCode() );
        plannedService.setTripType( tripType );

        return plannedService;
    }

    /**
     * Checks if DayType object was successfully imported and is ready to be
     * used.
     *
     * @param tBlockPart
     *
     * @throws RailMlDatabaseLinkingException if no identifiers were found to be
     *                                        used in search.
     *
     * @return true if succeeded, otherwise false.
     */
    private boolean daytype_exists_for_linking( TBlockPart tBlockPart ) throws RailMlDatabaseLinkingException
    {
        EOperatingPeriod eOperatingPeriod = (EOperatingPeriod) tBlockPart.getOperatingPeriodRef();
        if( eOperatingPeriod == null )
        {
            throw new RailMlDatabaseLinkingException( "EOperatingPeriod is null." );
        }

        String id = eOperatingPeriod.getId();
        if( id == null || id.isEmpty() )
        {
            throw new RailMlDatabaseLinkingException( "EOperatingPeriod ID is null." );
        }

        return this.bufferedDayTypes.containsKey( id );
    }

    /**
     * Checks if TrainType exists in database and could be used for making a
     * reference to it.
     *
     * @param tBlockPart
     *
     * @throws RailMlDatabaseLinkingException if no identifiers were found to be
     *                                        used in search.
     *
     * @return true if succeeded, otherwise false.
     */
    private boolean traintype_exists_for_linking( TBlockPart tBlockPart ) throws RailMlDatabaseLinkingException
    {
        TFormation tFormation = (TFormation) tBlockPart.getFormationRef();
        if( tFormation == null )
        {
            throw new RailMlDatabaseLinkingException( "TFormation is null." );
        }

        String id = tFormation.getId();
        if( id == null || id.isEmpty() )
        {
            throw new RailMlDatabaseLinkingException( "TFormation ID is null." );
        }

        return this.bufferedTrainTypes.containsKey( id );
    }

    /**
     * Checks if TTOBject exists in database and could be used for making a
     * reference to it.
     *
     * @param eOcpTT
     *
     * @throws RailMlDatabaseLinkingException if no identifiers were found to be
     *                                        used in search.
     *
     * @return true if succeeded, otherwise false.
     */
    private boolean ttobject_exists_for_linking( EOcpTT eOcpTT ) throws RailMlDatabaseLinkingException
    {
        EOcp eOcp = (EOcp) eOcpTT.getOcpRef();
        if( eOcp == null )
        {
            throw new RailMlDatabaseLinkingException( "EOcp is null." );
        }

        String id = eOcp.getId();
        if( id == null || id.isEmpty() )
        {
            throw new RailMlDatabaseLinkingException( "EOcp ID is null." );
        }

        return this.bufferedTtObjects.containsKey( id );
    }

    /**
     * Converts EOcpTT element to TripAction object.
     *
     * @param eOcpTT
     *
     * @throws RailMlNullAttributeException if used RailML element attribute is
     *                                      empty or null.
     * @throws RailMlNullElementException   if used RailML element is empty or
     *                                      null.
     *
     * @return TripAction object.
     */
    private TripAction tripaction_from_tocptt( EOcpTT eOcpTT ) throws RailMlNullAttributeException, RailMlNullElementException
    {
        TripAction tripAction = null;

        String remarks = eOcpTT.getRemarks();
        if( remarks == null || remarks.isEmpty() )
        {
            throw new RailMlNullAttributeException( "Remarks element is null." );
        }

        ActionType actionType = this.dataController.getActionType( Integer.parseInt( remarks ) );
        if( actionType == null )
        {
            throw new RailMlNullElementException( "Action type is null." );
        }

        switch( actionType.getMainActionTypeEnum() )
        {
            case RUN_TRIP:
                tripAction = new ActionRunTrip();
                break;
            case PASSENGER_STOP:
                tripAction = new ActionPassengerStop();
                break;
            case CARGO_STOP:
                tripAction = new ActionCargoStop();
                break;
            case SCHEDULING_STOP:
                tripAction = new ActionSchedulingStop();
                break;
            case TRAIN_MOVING:
                tripAction = new ActionTrainMoving();
                break;
            case TRAIN_FORMATION:
                tripAction = new ActionTrainFormation();
                break;
            case PASS_OBJECT:
                tripAction = new ActionPassObject();
                break;
            case GLUE:
                tripAction = new ActionGlue();
                break;
            default:
                throw new RailMlNullElementException( "MainAction type is null." );
        }

        tripAction.setActionType( actionType );

        EOcp eOcp = (EOcp) eOcpTT.getOcpRef();
        TTObject tTObject = this.bufferedTtObjects.get( eOcp.getId() );
        tripAction.setTimetableObject( tTObject );

        EArrivalDepartureTimes eArrivalDepartureTimes = RailMlFacade.GetTimes( eOcpTT );

        Integer plannedDurationSecs = DateTimeHelper.GetPlannedDuration( eArrivalDepartureTimes );
        tripAction.setMinSecs( plannedDurationSecs );
        tripAction.setPlannedSecs( plannedDurationSecs );

        tripAction.setConsumed( false );
        tripAction.setTimesValid( false );

        return tripAction;
    }

    /**
     * Gets TripType object from database by provided code.
     *
     * @param code
     *
     * @return TripType object.
     */
    private TripType triptype_from_description( String code )
    {
        return this.dataController.getTripType( code );
    }

    /**
     * Converts TrainPart element to TripAction object. Tries to link trip
     * template with template in database, imports it into the database if fails
     * to link.
     *
     * @param eTrainPart
     *
     * @throws RailMlNullAttributeException if used RailML element attribute is
     *                                      empty or null.
     * @throws RailMlNullElementException   if used RailML element is empty or
     *                                      null.
     *
     * @return TripAction object.
     */
    private TripAction triptemplate_from_trainpart( ETrainPart eTrainPart ) throws RailMlNullAttributeException, RailMlNullElementException
    {
        ActionRunTrip serviceTripAction = new ActionRunTrip();
        serviceTripAction.setConsumed( false );
        serviceTripAction.setTimesValid( false );

        String actionTypeId = eTrainPart.getCode();
        ActionType actionType = this.dataController.getActionType( Integer.parseInt( actionTypeId ) );
        serviceTripAction.setActionType( actionType );

        PlannedTrip plannedTrip = plannedtrip_from_trainpart( eTrainPart );
        serviceTripAction.setRefTrip( plannedTrip );

        boolean IsTemplateImporting = false;
        BasicTrip template = triptemplate_find_in_db( eTrainPart );
        if( template == null )
        {
            template = triptemplate_find_in_buffer( eTrainPart );
            if( template == null )
            {
                template = triptemplate_construct( eTrainPart );
                this.bufferedTemplates.add( template );
                IsTemplateImporting = true;
            }
        }
        plannedTrip.setTripTemplate( template );

        Integer plannedDurationSecs = 0;
        List<EOcpTT> eOcpTTs = RailMlFacade.GetEOcpTTs( eTrainPart );
        for( int eOcpNo = 0; eOcpNo < eOcpTTs.size(); eOcpNo++ )
        {
            EOcpTT eOcpTT = eOcpTTs.get( eOcpNo );
            TripAction tripAction = tripaction_from_tocptt( eOcpTT );
            tripAction.setSeqNo( eOcpNo + 1 );

            plannedDurationSecs += tripAction.getPlannedSecs();

            if( IsTemplateImporting )
            {
                template.addTripAction( tripAction );
            }

            if( tripAction instanceof ActionTrainMoving
                && eOcpNo + 1 < eOcpTTs.size() )
            {
                EOcpTT eOcpTT2 = eOcpTTs.get( eOcpNo + 1 );
                EOcp eOcp = (EOcp) eOcpTT2.getOcpRef();
                TTObject tTObject = this.dataController.getTtObject( eOcp.getId() );
                ((ActionTrainMoving) tripAction).setTimetableObject2( tTObject );
            }

            if( eOcpNo == 0 )
            {
                TTObject ttObject = tripAction.getTimetableObject();
                serviceTripAction.setTimetableObject( ttObject );

                plannedTrip.setPlannedStartObj( ttObject );

                Integer plannedStartSecs = DateTimeHelper.GetStartSecs( eOcpTT );
                plannedTrip.setPlannedStartSecs( plannedStartSecs );

                if( IsTemplateImporting )
                {
                    template.setPlannedStartObj( ttObject );
                }
            } else if( eOcpNo == eOcpTTs.size() - 1 )
            {
                TTObject ttObject = tripAction.getTimetableObject();
                serviceTripAction.setTimetableObject2( ttObject );

                plannedTrip.setPlannedStopObj( ttObject );

                Integer plannedStopSecs = plannedTrip.getPlannedStartSecs() + plannedDurationSecs;
                plannedTrip.setPlannedStopSecs( plannedStopSecs );

                if( IsTemplateImporting )
                {
                    template.setPlannedStopObj( ttObject );
                }
            }
        }

        plannedTrip.setDurationSecs( plannedDurationSecs );

        serviceTripAction.setMinSecs( plannedDurationSecs );
        serviceTripAction.setPlannedSecs( plannedDurationSecs );

        if( IsTemplateImporting )
        {
            template.setDurationSecs( plannedDurationSecs );
        }

        return serviceTripAction;
    }

    /**
     * Creates a new trip template from ETrainPart element.
     *
     * @param eTrainPart
     *
     * @return BasicTrip object.
     */
    private BasicTrip triptemplate_construct( ETrainPart eTrainPart )
    {
        BasicTrip template = new BasicTrip();

        template.setDescription( "RailML Import: " + eTrainPart.getName() );
        template.setConsumed( false );
        template.setUtcTimes( true );
        template.setTimesAreValid( false );
        template.setActionTimesValid( false );
        template.setOrigoSecs( 0 );
        template.setValid( true );

        Integer tripUserTypeId = TripUserType.State.SYSTEM_USER.getValue();
        TripUserType tripUserType = this.dataController.getTripUserType( tripUserTypeId );
        template.setTripUserType( tripUserType );

        JsonObject jsonObject = Json.createObjectBuilder().build();
        template.setRouting( jsonObject );

        TTArea areaObj = (TTArea) this.dataController.getArea( eTrainPart.getLine() );
        template.setAreaObj( areaObj );

        TripType tripType = triptype_from_description( eTrainPart.getRemarks() );
        template.setTripType( tripType );

        return template;
    }

    /**
     * Tries to find similar trip template in database. Compares template path
     * hash with hash of template in database for it.
     *
     * @param eTrainPart
     *
     * @throws RailMlNullElementException if filter or hash creation methods
     *                                    failed.
     *
     * @return BasicTrip object if succeeded, otherwise null.
     */
    private BasicTrip triptemplate_find_in_db( ETrainPart eTrainPart ) throws RailMlNullElementException
    {
        BasicTripFilter filter = triptemplate_filter_construct( eTrainPart );
        List<BasicTrip> templates = this.dataController.findAllRelatedTemplates( filter );

        String trainPartHash = trainpart_hash_construct( eTrainPart );
        for( BasicTrip template : templates )
        {
            String templateHash = triptemplate_hash_construct( template );
            if( templateHash == null ? trainPartHash == null : templateHash.equals( trainPartHash ) )
            {
                return template;
            }
        }

        return null;
    }

    /**
     * Tries to find similar trip template in local buffer. Compares template
     * path hash with hash of template in buffer for it.
     *
     * @param eTrainPart
     *
     * @throws RailMlNullElementException if filter or hash creation methods
     *                                    failed.
     *
     * @return BasicTrip object if succeeded, otherwise null.
     */
    private BasicTrip triptemplate_find_in_buffer( ETrainPart eTrainPart ) throws RailMlNullElementException
    {
        String trainPartHash = trainpart_hash_construct( eTrainPart );
        for( BasicTrip template : this.bufferedTemplates )
        {
            String templateHash = triptemplate_hash_construct( template );
            if( templateHash == null ? trainPartHash == null : templateHash.equals( trainPartHash ) )
            {
                return template;
            }
        }

        return null;
    }

    /**
     * Creates filter on the basis of provided TrainPart element information.
     *
     * @param eTrainPart
     *
     * @throws RailMlNullElementException if filter creation fails.
     *
     * @return BasicTripFilter object.
     */
    private BasicTripFilter triptemplate_filter_construct( ETrainPart eTrainPart ) throws RailMlNullElementException
    {
        BasicTripFilter filter = new BasicTripFilter();
        filter.setValidFilter( true );

        String areaName = eTrainPart.getLine();
        TTArea tTArea = (TTArea) this.dataController.getArea( areaName );
        filter.setAreaFilter( tTArea );

        TripType tripType = triptype_from_description( eTrainPart.getRemarks() );
        filter.setTripTypeFilter( tripType );

        EOcp eOcpStart = RailMlFacade.getFirstOcp( eTrainPart );
        TTObject plannedStartObj = this.bufferedTtObjects.get( eOcpStart.getId() );
        filter.setPlannedStartObjFilter( plannedStartObj );

        EOcp eOcpStop = RailMlFacade.getLastOcp( eTrainPart );
        TTObject plannedStopObj = this.bufferedTtObjects.get( eOcpStop.getId() );
        filter.setPlannedStopObjFilter( plannedStopObj );

        return filter;
    }

    /**
     * Creates PlannedTrip object on the basis of provided TrainPart element
     * information.
     *
     * @param eTrainPart
     *
     * @return PlannedTrip object.
     */
    private PlannedTrip plannedtrip_from_trainpart( ETrainPart eTrainPart )
    {
        PlannedTrip plannedTrip = new PlannedTrip();

        plannedTrip.setTimesAreValid( false );
        plannedTrip.setConsumed( false );
        plannedTrip.setUtcTimes( false );
        plannedTrip.setOrigoSecs( 0 );
        plannedTrip.setActionsFromTemplate( true );
        plannedTrip.setValid( true );

        JsonObject jsonObject = Json.createObjectBuilder().build();
        plannedTrip.setRouting( jsonObject );

        TTArea areaObj = (TTArea) this.dataController.getArea( eTrainPart.getLine() );
        plannedTrip.setAreaObj( areaObj );

        TripType tripType = triptype_from_description( eTrainPart.getDescription() );
        plannedTrip.setTripType( tripType );

        plannedTrip.setDescription( "RailML Import: " + eTrainPart.getName() );

        SchedulingState plannedState = this.dataController.getSchedulingState();
        plannedTrip.setPlannedState( plannedState );

        EOperatingPeriodRef eOperatingPeriodRef = eTrainPart.getOperatingPeriodRef();
        String dayTypeId = daytypeid_from_operatingperiodref( eOperatingPeriodRef );
        DayType dayType = this.bufferedDayTypes.get( dayTypeId );
        plannedTrip.setDayTypeList( new ArrayList<>(Arrays.asList(dayType)) );  // @Todo: check

        EFormationTT eFormationTT = eTrainPart.getFormationTT();
        String trainTypeId = traintypeid_from_formationtt( eFormationTT );
        TrainType trainType = this.bufferedTrainTypes.get( trainTypeId );
        plannedTrip.setTrainType( trainType );

        return plannedTrip;
    }

    /**
     * Gets TrainType ID from EFormationTT element.
     *
     * @param eFormationTT
     *
     * @return String
     */
    private String traintypeid_from_formationtt( EFormationTT eFormationTT )
    {
        EFormation eFormation = (EFormation) eFormationTT.getFormationRef();
        return eFormation.getId();
    }

    /**
     * Creates path hash of provided trip template.
     *
     * @param template
     *
     * @return String
     */
    private String triptemplate_hash_construct( BasicTrip template )
    {
        String hash = "";

        for( TripAction tripAction : template.getTripActions() )
        {
            hash += tripAction.getTimetableObject().getTTObjId().toString();
            hash += " (" + tripAction.getPlannedSecs().toString() + ") -> ";
        }

        return hash;
    }

    /**
     * Creates path hash of provided ETrainPart element.
     *
     * @param eTrainPart
     *
     * @throws RailMlNullElementException if creation process fails.
     *
     * @return String
     */
    private String trainpart_hash_construct( ETrainPart eTrainPart ) throws RailMlNullElementException
    {
        String hash = "";

        List<EOcpTT> eOcpTTs = RailMlFacade.GetEOcpTTs( eTrainPart );
        for( EOcpTT eOcpTT : eOcpTTs )
        {
            EOcp eOcp = (EOcp) eOcpTT.getOcpRef();
            hash += eOcp.getNumber();

            EArrivalDepartureTimes eArrivalDepartureTimes = RailMlFacade.GetTimes( eOcpTT );
            Integer plannedSecs = DateTimeHelper.GetPlannedDuration( eArrivalDepartureTimes );
            hash += " (" + plannedSecs.toString() + ") -> ";
        }

        return hash;
    }

}
