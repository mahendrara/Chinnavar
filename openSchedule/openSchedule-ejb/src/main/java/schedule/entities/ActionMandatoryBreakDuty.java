/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.entities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(TripAction.ACTION_MANDATORY_BREAK_DUTY)
public class ActionMandatoryBreakDuty extends ActionBreakDuty{

    @Override
    public TripAction createDerivedInstance() {
        return new ActionMandatoryBreakDuty();
    }

//    @Override
//    public MainActionType getClassForActionType() {
//        return MainActionType.MANDATORY_BREAK_DUTY;
//    }    
}
