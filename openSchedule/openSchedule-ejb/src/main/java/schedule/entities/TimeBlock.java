/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.entities;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import schedule.entities.util.TimeHelper;

/**
 *
 * @author Jia Li
 */
@Entity
@Table(name = "timeblocks")
@NamedQueries({
    @NamedQuery(name = "TimeBlock.findByDayTypes", query = "SELECT p FROM TimeBlock p WHERE p.dayType.dayTypeId in :a"),
    @NamedQuery(name = "TimeBlock.deleteByDayType", query = "DELETE FROM TimeBlock s WHERE s.dayType.dayTypeId in :a")})
public class TimeBlock extends EditableEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @Column(name = "blockid")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer blockId;
    /*@Basic(optional = false)
    @Column(name = "seqno")
    private Integer seqNo;*/
    @Basic(optional = false)
    @Column(name = "description")
    private String description;
    @Basic(optional = false)
    @Column(name = "startsecs")
    private Integer startSecs;
    @Basic(optional = false)
    @Column(name = "endsecs")
    private Integer endSecs;
    @Basic(optional = false)
    @Column(name = "speedprofile")
    private Integer speedProfile;
    @Basic(optional = false)
    @Column(name = "headway")
    private Integer headway;
    //@Column(name = "numberoftrains")
    //private Integer numberOfTrains;
    @JoinColumn(name = "dayTypeId", referencedColumnName = "dayTypeId")
    @ManyToOne(optional = false)
    private DayType dayType;
    @Column(name = "utctimes")
    private boolean utcTimes;
    @Column(name = "timezone")
    private String timeZone;

    /*@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "join_timeblocks_trips",
            joinColumns = @JoinColumn(name = "blockid"),
            inverseJoinColumns = @JoinColumn(name = "tripid"))
    private List<ServiceTrip> plannedServices;*/
 /*@OneToMany
    @JoinTable(name = "join_timeblocks_templates",
            joinColumns=@JoinColumn(name = "blockid", referencedColumnName = "blockid"),
            inverseJoinColumns=@JoinColumn(name = "templateid", referencedColumnName = "tripid"))
    @MapKeyColumn(name = "seqno")
    Map<Integer, BasicTrip> tripTemplates;*/
 /*@OneToMany(mappedBy="timeBlock", cascade = CascadeType.ALL, orphanRemoval = true)
    List<TimeBlockTripTemplate> tripTemplates;*/
    public Integer getBlockId() {
        return blockId;
    }

    public void setBlockId(Integer blockId) {
        this.blockId = blockId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStartSecs() {
        return startSecs;
    }

    public void setStartSecs(Integer startSecs) {
        this.startSecs = startSecs;
    }

    public Integer getEndSecs() {
        return endSecs;
    }

    public void setEndSecs(Integer endSecs) {
        this.endSecs = endSecs;
    }

    public Integer getSpeedProfile() {
        return speedProfile;
    }

    public void setSpeedProfile(Integer speedProfile) {
        this.speedProfile = speedProfile;
    }

    public Integer getHeadway() {
        return headway;
    }

    public void setHeadway(Integer headway) {
        this.headway = headway;
    }

    public DayType getDayType() {
        return dayType;
    }

    public void setDayType(DayType dayType) {
        this.dayType = dayType;
    }

    public void setStartTime(Date time) {
        startSecs = Integer.valueOf("" + (this.getStartDay() * 3600 * 24 + TimeHelper.getLocalSecsFrom(false, time, timeZone)));
    }

    public void setEndTime(Date time) {
        endSecs = Integer.valueOf("" + (this.getEndDay() * 3600 * 24 + TimeHelper.getLocalSecsFrom(false, time, timeZone)));
    }

    public Date getStartTime() {
        if (timeZone != null) {
            return TimeHelper.getUtcTimeFrom(utcTimes, null, startSecs, timeZone);
        } else {
            return new Date();
        }
    }

    public Date getEndTime() {
        if (this.timeZone != null) {
            return TimeHelper.getUtcTimeFrom(utcTimes, null, endSecs, timeZone);
        } else {
            return new Date();
        }
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public boolean isUtcTimes() {
        return utcTimes;
    }

    public void setUtcTimes(boolean utcTimes) {
        this.utcTimes = utcTimes;
    }

    public Integer getStartDay() {
        return TimeHelper.getDayFor(false, null, this.startSecs, this.timeZone);
    }

    public void setStartDay(Integer day) {
        this.startSecs = this.startSecs % 3600 % 24 + day * 3600 * 24;
    }

    public Integer getEndDay() {
        return TimeHelper.getDayFor(false, null, this.endSecs, this.timeZone);
    }

    public void setEndDay(Integer day) {
        if (endSecs != null) {
            this.endSecs = this.endSecs % 3600 % 24 + day * 3600 * 24;
        } else {
            this.endSecs = day * 3600 * 24;
        }
    }

    /*public List<ServiceTrip> getPlannedServices() {
        return plannedServices;
    }

    public void setPlannedServices(List<ServiceTrip> plannedServices) {
        this.plannedServices = plannedServices;
    }*/

 /*public Integer getNumberOfTrains() {
        return numberOfTrains;
    }

    public void setNumberOfTrains(Integer numberOfTrains) {
        this.numberOfTrains = numberOfTrains;
    }*/

 /*public Map<Integer, BasicTrip> getTripTemplates() {
        return tripTemplates;
    }

    public void setTripTemplates(Map<Integer, BasicTrip> tripTemplates) {
        this.tripTemplates = tripTemplates;
    }*/

 /*public List<TimeBlockTripTemplate> getTripTemplates() {
        return tripTemplates;
    }

    public void setTripTemplates(List<TimeBlockTripTemplate> tripTemplates) {
        this.tripTemplates = tripTemplates;
    }*/
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (blockId != null ? blockId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TimeBlock)) {
            return false;
        }
        TimeBlock other = (TimeBlock) object;
        if ((this.blockId == null && other.blockId != null) || (this.blockId != null && !this.blockId.equals(other.blockId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "schedule.entities.TimeBlock[blockid=" + blockId + " ]";
    }

}
