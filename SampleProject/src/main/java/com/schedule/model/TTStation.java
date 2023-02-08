package com.schedule.model;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 *
 * @author EBIScreen
 */
@Entity
@DiscriminatorValue("S")
public class TTStation extends TTObject{
    public String getShortUIName() {
        
        return this.getScheduleName();
    }

    public TTStation(Integer ttObjId, String description) {
        super(ttObjId, description);
    }

    public TTStation() {
    }
}
