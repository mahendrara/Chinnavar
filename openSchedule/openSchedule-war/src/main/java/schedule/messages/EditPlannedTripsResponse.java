/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.messages;

import java.util.LinkedList;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Jia Li
 */
@XmlRootElement(name = "EditPlannedTripsResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class EditPlannedTripsResponse extends ResponseItem {
    @XmlAttribute(name = "ReqID")
    private Integer requestId;

    @XmlElementWrapper(name = "Trips")
    @XmlElements({
        @XmlElement(name = "Trip", type = EidtPlannedTripsResponseItem.class)
    })
    private LinkedList<EidtPlannedTripsResponseItem> items;

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public LinkedList<EidtPlannedTripsResponseItem> getItems() {
        return items;
    }

    public void setItems(LinkedList<EidtPlannedTripsResponseItem> items) {
        this.items = items;
    }

}


