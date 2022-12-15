/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.entities;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author EBIScreen
 */
@Entity
@Table(name = "ttobjects")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "objectclass", discriminatorType = DiscriminatorType.CHAR)
@NamedQueries({
    @NamedQuery(name = "TTObject.findAllByType", query = "SELECT t FROM TTObject t WHERE t.ttObjectType = :objecttype")
})
public abstract class TTObject extends EditableEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ttObjId")
    private Integer ttObjId;

    @Column(name = "extPhyscialId")
    private Integer extPhysicalId;

    @Column(name = "extPhyscialId2")
    private Integer extPhysicalId2;

    @Column(name = "extLogicalId")
    private Integer extLogicalId;

    @Basic(optional = false)
    @Column(name = "description")
    private String description;

    @Column(name = "scheduleName")
    private String scheduleName;

    @Column(name = "objectclass", updatable = false, insertable = false)
    private char objectClass;

    @Column(name = "externalName")
    private String externalName;

    @Column(name = "userName")
    private String userName;

    @Column(name = "timezone")
    private String timeZone;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ttobjtypeid", referencedColumnName = "ttobjtypeid")
    private TTObjectType ttObjectType;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "join_ttobjects",
            joinColumns = @JoinColumn(name = "parentID"),
            inverseJoinColumns = @JoinColumn(name = "childID"))
    private List<TTObject> childObjects;

    @ManyToOne
    @JoinTable(name = "join_ttobjects",
            joinColumns = @JoinColumn(name = "childID"),
            inverseJoinColumns = @JoinColumn(name = "parentID"))
    private TTObject parentObject;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "ttobjid")
    private Collection<TTPropValues> ttpropvaluesCollection;

    @ManyToOne(optional = false)
    @JoinColumn(name = "objstate", referencedColumnName = "stateid")
    private TTObjectState ttObjectState;

    @OneToMany
    @JoinColumn(name = "textKey", referencedColumnName = "userName")
    @MapKey(name = "locale")
    private Map<Locale, TextKey> textKeys;

    public List<TTObject> getChildObjects() {
        return childObjects;
    }

    public TTObject getParentObject() {
        return this.parentObject;
    }

    public void setParentObject(TTObject parent) {
        this.parentObject = parent;
    }

    public int getChildCount() {
        if (childObjects != null) {
            return childObjects.size();
        }
        return 0;
    }

    boolean isParentOf(TTObject check) {

        List<TTObject> search = getChildObjects();

        for (TTObject child : search) {

            if (check.getTTObjId() == child.getTTObjId()) {
                return true;
            }
        }
        return false;
    }

    public void setChildObjects(List<TTObject> childObjects) {
        this.childObjects = childObjects;
    }

    public TTObjectType getTTObjectType() {
        return ttObjectType;
    }

    public void setTTObjectType(TTObjectType ttObjectType) {
        this.ttObjectType = ttObjectType;
    }

    public TTObject() {
        ttObjId = 0;
        description = "";
    }

    public TTObject(Integer ttObjId) {
        this.ttObjId = ttObjId;
        description = "";
    }

    public TTObject(Integer ttObjId, String description) {
        this.ttObjId = ttObjId;
        this.description = description;
    }

    public Integer getExtPhysicalId() {
        return extPhysicalId;
    }

    public void setExtPhysicalId(Integer ebiscreenId) {
        this.extPhysicalId = ebiscreenId;
    }

    public Integer getExtPhysicalId2() {
        return extPhysicalId2;
    }

    public void setExtPhysicalId2(Integer ebiscreenId2) {
        this.extPhysicalId2 = ebiscreenId2;
    }

    public Integer getExtLogicalId() {
        return extLogicalId;
    }

    public void setExtLogicalId(Integer atrId) {
        this.extLogicalId = atrId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }

    public char getObjectClass() {
        return objectClass;
    }

    public void setObjectClass(char objectClass) {
        this.objectClass = objectClass;
    }

    public Integer getTTObjId() {
        return ttObjId;
    }

    public void setTTObjId(Integer ttObjId) {
        this.ttObjId = ttObjId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getText(Locale locale) {
        if (textKeys != null && textKeys.containsKey(locale)) {
            return textKeys.get(locale).getText();
        }
        return getDescription();
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (ttObjId != null ? ttObjId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TTObject)) {
            return false;
        }
        TTObject other = (TTObject) object;
        if ((this.ttObjId == null && other.ttObjId != null) || (this.ttObjId != null && !this.ttObjId.equals(other.ttObjId))) {
            return false;
        }
        return true;
    }

    public String getExternalName() {
        return externalName;
    }

    public void setExternalName(String externalName) {
        this.externalName = externalName;
    }

    public void setTtpropvaluesCollection(Collection<TTPropValues> ttpropvaluesCollection) {
        this.ttpropvaluesCollection = ttpropvaluesCollection;
    }

    public Collection<TTPropValues> getTtpropvaluesCollection() {
        return ttpropvaluesCollection;
    }

    public int getPropertyCount() {
        if (ttpropvaluesCollection == null) {
            return 0;
        }

        return ttpropvaluesCollection.size();
    }

    public TTObjectState getTTObjectState() {
        return this.ttObjectState;
    }

    public void setTTObjectState(TTObjectState ttObjectState) {
        this.ttObjectState = ttObjectState;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public String toString() {
        return "schedule.entities.TTObject[ttObjId=" + ttObjId + "]";
    }

}
