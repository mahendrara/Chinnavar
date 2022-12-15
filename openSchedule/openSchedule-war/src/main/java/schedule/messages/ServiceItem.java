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
@XmlRootElement(name = "Service")
@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceItem extends StandardItem{
    /*@XmlType(name="ServiceOperation")
    @XmlEnum
    public enum Operation {
        UNKNOWN,
        MODIFY,
        CREATE,
        DELETE
    }*/
    
    public enum ServiceType {
        UNKNOWN,
        SCHEDULED,
        PLANNED,
        DEGRADED
    }
    
    @XmlElement(name = "TripId")
    private Integer tripId;
    
    @XmlElement(name = "DayCode")
    private Integer dayCode;
    
    @XmlElement(name = "CurrentState")
    private Integer currentState;

    @XmlAttribute(name = "Operation")
    private Operation operation;

    @XmlAttribute(name = "ServiceType")
    private ServiceType serviceType;
    
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

    void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
        /*if (service instanceof ScheduledService) {
            serviceType = ServiceType.SCHEDULED;
        } else if (service instanceof PlannedService) {
            serviceType = ServiceType.PLANNED;
        } else if (service instanceof DegradedService) {
            
        }else {
            // This is errornous situation! 
            // Somebody added new service type and didn't update this.
            throw new IllegalArgumentException("Unknown service instancetype. Add it here.");
        }*/
    }
    
    public ServiceType getServiceType() {
        return serviceType;
    }
    
    public Integer getDayCode() {
        return dayCode;
    }

    public void setDayCode(Integer dayCode) {
        this.dayCode = dayCode;
    }
    public Integer getCurrentState() {
        return currentState;
    }

    public void setCurrentState(Integer currentState) {
        this.currentState = currentState;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
