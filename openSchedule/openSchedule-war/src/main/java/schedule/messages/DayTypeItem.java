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
@XmlRootElement(name = "DayType")
@XmlAccessorType(XmlAccessType.FIELD)
public class DayTypeItem extends StandardItem{
    /*@XmlType(name="DayTypeOperation")
    @XmlEnum
    public enum DayTypeOperation {
        UNKNOWN,
        MODIFY,
        CREATE,
        DELETE
    }*/


    @XmlElement(name = "Id")
    private Integer dayTypeId;
    
    @XmlAttribute(name = "DayTypeOperation")
    private Operation operation;
    
    @XmlElement(name = "SId")
    private Integer scheduleId;
    
    @XmlElement(name = "Desc")
    private String description;
    
    @XmlElement(name = "Abbr")
    private String abbr;

    public Integer getDayTypeId() {
        return dayTypeId;
    }

    public void setDayTypeId(Integer dayTypeId) {
        this.dayTypeId = dayTypeId;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public Integer getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Integer scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAbbr() {
        return abbr;
    }

    public void setAbbr(String abbr) {
        this.abbr = abbr;
    }
}
