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

@XmlRootElement(name = "data")
@XmlAccessorType(XmlAccessType.FIELD)
public class StandardXmlMsgData {

    @XmlElementWrapper(name = "Items")
    @XmlElements({
        @XmlElement(name = "ScheduledDay", type = ScheduledDayItem.class),
        @XmlElement(name = "Service", type = ServiceItem.class),
        @XmlElement(name = "MovementTripTemplate", type = MovementTripTemplateItem.class),
        @XmlElement(name = "Employee", type = EmployeeItem.class),
        @XmlElement(name = "Maintenance", type = MaintenanceItem.class),
        @XmlElement(name = "ShortTurn", type = ShortTurnItem.class),
        @XmlElement(name = "TypesModified", type = TypesModifiedItem.class),
        @XmlElement(name = "TripTemplate", type = TripTemplateItem.class),
        @XmlElement(name = "DayType", type = DayTypeItem.class),
        @XmlElement(name = "TimeBlock", type = TimeBlockItem.class),
        @XmlElement(name = "TrainConsistsModified", type = TrainConsistsModifiedItem.class),
        @XmlElement(name = "MovementTrip", type = MovementTripItem.class)
    })
    private LinkedList<StandardItem> items;

    public LinkedList<StandardItem> getStandardItems() {
        return items;
    }

    public void setStandardItems(LinkedList<StandardItem> items) {
        this.items = items;
    }
}
