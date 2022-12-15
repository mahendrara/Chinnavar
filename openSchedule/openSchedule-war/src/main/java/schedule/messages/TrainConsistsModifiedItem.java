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
@XmlRootElement(name = "TrainConsistsModified")
@XmlAccessorType(XmlAccessType.FIELD)
public class TrainConsistsModifiedItem extends StandardItem {

    /*@XmlType(name = "TrainConsistsModifiedOperation")
    @XmlEnum
    public enum Operation {
        CREATE,
        MODIFY,
        DELETE
    }*/

    @XmlAttribute(name = "Operation")
    private Operation operation;

    @XmlAttribute(name = "TrainConsistId")
    private Integer trainConsistId;

    TrainConsistsModifiedItem() {
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public Integer getTrainConsistId() {
        return trainConsistId;
    }

    public void setTrainConsistId(Integer id) {
        trainConsistId = id;
    }
}
