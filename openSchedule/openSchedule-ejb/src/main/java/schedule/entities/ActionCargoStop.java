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
@DiscriminatorValue(TripAction.ACTION_CARGO_STOP)
public class ActionCargoStop extends TripActionBase{
    private static final long serialVersionUID = 1L;
    public ActionCargoStop() {

    }
    @Override
    public TripAction createDerivedInstance() {
        return new ActionCargoStop();
    }
    /*@Override
    public MainActionType getClassForActionType() {
        return MainActionType.CARGO_STOP;
    }*/

}
