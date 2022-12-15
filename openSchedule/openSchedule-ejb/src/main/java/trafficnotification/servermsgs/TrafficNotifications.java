/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trafficnotification.servermsgs;

import java.util.LinkedList;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author jiali
 */
 

@XmlRootElement(name="RCS.FI.TMS.NotificationList")
@XmlAccessorType(XmlAccessType.FIELD)
public class TrafficNotifications {
    public enum NotificationType
    {
        unknown,
        alarm,
        warning,
        notification        
    }
 
    @XmlElementWrapper(name="NotifyList")
    @XmlElement(name="Notify")
    private LinkedList<TrafficNotificationItem> trafficNotificationList;
    
    @XmlElement(name="Hdr")
    private StandardXMLHdr xmlHeader;

    //@XmlElementWrapper(name="Hdr")
    
    public StandardXMLHdr getXmlHeader() {
        return xmlHeader;
    }

    public void setXmlHeader(StandardXMLHdr xmlHeader) {
        this.xmlHeader = xmlHeader;
    }

    
    public LinkedList<TrafficNotificationItem> getTrafficNotificationList() {
        return trafficNotificationList;
    }

    public void setTrafficNotificationList(LinkedList<TrafficNotificationItem> trafficNotificationList) {
        this.trafficNotificationList = trafficNotificationList;
    }
    
}
