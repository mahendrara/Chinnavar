/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.entities;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author EBIScreen
 */
@Entity
@Table(name = "connections")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="objectclass", discriminatorType=DiscriminatorType.CHAR)
@XmlRootElement
/*@NamedQueries({
    @NamedQuery(name = "Connections.findAll", query = "SELECT c FROM Connections c"),
    @NamedQuery(name = "Connections.findByConnId", query = "SELECT c FROM Connections c WHERE c.connId = :connId"),
    @NamedQuery(name = "Connections.findByObjectclass", query = "SELECT c FROM Connections c WHERE c.objectclass = :objectclass"),
    @NamedQuery(name = "Connections.findByConnType", query = "SELECT c FROM Connections c WHERE c.connType = :connType"),
    @NamedQuery(name = "Connections.findByConnStatus", query = "SELECT c FROM Connections c WHERE c.connStatus = :connStatus"),
    @NamedQuery(name = "Connections.findByMinOverlap", query = "SELECT c FROM Connections c WHERE c.minOverlap = :minOverlap")})
*/
public abstract class Connection implements Serializable {
    
    public enum ConnType {
        INFORMATIONONLY,
        DESIRABLE,
        CONSTRAINT,
     }
    public enum ConnStatus {
        DISABLED,
        PLANNED,
        ACTIVE
     }
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "connId")
    private Integer connId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "conntype")
    private ConnType connType;
    @Basic(optional = false)
    @NotNull
    @Column(name = "connstatus")
    private ConnStatus connStatus;
    
    // todo change theses two to objects??
    @Column(name = "daytypeID")
    private Integer dayTypeId;

    @JoinColumn(name = "sourcetripid", referencedColumnName = "tripid")
    @ManyToOne(optional = false)
    private TimedTrip sourceTrip;
    
    @JoinColumn(name = "sourceid", referencedColumnName = "actionid")
    @ManyToOne(optional = false)
    private TripAction sourceTripAction;
    
    @JoinColumn(name = "targettripid", referencedColumnName = "tripid")
    @ManyToOne(optional = false)
    private TimedTrip targetTrip;
    
    @JoinColumn(name = "targetid", referencedColumnName = "actionid")
    @ManyToOne(optional = false)
    private TripAction targetTripAction;
    
    // todo change theses two to objects??
    @Column(name = "scheduleddaycode")
    private Integer scheduledDayCode;
      
    @Column(name = "minoverlap")
    private Integer minOverlap;
 
    public Integer getConnId() {
        return connId;
    }

    public TimedTrip getSourceTrip() {
        return sourceTrip;
    }

    public void setSourceTrip(TimedTrip sourceTrip) {
        this.sourceTrip = sourceTrip;
    }

    public TimedTrip getTargetTrip() {
        return targetTrip;
    }

    public void setTargetTrip(TimedTrip targetTrip) {
        this.targetTrip = targetTrip;
    }

    public TripAction getSourceTripAction() {
        return sourceTripAction;
    }

    public void setSourceTripAction(TripAction sourceTripAction) {
        this.sourceTripAction = sourceTripAction;
    }

    public TripAction getTargetTripAction() {
        return targetTripAction;
    }

    public void setTargetTripAction(TripAction targetTripAction) {
        this.targetTripAction = targetTripAction;
    }
    
    
    public void setConnId(Integer connId) {
        this.connId = connId;
    }
    public ConnType getConnType() {
        return connType;
    }

    public void setConnType(ConnType connType) {
        this.connType = connType;
    }

    public ConnStatus getConnStatus() {
        return connStatus;
    }

    public void setConnStatus(ConnStatus connStatus) {
        this.connStatus = connStatus;
    }
     public Integer getDayTypeId() {
        return dayTypeId;
    }

    public void setDayTypeId(Integer dayTypeId) {
        this.dayTypeId = dayTypeId;
    }

    public Integer getScheduledDayCode() {
        return scheduledDayCode;
    }

    public void setScheduledDayCode(Integer scheduledDayCode) {
        this.scheduledDayCode = scheduledDayCode;
    }
    
    public Integer getMinOverlap() {
        return minOverlap;
    }

    public void setMinOverlap(Integer minOverlap) {
        this.minOverlap = minOverlap;
    }
    public Date getSourceActionStartHour(){

        if(this.sourceTrip!=null && this.sourceTripAction!=null)
        {
            int totalSecs = sourceTrip.getActionStartSecs(sourceTripAction);           
            return new Date(totalSecs*1000L);
        }
        return null;
    }
    public Date getSourceActionEndHourString(){

        if(this.sourceTrip!=null && this.sourceTripAction!=null)
        {
            int totalSecs = sourceTrip.getActionStopSecs(sourceTripAction);           
            return new Date(totalSecs*1000L);
        }
        return null;
    }
    public Date getTargetActionStartHourString(){

        if(this.targetTrip!=null && this.targetTripAction!=null)
        {
            int totalSecs = targetTrip.getActionStartSecs(targetTripAction);           
            return new Date(totalSecs*1000L);
        }     
        return null;
    }
     public Date getTargetActionEndHourString(){

        if(this.targetTrip!=null && this.targetTripAction!=null)
        {
            int totalSecs = targetTrip.getActionStopSecs(targetTripAction);           
            return new Date(totalSecs*1000L);
        }     
        return null;
    } 
    
    public abstract String getClassDescription(); 

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (connId != null ? connId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Connection)) {
            return false;
        }
        Connection other = (Connection) object;
        if ((this.connId == null && other.connId != null) || (this.connId != null && !this.connId.equals(other.connId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ScheduleEntities.Connections[ connId=" + connId + " ]";
    }
    
}
