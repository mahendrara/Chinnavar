/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule.entities;

import java.util.ArrayList;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 *
 * @author EBIScreen
 */
@Entity
@DiscriminatorValue("E")
public class Employee extends TTObject{

    public Employee() {
    }
    
    /**
     * Sets Last name to Employee
     * @param sn Last name to be inserted
     */
    public void setLastname(String sn) {
        if (this.getTtpropvaluesCollection() == null)
            this.setTtpropvaluesCollection(new ArrayList<TTPropValues>());

        // Check if property already exists
        for(TTPropValues x : this.getTtpropvaluesCollection())
        {
            if (x.getPropid().getPropid() == TTProperty.TTPropertyItemTypeEnum.LASTNAME.getDBCode())
            {
                x.setSvalue(sn);
                return;
            }
        }
        // Add new property
        TTPropValues val = new TTPropValues();
        val.setTtobjid(this);
        
        // Find Lastname property from facade
        TTObjectType ot = this.getTTObjectType();
        for (TTProperty p : ot.getTTPropertys()) {
            if (p.getPropid() == TTProperty.TTPropertyItemTypeEnum.LASTNAME.getDBCode()) {
                val.setPropid(p);
                break;
            }
        }
        val.setSvalue(sn);
        
        this.getTtpropvaluesCollection().add(val);
    }      
    public String getLastname() {
        if (this.getTtpropvaluesCollection() == null)
            this.setTtpropvaluesCollection(new ArrayList<TTPropValues>());

        for(TTPropValues x : this.getTtpropvaluesCollection())
        {
            if (x.getPropid().getPropid() == TTProperty.TTPropertyItemTypeEnum.LASTNAME.getDBCode())
            {
                return x.getSvalue();
            }
        }
        return "";
    }

    public void setFirstname(String fn) {
        if (this.getTtpropvaluesCollection() == null)
            this.setTtpropvaluesCollection(new ArrayList<TTPropValues>());

        // Check if property already exists
        for(TTPropValues x : this.getTtpropvaluesCollection())
        {
            if (x.getPropid().getPropid() == TTProperty.TTPropertyItemTypeEnum.FIRSTNAME.getDBCode())
            {
                x.setSvalue(fn);
                return;
            }
        }
        // Add new property
        TTPropValues val = new TTPropValues();
        val.setTtobjid(this);
        
        // Find Lastname property from facade
        TTObjectType ot = this.getTTObjectType();
        for (TTProperty p : ot.getTTPropertys()) {
            if (p.getPropid() == TTProperty.TTPropertyItemTypeEnum.FIRSTNAME.getDBCode()) {
                val.setPropid(p);
                break;
            }
        }
        val.setSvalue(fn);
        
        this.getTtpropvaluesCollection().add(val);
    }
    public String getFirstname() {
        if (this.getTtpropvaluesCollection() == null)
            this.setTtpropvaluesCollection(new ArrayList<TTPropValues>());

        for(TTPropValues x : this.getTtpropvaluesCollection())
        {
            if (x.getPropid().getPropid() == TTProperty.TTPropertyItemTypeEnum.FIRSTNAME.getDBCode())
            {
                return x.getSvalue();
            }
        }
        return "";
    }
    
    public void setCustomerId(String customerId) {
        this.setScheduleName(customerId);
    }
    public String getCustomerId() {
        return this.getScheduleName();
    }
    
    public void setDriverName(String driverName) {
        this.setDescription(driverName);
    }
    public String getDriverName() {
        return this.getDescription();
    }
}