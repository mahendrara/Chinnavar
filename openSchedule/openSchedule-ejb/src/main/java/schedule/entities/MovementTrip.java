/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.entities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 *
 * @author Jia Li
 */
@Entity
@DiscriminatorValue(Trip.MOVEMENT_TRIP)
public class MovementTrip extends MovementTripBase {
    
    public enum MovementTripSubType {
        TRIPTYPE_UNKNOWN(0),
        PLANNED(1),
        SCHEDULED(2),
        DEFAULT(3),
        ALTERNATIVE(4),
        FORMATION(5);
        
        private final int value;

        private MovementTripSubType(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return this.value;
        }
        
        public static MovementTripSubType parse(int value) {
            MovementTripSubType type = null; // Default
            for (MovementTripSubType item : MovementTripSubType.values()) {
                if (item.getValue() == value) {
                    type = item;
                    break;
                }
            }
            return type;
        }
    }
/*
    @OneToOne(mappedBy = "refTrip")
    private ActionMovementTrip refTrip;

    public ActionMovementTrip getRefTrip() {
        return refTrip;
    }

    public void setRefTrip(ActionMovementTrip refTrip) {
        this.refTrip = refTrip;
    }
*/
}
