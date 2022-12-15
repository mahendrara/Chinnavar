/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule.entities;

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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author spirttin
 */
@Entity
@Table(name = "ttproperty")
@XmlRootElement
public class TTProperty implements Serializable {
    
    public enum TTPropertyItemTypeEnum {
        FIRSTNAME(1),
        LASTNAME(2);
        
        private final int dbCode;
        public int getDBCode() {
            return dbCode;
        }
        TTPropertyItemTypeEnum(int dbCode) {
            this.dbCode = dbCode;
        }
    }

    public enum TTPropertyDataTypeEnum {
        TTOBJECT(1),
        TRIPOBJECT(2),
        INTEGER(3),
        BOOLEAN(4),
        STRING(5);
        
        private final int dbCode;
        public int getDBCode() {
            return dbCode;
        }
        TTPropertyDataTypeEnum(int dbCode) {
            this.dbCode = dbCode;
        }
    }
        
    private static final long serialVersionUID = 1L;

    @Basic(optional = false)
    @NotNull
    @Column(name = "propertytype")
    private int propertytype;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "propid")
    private Collection<TTPropValues> ttpropvaluesCollection;
    
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "propid")
    private Integer propid;
    @Column(name = "systempropid")
    private Integer systempropid;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 254)
    @Column(name = "description")
    private String description;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 254)
    @Column(name = "username")
    private String userName;
    @JoinColumn(name = "ttobjtypeid", referencedColumnName = "ttobjtypeid")
    @ManyToOne(optional = false)
    private TTObjectType ttobjtypeid;

    @OneToMany
    @JoinColumn(name = "textKey", referencedColumnName = "userName")
    @MapKey(name = "locale")
    private Map<Locale, TextKey> textKeys;
    
    public TTProperty() {
    }

    public TTProperty(Integer propid) {
        this.propid = propid;
    }

    public TTProperty(Integer propid, int propertytype, String description, String userName) {
        this.propid = propid;
        this.propertytype = propertytype;
        this.description = description;
        this.userName = userName;
    }

    public Integer getPropid() {
        return propid;
    }

    public void setPropid(Integer propid) {
        this.propid = propid;
    }

    public Integer getSystempropid() {
        return systempropid;
    }

    public void setSystempropid(Integer systempropid) {
        this.systempropid = systempropid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public TTObjectType getTtobjtypeid() {
        return ttobjtypeid;
    }

    public void setTtobjtypeid(TTObjectType ttobjtypeid) {
        this.ttobjtypeid = ttobjtypeid;
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
        if (!(object instanceof TTProperty)) {
            return false;
        }
        TTProperty other = (TTProperty) object;
        if ((this.propid == null && other.propid != null) || (this.propid != null && !this.propid.equals(other.propid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "schedule.entities.Ttproperty[ propid=" + propid + " ]";
    }

    public int getPropertytype() {
        return propertytype;
    }

    public void setPropertytype(int propertytype) {
        this.propertytype = propertytype;
    }

    @XmlTransient
    public Collection<TTPropValues> getTtpropvaluesCollection() {
        return ttpropvaluesCollection;
    }

    public void setTtpropvaluesCollection(Collection<TTPropValues> ttpropvaluesCollection) {
        this.ttpropvaluesCollection = ttpropvaluesCollection;
    }
    
    public String getText(Locale locale) {
        if (textKeys.containsKey(locale)) {
            return textKeys.get(locale).getText();
        }
        return getDescription();
    }

}
