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
@XmlRootElement(name = "ScheduledTTReq")
@XmlAccessorType(XmlAccessType.FIELD)
public class ScheduledTimetableRequest extends RequestItem{
    @XmlElement(name = "ReqID")
    private String requestId;
    
    @XmlElement(name = "DayCode")
    private Integer dayCode;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Integer getDayCode() {
        return dayCode;
    }

    public void setDayCode(Integer dayCode) {
        this.dayCode = dayCode;
    }
}
