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
@DiscriminatorValue(TripAction.ACTION_PASS_OBJECT)
public class ActionPassObject extends TripActionBase{
        
    public enum ActionPassObjectTypeEnum {
        PASS_OBJECT_NO_PLANNED_SPEED(1),
        PASS_OBJECT_WITH_PLANNED_SPEED(2);
        
        private final int value;

        ActionPassObjectTypeEnum(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return this.value;
        }
    }
        
    private static final long serialVersionUID = 1L;
    public ActionPassObject() {

    }
    @Override
    public TripAction createDerivedInstance() {
        return new ActionPassObject();
    }
    /*@Override
    public MainActionType getClassForActionType() {
        return MainActionType.PASS_OBJECT;
    }*/
}
