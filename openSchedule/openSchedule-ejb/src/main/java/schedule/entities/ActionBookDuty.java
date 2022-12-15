
package schedule.entities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 *
 * @author luyang
 */
@Entity
@DiscriminatorValue(TripAction.ACTION_BOOK_DUTY)
public class ActionBookDuty extends ActionDuty{      
    public ActionBookDuty(){
       
    }
    
    @Override
    public TripAction createDerivedInstance() {
        return new ActionBookDuty();
    }

//    @Override
//    public MainActionType getClassForActionType() {        
//        return MainActionType.BOOK_DUTY;        
//    }    

}
