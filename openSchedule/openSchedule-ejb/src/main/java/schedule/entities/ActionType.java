/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.entities;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import schedule.entities.TripAction.MainActionTypeEnum;

/**
 *
 * @author EBIScreen
 */
@Entity
@Table(name = "actiontypes")
/*@NamedQueries({
 @NamedQuery(name = "ActionType.findAll", query = "SELECT a FROM ActionType a"),
 @NamedQuery(name = "ActionType.findByActiontype", query = "SELECT a FROM ActionType a WHERE a.actiontype = :actiontype"),
 @NamedQuery(name = "ActionType.findByActionsubtype", query = "SELECT a FROM ActionType a WHERE a.actionsubtype = :actionsubtype"),
 @NamedQuery(name = "ActionType.findByUsertype", query = "SELECT a FROM ActionType a WHERE a.usertype = :usertype"),
 @NamedQuery(name = "ActionType.findByDescription", query = "SELECT a FROM ActionType a WHERE a.description = :description"),
 @NamedQuery(name = "ActionType.findByTxtindex", query = "SELECT a FROM ActionType a WHERE a.txtindex = :txtindex"),
 @NamedQuery(name = "ActionType.findByAtypeId", query = "SELECT a FROM ActionType a WHERE a.atypeId = :atypeId")})
 */
public class ActionType implements Serializable {

    private static final long serialVersionUID = 1L;
    //@Basic(optional = false)
    //@Column(name = "actiontype")
    //private Integer actionType;
    @Basic(optional = false)
    @Column(name = "actionsubtype")
    private Integer actionSubtype;
    @Column(name = "usertype")
    private Integer userType;
    @Basic(optional = false)
    @Column(name = "description")
    private String description;
    @Column(name = "defaultminsecs")
    private Integer defaultMinSecs;
    @Column(name = "defaultplansecs")
    private Integer defaultPlanSecs;
    @Id
    @Basic(optional = false)
    @Column(name = "atypeId")
    private Integer atypeId;
    @Column(name = "used")
    private Boolean used;
    @Column(name = "userName")
    private String userName;

    @OneToMany
    @JoinColumn(name = "textKey", referencedColumnName = "userName")
    @MapKey(name = "locale")
    private Map<Locale, TextKey> textKeys;

//    @OneToMany(cascade = CascadeType.ALL, mappedBy = "actionType")
//    private List<TripAction> ttActions;

    @JoinColumn(name = "actiontype", referencedColumnName = "mactiontypeid")
    @ManyToOne(optional = false)
    private MainActionType mainActionType;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "join_ttobjecttype_actiontype",
            joinColumns = @JoinColumn(name = "atypeid"),
            inverseJoinColumns = @JoinColumn(name = "ttobjtypeid"))
    private List<TTObjectType> ttObjectTypes;

    public ActionType() {
    }

    public ActionType(Integer atypeId) {
        this.atypeId = atypeId;
    }

    public static String getDuration(Integer seconds) {
        if (seconds != null) {
            Integer temp = Math.abs(seconds);
            int hour = temp / 3600;
            int min = temp % 3600 / 60;
            int secs = temp % 60;
            StringBuilder str = new StringBuilder();
            if (hour < 10) {
                str.insert(0, "0");
            }
            str.append(hour).append(":");
            if (min < 10) {
                str.append("0");
            }
            str.append(min).append(":");
            if (secs < 10) {
                str.append("0");
            }
            str.append(secs);
            if (seconds >= 0) {
                return str.toString();
            } else {
                return "-" + str.toString();
            }
        } else {
            return null;
        }
    }

    /*public ActionType(Integer atypeId, TripAction.MainActionTypeEnum actionType, int actionSubtype, String description) {
     this.atypeId = atypeId;
     this.mainActionType =actionType.getValue();
     this.actionSubtype = actionSubtype;
     this.description = description;
     }
     public TripAction.MainActionTypeEnum getActionType() {
     return TripAction.MainActionTypeEnum.parse(this.mainActionType.getnMactionTypeId());
     }
     public void setActionType(TripAction.MainActionTypeEnum actionType) {
     this.actionType = actionType.getValue();
     }*/
    public MainActionType getMainActionType() {
        return mainActionType;
    }

    public void setMainActionType(MainActionType mainActionType) {
        this.mainActionType = mainActionType;
    }

    public MainActionTypeEnum getMainActionTypeEnum() {
        return MainActionTypeEnum.parse(mainActionType.getMactionTypeId());
    }

    /*public void setMainActionTypeEnum(MainActionTypeEnum mainActionType) {
     this.mainActionType = mainActionType;
     }*/
    public Integer getActionSubtype() {
        return actionSubtype;
    }

    public void setActionSubtype(Integer actionSubtype) {
        this.actionSubtype = actionSubtype;
    }

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
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

    public Integer getDefaultMinSecs() {
        return defaultMinSecs;
    }

    public void setDefaultMinSecs(Integer defaultMinSecs) {
        this.defaultMinSecs = defaultMinSecs;
    }

    public Integer getDefaultPlanSecs() {
        return defaultPlanSecs;
    }

    public void setDefaultPlanSecs(Integer defaultPlanSecs) {
        this.defaultPlanSecs = defaultPlanSecs;
    }

    public Integer getAtypeId() {
        return atypeId;
    }

    public void setAtypeId(Integer atypeId) {
        this.atypeId = atypeId;
    }
/*
    public List<TripAction> getTTActionCollection() {
        return ttActions;
    }

    public void setTTActionCollection(List<TripAction> ttActions) {
        this.ttActions = ttActions;
    }
*/
    public List<TTObjectType> getTTObjTypes() {
        return ttObjectTypes;
    }

    public void setTTObjTypes(List<TTObjectType> ttobjTypes) {
        this.ttObjectTypes = ttobjTypes;
    }

    public boolean isMainActionType(MainActionTypeEnum mainActionTypeEnum) {
        return this.mainActionType.getMactionTypeId() == mainActionTypeEnum.getValue();
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (atypeId != null ? atypeId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ActionType)) {
            return false;
        }
        ActionType other = (ActionType) object;
        if ((this.atypeId == null && other.atypeId != null) || (this.atypeId != null && !this.atypeId.equals(other.atypeId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "schedule.entities.Actiontype[atypeId=" + atypeId + "]";
    }

    public Boolean isUsed() {
        return used;
    }

    public void setUsed(Boolean used) {
        this.used = used;
    }

}
