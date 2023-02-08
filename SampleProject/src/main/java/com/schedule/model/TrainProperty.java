package com.schedule.model;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.schedule.base.model.Locale;

/**
 *
 * @author spirttin
 */
@Entity
@Table(name = "trainproperty")
@NamedQueries({
    @NamedQuery(name = "TrainProperty.findAll", query = "SELECT t FROM TrainProperty t")})
//@TypeDef(name="tmsValueTypeConverter", typeClass = TMSValueTypeConverter.class)
public class TrainProperty implements Serializable {

    /*public enum tmsValueType implements Serializable{
        TTOBJECT(0),
        TRIPOBJECT(1),
        INTEGER(2),
        BOOLEAN(3),
        STRING(4),
        DOUBLE(5),
        BITVECTOR(6),
        TMSENUM(7),
        AUTOBOOLEAN(8);
        
        private final int value;
        
        tmsValueType(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }*/
    
     public enum tmsValueType {
        TTOBJECT,
        TRIPOBJECT,
        INTEGER,
        BOOLEAN,
        STRING,
        DOUBLE,
        BITVECTOR,
        TMSENUM,
        AUTOBOOLEAN
    }

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "propid")
    private Integer propid;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 254)
    @Column(name = "description")
    private String description;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 254)
    @Column(name = "username")
    private String username;
    @Size(max = 254)
    @Column(name = "extname")
    private String extname;
    @Column(name = "alarmrule")
    private Integer alarmrule;
    @Column(name = "epropertytype")
    //@Enumerated(EnumType.STRING)//same name in db and enum
    //@Convert(converter = tmsValueTypeConverter.class)
    private String propertyType;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "propid")
    private Collection<TrainTypeProperty> trainTypePropertyCollection;

    @ManyToOne(optional = false)
    @JoinColumn(name = "traintypeid", referencedColumnName = "traintypeid")
    private TrainType traintypeid;

    @OneToMany
    @JoinColumn(name = "textKey", referencedColumnName = "username")
    @MapKey(name = "locale")
    private Map<Locale, TextKey> textKeys;

    public TrainProperty() {
    }

    public TrainProperty(Integer propid) {
        this.propid = propid;
    }

    /*
    public TrainProperty(Integer propid, int propertytype, String description, String username) {
        this.propid = propid;
        this.propertytype = propertytype;
        this.description = description;
        this.username = username;
    }
     */
    public Integer getPropid() {
        return propid;
    }

    public void setPropid(Integer propid) {
        this.propid = propid;
    }

    /*
    public Integer getSystempropid() {
        return systempropid;
    }

    public void setSystempropid(Integer systempropid) {
        this.systempropid = systempropid;
    }
     */
 /*
    public int getPropertytype() {
        return propertytype;
    }

    public void setPropertytype(int propertytype) {
        this.propertytype = propertytype;
    }
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public String getExtname() {
        return extname;
    }

    public void setExtname(String extname) {
        this.extname = extname;
    }

    public Integer getAlarmrule() {
        return alarmrule;
    }

    public void setAlarmrule(Integer alarmrule) {
        this.alarmrule = alarmrule;
    }

    public Collection<TrainTypeProperty> getTrainTypePropertyCollection() {
        return trainTypePropertyCollection;
    }

    public void setTrainTypePropertyCollection(Collection<TrainTypeProperty> trainTypePropertyCollection) {
        this.trainTypePropertyCollection = trainTypePropertyCollection;
    }

    public TrainType getTraintypeid() {
        return traintypeid;
    }

    public void setTraintypeid(TrainType traintypeid) {
        this.traintypeid = traintypeid;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (propid != null ? propid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TrainProperty)) {
            return false;
        }
        TrainProperty other = (TrainProperty) object;
        if ((this.propid == null && other.propid != null) || (this.propid != null && !this.propid.equals(other.propid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "schedule.entities.TrainProperty[ propid=" + propid + " ]";
    }

    public String getText(Locale locale) {
        if (textKeys.containsKey(locale)) {
            return textKeys.get(locale).getText();
        }
        return getDescription();
    }
}

