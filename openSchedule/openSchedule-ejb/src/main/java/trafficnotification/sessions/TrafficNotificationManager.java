/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trafficnotification.sessions;

import java.util.LinkedList;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.event.Observes;
//import trafficnotification.mdbs.WSJMSTrafficNotificationMsg;
import trafficnotification.servermsgs.TrafficNotificationItem;
import trafficnotification.servermsgs.TrafficNotifications;

/**
 *
 * @author jiali
 */
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@Lock(LockType.READ)
@Singleton
public class TrafficNotificationManager {

    private LinkedList<TrafficNotificationItem> trafficNotificationList;

    public TrafficNotificationManager() {
        this.trafficNotificationList = new LinkedList<>();
    }

    @Lock(LockType.WRITE)
    public void AddTrafficNotifications(@Observes /*@WSJMSTrafficNotificationMsg*/ TrafficNotifications items) {
        for(TrafficNotificationItem item : items.getTrafficNotificationList() ) {
            trafficNotificationList.add(item);
        }
        //this.trafficNotificationList.add(item);
    }
    
    @Lock(LockType.WRITE)
    public void clearTrafficNotificationItems() {
        this.trafficNotificationList.clear();
    }

    /*public void AddTrafficNotificationItems() {
        
     }*/
    public LinkedList<TrafficNotificationItem> getTrafficNotificationList() {
        return trafficNotificationList;
    }
}
