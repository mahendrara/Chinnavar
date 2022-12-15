/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.messages;

import java.util.List;
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
@XmlRootElement(name = "ServiceDutyChange")
@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceDutyChangeXmlMsgData extends StandardItem {

    @XmlElementWrapper(name = "Services")
    @XmlElements({
        @XmlElement(name = "Service", type = ServiceInServiceDutyChange.class)
    })
    private List<ServiceInServiceDutyChange> services;
    
    @XmlElementWrapper(name = "Duties")
    @XmlElements({
        @XmlElement(name = "Duty", type = DutyInServiceDutyChange.class)
    })
    private List<DutyInServiceDutyChange> duties;

    public List<ServiceInServiceDutyChange> getServices() {
        return services;
    }

    public void setServices(List<ServiceInServiceDutyChange> services) {
        this.services = services;
    }

    public List<DutyInServiceDutyChange> getDuties() {
        return duties;
    }

    public void setDuties(List<DutyInServiceDutyChange> duties) {
        this.duties = duties;
    }
}
