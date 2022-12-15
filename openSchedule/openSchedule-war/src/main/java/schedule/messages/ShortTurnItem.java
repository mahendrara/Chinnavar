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
 * @author spirttin
 */
@XmlRootElement(name = "ShortTurn")
@XmlAccessorType(XmlAccessType.FIELD)
public class ShortTurnItem extends StandardItem{
    /*public enum ShortTurnOperation {
        UNKNOWN,
        MODIFY,
        CREATE,
        DELETE
    }*/
    
    @XmlElement(name = "Id")
    private Integer id;

    @XmlElement(name = "FromId")
    private Integer fromId;

    @XmlElement(name = "ToId")
    private Integer toId;

    @XmlElement(name = "LocationId")
    private Integer locationId;
    
    @XmlElement(name = "DestinationId")
    private Integer destinationId;
    
    @XmlElement(name = "FromCurrent")
    private boolean fromCurrent;
    
    @XmlAttribute(name = "ShortTurnOperation")
    private Operation shortTurnOperation;

    ShortTurnItem() {
    }
    
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getToId() {
        return toId;
    }
    public void setToId(Integer toId) {
        this.toId = toId;
    }

    public Integer getFromId() {
        return fromId;
    }
    public void setFromId(Integer fromId) {
        this.fromId = fromId;
    }

    public Integer getLocationId() {
        return locationId;
    }
    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }

    public Integer getDestinationId() {
        return destinationId;
    }
    public void setDestinationId(Integer destinationId) {
        this.destinationId = destinationId;
    }

    public boolean getFromCurrent() {
        return fromCurrent;
    }
    public void setFromCurrent(boolean fromCurrent) {
        this.fromCurrent = fromCurrent;
    }
    
    public Operation getOperation() {
        return shortTurnOperation;
    }
    public void setOperation(Operation shortTurnOperation) {
        this.shortTurnOperation = shortTurnOperation;
    }
}
