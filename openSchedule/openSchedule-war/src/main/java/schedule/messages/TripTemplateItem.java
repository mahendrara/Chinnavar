/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Jia Li
 */
@XmlRootElement(name = "TripTemplate")
@XmlAccessorType(XmlAccessType.FIELD)
public class TripTemplateItem extends StandardItem {

    /*@XmlType(name = "TripTemplateOperation")
    @XmlEnum
    public enum Operation {
        UNKNOWN,
        MODIFY,
        CREATE,
        DELETE
    }*/

    @XmlElement(name = "TripId")
    private Integer tripId;

    @XmlAttribute(name = "TripTemplateOperation")
    private Operation operation;

    @XmlElement(name = "Version")
    private Integer version;

    public Integer getTripId() {
        return tripId;
    }

    public void setTripId(Integer tripId) {
        this.tripId = tripId;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
