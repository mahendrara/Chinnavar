/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.entities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(TripAction.ACTION_OPTIONAL_BREAK_DUTY)
public class ActionOptionalBreakDuty extends ActionBreakDuty{

    @Override
    public TripAction createDerivedInstance() {
        return new ActionOptionalBreakDuty();
    }

//    @Override
//    public MainActionType getClassForActionType() {
//        return MainActionType.OPTIONAL_BREAK_DUTY;
//    }    
}
