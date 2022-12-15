/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.messages;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author jia
 */
@XmlRootElement(name = "ScheduledTimetables")
@XmlAccessorType(XmlAccessType.FIELD)
public class ScheduledTimetableResponse extends ResponseItem {
    @XmlAttribute(name = "ReqID")
    private String requestId;
    
    @XmlAttribute(name = "DayCode")
    private Integer dayCode;
    
    //@XmlAttribute(name = "currentDay")
    //private Boolean currentDay;
    @XmlAttribute(name = "StartIndex")
    private Integer startIndex;
    
    @XmlAttribute(name = "TotalCount")
    private Integer totalCount;

    //@XmlElementWrapper()
    @XmlElements({
        @XmlElement(name = "t", type = ScheduledTimetableResponseItem.class)
    })
    private List<ScheduledTimetableResponseItem> items;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public List<ScheduledTimetableResponseItem> getItems() {
        return items;
    }

    public void setItems(List<ScheduledTimetableResponseItem> items) {
        this.items = items;
    }

    public Integer getDayCode() {
        return dayCode;
    }

    public void setDayCode(Integer dayCode) {
        this.dayCode = dayCode;
    }

    /*public Boolean getCurrentDay() {
        return currentDay;
    }

    public void setCurrentDay(Boolean currentDay) {
        this.currentDay = currentDay;
    }*/

    public Integer getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(Integer startIndex) {
        this.startIndex = startIndex;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }    
    
}


