/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.entities;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.Size;

/**
 *
 * @author Jia Li
 */
@Entity
@Table(name = "ttobjecttypes")
public class TTObjectType implements Serializable {
    public enum TTObjectTypeEnum {
        PLATFORM(1),
        JUNCTION(2),
        SIDING(3),
        STAFF_PLATFORM(4),
        REVERSING_BERTH(5),
        DEPOT_ROAD(6),
        THROUGH_LINE(7),
        DEPOT(8),
        WAITING_POINT(9),
        INJECTION_POINT(10),
        STATION(11),
        BRANCH_STATION(12),
        DEPOT_STATION(13),
        AREA_LINE(14),
        TERMINUS(15),
        TERMINUS_STATION(16),
        TRAIN_POSITION_SUBSYSTEM(17),
        ROUTE(18),
        SHUNTING_ROUTE(19),
        ACTION_POINT(20),
        COMMAND(21),
        DRIVER(22),
        AUXILARY_EMPLOYEE(23),
        GENERIC_LOCATION_PARENT(24),
        TRAIN_COMMAND(25),
        DEPOT_STABLING_TRACK(26),
        LOCATION_GROUP(27);
        
        private final int dbCode;
        public int getDBCode() {
            return dbCode;
        }
        TTObjectTypeEnum(int dbCode) {
            this.dbCode = dbCode;
        }
    }
        
        
    @Size(max = 254)
    @Column(name = "username")
    private String userName;

    private static final long serialVersionUID = 1L;
    @Size(max = 254)
    @Column(name = "description")
    private String description;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ttobjtypeid")
    private Integer ttObjTypeId;

    @ManyToMany(mappedBy = "ttObjectTypes")
    private List<ActionType> actionTypes;

    @OneToMany(mappedBy = "ttObjectType")
    @OrderBy(value="description")
    private List<TTObject> ttObjects;

    @OneToMany(mappedBy = "ttobjtypeid")
    @OrderBy(value="propid")
    private List<TTProperty> ttPropertys;
    
    @OneToMany
    @JoinColumn(name ="textKey", referencedColumnName = "userName")
    @MapKey(name="locale")
    private Map<Locale,TextKey> textKeys;
    
    public TTObjectType() {
    }

    public TTObjectType(Integer ttObjTypeId) {
        this.ttObjTypeId = ttObjTypeId;
    }

    public TTObjectType(Integer ttObjTypeId, String description) {
        this.description = description;
        this.ttObjTypeId = ttObjTypeId;
    }

    public String getText(Locale locale) {
        if(textKeys.containsKey(locale))
            return textKeys.get(locale).getText();
        return getDescription();
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    
    public Integer getTTObjTypeId() {
        return ttObjTypeId;
    }

    public void setTTObjTypeId(Integer ttObjTypeId) {
        this.ttObjTypeId = ttObjTypeId;
    }

    public List<ActionType> getActionTypes() {
        return actionTypes;
    }

    public void setActionTypes(List<ActionType> actionTypes) {
        this.actionTypes = actionTypes;
    }

    public List<TTObject> getTTObjects() {
        return ttObjects;
    }

    public void setTTObject(List<TTObject> ttObjects) {
        this.ttObjects = ttObjects;
    }
    public List<TTProperty> getTTPropertys() {
        return ttPropertys;
    }

    public void setTTPropertys(List<TTProperty> ttPropertys) {
        this.ttPropertys = ttPropertys;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (ttObjTypeId != null ? ttObjTypeId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TTObjectType)) {
            return false;
        }
        TTObjectType other = (TTObjectType) object;
        if ((this.ttObjTypeId == null && other.ttObjTypeId != null) || (this.ttObjTypeId != null && !this.ttObjTypeId.equals(other.ttObjTypeId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "schedule.entities.TTobjectTypes[ttobjtypeid=" + ttObjTypeId + "]";
    }

    public String getUserName() {
        return userName;
    }

    public void setUsername(String userName) {
        this.userName = userName;
    }

}