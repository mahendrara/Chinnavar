/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Jia Li
 */
@XmlRootElement(name = "t")
@XmlAccessorType(XmlAccessType.FIELD)
class ScheduledTimetableResponseItem{
    @XmlElement(name = "n")
    private String service;

    @XmlElement(name = "p")
    private String platform;
    
    @XmlElement(name = "pe")
    private String platformExtName;
    
    @XmlElement(name = "arr")
    private String arrivalTime;
    
    @XmlElement(name = "dep")
    private String departureTime;
    
    @XmlElement(name = "tId")
    private Integer tripId;
    
    @XmlElement(name = "sId")
    private Integer serviceId;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getPlatformExtName() {
        return platformExtName;
    }

    public void setPlatformExtName(String platformExtName) {
        this.platformExtName = platformExtName;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public Integer getTripId() {
        return tripId;
    }

    public void setTripId(Integer tripId) {
        this.tripId = tripId;
    }

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serId) {
        this.serviceId = serId;
    }
}
