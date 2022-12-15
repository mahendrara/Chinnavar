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
@DiscriminatorValue(TripAction.ACTION_TRAIN_INTERNAL)
public class ActionTrainInternal extends MovementActionBase{
    private static final long serialVersionUID = 1L;
                
    public ActionTrainInternal() {

    }
    @Override
    public TripAction createDerivedInstance() {
        return new ActionTrainInternal();
    }
}
