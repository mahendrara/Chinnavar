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
@DiscriminatorValue(TripAction.ACTION_CREW)
public class ActionCrew extends TripAction{
    private static final long serialVersionUID = 1L;
    public ActionCrew() {

    }
    @Override
    public TripAction createDerivedInstance() {
        return new ActionCrew();
    }
    /*@Override
    public MainActionType getClassForActionType() {
        return MainActionType.CREW;
    }*/
}
