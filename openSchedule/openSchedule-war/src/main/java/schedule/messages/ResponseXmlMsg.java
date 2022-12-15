/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.messages;

import java.util.LinkedList;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Jia Li
 */
@XmlRootElement(name = "rcsMsg")
@XmlAccessorType(XmlAccessType.FIELD)
public class ResponseXmlMsg {

    @XmlElement(name = "hdr")
    private StandardXmlHeader xmlHeader;

    @XmlElementWrapper(name = "data")
    @XmlElements({
        @XmlElement(name = "EditPlannedTripsResponse", type = EditPlannedTripsResponse.class),
        @XmlElement(name = "ScheduledTimetables", type = ScheduledTimetableResponse.class)
    })
    private LinkedList<ResponseItem> dataItems; 

    public StandardXmlHeader getXmlHeader() {
        return xmlHeader;
    }

    public void setXmlHeader(StandardXmlHeader xmlHeader) {
        this.xmlHeader = xmlHeader;
    }

    public LinkedList<ResponseItem> getDataItems() {
        return dataItems;
    }

    public void setDataItems(LinkedList<ResponseItem> dataItems) {
        this.dataItems = dataItems;
    }

}
