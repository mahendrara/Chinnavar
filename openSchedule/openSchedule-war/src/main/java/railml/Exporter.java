/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package railml;

import java.io.File;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import javafx.util.Pair;
import org.jfree.data.time.Day;
import org.railml.schemas._2011.EArrivalDepartureTimes;
import org.railml.schemas._2011.EBlock;
import org.railml.schemas._2011.EBlockPartSequence;
import org.railml.schemas._2011.EBlockParts;
import org.railml.schemas._2011.EBlocks;
import org.railml.schemas._2011.ECategories;
import org.railml.schemas._2011.ECategory;
import org.railml.schemas._2011.ECirculations;
import org.railml.schemas._2011.EFormation;
import org.railml.schemas._2011.EFormationTT;
import org.railml.schemas._2011.EFormations;
import org.railml.schemas._2011.EOcp;
import org.railml.schemas._2011.EOcpTT;
import org.railml.schemas._2011.EOcpsTT;
import org.railml.schemas._2011.EOperatingPeriod;
import org.railml.schemas._2011.EOperatingPeriodRef;
import org.railml.schemas._2011.EOperatingPeriods;
import org.railml.schemas._2011.EOperationControlPoints;
import org.railml.schemas._2011.ERostering;
import org.railml.schemas._2011.ERosterings;
import org.railml.schemas._2011.EStopDescription;
import org.railml.schemas._2011.ETimetablePeriod;
import org.railml.schemas._2011.ETimetablePeriods;
import org.railml.schemas._2011.ETrainPart;
import org.railml.schemas._2011.ETrainParts;
import org.railml.schemas._2011.Infrastructure;
import org.railml.schemas._2011.Railml;
import org.railml.schemas._2011.Rollingstock;
import org.railml.schemas._2011.TBlockPart;
import org.railml.schemas._2011.TBlockPartRef;
import org.railml.schemas._2011.TCirculation;
import org.railml.schemas._2011.TOcpTTType;
import org.railml.schemas._2011.TOnOff;
import org.railml.schemas._2011.Timetable;
import schedule.entities.ActionPassObject;
import schedule.entities.ActionSchedulingStop;
import schedule.entities.ActionTrainMoving;
import schedule.entities.DayInSchedule;
import schedule.entities.DayType;
import schedule.entities.PlannedService;
import schedule.entities.PlannedTrip;
import schedule.entities.Schedule;
import schedule.entities.TTObject;
import schedule.entities.TrainType;
import schedule.entities.TripAction;
import schedule.entities.TripType;
import schedule.sessions.PlannedServiceFacade;
import schedule.sessions.TripTypeFacade;
import schedule.uiclasses.PlannedServiceFilter;
import schedule.uiclasses.TripTypeFilter;

/**
 *
 * @author spirttin
 */
@Named("Exporter")
@SessionScoped
public class Exporter implements Serializable
{
    // Conversion map from OpenSchedule DB object to  railml object ID
    // key == OpenSchedule DB object. Represented as an Object, since there are many different entitytypes
    // value == railML object. 
    private Map<Object, Object> conversionMap;
    private Map<String, ECategory> categoryMap;

    private static final String TIMETABLEPERIOD_PREFIX = "ttp";

    private String generateTimetablePeriodKeyFromScheduleId( Schedule schedule )
    {
        return TIMETABLEPERIOD_PREFIX + schedule.getScheduleId();
    }

    private static final String OPERATINGPERIOD_PREFIX = "op";

    private String generateOperatingPeriodKeyFromDaytypeId( DayType dayType )
    {
        return OPERATINGPERIOD_PREFIX + dayType.getDayTypeId();
    }

    private static final String FORMATION_PREFIX = "f";

    private String generateFormationKeyTrainTypeId( TrainType trainType )
    {
        return FORMATION_PREFIX + trainType.getTrainTypeId();
    }

    private static final String TRAINPART_PREFIX = "tp";

    private String generateTrainPartKeyPlannedTripId( PlannedTrip plannedTrip )
    {
        return TRAINPART_PREFIX + plannedTrip.getTripId();
    }

    private String generateTrainPartKeyPlannedTripId( PlannedTrip plannedTrip, DayType dayType )
    {
        return TRAINPART_PREFIX + plannedTrip.getTripId() + "_" + dayType.getDayTypeId();
    }

    private static final String OCP_PREFIX = "ocp";

    private String generateOcpKeyFromObjectId( TTObject object )
    {
        return OCP_PREFIX + object.getTTObjId();
    }

    private static final String BLOCKPART_PREFIX = "bp";

    private String generateBlockPartKeyFromPlannedTripId( PlannedTrip plannedTrip )
    {
        return BLOCKPART_PREFIX + plannedTrip.getTripId();
    }

    private String generateBlockPartKeyFromPlannedTripId( PlannedTrip plannedTrip, DayType dayType )
    {
        return BLOCKPART_PREFIX + plannedTrip.getTripId() + "_" + dayType.getDayTypeId();
    }

    private static final String BLOCK_PREFIX = "b";

    private String generateBlockKeyFromPlannedServiceId( PlannedService plannedService )
    {
        return BLOCK_PREFIX + plannedService.getTripId();
    }

    private String generateBlockKeyFromPlannedServiceId( PlannedService plannedService, DayType dayType )
    {
        return BLOCK_PREFIX + plannedService.getTripId() + "_" + dayType.getDayTypeId();
    }

    @Inject
    private PlannedServiceFacade ejbPlannedServiceFacade;
    @Inject
    private TripTypeFacade ejbTripTypeFacade;

    @Context
    ServletContext servletContext;

    public Exporter()
    {
    }

    /**
     * Generates railML file, which is downloadable
     *
     * @param schedule
     *
     * @throws Exception
     */
    public File generateDownload( Schedule schedule ) throws Exception
    {
        Railml railml = buildRailml( schedule );

        // TODO :: Add timestamp to the name?
        File file = File.createTempFile( "ExportedTT", ".xml" );
        file.deleteOnExit();

        JAXBContext jaxbContext = JAXBContext.newInstance( Railml.class );
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, true );
        jaxbMarshaller.marshal( railml, file );

        return file;
    }

    private Railml buildRailml( Schedule schedule )
    {
        Railml data = new Railml();

        conversionMap = new HashMap<>();
        categoryMap = new HashMap<>();

        buildRollingstock( data, schedule );
        addEmptyInfrastructureCategory( data );
        buildTimetable( data, schedule );

        return data;
    }

    private void buildRollingstock( Railml data, Schedule schedule )
    {
        // Build structure
        Rollingstock rollingStock = new Rollingstock();
        // Always one rollingstock
        rollingStock.setId( "roll1" );

        EFormations formations = new EFormations();
        rollingStock.setFormations( formations );
        data.setRollingstock( rollingStock );

        // Build formations
        List<DayType> dayTypes = schedule.getDayTypes();
        for( DayType dayType : dayTypes )
        {
            PlannedServiceFilter filter = new PlannedServiceFilter();
            filter.setDayTypeFilter( dayType );
            filter.setValidFilter( true );

            List<PlannedService> plannedServices = ejbPlannedServiceFacade.findAll( filter );
            for( PlannedService plannedService : plannedServices )
            {
                // Check if given traintype already exists and if not, add it
                TrainType trainType = plannedService.getTrainType();
                if( trainType == null )
                {
                    System.out.println( "Exporter:buildRollingstock:service: " + plannedService.getDescription() );
                } else if( !conversionMap.containsKey( trainType ) )
                {
                    EFormation formation = new EFormation();
                    formation.setId( generateFormationKeyTrainTypeId( trainType ) );
                    formation.setName( trainType.getDescription() );
                    formation.setCode( trainType.getDescription() );
                    formation.setDescription( trainType.getDescription() );
                    // TODO: length, speed, weight?

                    // Add object to conversionMap
                    conversionMap.put( trainType, formation );

                    // And add formation to xml
                    data.getRollingstock().getFormations().getFormation().add( formation );
                }
            }
        }
    }

    private void addEmptyInfrastructureCategory( Railml data )
    {
        //
        // Elements to infrastructure will be added when trips are looped through
        //
        Infrastructure infrastructure = new Infrastructure();
        // Always only one infrastructure
        infrastructure.setId( "infra1" );

        EOperationControlPoints ocps = new EOperationControlPoints();
        infrastructure.setOperationControlPoints( ocps );

        data.setInfrastructure( infrastructure );
    }

    private void buildTimetable( Railml data, Schedule schedule )
    {
        Timetable timetable = new Timetable();
        // Hardcoded, only one schedule can be introduced per XML
        timetable.setId( "time1" );

        data.setTimetable( timetable );

        buildTimetablePeriods( data, schedule );
        buildOperatingPeriods( data, schedule );
        buildTrainParts( data, schedule );
        buildRosterings( data, schedule );
    }

    private void buildTimetablePeriods( Railml data, Schedule schedule )
    {
        ETimetablePeriods ttps = new ETimetablePeriods();
        data.getTimetable().setTimetablePeriods( ttps );

        // Generate data
        ETimetablePeriod ttp = new ETimetablePeriod();
        ttp.setId( generateTimetablePeriodKeyFromScheduleId( schedule ) );
        ttp.setName( schedule.getDescription() );

        // Set reference to template.
        if( schedule.getScheduleTemplate() != null )
        {
            int templateHash = schedule.getScheduleTemplate().hashCode();
            String templateHashStr = Integer.toString( templateHash );
            ttp.setCode( templateHashStr );
        }

        try
        {
            GregorianCalendar c = new GregorianCalendar();
            c.setTime( schedule.getStartTime() );
            c.add( GregorianCalendar.DAY_OF_YEAR, 1 );
            c.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
            XMLGregorianCalendar startDate = DatatypeFactory.newInstance().newXMLGregorianCalendar( c );
            ttp.setStartDate( startDate );

            c.add( GregorianCalendar.DAY_OF_YEAR, schedule.getNumberOfDays() - 1 );
            XMLGregorianCalendar endDate = DatatypeFactory.newInstance().newXMLGregorianCalendar( c );
            ttp.setEndDate( endDate );
        } catch( Exception e )
        {
            System.out.println( "DATE CONVERSION ERROR" );
        }

        // Add generated object to railml 
        data.getTimetable().getTimetablePeriods().getTimetablePeriod().add( ttp );

        // Add generated ttp key to match object
        conversionMap.put( schedule, ttp );
    }

    private TripType getTemplateTripType( PlannedTrip plannedTrip )
    {
        TripTypeFilter tripTypeFilter = new TripTypeFilter();
        tripTypeFilter.setValid( true );
        tripTypeFilter.setToTripType( plannedTrip.getTripType().getTripType().getValue() );
        //Assume the subtypes are the same
        tripTypeFilter.setTripSubType( plannedTrip.getTripType().getTripSubType() );

        TripType tripType = ejbTripTypeFacade.findFirst( tripTypeFilter );
        return tripType;
    }

    private void buildCategories( Railml data, TripType templateTripType )
    {
        String id = templateTripType.getExternalId();
        if( !this.categoryMap.containsKey( id ) )
        {
            ECategory category = new ECategory();
            category.setId( id );

            if( data.getTimetable().getCategories() == null )
            {
                ECategories categories = new ECategories();
                data.getTimetable().setCategories( categories );
            }

            data.getTimetable().getCategories().getCategory().add( category );
            this.categoryMap.put( id, category );
        }
    }

    private void buildOperatingPeriods( Railml data, Schedule schedule )
    {
        EOperatingPeriods ops = new EOperatingPeriods();
        data.getTimetable().setOperatingPeriods( ops );

        final String bitmask = String.format( "%" + schedule.getNumberOfDays() + "s", "0" ).replace( ' ', '0' );
        XMLGregorianCalendar startDate = null;
        XMLGregorianCalendar endDate = null;
        try
        {
            GregorianCalendar c = new GregorianCalendar();
            c.setTime( schedule.getStartTime() );
            c.add( GregorianCalendar.DAY_OF_YEAR, 1 );
            c.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
            startDate = DatatypeFactory.newInstance().newXMLGregorianCalendar( c );

            c.add( GregorianCalendar.DAY_OF_YEAR, schedule.getNumberOfDays() - 1 );
            endDate = DatatypeFactory.newInstance().newXMLGregorianCalendar( c );
        } catch( Exception e )
        {
            System.out.println( "DATE CONVERSION ERROR" );
        }

        // Generate Operating periods 
        Map<DayType, EOperatingPeriod> opMap = new HashMap<>();
        for( DayType dt : schedule.getDayTypes() )
        {
            EOperatingPeriod op = new EOperatingPeriod();
            op.setId( generateOperatingPeriodKeyFromDaytypeId( dt ) );
            op.setName( dt.getDescription() );
            op.setCode( dt.getAbbr() );
            op.setStartDate( startDate );
            op.setEndDate( endDate );
            op.setTimetablePeriodRef( conversionMap.get( schedule ) );
            op.setBitMask( bitmask );

            // Add generated object to railml
            data.getTimetable().getOperatingPeriods().getOperatingPeriod().add( op );

            // Add generated op key to match object
            conversionMap.put( dt, op );

            opMap.put( dt, op );
        }

        // Then fill bitmasks
        GregorianCalendar origoDay = new GregorianCalendar();
        origoDay.setTime( schedule.getStartTime() );
        for( DayInSchedule dis : schedule.getDayInSchedules() )
        {

            // Get position of 'dis' in bitmask to 'daydiff'
            int daycode = dis.getDayCodeNo();
            int year = daycode / 1000;
            int day = daycode % 1000;

            GregorianCalendar currentDay = new GregorianCalendar();
            currentDay.set( GregorianCalendar.YEAR, year );
            currentDay.set( GregorianCalendar.DAY_OF_YEAR, day );

            long diff = currentDay.getTime().getTime() - origoDay.getTime().getTime();
            long daydiff = TimeUnit.DAYS.convert( diff, TimeUnit.MILLISECONDS );

            // Then change given bitmask
            for( DayType dis_dt: dis.getDayTypeList()) {
                EOperatingPeriod op = opMap.get(dis_dt);
                StringBuilder builder = new StringBuilder(op.getBitMask());
                builder.setCharAt((int) daydiff, '1');
                op.setBitMask(builder.toString());
            }
        }
    }


    private void buildTrainParts( Railml data, Schedule schedule )
    {
        ETrainParts trainParts = new ETrainParts();
        data.getTimetable().setTrainParts( trainParts );

        List<DayType> dayTypes = schedule.getDayTypes();
        TripType templateTripType = null;
        HashMap<DayType, ETrainPart> tempMap = null;
        for( DayType dayType : dayTypes )
        {
            PlannedServiceFilter filter = new PlannedServiceFilter();
            filter.setDayTypeFilter( dayType );
            filter.setValidFilter( true );

            List<PlannedService> pss = ejbPlannedServiceFacade.findAll( filter );
            for( PlannedService ps : pss )
            {
                for( PlannedTrip pt : ps.getPlannedTrips() )
                {
                    ETrainPart tp = new ETrainPart();
                    tp.setId( generateTrainPartKeyPlannedTripId( pt, dayType ) );
                    tp.setLine( pt.getAreaObj().getScheduleName() );

                    templateTripType = getTemplateTripType( pt );
                    buildCategories( data, templateTripType );
                    tp.setCategoryRef( this.categoryMap.get( templateTripType.getExternalId() ) );

                    // Build sub stuff
                    buildFormationTT( tp, ps );
                    buildOperatingPeriodRef( tp, dayType );
                    buildOcpsTT( data, tp, pt, dayType );

                    // Add generated object to railml
                    data.getTimetable().getTrainParts().getTrainPart().add( tp );

                    // Add generated op key to match object
                    if(!conversionMap.containsKey(pt)) {
                        tempMap = new HashMap<DayType, ETrainPart>() {{ put(dayType, tp); }};
                        conversionMap.put(pt, tempMap);
                    }else {
                        tempMap = (HashMap<DayType, ETrainPart>) conversionMap.get(pt);
                        tempMap.put(dayType, tp);
                        conversionMap.put(pt, tempMap);
                    }
                }
            }
        }
    }

    private void buildFormationTT( ETrainPart tp, PlannedService ps )
    {
        EFormationTT ftt = new EFormationTT();
        ftt.setFormationRef( conversionMap.get( ps.getTrainType() ) );
        tp.setFormationTT( ftt );
    }

    private void buildOperatingPeriodRef( ETrainPart tp, DayType dt )
    {
        EOperatingPeriodRef opr = new EOperatingPeriodRef();
        opr.setRef( conversionMap.get( dt ) );  // @Todo: check
        tp.setOperatingPeriodRef( opr );
    }

    private void buildOcpsTT( Railml data, ETrainPart tp, PlannedTrip pt, DayType dt )
    {
        EOcpsTT ocpsTT = new EOcpsTT();
        tp.setOcpsTT( ocpsTT );

        int seq = 1;
        TripAction curr;
        int size = pt.getTripActions().size();
        while( seq <= size )
        {
            curr = pt.getTripTemplate().getTripAction( seq++ );
            buildOcpTT( data, tp, pt, dt, curr );
        }
    }

    private void buildOcpTT( Railml data, ETrainPart tp, PlannedTrip pt, DayType dt, TripAction currAction )
    {
        if( currAction instanceof ActionTrainMoving )
        {
            return;
        }

        EOcpTT ott = new EOcpTT();
        EArrivalDepartureTimes adt = new EArrivalDepartureTimes();
        GregorianCalendar c1 = new GregorianCalendar();
        GregorianCalendar c2 = new GregorianCalendar();
        // TODO: Is this right? How to select?
        adt.setScope( "scheduled" );

        try
        {
            //pass action or stop action
            //Date startDate = new Date( (origoSecs + currAction.getTimeFromTripStart())
            //                           * 1000L );
            //Date endDate = new Date( (origoSecs + currAction.getTimeFromTripStart()
            //                          + currAction.getPlannedSecs()) * 1000L );

            // Build or finds OCP
            int seconds = 0;
            EOperatingPeriod op = (EOperatingPeriod) (conversionMap.get(dt)); // @Todo: check
            EOcp ocp = (EOcp) buildOcp( data, currAction.getTimetableObject() );
            if( currAction instanceof ActionPassObject )
            {
                ott.setOcpType( TOcpTTType.PASS );
            } else
            {
                // Passenger Stop, Scheduling Stop or Cargo Stop;
                ott.setOcpType( TOcpTTType.STOP );

                buildStopDescription( ott, currAction );
                //Assume each Operating Period has the same DST
                seconds = pt.getPlannedStartSecs() + currAction.getTimeFromTripStart();

                c1.set(op.getStartDate().getYear(), op.getStartDate().getMonth(), op.getStartDate().getDay(), seconds / 3600, seconds / 60 % 60, seconds % 60);
                c1.setTimeZone(TimeZone.getTimeZone(currAction.getTimetableObject().getTimeZone()));
                
                XMLGregorianCalendar arrival = DatatypeFactory.newInstance().newXMLGregorianCalendar( c1 );
                arrival.setMillisecond(0);

                adt.setArrival( arrival );
                adt.setArrivalDay( BigInteger.valueOf( seconds / (3600 * 24) ) );
            }

            //c2.setTime( endDate );
            //c2.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
            seconds = seconds + currAction.getPlannedSecs();
            c2.set(op.getStartDate().getYear(), op.getStartDate().getMonth(), op.getStartDate().getDay(), seconds / 3600, seconds / 60 % 60, seconds % 60);
            c2.setTimeZone(TimeZone.getTimeZone(currAction.getTimetableObject2() != null ? currAction.getTimetableObject2().getTimeZone() : currAction.getTimetableObject().getTimeZone()));

            XMLGregorianCalendar departure = DatatypeFactory.newInstance().newXMLGregorianCalendar( c2 );
            departure.setMillisecond(0);
            
            adt.setDeparture( departure );
            adt.setDepartureDay( BigInteger.valueOf( seconds / (3600 * 24) ) );

            ott.getTimes().add( adt );
            ott.setOcpRef( ocp );

        } catch( DatatypeConfigurationException ex )
        {
        }

        tp.getOcpsTT().getOcpTT().add( ott );
    }

    private void buildRosterings( Railml data, Schedule schedule )
    {
        ERosterings rosterings = new ERosterings();
        data.getTimetable().setRosterings( rosterings );

        buildRostering( data.getTimetable().getRosterings(), schedule );
    }

    private void buildRostering( ERosterings rosterings, Schedule schedule )
    {
        ERostering rostering = new ERostering();
        // Always only one roster
        rostering.setId( "rostering1" );
        rosterings.getRostering().add( rostering );

        // No formation reference, nor depot as currently those are not recognized in TSched
        buildBlockParts( rostering, schedule );
        buildBlocks( rostering, schedule );
        buildCirculations( rostering, schedule );
    }

    private void buildBlockParts( ERostering r, Schedule s )
    {
        EBlockParts bps = new EBlockParts();
        r.setBlockParts( bps );

        List<DayType> dts = s.getDayTypes();
        for( DayType dt : dts )
        {
            PlannedServiceFilter filter = new PlannedServiceFilter();
            filter.setDayTypeFilter( dt );
            filter.setValidFilter( true );

            List<PlannedService> pss = ejbPlannedServiceFacade.findAll( filter );
            for( PlannedService ps : pss )
            {
                for( PlannedTrip pt : ps.getPlannedTrips() )
                {
                    buildBlockPart( r.getBlockParts(), ps, pt, dt );
                }
            }
        }
    }

    private void buildBlockPart( EBlockParts blockParts, PlannedService ps, PlannedTrip pt, DayType dt )
    {
        TBlockPart bp = new TBlockPart();
        bp.setId( generateBlockPartKeyFromPlannedTripId( pt, dt ) );
        bp.setTrainPartRef( ((HashMap<DayType, ETrainPart>) conversionMap.get(pt)).get(dt) );
        bp.setOperatingPeriodRef( conversionMap.get( dt ) ); // @Todo: check
        bp.setMission( "timetable" );
        bp.setFormationRef( conversionMap.get( ps.getTrainType() ) );

        // Add to xml struct
        blockParts.getBlockPart().add( bp );

        // Add generated op key to match object
        // CANT SAVE PT ITSELF AS IT IS ALREADY SAVED IN TRAINPART
        HashMap<DayType, TBlockPart> tempMap;
        if(!conversionMap.containsKey(pt.getServiceAction())) {
            tempMap = new HashMap<DayType, TBlockPart>() {{ put(dt, bp); }};
            conversionMap.put(pt.getServiceAction(), tempMap);
        }else {
            tempMap = (HashMap<DayType, TBlockPart>) conversionMap.get(pt.getServiceAction());
            tempMap.put(dt, bp);
            conversionMap.put(pt.getServiceAction(), tempMap);
            // serviceaction SHOULD BE PARENT ACTION OF TIMEDTRIP
        }
    }

    private void buildBlocks( ERostering rostering, Schedule schedule )
    {
        EBlocks blocks = new EBlocks();
        rostering.setBlocks( blocks );

        List<DayType> dayTypes = schedule.getDayTypes();
        for( DayType dayType : dayTypes )
        {
            PlannedServiceFilter filter = new PlannedServiceFilter();
            filter.setDayTypeFilter( dayType );
            filter.setValidFilter( true );

            List<PlannedService> plannedServices = ejbPlannedServiceFacade.findAll( filter );
            for( PlannedService ps : plannedServices )
            {
                // Serialize only services, which have trips
                if( !ps.getPlannedTrips().isEmpty() )
                {
                    buildBlock( rostering.getBlocks(), ps, dayType );
                }
            }
        }
    }

    private void buildBlock( EBlocks blocks, PlannedService plannedService, DayType dayType )
    {
        EBlock block = new EBlock();
        block.setId( generateBlockKeyFromPlannedServiceId( plannedService, dayType ) );
        block.setName( plannedService.getDescription() );

        // Add to data struct
        blocks.getBlock().add( block );

        // Add to conversion map
        HashMap<DayType, EBlock> tempMap;
        if(!conversionMap.containsKey(plannedService)) {
            tempMap = new HashMap<DayType, EBlock>() {{ put(dayType, block); }};
            conversionMap.put(plannedService, tempMap);
        }else {
            tempMap = (HashMap<DayType, EBlock>) conversionMap.get(plannedService);
            tempMap.put(dayType, block);
            conversionMap.put(plannedService, tempMap);
        }

        for( PlannedTrip plannedTrip : plannedService.getPlannedTrips() )
        {
            buildBlockPartSequence( block, plannedTrip, dayType );
        }
    }

    //circulations are added to make the railML file valid using RailVIVID, but we don't use it.
    private void buildCirculations( ERostering r, Schedule s )
    {
        ECirculations cs = new ECirculations();
        r.setCirculations( cs );

        for( int i = 0; i < r.getBlocks().getBlock().size() - 1; i++ )
        {
            buildCirculation( cs, r.getBlocks().getBlock().get( i ), r.getBlocks().getBlock().get( i + 1 ) );
        }
    }

    private void buildCirculation( ECirculations cs, EBlock block, EBlock nextBlock )
    {
        TCirculation c = new TCirculation();
        c.setBlockRef( block );
        c.setOperatingPeriodRef( ((TBlockPart) (block.getBlockPartSequence().get( 0 ).getBlockPartRef().get( 0 ).getRef())).getOperatingPeriodRef() );
        c.setNextBlockRef( nextBlock );
        c.setNextOperatingPeriodRef( ((TBlockPart) (nextBlock.getBlockPartSequence().get( 0 ).getBlockPartRef().get( 0 ).getRef())).getOperatingPeriodRef() );
        cs.getCirculation().add( c );
    }

    private void buildBlockPartSequence(EBlock block, PlannedTrip plannedTrip, DayType dayType)
    {
        EBlockPartSequence bps = new EBlockPartSequence();

        TripAction action = plannedTrip.getServiceAction();

        BigInteger actionSeqNo = BigInteger.valueOf( action.getSeqNo() );
        bps.setSequence( actionSeqNo );

        TBlockPartRef bpr = new TBlockPartRef();
        bpr.setRef( ((HashMap<DayType, TBlockPart>) conversionMap.get(action)).get(dayType) );
        bps.getBlockPartRef().add( bpr );

        block.getBlockPartSequence().add( bps );
    }

    private Object buildOcp( Railml data, TTObject object )
    {
        // Add item if it does not exist
        if( !conversionMap.containsKey( object ) )
        {
            EOcp o = new EOcp();
            o.setId( generateOcpKeyFromObjectId( object ) );
            o.setName( object.getDescription() );
            o.setCode( object.getScheduleName() );
            o.setNumber( object.getTTObjId().toString() );

            // Add object to map
            conversionMap.put( object, o );

            // And to railml
            data.getInfrastructure().getOperationControlPoints().getOcp().add( o );
        }

        return conversionMap.get( object );
    }

    private void buildStopDescription( EOcpTT ocp, TripAction tripAction )
    {
        EStopDescription stopDesc = new EStopDescription();
        if( tripAction instanceof ActionSchedulingStop )
        {
            stopDesc.setCommercial( Boolean.FALSE );
        } else
        {
            //Cargo or Passenger stop
            stopDesc.setCommercial( Boolean.TRUE );
            switch( tripAction.getActionType().getActionSubtype() )
            {
                case 1:
                    stopDesc.setOnOff( TOnOff.BOTH );
                    break;
                case 2:
                    stopDesc.setOnOff( TOnOff.ON );
                    break;
                default:
                    stopDesc.setOnOff( TOnOff.OFF );
                    break;
            }
        }

        ocp.setStopDescription( stopDesc );
    }
}
