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
 * @author spirttin
 */
@XmlRootElement(name = "TypesModified")
@XmlAccessorType(XmlAccessType.FIELD)
public class TypesModifiedItem extends StandardItem {

    /*@XmlType(name = "TypesModifiedOperation")
    @XmlEnum
    public enum Operation {
        CREATE,
        MODIFY,
        DELETE
    }*/

    @XmlAttribute(name = "Operation")
    private Operation operation;

    @XmlAttribute(name = "TrainTypeId")
    private Integer trainTypeId;

    TypesModifiedItem() {
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public Integer getTrainTypeId() {
        return trainTypeId;
    }

    public void setTrainTypeId(Integer id) {
        trainTypeId = id;
    }
}
