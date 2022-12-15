/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trafficNotification.endpoint;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import trafficNotification.clientmsgs.TrafficNotificationMsgDecoder;
import trafficNotification.clientmsgs.TrafficNotificationMsgEncoder;
//import trafficnotification.mdbs.WSJMSTrafficNotificationMsg;
import trafficnotification.servermsgs.TrafficNotifications;

/**
 *
 * @author jiali
 */
@ServerEndpoint(value = "/trafficnotificationendpoint", encoders = {TrafficNotificationMsgEncoder.class}, decoders = {TrafficNotificationMsgDecoder.class})
public class TrafficNotificationWSEndPoint {

    private static Set<Session> sessions = Collections.synchronizedSet(new HashSet<Session>());
    static final Logger logger = Logger.getLogger("TrafficNotificationWSEndPoint");
    @Inject
    trafficnotification.sessions.TrafficNotificationManager trafficNotificationManager;

    public synchronized void onJMSMessage(@Observes /*@WSJMSTrafficNotificationMsg*/ TrafficNotifications msg) {
        //logger.log(Level.INFO, "Got JMS Message at WebSocket!");
        try {
            for (Session session : sessions) {
                if (session.isOpen()) {
                    session.getBasicRemote().sendObject(msg);
                }
            }
        } catch (IOException | EncodeException e) {
            logger.log(Level.SEVERE, null, e);
        }
    }

    @OnMessage
    public void onMessage(Session session, TrafficNotifications msg) {
    }

    @OnOpen
    public void onOpen(Session session) {
        //session.getUserProperties().put(null, this)
        System.out.println("onOpen: " + session.getId());
        //synchronized (sessions) {
        sessions.add(session);
        //}
        try {
            session.getBasicRemote().sendObject(trafficNotificationManager);
        } catch (IOException | EncodeException e) {
            logger.log(Level.SEVERE, null, e);
        }
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        System.out.println("onClose: " + session.getId());
    }

    @OnError
    public void onError(Session session, Throwable t) {
        //queue.remove(session);
        System.out.println("onError: " + session.getId() + ": " + t.toString());
    }
}
