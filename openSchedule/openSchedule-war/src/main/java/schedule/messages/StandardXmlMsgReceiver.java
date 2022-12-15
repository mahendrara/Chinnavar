/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.messages;

import java.io.StringReader;
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
import schedule.sessions.EmployeeFacade;
import schedule.sessions.ScheduledDayFacade;
import schedule.sessions.ScheduledServiceFacade;
import schedule.sessions.ScheduledTripFacade;

/**
 *
 * @author Jia Li
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic")
    ,
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "TMS.OpenSchedule.Changes")
    ,
    @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    ,
    @ActivationConfigProperty(propertyName = "connectionFactoryLookup", propertyValue = "jms/openSchedule-connectionFactory")
    ,
    @ActivationConfigProperty(propertyName = "clientId", propertyValue = "TMS.OpenSchedule.Changes")
    ,
    @ActivationConfigProperty(propertyName = "messageSelector", propertyValue = "rcsschema = 'RCS.E2K.TMS.SERVICE_STATE.V1' OR rcsschema = 'RCS.E2K.TMS.SCHEDULED_DAY.V1' OR rcsschema = 'RCS.E2K.TMS.EMPLOYEE_STATE.V1'")
    ,
    @ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "TMS.OpenSchedule.Changes1")
})
public class StandardXmlMsgReceiver implements MessageListener {

    static final Logger logger = Logger.getLogger("StandardXmlMsgReceiver");
    @Resource
    public MessageDrivenContext mdc;
    Unmarshaller jaxbUnmarshaller;

    @Inject
    private ScheduledServiceFacade ejbScheduledServiceFacade;
    //@Inject
    //private PlannedServiceFacade ejbPlannedServiceFacade;
    @Inject
    private ScheduledTripFacade ejbScheduledTripFacade;
    //@Inject
    //private PlannedTripFacade ejbPlannedTripFacade;
    @Inject
    private ScheduledDayFacade ejbScheduledDayFacade;
    @Inject
    private EmployeeFacade ejbEmployeeFacade;

    public StandardXmlMsgReceiver() {
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(StandardXmlMsg.class);
            jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        } catch (JAXBException ex) {
            Logger.getLogger(StandardXmlMsgReceiver.class.getName()).log(Level.SEVERE, null, ex);
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
                        "StandardXmlMsgReceiver: Message received: {0}", inMessage.getStringProperty("rcsschema"));
                logger.log(Level.INFO,
                        "StandardXmlMsgReceiver: {0}", body);
                StandardXmlMsg xmlMsg = (StandardXmlMsg) jaxbUnmarshaller.unmarshal(new StringReader(body));

                switch (xmlMsg.getXmlHeader().getSchema()) {
                    case "RCS.E2K.TMS.SERVICE_STATE.V1": {
                        ejbScheduledServiceFacade.evictAll();
                        this.ejbScheduledTripFacade.evictAll();
                        logger.log(Level.INFO, "evit all scheduled services and scheduled trips");

                    }
                    break;
                    case "RCS.E2K.TMS.SCHEDULED_DAY.V1": {
                        ejbScheduledDayFacade.evictAll();
                        logger.log(Level.INFO, "evit all scheduled days");

                    }
                    break;
                    case "RCS.E2K.TMS.EMPLOYEE_STATE.V1": {
                        ejbEmployeeFacade.evictAll();
                        logger.log(Level.INFO, "evit all employees");
                    }
                    break;

                    default:
                        logger.log(Level.WARNING,
                                "StandardXmlMsgReceiver: Unknown Message: {0}",
                                xmlMsg.getXmlHeader().getSchema());
                        break;
                }

            }
        } catch (UnmarshalException e) {
            logger.log(Level.SEVERE,
                    "StandardXmlMsgReceiver.onMessage:  {0}", e.toString());
            mdc.setRollbackOnly();
        } catch (javax.jms.JMSRuntimeException | JAXBException | JMSException | EJBException e) {
            logger.log(Level.SEVERE,
                    "StandardXmlMsgReceiver.onMessage:  {0}", e.toString());
            mdc.setRollbackOnly();
        } catch (Exception e) {
            logger.log(Level.SEVERE,
                    "StandardXmlMsgReceiver.onMessage:  {0}", e.toString());
            mdc.setRollbackOnly();
        }
    }
    
}
