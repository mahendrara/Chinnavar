/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule.entities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 *
 * @author EBIScreen
 */
@Entity
@DiscriminatorValue(TripAction.ACTION_TRAIN_FORMATION)
public class ActionTrainFormation extends ServiceActionBase{
    private static final long serialVersionUID = 1L;
    
    public enum ActionTrainFormationTypeEnum {
        START_RUN(1),
        COMPOSITION(2),
        DECOMPOSTION(3),
        END_RUN(4);
        
        private final int value;

        ActionTrainFormationTypeEnum(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return this.value;
        }
    }
    
     @JoinColumn(name = "reftripid", referencedColumnName = "tripid")
     @ManyToOne(optional = false)
     private MovementTrip refTrip;
    
    public ActionTrainFormation() {

    }

    public MovementTrip getRefTrip() {
        return refTrip;
    }

    public void setRefTrip(MovementTrip refTrip) {
        this.refTrip = refTrip;
    }
    
    
    @Override
    public TripAction createDerivedInstance() {
        return new ActionTrainFormation();
    }
    
    @Override
    public void cloneDataToNonPersited(TripAction target) {
        super.cloneDataToNonPersited(target);
        ((ActionTrainFormation) target).setRefTrip(this.getRefTrip());
    }
}
