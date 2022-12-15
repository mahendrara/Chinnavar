/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Jia Li
 */
@XmlRootElement(name = "Service")
@XmlAccessorType(XmlAccessType.FIELD)
class ServiceInServiceDutyChange extends StandardItem {

    @XmlAttribute(name = "class")
    private String objectClass;

    @XmlAttribute(name = "dc")
    private Integer dayCode;

    @XmlAttribute(name = "serdbid")
    private Integer serviceId;

    @XmlAttribute(name = "Operation")
    private Operation operation;
    
    @XmlAttribute(name = "Ver")
    private Integer version;

    public String getObjectClass() {
        return objectClass;
    }

    public void setObjectClass(String objectClass) {
        this.objectClass = objectClass;
    }

    public Integer getDayCode() {
        return dayCode;
    }

    public void setDayCode(Integer dayCode) {
        this.dayCode = dayCode;
    }

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
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
