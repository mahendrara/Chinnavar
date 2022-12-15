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
@XmlRootElement(name = "TimeBlock")
@XmlAccessorType(XmlAccessType.FIELD)
public class TimeBlockItem extends StandardItem{
    /*@XmlType(name="TimeBlockOperation")
    @XmlEnum
    public enum TimeBlockOperation {
        UNKNOWN,
        MODIFY,
        CREATE,
        DELETE
    }*/

    @XmlElement(name = "BlockId")
    private Integer blockId;

    @XmlElement(name = "DayTypeId")
    private Integer dayTypeId;
    
    @XmlAttribute(name = "TimeBlockOperation")
    private Operation operation;

    public Integer getBlockId() {
        return blockId;
    }

    public void setBlockId(Integer blockId) {
        this.blockId = blockId;
    }

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
}
