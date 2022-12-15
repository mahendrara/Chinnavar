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

@XmlRootElement(name = "Trip")
@XmlAccessorType(XmlAccessType.FIELD)
class EidtPlannedTripsResponseItem{

    @XmlAttribute(name = "TrID")
    private Integer tripId;

    @XmlAttribute(name = "Success")
    protected boolean success;

    public EidtPlannedTripsResponseItem() {
    }

    public EidtPlannedTripsResponseItem(Integer tripId, boolean success) {
        this.tripId = tripId;
        this.success = success;
    }
    
    public Integer getTripId() {
        return tripId;
    }

    public void setTripId(Integer tripId) {
        this.tripId = tripId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

}