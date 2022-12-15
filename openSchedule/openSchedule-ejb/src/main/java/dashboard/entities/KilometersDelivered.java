/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dashboard.entities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 *
 * @author jiali
 */
@Entity
@DiscriminatorValue("K")
public class KilometersDelivered extends DashboardKPI{
    
}
