/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

/**
 *
 * @author EBIScreen
 */
@Entity
@Table(name = "daysinschedule", uniqueConstraints=@UniqueConstraint(columnNames = {"scheduleid","dayCodeNo"}))
/*@NamedQueries({
    @NamedQuery(name = "DayInSchedule.findAll", query = "SELECT d FROM DayInSchedule d"),
    @NamedQuery(name = "DayInSchedule.findByDayId", query = "SELECT d FROM DayInSchedule d WHERE d.dayId = :dayId"),
    @NamedQuery(name = "DayInSchedule.findByDayCodeNo", query = "SELECT d FROM DayInSchedule d WHERE d.dayCodeNo = :dayCodeNo"),
    @NamedQuery(name = "DayInSchedule.findByDaySeqNo", query = "SELECT d FROM DayInSchedule d WHERE d.daySeqNo = :daySeqNo")})*/
public class DayInSchedule extends EditableEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    @Column(name = "dayid")
    private Integer dayId;
    @Column(name = "daycodeno")
    private Integer dayCodeNo;

    @Column(name = "weeknumber")
    private Integer weekNumber;

    @Column(name = "weekdayno")
    private Integer weekDayNo;

    @Column(name = "dayseqno")
    private Integer daySeqNo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "scheduleid", referencedColumnName = "scheduleid")
    private ScheduleBase scheduleParent;

    @ManyToOne(optional = false)
    @JoinColumn(name = "daytypeid", referencedColumnName = "daytypeid")
    private DayType dayType;

    @JoinTable(name = "join_daysinschedule_daytypes",
            joinColumns = {@JoinColumn(name = "dayid", referencedColumnName = "dayid")},
            inverseJoinColumns = {@JoinColumn(name = "daytypeid", referencedColumnName = "daytypeid")})
    @ManyToMany
    private List<DayType> dayTypeList;

    public DayInSchedule() {
    }

    public List<DayType> getDayTypeList() {
        return this.dayTypeList;
    }
    public void setDayTypeList(List<DayType> dayTypeList) {
        this.dayTypeList = dayTypeList;
    }
    public void addDayTypeInList(DayType dayType) {
        if(this.dayTypeList == null) {
            this.dayTypeList = new ArrayList<>();
        }
        this.dayTypeList.add(dayType);
    }
    
    public Integer getWeekDayNo() {
        return weekDayNo;
    }

    public void setWeekDayNo(Integer weekDayNo) {
        this.weekDayNo = weekDayNo;
    }

    public Integer getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(Integer weekNumber) {
        this.weekNumber = weekNumber;
    }

    public Date getDateOfDay() {
        Calendar tempCalendar = Calendar.getInstance();
        tempCalendar.set(Calendar.YEAR, this.dayCodeNo / 1000);
        tempCalendar.set(Calendar.DAY_OF_YEAR, this.dayCodeNo % 1000);
        return tempCalendar.getTime();
    }

    public Integer getDayInYear() {
        return this.dayCodeNo % 1000;
    }

    public Integer getYear() {
        return this.dayCodeNo / 1000;
    }

    public DayType getDayType() {
        return dayType;
    }

    public void setDayType(DayType dayType) {
        this.dayType = dayType;
    }
    /*public String getDayTypeShort() {
        return this.dayType.getAbbr();
    }
    public String getDayTypeLong() {
        return this.dayType.getDescription();
    }*/

    public ScheduleBase getScheduleParent() {
        return scheduleParent;
    }

    public void setScheduleParent(ScheduleBase scheduleParent) {
        this.scheduleParent = scheduleParent;
    }
    public DayInSchedule(Integer dayId) {
        this.dayId = dayId;
    }

    public Integer getDayId() {
        return dayId;
    }

    public void setDayId(Integer dayId) {
        this.dayId = dayId;
    }

    public Integer getDayCodeNo() {
        return dayCodeNo;
    }

    public void setDayCodeNo(Integer dayCodeNo) {
        this.dayCodeNo = dayCodeNo;
    }

    public Integer getDaySeqNo() {
        return daySeqNo;
    }

    public void setDaySeqNo(Integer daySeqNo) {
        this.daySeqNo = daySeqNo;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (dayId != null ? dayId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DayInSchedule)) {
            return false;
        }
        DayInSchedule other = (DayInSchedule) object;
        if ((this.dayId == null && other.dayId != null) || (this.dayId != null && !this.dayId.equals(other.dayId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ScheduleEntities.DayInSchedule[dayId=" + dayId + "]";
    }

}
