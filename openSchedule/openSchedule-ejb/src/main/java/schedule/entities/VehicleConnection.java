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
@DiscriminatorValue("V")
public class VehicleConnection extends Connection{

    public String getClassDescription() {
        return "Vehicle connection";
    }
}
