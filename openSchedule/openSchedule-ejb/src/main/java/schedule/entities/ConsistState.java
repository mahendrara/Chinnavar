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
 * @author spirttin
 */
@Entity
@Table(name = "consiststates")
public class ConsistState implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "stateid")
    private Integer stateId;
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

    @OneToMany
    @JoinColumn(name ="textKey", referencedColumnName = "userName")
    @MapKey(name="locale")
    private Map<Locale,TextKey> textKeys;

    public ConsistState() {
    }

    public ConsistState(Integer stateId) {
        this.stateId = stateId;
    }

    public ConsistState(Integer stateId, String description, String userName) {
        this.stateId = stateId;
        this.description = description;
        this.userName = userName;
    }

    public Integer getStateId() {
        return stateId;
    }

    public void setStateId(Integer stateId) {
        this.stateId = stateId;
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

    /** 
     * Gives actual textual description in a locale mode
     * @param locale
     * @return 
     */
    public String getText(Locale locale) {
        if(textKeys.containsKey(locale))
            return textKeys.get(locale).getText();
        return getDescription();
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (stateId != null ? stateId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ConsistState)) {
            return false;
        }
        ConsistState other = (ConsistState) object;
        if ((this.stateId == null && other.stateId != null) || (this.stateId != null && !this.stateId.equals(other.stateId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "schedule.entities.Consiststates[ stateid=" + stateId + " ]";
    }
    
}
