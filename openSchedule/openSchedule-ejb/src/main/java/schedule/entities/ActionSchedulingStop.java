/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule.entities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 *
 * @author EBIScreen
 */
@Entity
@DiscriminatorValue(TripAction.ACTION_SCHEDULING_STOP)
public class ActionSchedulingStop extends TripActionBase{
    
    public enum ActionSchedulingStopTypeEnum {
        SCHEDULING_STOP_TRAIN_WAITING(1),
        SCHEDULING_STOP_TRAIN_WAITING_INJECTION(2),
        SCHEDULING_STOP_TRAIN_WAITING_TURING(3);
        
        private final int value;

        ActionSchedulingStopTypeEnum(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return this.value;
        }
    }
    
    private static final long serialVersionUID = 1L;
    public ActionSchedulingStop() {

    }
    @Override
    public TripAction createDerivedInstance() {
        return new ActionSchedulingStop();
    }
    /*@Override
    public MainActionType getClassForActionType() {
        return MainActionType.SCHEDULING_STOP;
    }*/
}
