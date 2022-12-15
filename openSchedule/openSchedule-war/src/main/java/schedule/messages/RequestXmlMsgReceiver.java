/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.messages;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author Jia Li
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "TMS.OpenSchedule.Changes"),
    @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable"),
    @ActivationConfigProperty(propertyName = "connectionFactoryLookup", propertyValue = "jms/openSchedule-connectionFactory"),
    @ActivationConfigProperty(propertyName = "clientId", propertyValue = "TMS.OpenSchedule.Changes"),
    @ActivationConfigProperty(propertyName = "messageSelector", propertyValue = "rcsschema = 'RCS.E2K.TMS.EDIT_PLANNED_TRIPS_REQUEST.V1' OR rcsschema = 'RCS.E2K.TMS.SCHEDULED_TIMETABLE_REQUEST.V1'"),
    @ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "TMS.OpenSchedule.Changes2")
})
public class RequestXmlMsgReceiver implements MessageListener {

    static final Logger logger = Logger.getLogger("RequestXmlMsgReceiver");
    @Resource
    public MessageDrivenContext mdc;
    Unmarshaller jaxbUnmarshaller;

    @Inject
    private TimetableMsgHandler ejbTimeTableMsgHandler;

    public RequestXmlMsgReceiver() {
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(RequestXmlMsg.class);
            jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        } catch (JAXBException ex) {
            Logger.getLogger(RequestXmlMsgReceiver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @PostConstruct
    void initialize() {
    }

    @Override
    public void onMessage(Message inMessage) {
        try {
            if (inMessage instanceof TextMessage) {

                String body = inMessage.getBody(String.class);
                logger.log(Level.INFO,
                        "RequestXmlMsgReceiver: Message received: {0}", inMessage.getStringProperty("rcsschema"));
                logger.log(Level.INFO,
                        "RequestXmlMsgReceiver: {0}", body);
                    String requestId = inMessage.getStringProperty("requesterid");
                    RequestXmlMsg xmlMsg = (RequestXmlMsg) (jaxbUnmarshaller.unmarshal(new StringReader(body)));
                    LinkedList<RequestItem> xmlItems = xmlMsg.getDataItems();
                    switch (xmlMsg.getXmlHeader().getSchema()) {
                        case "RCS.E2K.TMS.EDIT_PLANNED_TRIPS_REQUEST.V1":

                            ejbTimeTableMsgHandler.handleEditPlannedTripsRequest(requestId, xmlItems, xmlMsg.getXmlHeader().getReplyTo());
                            //logger.log(Level.INFO, xmlMsg.getXmlHeader().getSchema());
                            break;
                        case "RCS.E2K.TMS.SCHEDULED_TIMETABLE_REQUEST.V1":
                            ejbTimeTableMsgHandler.handleScheduledTimetableRequest(xmlItems, xmlMsg.getXmlHeader().getReplyTo());
                            //logger.log(Level.INFO, xmlMsg.getXmlHeader().getSchema());
                            break;
                        default:
                            logger.log(Level.WARNING,
                                    "RequestXmlMsgReceiver: Unknown Message: {0}",
                                    xmlMsg.getXmlHeader().getSchema());
                            break;

                    }
                }
            
        } catch (UnmarshalException e) {
            logger.log(Level.SEVERE,
                    "RequestXmlMsgReceiver.onMessage:  {0}", e.toString());
            mdc.setRollbackOnly();
        } catch (javax.jms.JMSRuntimeException | JAXBException | JMSException | EJBException e) {
            logger.log(Level.SEVERE,
                    "RequestXmlMsgReceiver.onMessage:  {0}", e.toString());
            mdc.setRollbackOnly();
        } catch (Exception e) {
            logger.log(Level.SEVERE,
                    "RequestXmlMsgReceiver.onMessage:  {0}", e.toString());
            mdc.setRollbackOnly();
        }
    }
}

