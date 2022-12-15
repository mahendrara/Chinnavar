/*
 * To change this template, choose Tools | Templates
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

/**
 *
 * @author EBIScreen
 */
@Entity
@Table(name = "tripTypes")
public class TripType implements Serializable {
    
     public enum TripMainType {
        TRIPTYPE_UNKNOWN(0),
        COMMERCIAL_TEMPLATE(1),
        NONCOMMERCIAL_TEMPLATE(2),
        OPPORTUNITY_TEMPLATE(3),
        COMMERCIAL(4),
        NONCOMMERCIAL(5),
        OPPORTUNITY(6),
        SERVICE_TEMPLATE(7),
        SERVICE(8),
        NORMAL_DUTY(9),
        SPARE_DUTY(10),
        MOVEMENT_TRIP_TEMPLATE(11),
        MOVEMENT_TRIP(12),
        MOVEMENT_TRIP_GROUP(13);
        
        private final int value;

        private TripMainType(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return this.value;
        }
        
        public static TripMainType parse(int value) {
            TripMainType type = null; // Default
            for (TripMainType item : TripMainType.values()) {
                if (item.getValue() == value) {
                    type = item;
                    break;
                }
            }
            return type;
        }
    }
    private static final long serialVersionUID = 1L;
    @Basic(optional = false)
    @Column(name = "tripType")
    private TripMainType tripType;
    @Basic(optional = false)
    @Column(name = "tripSubtype")
    private Integer tripSubType;
    @Basic(optional = false)
    @Column(name = "combination")
    private boolean combination;
    @Basic(optional = false)
    @Column(name = "template")
    private boolean template;
    @Column(name = "totripType")
    private Integer toTripType;
    @Basic(optional = false)
    @Column(name = "description")
    private String description;
    @Id
    @Basic(optional = false)
    @Column(name = "tripTypeId")
    private Integer tripTypeId;
    @Basic(optional = false)
    @Column(name = "valid")
    private boolean valid;
    @Column(name = "userName")
    private String userName;
    @Column(name = "defaultspeed")
    private Integer defaultSpeed;
    @Column(name = "externalid")
    private String externalId;
    
    @OneToMany
    @JoinColumn(name ="textKey", referencedColumnName = "userName")
    @MapKey(name="locale")
    private Map<Locale,TextKey> textKeys;
    
//    @OneToMany(cascade = CascadeType.ALL, mappedBy = "tripType")
//    private Collection<Trip> tripCollection;
    

    public TripType() {
    }

    public TripType(Integer tripTypeId) {
        this.tripTypeId = tripTypeId;
    }
    
    public TripType(Integer tripTypeId, String description) {
        this.tripTypeId = tripTypeId;
        this.description = description;
    }

    public TripType(Integer tripTypeId, TripMainType tripType, Integer tripSubtype, boolean combination, boolean template, String description) {
        this.tripTypeId = tripTypeId;
        this.tripType = tripType;
        this.tripSubType = tripSubtype;
        this.combination = combination;
        this.template = template;
        this.description = description;
    }

    public TripMainType getTripType() {
        return tripType;
    }

    public void setTripType(TripMainType tripType) {
        this.tripType = tripType;
    }

    public int getTripSubType() {
        return tripSubType;
    }

    public void setTripSubType(int tripSubTypeId) {
        this.tripSubType = tripSubTypeId;
    }

    public boolean getCombination() {
        return combination;
    }

    public void setCombination(boolean combination) {
        this.combination = combination;
    }

    public boolean getTemplate() {
        return template;
    }

    public void setTemplate(boolean template) {
        this.template = template;
    }

    public Integer getToTripType() {
        return toTripType;
    }

    public void setToTripType(Integer toTripType) {
        this.toTripType = toTripType;
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
    
    public Integer getTripTypeId() {
        return tripTypeId;
    }

    public void setTripTypeId(Integer tripTypeId) {
        this.tripTypeId = tripTypeId;
    }

/*    public Collection<Trip> getTripCollection() {
        return tripCollection;
    }

    public void setTripCollection(Collection<Trip> tripCollection) {
        this.tripCollection = tripCollection;
    }
*/
    
    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public Integer getDefaultSpeed() {
        return defaultSpeed;
    }

    public void setDefaultSpeed(Integer defaultSpeed) {
        this.defaultSpeed = defaultSpeed;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (tripTypeId != null ? tripTypeId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TripType)) {
            return false;
        }
        TripType other = (TripType) object;
        if ((this.tripTypeId == null && other.tripTypeId != null) || (this.tripTypeId != null && !this.tripTypeId.equals(other.tripTypeId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "schedule.entities.TripType[tripTypeId=" + tripTypeId + "]";
    }

}
