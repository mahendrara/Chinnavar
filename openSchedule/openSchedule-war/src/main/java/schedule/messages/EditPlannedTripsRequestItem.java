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

@XmlRootElement(name = "Trip")
@XmlAccessorType(XmlAccessType.FIELD)
public class EditPlannedTripsRequestItem{
    
    public EditPlannedTripsRequestItem() {
    }

    public EditPlannedTripsRequestItem(Integer tripId, Integer actionId, Integer templateId) {
        this.tripId = tripId;
        this.actionId = actionId;
        this.templateId = templateId;
    }
    
    @XmlElement(name = "TrID")
    private Integer tripId;

    @XmlElement(name = "AcID")
    private Integer actionId;
    
    @XmlElement(name = "TmplID")
    private Integer templateId;
    
    @XmlElement(name ="DwellChange")
    private Integer dwellChange;
    
    public Integer getTripId() {
        return tripId;
    }

    public void setTripId(Integer tripId) {
        this.tripId = tripId;
    }

    public Integer getActionId() {
        return actionId;
    }

    public void setActionId(Integer actionId) {
        this.actionId = actionId;
    }

    public Integer getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Integer templateId) {
        this.templateId = templateId;
    }

    public Integer getDwellChange() {
        return dwellChange;
    }

    public void setDwellChange(Integer dwellChange) {
        this.dwellChange = dwellChange;
    }
}

