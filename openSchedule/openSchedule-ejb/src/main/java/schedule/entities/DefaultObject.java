/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.entities;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 *
 * @author Jia Li
 */
@Entity
@Table(name = "defaultObjects")
@NamedQueries({
    @NamedQuery(name = "DefaultObject.findAll", query = "SELECT d FROM DefaultObject d"),
    @NamedQuery(name = "DefaultObject.findDefaultAction", query="SELECT d.defaultActionType from DefaultObject d where d.defaultObjectPK.scheduleName = :scheduleName and d.defaultObjectPK.prevScheduleName = :prevScheduleName"),
    @NamedQuery(name = "DefaultObject.findDefaultMovementId", query="SELECT d.defaultMovementId from DefaultObject d where d.defaultObjectPK.scheduleName = :scheduleName and d.defaultObjectPK.prevScheduleName = :prevScheduleName"),
    @NamedQuery(name = "DefaultObject.findAlternativeMovementId", query="SELECT d.alternativeMovementId from DefaultObject d where d.defaultObjectPK.scheduleName = :scheduleName and d.defaultObjectPK.prevScheduleName = :prevScheduleName"),
   })
public class DefaultObject implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected DefaultObjectPK defaultObjectPK;
    @ManyToOne
    @JoinColumn(name = "defaultatypeid", referencedColumnName = "atypeid")
    ActionType defaultActionType;
    @Column(name = "defaultmovementid")
    private Integer defaultMovementId;
    @Column(name = "alternativemovementid")
    private Integer alternativeMovementId;
            

    public DefaultObject() {
    }

    public DefaultObject(DefaultObjectPK defaultObjectPK) {
        this.defaultObjectPK = defaultObjectPK;
    }

    public DefaultObject(String prevScheduleName, String scheduleName) {
        this.defaultObjectPK = new DefaultObjectPK(prevScheduleName, scheduleName);
    }

    public DefaultObjectPK getDefaultObjectPK() {
        return defaultObjectPK;
    }

    public void setDefaultObjectPK(DefaultObjectPK defaultObjectPK) {
        this.defaultObjectPK = defaultObjectPK;
    }

    public ActionType getDefaultActionType() {
        return defaultActionType;
    }

    public void setDefaultActionType(ActionType defaultActionType) {
        this.defaultActionType = defaultActionType;
    }

    public Integer getDefaultMovementId() {
        return defaultMovementId;
    }

    public void setDefaultMovementId(Integer defaultMovementId) {
        this.defaultMovementId = defaultMovementId;
    }

    public Integer getAlternativeMovementId() {
        return alternativeMovementId;
    }

    public void setAlternativeMovementId(Integer alternativeMovementId) {
        this.alternativeMovementId = alternativeMovementId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (defaultObjectPK != null ? defaultObjectPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DefaultObject)) {
            return false;
        }
        DefaultObject other = (DefaultObject) object;
        if ((this.defaultObjectPK == null && other.defaultObjectPK != null) || (this.defaultObjectPK != null && !this.defaultObjectPK.equals(other.defaultObjectPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "dashboard.entities.Defaultobject[ defaultObjectPK=" + defaultObjectPK + " ]";
    }
    
}
