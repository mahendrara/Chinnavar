package schedule.messages;

import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.Destination;
import javax.jms.JMSConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.Topic;
import javax.jms.Queue;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import schedule.entities.BasicTrip;
import schedule.entities.DayType;
import schedule.entities.Duty;
import schedule.entities.Employee;
import schedule.entities.MovementTrip;
import schedule.entities.MovementTripTemplate;
import schedule.entities.ScheduledDay;
import schedule.entities.ScheduledDuty;
import schedule.entities.ScheduledService;
import schedule.entities.ServiceTrip;
import schedule.entities.ShortTurn;
import schedule.entities.TimeBlock;
import schedule.entities.TrainConsist;
import schedule.entities.TrainType;
import schedule.entities.Trip;
import schedule.uiclasses.util.JsfUtil;
import schedule.uiclasses.util.UiText;

/**
 *
 * @author Jia Li
 */
@Stateless
public class XmlMessageSender
{

    // Comes from Glassfish Resources->JMS Resources->DestinationResources
    @Resource(mappedName = "jms/openSchedule-changeTopic")
    private Destination destination;

    @Inject
    @JMSConnectionFactory("jms/openSchedule-connectionFactory")
    private JMSContext context;

    //JMSProducer producer;
    private final String sender = "RCS.E2K.TMS.OpenSchedule";
    static final Logger logger = Logger.getLogger( "XMLMessageSender" );

    @Inject
    private UiText uiText;

    //@Inject
    //private TimetableMsgHandler ejbTimeTableMsgHandler;
    private String getCurrentUTC()
    {
        TimeZone timeZone = TimeZone.getTimeZone( "UTC" );
        DateFormat dateFormat = new SimpleDateFormat( "YYYYMMdd'T'HHmmss" );
        dateFormat.setTimeZone( timeZone );
        return dateFormat.format( new Date() );
    }

    @PostConstruct
    private void initDestination()
    {
        try
        {
            // DO IN RETARDED WAY
            // Since 'dest' is normal Topic/Queue
            // and producer expects HornetQTopic/HornetQQueue, create new topic/queu
            // through context, which creates HornetQTopic/Queue and everything works 
            // fine after that
            // TODO: Convert initial @Resource parameter for 'destination' so, that
            // it constructs HornetQTopic right away and this kludge can be removed
            if( destination instanceof Topic )
            {
                String name = ((Topic) destination).getTopicName();
                destination = context.createTopic( name );
            } else if( destination instanceof Queue )
            {
                String name = ((Queue) destination).getQueueName();
                destination = context.createQueue( name );
            }

            /*
             * producer = context.createProducer(); CountDownLatch latch = new
             * CountDownLatch(1); producer.setAsync(new TTCompletionListener(latch));
             */
        } catch( Exception ex )
        {
            Logger.getLogger( XmlMessageSender.class.getName() ).log( Level.SEVERE, null, ex );
        }
    }

    private boolean sendStandardXmlMsg( String schema, LinkedList<StandardItem> items )
    {

        JAXBContext jaxbContext;
        try
        {
            // Convert items to XmlMsg
            StandardXmlMsgData msgData = new StandardXmlMsgData();
            msgData.setStandardItems( items );

            // Build header
            StandardXmlHeader msgHeader = new StandardXmlHeader();
            msgHeader.setSchema( schema );
            msgHeader.setSender( sender );
            msgHeader.setUtc( getCurrentUTC() );

            // Combine header and data to actual message
            StandardXmlMsg msg = new StandardXmlMsg();
            msg.setXmlHeader( msgHeader );
            msg.setXmlData( msgData );

            StringWriter writer = new StringWriter();
            jaxbContext = JAXBContext.newInstance( StandardXmlMsg.class );
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.marshal( msg, writer );
            logger.log( Level.INFO, writer.toString() );

            // Check if context is valid
            JMSProducer producer = context.createProducer();

            producer.setProperty( "rcsschema", schema );
            CountDownLatch latch = new CountDownLatch( 1 );
            producer.setAsync( new TTCompletionListener( latch ) );
            producer.send( destination, writer.toString() );

        } catch( Exception ex )
        {
            logger.log( Level.SEVERE, "Sending " + schema + " Failed!", ex );
            JsfUtil.addWarnMessage( schema + " " + uiText.get( "Warn_NotificationFail" ) );
            
            return false;
        }

        return true;

    }

    private boolean sendSingleXmlMsg( String schema, StandardItem item )
    {

        JAXBContext jaxbContext;
        try
        {
            List<StandardItem> items = new ArrayList<>();
            items.add( item );

            // Build header
            StandardXmlHeader msgHeader = new StandardXmlHeader();
            msgHeader.setSchema( schema );
            msgHeader.setSender( sender );
            msgHeader.setUtc( getCurrentUTC() );

            // Combine header and data to actual message
            SingleXmlMsg msg = new SingleXmlMsg();
            msg.setXmlHeader( msgHeader );
            msg.setXmlData( items );

            StringWriter writer = new StringWriter();
            jaxbContext = JAXBContext.newInstance( SingleXmlMsg.class );
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.marshal( msg, writer );
            logger.log( Level.INFO, writer.toString() );

            // Check if context is valid
            JMSProducer producer = context.createProducer();

            producer.setProperty( "rcsschema", schema );
            CountDownLatch latch = new CountDownLatch( 1 );
            producer.setAsync( new TTCompletionListener( latch ) );
            producer.send( destination, writer.toString() );
        } catch( Exception ex )
        {
            logger.log( Level.SEVERE, "Sending " + schema + " Failed!", ex );
            JsfUtil.addWarnMessage( schema + " " + uiText.get( "Warn_NotificationFail" ) );
            
            return false;
        }

        return true;
    }

    private boolean sendResponseXmlMsg( String schema, String replyTo, LinkedList<ResponseItem> responseDataItems, String requestIdInHeader )
    {

        // Convert items to XmlMsg
        JAXBContext jaxbContext;
        try
        {
            // Build header
            StandardXmlHeader msgHeader = new StandardXmlHeader();
            msgHeader.setSchema( schema );
            msgHeader.setSender( sender );
            msgHeader.setUtc( getCurrentUTC() );
            msgHeader.setReplyTo( replyTo );

            // Combine header and data to actual message
            ResponseXmlMsg msg = new ResponseXmlMsg();
            msg.setXmlHeader( msgHeader );
            msg.setDataItems( responseDataItems );

            StringWriter writer = new StringWriter();
            jaxbContext = JAXBContext.newInstance( ResponseXmlMsg.class );
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.marshal( msg, writer );
            logger.log( Level.INFO, writer.toString() );

            // Check if context is valid
            JMSProducer producer = context.createProducer();

            producer.setProperty( "rcsschema", schema );
            if( requestIdInHeader != null )
            {
                producer.setProperty( "requestId", requestIdInHeader );
            }
            CountDownLatch latch = new CountDownLatch( 1 );
            producer.setAsync( new TTCompletionListener( latch ) );
            producer.send( destination, writer.toString() );

        } catch( Exception ex )
        {
            logger.log( Level.SEVERE, "Sending " + schema + " Failed!", ex );
            return false;
        }

        return true;
    }

    private boolean sendRequestXmlMsg( Destination dest, String schema, LinkedList<RequestItem> requestDataItems )
    {

        // Convert items to XmlMsg
        JAXBContext jaxbContext;
        try
        {
            // Build header
            StandardXmlHeader msgHeader = new StandardXmlHeader();
            msgHeader.setSchema( schema );
            msgHeader.setSender( sender );
            msgHeader.setUtc( getCurrentUTC() );

            // Combine header and data to actual message
            RequestXmlMsg msg = new RequestXmlMsg();
            msg.setXmlHeader( msgHeader );
            msg.setDataItems( requestDataItems );

            StringWriter writer = new StringWriter();
            jaxbContext = JAXBContext.newInstance( RequestXmlMsg.class );
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.marshal( msg, writer );
            logger.log( Level.INFO, writer.toString() );

            // Check if context is valid
            JMSProducer producer = context.createProducer();
            producer.setProperty( "rcsschema", schema );
            CountDownLatch latch = new CountDownLatch( 1 );
            producer.setAsync( new TTCompletionListener( latch ) );
            producer.send( dest, writer.toString() );

        } catch( Exception ex )
        {
            logger.log( Level.SEVERE, "Sending " + schema + " Failed!", ex );
            JsfUtil.addWarnMessage( schema + " " + uiText.get( "Warn_NotificationFail" ) );
            
            return false;
        }

        return true;
    }

    private boolean sendStandardItemMsg( String schema, StandardItem item )
    {
        // Create message item and add it to list
        LinkedList<StandardItem> items = new LinkedList<>();
        items.add( item );
        return sendStandardXmlMsg( schema, items );
    }

    public boolean sendTripTemplateMsg( BasicTrip basicTrip, Operation operation )
    {
        // Defines
        String schema = "RCS.E2K.TMS.TripTemplateModified.V1";

        // Item creation
        TripTemplateItem item = new TripTemplateItem();
        item.setTripId( basicTrip.getTripId() );
        item.setVersion( basicTrip.getVersion() );
        item.setOperation( operation );

        // Sending
        return sendStandardItemMsg( schema, item );
    }

    public boolean sendTimeBlockMsg( TimeBlock timeBlock, Operation operation )
    {
        String schema = "RCS.E2K.TMS.TimeBlock.V1";

        TimeBlockItem item = new TimeBlockItem();
        item.setBlockId( timeBlock.getBlockId() );
        item.setDayTypeId( timeBlock.getDayType().getDayTypeId() );

        item.setOperation( operation );

        return sendStandardItemMsg( schema, item );
    }

    public boolean sendTimeBlockMsg( List<TimeBlock> timeBlocks, Operation operation )
    {
        String schema = "RCS.E2K.TMS.TimeBlock.V1";

        LinkedList<StandardItem> list = new LinkedList<>();
        if( timeBlocks != null )
        {
            Iterator<TimeBlock> it = timeBlocks.iterator();
            TimeBlock timeBlock;
            while( it.hasNext() )
            {
                timeBlock = it.next();
                TimeBlockItem item = new TimeBlockItem();
                item.setBlockId( timeBlock.getBlockId() );
                item.setDayTypeId( timeBlock.getDayType().getDayTypeId() );

                item.setOperation( operation );
                list.add( item );
            }
        }

        return sendStandardXmlMsg( schema, list );
    }

    public boolean sendDayTypeMsg( DayType dayType, Operation operation )
    {
        String schema = "RCS.E2K.TMS.DayTypeModified.V1";

        DayTypeItem item = new DayTypeItem();
        item.setDayTypeId( dayType.getDayTypeId() );
        item.setDescription( dayType.getDescription() );
        item.setAbbr( dayType.getAbbr() );
        item.setScheduleId( dayType.getScheduleParent().getScheduleId() );
        item.setOperation( operation );

        return sendStandardItemMsg( schema, item );
    }

    public boolean sendDayTypeMsg( List<DayType> dayTypes, Operation operation )
    {
        String schema = "RCS.E2K.TMS.DayTypeModified.V1";

        LinkedList<StandardItem> items = new LinkedList<>();

        Iterator<DayType> it = dayTypes.iterator();
        DayType dayType;
        while( it.hasNext() )
        {
            DayTypeItem item = new DayTypeItem();
            dayType = it.next();
            item.setDayTypeId( dayType.getDayTypeId() );
            item.setDescription( dayType.getDescription() );
            item.setAbbr( dayType.getAbbr() );
            item.setScheduleId( dayType.getScheduleParent().getScheduleId() );
            item.setOperation( operation );
            items.add( item );
        }

        return sendStandardXmlMsg( schema, items );
    }

    public boolean sendMovementTripTemplateMsg( MovementTripTemplate movementTripTemplate, Operation operation )
    {
        // Defines
        String schema = "RCS.E2K.TMS.MovementTripTemplate.V1";

        // Item creation
        MovementTripTemplateItem item = new MovementTripTemplateItem();
        item.setTripId( movementTripTemplate.getTripId() );
        item.setOperation( operation );

        // Sending
        return sendStandardItemMsg( schema, item );
    }

    public boolean sendMovementTripMsg( MovementTrip movementTrip, Operation operation )
    {
        // Defines
        String schema = "RCS.E2K.TMS.MovementTrip.V1";

        // Item creation
        MovementTripItem item = new MovementTripItem();
        item.setTripId( movementTrip.getTripId() );
        item.setOperation( operation );

        // Sending
        return sendStandardItemMsg( schema, item );
    }

    public boolean sendScheduledDayMsg( ScheduledDay scheduledDay, Operation operation )
    {
        // Defines
        String schema = "RCS.E2K.TMS.ScheduledDay.V1";

        // Item creation
        ScheduledDayItem item = new ScheduledDayItem();
        item.setDayCode( scheduledDay.getScheduledDayCode() );
        item.setArchived( scheduledDay.getArchived() );
        item.setActive( scheduledDay.isActive() );
        item.setOperation( operation );
        item.setVersion( scheduledDay.getVersion() );
        // Sending
        return sendStandardItemMsg( schema, item );
    }

    public boolean sendScheduledDayMsg( List<ScheduledDay> scheduledDays, Operation operation )
    {
        // Defines
        String schema = "RCS.E2K.TMS.ScheduledDay.V1";

        LinkedList<StandardItem> items = new LinkedList<>();

        Iterator<ScheduledDay> scheduledDayIterator = scheduledDays.iterator();
        ScheduledDay scheduledDay;
        while( scheduledDayIterator.hasNext() )
        {
            ScheduledDayItem item = new ScheduledDayItem();
            scheduledDay = scheduledDayIterator.next();
            item.setDayCode( scheduledDay.getScheduledDayCode() );
            item.setArchived( scheduledDay.getArchived() );
            item.setArchivedState( scheduledDay.getArchivedState() );
            item.setActive( scheduledDay.isActive() );
            item.setOperation( operation );
            item.setVersion( scheduledDay.getVersion() );
            items.add( item );
        }

        // Sending
        return sendStandardXmlMsg( schema, items );
    }

    public boolean sendEmployeeMsg( Employee emp, Operation operation )
    {
        // Defines
        String schema = "RCS.E2K.TMS.Employee.V1";

        // Item creation
        EmployeeItem item = new EmployeeItem();
        item.setAvailability( emp.getTTObjectState().getStateId() );
        item.setCustomerId( emp.getCustomerId() );
        item.setFirstName( emp.getFirstname() );
        item.setId( emp.getTTObjId() );
        item.setLastName( emp.getLastname() );
        item.setOperation( operation );
        item.setUserName( emp.getDescription() ); // Employees 'username' is in description field

        // Sending
        return sendStandardItemMsg( schema, item );
    }

    public boolean sendShortTurnMsg( ShortTurn st, Operation operation )
    {
        // Defines
        String schema = "RCS.E2K.TMS.ShortTurn.V1";

        // Item creation
        ShortTurnItem item = new ShortTurnItem();
        item.setDestinationId( st.getDestinationId().getTTObjId() );
        item.setFromCurrent( st.getFromCurrent() );
        item.setFromId( st.getFromId().getTTObjId() );
        item.setId( st.getId() );
        item.setLocationId( st.getLocationId().getTTObjId() );
        item.setOperation( operation );
        item.setToId( st.getToId().getTTObjId() );

        // Sending
        return sendStandardItemMsg( schema, item );
    }

    public boolean sendMaintenanceMsg( MaintenanceItem.Operation operation )
    {
        // Defines
        String schema = "RCS.E2K.TMS.Maintenance.V1";

        // Item creation
        MaintenanceItem item = new MaintenanceItem();
        item.setOperation( operation );

        // Sending
        return sendStandardItemMsg( schema, item );
    }

    public boolean sendTypesModifiedMsg( TrainType tt, Operation operation )
    {
        // Defines
        String schema = "RCS.E2K.TMS.TypesModified.V1";

        // Item creation
        TypesModifiedItem item = new TypesModifiedItem();
        item.setOperation( operation );
        item.setTrainTypeId( tt.getTrainTypeId() );

        // Sending
        return sendStandardItemMsg( schema, item );
    }

    public boolean sendTrainConsistsModifiedMsg( TrainConsist tc, Operation operation )
    {
        // Defines
        String schema = "RCS.E2K.TMS.TrainConsistsModified.V1";

        // Item creation
        TrainConsistsModifiedItem item = new TrainConsistsModifiedItem();
        item.setOperation( operation );
        item.setTrainConsistId( tc.getConsistId() );

        // Sending
        return sendStandardItemMsg( schema, item );
    }

    public boolean sendPlannedTripResponseMsg( String requestIdInHeader, String replyTo, Integer requestId, boolean isSuccess, Integer serviceId )
    {
        String schema = "RCS.E2K.TMS.EDIT_PLANNED_TRIPS_RESPONSE.V1";
        LinkedList<ResponseItem> list = new LinkedList<>();
        EditPlannedTripsResponse msgData = new EditPlannedTripsResponse();

        LinkedList<EidtPlannedTripsResponseItem> items = new LinkedList<>();
        items.add( new EidtPlannedTripsResponseItem( serviceId, isSuccess ) );
        msgData.setItems( items );
        msgData.setRequestId( requestId );
        list.add( msgData );

        return sendResponseXmlMsg( schema, replyTo, list, requestIdInHeader );
    }

    public boolean sendScheduledTimetableMsg( String replyTo, String requestId, LinkedList<ScheduledTimetableResponseItem> items, Integer dayCode, boolean isCurrentDay )
    {
        String schema = "RCS.E2K.TMS.SCHEDULED_TIMETABLE.V1";

        int totalCount = items.size();
        int countPerMsg = 6000;
        String confCount = ResourceBundle.getBundle( "Bundle" ).getString( "MAX_STOP_SCHEDULED_TIMETABLE_V1" );
        try
        {
            int conf = Integer.parseInt( confCount );
            countPerMsg = conf;
        } catch( NumberFormatException ex )
        {
            logger.log( Level.INFO, "parsing max count fails" );
            logger.log( Level.SEVERE, ex.getMessage() );
        }
        for( int i = 0; i <= totalCount / countPerMsg; i++ )
        {
            LinkedList<ResponseItem> list = new LinkedList<>();
            ScheduledTimetableResponse msgData = new ScheduledTimetableResponse();
            int lastIndex = countPerMsg * (i + 1);
            if( lastIndex > totalCount )
            {
                lastIndex = totalCount;
            }
            msgData.setItems( items.subList( i * countPerMsg, lastIndex ) );
            msgData.setRequestId( requestId );
            msgData.setDayCode( dayCode );
            msgData.setStartIndex( i * countPerMsg );
            msgData.setTotalCount( totalCount );
            list.add( msgData );

            sendResponseXmlMsg( schema, replyTo, list, null );
        }

        return true;
    }

    public boolean sendServiceDutyChangeMsg( List<? extends ServiceTrip> services, List<? extends Duty> duties )
    {
        // Defines
        String schema = "RCS.E2K.TMS.ServiceDutyChange.V1";

        List<DutyInServiceDutyChange> items = new ArrayList<>();

        if( duties != null )
        {
            Iterator<? extends Duty> iterator = duties.iterator();
            Duty dutyTrip;
            while( iterator.hasNext() )
            {
                dutyTrip = iterator.next();
                DutyInServiceDutyChange duty = constructDuty( dutyTrip );
                items.add( duty );
            }
        }

        List<ServiceInServiceDutyChange> items2 = new ArrayList<>();

        if( services != null )
        {
            Iterator<? extends ServiceTrip> iterator2 = services.iterator();
            ServiceTrip serviceTrip;
            while( iterator2.hasNext() )
            {
                serviceTrip = iterator2.next();
                ServiceInServiceDutyChange service = constructService( serviceTrip );
                items2.add( service );
            }
        }

        ServiceDutyChangeXmlMsgData item = new ServiceDutyChangeXmlMsgData();
        item.setDuties( items );
        item.setServices( items2 );

        return this.sendSingleXmlMsg( schema, item );
    }

    public boolean sendServiceDutyChangeMsg( ServiceTrip service, List<Duty> duties )
    {
        List<ServiceTrip> list = new ArrayList<>();
        list.add( service );
        return this.sendServiceDutyChangeMsg( list, duties );
    }

    public boolean sendServiceChangeMsg( ServiceTrip serviceTrip, Operation operation )
    {
        // Defines
        String schema = "RCS.E2K.TMS.ServiceDutyChange.V1";

        // Item creation
        ServiceDutyChangeXmlMsgData xmlData = new ServiceDutyChangeXmlMsgData();
        xmlData.setDuties( null );
        List<ServiceInServiceDutyChange> serviceList = new ArrayList<>();
        if( serviceTrip != null )
        {
            ServiceInServiceDutyChange service = constructService( serviceTrip );
            serviceList.add( service );
        }
        xmlData.setServices( serviceList );

        return this.sendSingleXmlMsg( schema, xmlData );
    }

    private ServiceInServiceDutyChange constructService( ServiceTrip serviceTrip )
    {
        ServiceInServiceDutyChange service = new ServiceInServiceDutyChange();

        if( serviceTrip instanceof ScheduledService )
        {
            service.setObjectClass( Trip.SCHEDULED_SERVICE );
            if( ((ScheduledService) serviceTrip).getDay() != null )
            {
                service.setDayCode( ((ScheduledService) serviceTrip).getDay().getScheduledDayCode() );
            }
        } else
        {
            service.setObjectClass( Trip.PLANNED_SERVICE );
        }

        service.setServiceId( serviceTrip.getTripId() );
        service.setOperation( serviceTrip.isCreating() ? Operation.CREATE : (serviceTrip.isRemoving() ? Operation.DELETE : Operation.MODIFY) );
        service.setVersion( serviceTrip.getVersion() );
        return service;
    }

    private DutyInServiceDutyChange constructDuty( Duty dutyTrip )
    {
        DutyInServiceDutyChange duty = new DutyInServiceDutyChange();

        if( dutyTrip instanceof ScheduledDuty )
        {
            duty.setObjectClass( Trip.SCHEDULED_DUTY );
            if( ((ScheduledDuty) dutyTrip).getDay() != null )
            {
                duty.setDayCode( ((ScheduledDuty) dutyTrip).getDay().getScheduledDayCode() );
            }
        } else
        {
            duty.setObjectClass( Trip.PLANNED_DUTY );
        }

        duty.setDutyId( dutyTrip.getTripId() );
        //modified duty might does not have editing state
        duty.setOperation( dutyTrip.isCreating() ? Operation.CREATE : (dutyTrip.isRemoving() ? Operation.DELETE : Operation.MODIFY) );
        duty.setVersion( dutyTrip.getVersion() );
        return duty;
    }

    public boolean sendDutyChangeMsg( Duty dutyTrip, Operation operation )
    {
        // Defines
        String schema = "RCS.E2K.TMS.ServiceDutyChange.V1";

        // Item creation
        ServiceDutyChangeXmlMsgData xmlData = new ServiceDutyChangeXmlMsgData();
        xmlData.setDuties( null );
        ArrayList<DutyInServiceDutyChange> dutyList = new ArrayList<>();
        if( dutyTrip != null )
        {
            DutyInServiceDutyChange duty = constructDuty( dutyTrip );
            dutyList.add( duty );
        }
        xmlData.setDuties( dutyList );

        return this.sendSingleXmlMsg( schema, xmlData );
    }

}
