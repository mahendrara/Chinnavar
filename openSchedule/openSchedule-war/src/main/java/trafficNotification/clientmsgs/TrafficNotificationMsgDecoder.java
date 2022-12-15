/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trafficNotification.clientmsgs;

import dashboard.clientmsgs.DashboardMsg;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;
import trafficnotification.servermsgs.TrafficNotifications;

/**
 *
 * @author jiali
 */
public class TrafficNotificationMsgDecoder implements Decoder.Text<TrafficNotifications> {
    @Override
    public boolean willDecode(String s) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void init(EndpointConfig config) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void destroy() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public TrafficNotifications decode(String s) throws DecodeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
