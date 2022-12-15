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
@DiscriminatorValue(TripAction.ACTION_PASSENGER_STOP)
public class ActionPassengerStop extends TripActionBase{
    private static final long serialVersionUID = 1L;
    
    public enum ActionPassengerStopTypeEnum {
        TAKE_LEAVE_PASSENGER(1),
        TAKE_PASSENGER(2),
        LEAVE_PASSENGER(3);
        
        private final int value;

        ActionPassengerStopTypeEnum(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return this.value;
        }
    }
    
    public ActionPassengerStop() {

    }
    @Override
    public TripAction createDerivedInstance() {
        return new ActionPassengerStop();
    }
    /*@Override
    public MainActionType getClassForActionType() {
        return MainActionType.PASSENGER_STOP;
    }*/
    
    /*@Override
    public Integer getDefaultMinSecs() {

        return 15;
    }
    @Override
    public Integer getDefaultPlanSecs() {
        return 25;
    }*/
}
