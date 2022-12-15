/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule.entities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 *
 * @author Jia Li
 */
@Entity
@DiscriminatorValue("R")
public class TTRoute extends TTObject{
     public enum TTRouteSubTypeEnum {
        ROUTE_AP_TIMING(9);
        
        private final int value;

        private TTRouteSubTypeEnum(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return this.value;
        }
    }
}
