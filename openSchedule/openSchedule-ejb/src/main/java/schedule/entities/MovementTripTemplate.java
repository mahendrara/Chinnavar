/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule.entities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 *
 * @author Jia Li
 */
@Entity
@DiscriminatorValue(Trip.MOVEMENT_TRIP_TEMPLATE)
public class MovementTripTemplate extends MovementTripBase{
    
    public enum MovementTripTemplateSubType {
        TRIPTYPE_UNKNOWN(0),
        DEFAULT_ALL(1),
        FORMATION(2);
        
        private final int value;

        private MovementTripTemplateSubType(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return this.value;
        }
        
        public static MovementTripTemplateSubType parse(int value) {
            MovementTripTemplateSubType type = null; // Default
            for (MovementTripTemplateSubType item : MovementTripTemplateSubType.values()) {
                if (item.getValue() == value) {
                    type = item;
                    break;
                }
            }
            return type;
        }
    }
    
    @JoinColumn(name = "plannednextobj", referencedColumnName = "ttobjid")
    @ManyToOne(optional = false)
    private TTObject plannedNextObj;

    public TTObject getPlannedNextObj() {
        return plannedNextObj;
    }

    public void setPlannedNextObj(TTObject plannedNextObj) {
        this.plannedNextObj = plannedNextObj;
    }
}
