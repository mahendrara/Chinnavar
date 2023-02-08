package com.schedule.model;

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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import com.schedule.base.model.EditableEntity;

/**
 *
 * @author spirttin
 */
@Entity
@Table(name = "traintypeproperty")
@NamedQueries({
    @NamedQuery(name = "TrainTypeProperty.findAll", query = "SELECT t FROM TrainTypeProperty t")})
public class TrainTypeProperty extends EditableEntity  implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "valueid")
    private Integer valueid;
    @JoinColumn(name="traintypeid", referencedColumnName = "traintypeid")
    @ManyToOne(optional = false)
    private TrainType traintypeid;
    @JoinColumn(name = "tripid", referencedColumnName = "tripid")
    @ManyToOne
    private Trip tripid;
    @JoinColumn(name = "ttobjid", referencedColumnName = "ttobjid")
    @ManyToOne(optional = false)
    private TTObject ttobjid;
    @Column(name = "ivalue")
    private Integer ivalue;
    @Column(name = "bvalue")
    private Boolean bvalue;
    @Size(max = 254)
    @Column(name = "svalue")
    private String svalue;
    @JoinColumn(name = "propid", referencedColumnName = "propid")
    @ManyToOne(optional = false)
    private TrainProperty propid;

    public TrainTypeProperty() {
    }
    
    public TrainTypeProperty(Integer valueid) {
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

    public TrainProperty getPropid() {
        return propid;
    }

    public void setPropid(TrainProperty propid) {
        this.propid = propid;
    }

    public TrainType getTraintypeid() {
        return traintypeid;
    }

    public void setTripid(Trip tripid) {
        this.tripid = tripid;
    }
    public Trip getTripid() {
        return tripid;
    }

    public void setTTObjId(TTObject ttobjid) {
        this.ttobjid = ttobjid;
    }
    public TTObject getTTObjId() {
        return ttobjid;
    }

    public void setTraintypeid(TrainType traintypeid) {
        this.traintypeid = traintypeid;
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
        if (!(object instanceof TrainTypeProperty)) {
            return false;
        }
        TrainTypeProperty other = (TrainTypeProperty) object;
        if ((this.valueid == null && other.valueid != null) || (this.valueid != null && !this.valueid.equals(other.valueid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "schedule.entities.TrainTypeProperty[ valueid=" + valueid + " ]";
    }
    
}

