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
@DiscriminatorValue(TripAction.ACTION_GLUE)
public class ActionGlue extends ServiceActionBase {

    private static final long serialVersionUID = 1L;

    public enum ActionGlueTypeEnum {
        GLUE_START_TRIP(1),
        GLUE_STOP_TRIP(2),
        GLUE_WAIT(3),
        GLUE_WAIT_AND_TURN(4);
        
        private final int value;

        private ActionGlueTypeEnum(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return this.value;
        }
    }

    public ActionGlue() {

    }

    @Override
    public TripAction createDerivedInstance() {
        return new ActionGlue();
    }
    /*@Override
     public MainActionType getClassForActionType() {
     return MainActionType.GLUE;
     }*/
}
