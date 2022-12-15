///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package trafficnotification.mdbs;
//
//import java.io.StringReader;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import javax.annotation.PostConstruct;
//import javax.annotation.Resource;
//import javax.ejb.ActivationConfigProperty;
//import javax.ejb.MessageDriven;
//import javax.ejb.MessageDrivenContext;
//import javax.enterprise.event.Event;
//import javax.inject.Inject;
//import javax.jms.JMSException;
//import javax.jms.Message;
//import javax.jms.MessageListener;
//import javax.jms.TextMessage;
//import javax.xml.bind.JAXBContext;
//import javax.xml.bind.JAXBException;
//import javax.xml.bind.UnmarshalException;
//import javax.xml.bind.Unmarshaller;
//import trafficnotification.servermsgs.TrafficNotifications;
//
///**
// *
// * @author jiali
// */
////@JMSDestinationDefinition(name = "TMS.TrafficNotify", interfaceName = "javax.jms.Topic", resourceAdapter = "jmsra", destinationName = "TMS.TrafficNotify")
//@MessageDriven(activationConfig = {
//    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
//    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "TMS.TrafficNotify"),
//    //@ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable"),
//    @ActivationConfigProperty(propertyName = "clientId", propertyValue = "TMS.TrafficNotify"),
//    @ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "TMS.TrafficNotify")
//},
//        name = "TrafficNotificationMDB")
//public class TrafficNotificationMDB implements MessageListener {
//
//    static final Logger logger = Logger.getLogger("TrafficNotificationMDB");
//    @Resource
//    public MessageDrivenContext mdc;
//    Unmarshaller jaxbUnmarshaller;
//    //@Inject
//    //TrafficNotificationManager trafficNotificationManager;
//    @Inject
//    //@WSJMSTrafficNotificationMsg
//    Event<TrafficNotifications> jmsEvent;
//
//    public TrafficNotificationMDB() {
//        JAXBContext jaxbContext;
//        try {
//            jaxbContext = JAXBContext.newInstance(TrafficNotifications.class);
//            jaxbUnmarshaller = jaxbContext.createUnmarshaller();
//        } catch (JAXBException ex) {
//            Logger.getLogger(TrafficNotificationMDB.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//
//    @PostConstruct
//    void initialize() {
//    }
//
//    @Override
//    public void onMessage(Message inMessage) {
//        try {
//            if (inMessage instanceof TextMessage) {
//
//                String body = inMessage.getBody(String.class);
//                logger.log(Level.INFO,
//                        "TrafficNotificationMDB: Message received: {0}", body);
//
//
//                StringReader reader = new StringReader(body);
//                TrafficNotifications trafficNotifications = (TrafficNotifications) jaxbUnmarshaller.unmarshal(reader);
//
//                //logger.log(Level.INFO, "after jaxb: {0}", trafficNotifications.toString());
//                jmsEvent.fire(trafficNotifications);
//                /*for (TrafficNotificationItem item : trafficNotifications.getTrafficNotificationList()) {
//                 trafficNotificationManager.AddTrafficNotificationItem(item);
//                 }*/
//
//            } else {
//                logger.log(Level.WARNING,
//                        "TrafficNotificationMDB: Message of wrong type: {0}",
//                        inMessage.getClass().getName());
//            }
//        } catch (JMSException | UnmarshalException e) {
//            logger.log(Level.SEVERE,
//                    "TrafficNotificationMDB.onMessage:  {0}", e.toString());
//            mdc.setRollbackOnly();
//        } catch (JAXBException e) {
//            logger.log(Level.SEVERE,
//                    "TrafficNotificationMDB.onMessage:  {0}", e.toString());
//            mdc.setRollbackOnly();
//        }
//    }
//}
