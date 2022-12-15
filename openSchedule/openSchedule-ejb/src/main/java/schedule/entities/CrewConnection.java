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
@DiscriminatorValue("C")
public class CrewConnection extends Connection{
    
    @Override
    public String getClassDescription() {
        return "Crew connection";
    }
}
