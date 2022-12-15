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
@XmlRootElement(name = "ScheduledDay")
@XmlAccessorType(XmlAccessType.FIELD)
public class ScheduledDayItem extends StandardItem{
    /*@XmlType(name="ScheduledDayOperation")
    @XmlEnum
    public enum Operation {
        UNKNOWN,
        MODIFY,
        CREATE,
        DELETE,
        ARCHIVE
    }*/

    @XmlElement(name = "DayCode")
    private Integer dayCode;

    @XmlElement(name = "Archived")
    private boolean archived;
    
    @XmlElement(name = "ArcState")
    private Integer archivedState;

    @XmlElement(name = "Active")
    private boolean active;
    
    @XmlAttribute(name = "Operation")
    private Operation operation;
    
    @XmlAttribute(name = "Version")
    private Integer version;

    public Integer getDayCode() {
        return dayCode;
    }

    public void setDayCode(Integer dayCode) {
        this.dayCode = dayCode;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    public Integer getArchivedState() {
        return archivedState;
    }

    public void setArchivedState(Integer archivedState) {
        this.archivedState = archivedState;
    }

}
