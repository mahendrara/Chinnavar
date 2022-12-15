/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.entities;

import java.io.Serializable;
import java.util.Map;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author 
 */
@Entity
@Table(name = "tripusertype")
public class TripUserType implements Serializable {

    public enum State {

        UNKNOWN(0),
        SYSTEM(1),
        USER(2),
        SYSTEM_USER(3);
        
                
        private final int value;
        State(int value) {
            this.value = value;
        }
        public int getValue() {
            return this.value;
        }
    }

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "tripusertypeid")
    private Integer tripUserTypeId;
    @Size(max = 254)
    @Column(name = "state")
    private String state;
    @Size(max = 254)
    @Column(name = "description")
    private String description;
    @Column(name = "userName")
    private String userName;

    @OneToMany
    @JoinColumn(name = "textKey", referencedColumnName = "userName")
    @MapKey(name = "locale")
    private Map<Locale, TextKey> textKeys;

    public TripUserType() {
    }

    public Integer getTripUserTypeId() {
        return tripUserTypeId;
    }

    public void setTripUserTypeId(Integer tripUserTypeId) {
        this.tripUserTypeId = tripUserTypeId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getText(Locale locale) {
        if (textKeys.containsKey(locale)) {
            return textKeys.get(locale).getText();
        }
        return getDescription();
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (tripUserTypeId != null ? tripUserTypeId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TripUserType)) {
            return false;
        }
        TripUserType other = (TripUserType) object;
        if ((this.tripUserTypeId == null && other.tripUserTypeId != null) || (this.tripUserTypeId != null && !this.tripUserTypeId.equals(other.tripUserTypeId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "schedule.entities.TripUserType[ tripUserTypeId=" + tripUserTypeId + " ]";
    }

}
