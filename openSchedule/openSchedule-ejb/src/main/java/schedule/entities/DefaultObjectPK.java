/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.entities;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author Jia Li
 */
@Embeddable
public class DefaultObjectPK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 254)
    @Column(name = "prevschedulename")
    private String prevScheduleName;
    @Basic(optional = false)
    @NotNull
    @Column(name = "schedulename")
    private String scheduleName;

    public DefaultObjectPK() {
    }

    public DefaultObjectPK(String prevScheduleName, String scheduleName) {
        this.prevScheduleName = prevScheduleName;
        this.scheduleName = scheduleName;
    }

    public String getPrevScheduleName() {
        return prevScheduleName;
    }

    public void setPrevScheduleName(String prevScheduleName) {
        this.prevScheduleName = prevScheduleName;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }

    @Override
    public int hashCode() {
        return prevScheduleName.hashCode() + scheduleName.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DefaultObjectPK)) {
            return false;
        }
        DefaultObjectPK other = (DefaultObjectPK) object;
        if (!this.prevScheduleName.equals(other.prevScheduleName)) {
            return false;
        }
        if (!this.scheduleName.equals(other.scheduleName)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "schedule.entities.DefaultobjectPK[ prevScheduleName=" + prevScheduleName + ", scheduleName=" + scheduleName + " ]";
    }
    
}
