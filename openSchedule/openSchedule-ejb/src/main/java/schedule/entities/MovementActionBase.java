/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.entities;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

/**
 *
 * @author Jia Li
 */
@Entity
public abstract class MovementActionBase extends TripAction {

    @JoinColumn(name = "ttobjid3", referencedColumnName = "ttobjid")
    @ManyToOne(optional = false)
    private TTObject routeObject;

    @Transient
    private Integer signOfPlannedSecs;

    public TTObject getRouteObject() {
        return this.routeObject;  
    }

    public void setRouteObject(TTObject routeObject) {
        this.routeObject = routeObject;
    }

    @Override
    public void setPlannedDuration(Date plannedDuration) {
        super.setPlannedDuration(plannedDuration);
        if (this.signOfPlannedSecs == 0) {
            this.setPlannedSecs(0 - this.getPlannedSecs());
        }
    }

    public Integer getSignOfPlannedSecs() {
        return this.getPlannedSecs() >= 0 ? 1 : 0;
    }

    public void setSignOfPlannedSecs(Integer signOfPlannedSecs) {
        this.signOfPlannedSecs = signOfPlannedSecs;
    }

    // Overriden in actions, which inherit this
    @Override
    public void connectToCloned(TripAction tripAction) {
        super.connectToCloned(tripAction);
        this.setRouteObject(((MovementActionBase) tripAction).getRouteObject());
    }
}
