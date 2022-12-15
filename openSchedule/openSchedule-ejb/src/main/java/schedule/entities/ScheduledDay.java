/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule.entities;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 *
 * @author EBIScreen
 */
@Entity
@Table(name = "scheduledday")
/*@NamedQueries({
    @NamedQuery(name = "Scheduledday.findAll", query = "SELECT s FROM Scheduledday s"),
    @NamedQuery(name = "Scheduledday.findByScheduleddaycode", query = "SELECT s FROM Scheduledday s WHERE s.scheduleddaycode = :scheduleddaycode"),
    @NamedQuery(name = "Scheduledday.findByStartyear", query = "SELECT s FROM Scheduledday s WHERE s.startyear = :startyear"),
    @NamedQuery(name = "Scheduledday.findByStartmonth", query = "SELECT s FROM Scheduledday s WHERE s.startmonth = :startmonth"),
    @NamedQuery(name = "Scheduledday.findByStartday", query = "SELECT s FROM Scheduledday s WHERE s.startday = :startday"),
    @NamedQuery(name = "Scheduledday.findByArchived", query = "SELECT s FROM Scheduledday s WHERE s.archived = :archived"),
    @NamedQuery(name = "Scheduledday.findByScudulingstate", query = "SELECT s FROM Scheduledday s WHERE s.scudulingstate = :scudulingstate"),
    @NamedQuery(name = "Scheduledday.findByArchivedstate", query = "SELECT s FROM Scheduledday s WHERE s.archivedstate = :archivedstate")})
*/
public class ScheduledDay extends EditableEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "scheduleddaycode")
    private Integer scheduledDayCode;
    @Basic(optional = false)
    @Column(name = "startyear")
    private int startYear;
    @Basic(optional = false)
    @Column(name = "startmonth")
    private int startMonth;
    @Basic(optional = false)
    @Column(name = "startday")
    private int startDay;
    @Basic(optional = false)
    @Column(name = "archived")
    private boolean archived;
    @Basic(optional = false)
    @Column(name = "scudulingstate")
    private int schedulingState;
    @Column(name = "archivedstate")
    private Integer archivedState;
    @Column(name = "active")
    private boolean active;
    @Column(name = "version")
    private Integer version;
    @OneToMany(mappedBy = "day", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ScheduledService> ScheduledServices;
    
    @Transient
    private Date calDate;
     
    public ScheduledDay() {
    }

    public ScheduledDay(Integer scheduledDayCode) {
        this.scheduledDayCode = scheduledDayCode;
    }

    public ScheduledDay(Integer scheduledDayCode, int startYear, int startMonth, int startDay, boolean archived, int schedulingState) {
        this.scheduledDayCode = scheduledDayCode;
        this.startYear = startYear;
        this.startMonth = startMonth;
        this.startDay = startDay;
        this.archived = archived;
        this.schedulingState = schedulingState;
    }
    
    public Integer getScheduledDayCode() {
        return scheduledDayCode;
    }

    public void setScheduledDayCode(Integer scheduledDayCode) {
        this.scheduledDayCode = scheduledDayCode;
    }

    public int getStartYear() {
        return startYear;
    }

    public void setStartYear(int startYear) {
        this.startYear = startYear;
    }

    public int getStartMonth() {
        return startMonth;
    }

    public void setStartMonth(int startMonth) {
        this.startMonth = startMonth;
    }

    public int getStartDay() {
        return startDay;
    }

    public void setStartDay(int startDay) {
        this.startDay = startDay;
    }

    public boolean getArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public int getSchedulingState() {
        return schedulingState;
    }

    public void setSchedulingState(int schedulingState) {
        this.schedulingState = schedulingState;
    }

    public Integer getArchivedState() {
        return archivedState;
    }

    public void setArchivedState(Integer archivedState) {
        this.archivedState = archivedState;
    }
    
    //Assume DST will take effect on all the trips in the transition day(23:59:59)
    public Date getDateOfDay() {

        if(this.calDate==null)
        {
            // todo what if it differs from stored memeber...
            int calYear = this.scheduledDayCode / 1000;

            int calDayInYear = this.scheduledDayCode % 1000;

            Calendar tempCalendar = Calendar.getInstance();
            tempCalendar.set(Calendar.YEAR,calYear);
            tempCalendar.set(Calendar.DAY_OF_YEAR,calDayInYear);
            tempCalendar.set(Calendar.HOUR_OF_DAY,23);
            tempCalendar.set(Calendar.MINUTE,59);
            tempCalendar.set(Calendar.SECOND, 59);

            this.calDate = tempCalendar.getTime();
        }
        return calDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
    
    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
    
    public void increaseVersion() {
        if(this.version == null)
            this.version = 1;
        this.version++;
    }

    public List<ScheduledService> getScheduledServices() {
        return ScheduledServices;
    }
    public int getScheduledServicesCount() {
       
        return (ScheduledServices==null) ? 0 : ScheduledServices.size();
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (scheduledDayCode != null ? scheduledDayCode.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ScheduledDay)) {
            return false;
        }
        ScheduledDay other = (ScheduledDay) object;
        if ((this.scheduledDayCode == null && other.scheduledDayCode != null) || (this.scheduledDayCode != null && !this.scheduledDayCode.equals(other.scheduledDayCode))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ScheduleEntities.Scheduledday[scheduledDayCode=" + scheduledDayCode + "]";
    }

}
