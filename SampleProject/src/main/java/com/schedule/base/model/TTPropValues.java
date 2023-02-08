package com.schedule.base.model;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import com.schedule.model.TTObject;
import com.schedule.model.Trip;

/**
 *
 * @author spirttin
 */
@Entity
@Table(name = "ttpropvalues")
@XmlRootElement
/*@NamedQueries({
    @NamedQuery(name = "Ttpropvalues.findAll", query = "SELECT t FROM Ttpropvalues t")})
*/
public class TTPropValues implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "valueid")
    private Integer valueid;
    @Column(name = "ivalue")
    private Integer ivalue;
    @Column(name = "bvalue")
    private Boolean bvalue;
    @Size(max = 254)
    @Column(name = "svalue")
    private String svalue;
    @JoinColumn(name = "propid", referencedColumnName = "propid")
    @ManyToOne(optional = false)
    private TTProperty propid;
    @JoinColumn(name = "ttobjid", referencedColumnName = "ttobjid")
    @ManyToOne(optional = false)
    private TTObject ttobjid;
    @JoinColumn(name = "refttobjid", referencedColumnName = "ttobjid")
    @ManyToOne
    private TTObject refttobjid;
    @JoinColumn(name = "tripid", referencedColumnName = "tripid")
    @ManyToOne
    private Trip tripid;

    public TTPropValues() {
    }

    public TTPropValues(Integer valueid) {
        this.valueid = valueid;
    }

    public Integer getValueid() {
        return valueid;
    }

    public void setValueid(Integer valueid) {
        this.valueid = valueid;
    }

    public Integer getIvalue() {
        return ivalue;
    }

    public void setIvalue(Integer ivalue) {
        this.ivalue = ivalue;
    }

    public Boolean getBvalue() {
        return bvalue;
    }

    public void setBvalue(Boolean bvalue) {
        this.bvalue = bvalue;
    }

    public String getSvalue() {
        return svalue;
    }

    public void setSvalue(String svalue) {
        this.svalue = svalue;
    }

    public TTProperty getPropid() {
        return propid;
    }

    public void setPropid(TTProperty propid) {
        this.propid = propid;
    }

    public TTObject getTtobjid() {
        return ttobjid;
    }

    public void setTtobjid(TTObject ttobjid) {
        this.ttobjid = ttobjid;
    }

    public TTObject getRefttobjid() {
        return refttobjid;
    }

    public void setRefttobjid(TTObject refttobjid) {
        this.refttobjid = refttobjid;
    }

    public Trip getTripid() {
        return tripid;
    }

    public void setTripid(Trip tripid) {
        this.tripid = tripid;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (valueid != null ? valueid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TTPropValues)) {
            return false;
        }
        TTPropValues other = (TTPropValues) object;
        if ((this.valueid == null && other.valueid != null) || (this.valueid != null && !this.valueid.equals(other.valueid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "schedule.entities.Ttpropvalues[ valueid=" + valueid + " ]";
    }
    
}

