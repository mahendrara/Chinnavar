/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author spirttin
 */
@XmlRootElement(name = "Maintenance")
@XmlAccessorType(XmlAccessType.FIELD)
public class MaintenanceItem extends StandardItem{
    @XmlType(name = "MaintenanceOperation")
    @XmlEnum
    public enum Operation {
        CLEARDRIVINGTIMESTATISTIC,
        SAVEDRIVINGTIMESTATISTIC,
        TEST_JMS_LINK
    }

    @XmlAttribute(name = "Operation")
    private Operation operation;

    
    MaintenanceItem() {
    }
    
    public Operation getOperation() {
        return operation;
    }
    
    public void setOperation(Operation operation) {
        this.operation = operation;
    }
}
