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
@DiscriminatorValue("P")
public class PassengerConnection extends Connection{
    
    @Override
    public String getClassDescription() {
        return "Passenger connection";
    }
}
