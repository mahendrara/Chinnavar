/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trafficnotification.servermsgs;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author jiali
 */
@XmlRootElement(name = "Notify")
@XmlAccessorType(XmlAccessType.FIELD)
public class TrafficNotificationItem {

    @XmlAttribute(name = "id")
    private long id;
    @XmlAttribute(name = "typeid")
    private int typeId;
    @XmlAttribute(name = "typeclass")
    private int typeClass;
    @XmlElement(name = "ServiceName")
    private String service;
    @XmlElement(name = "Time")
    private long utcTimeSec;
    @XmlElement(name = "AtrNodeId")
    private long atrNodeId;
    @XmlElement(name = "LineId")
    private long lineId;
    @XmlElement(name = "Text")
    private String text;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }
    
    public int getTypeClass() {
        return typeClass;
    }

    public void setTypeClass(int typeClass) {
        this.typeClass = typeClass;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public long getUtcTimeSec() {
        return utcTimeSec;
    }

    public void setUtcTimeSec(long utcTimeSec) {
        this.utcTimeSec = utcTimeSec;
    }

    public long getAtrNodeId() {
        return atrNodeId;
    }

    public void setAtrNodeId(long atrNodeId) {
        this.atrNodeId = atrNodeId;
    }

    public long getLineId() {
        return lineId;
    }

    public void setLineId(long lineId) {
        this.lineId = lineId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
