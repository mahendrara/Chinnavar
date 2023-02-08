package com.schedule.base.model;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.schedule.model.DayType;
import com.schedule.model.ScheduleBase;

/**
 *
 * @author EBIScreen
 */
@Entity
@Table(name = "defaultdayrules")
//@NamedQueries({
//    @NamedQuery(name = "DefaultDayrule.findAll", query = "SELECT d FROM DefaultDayrule d"),
//    @NamedQuery(name = "DefaultDayrule.findByDefaultruleid", query = "SELECT d FROM DefaultDayrule d WHERE d.defaultruleid = :defaultruleid"),
//    @NamedQuery(name = "DefaultDayrule.findByWeekdayno", query = "SELECT d FROM DefaultDayrule d WHERE d.weekdayno = :weekdayno")})
public class DefaultDayRule implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    @Column(name = "defaultruleid")
    private Integer defaultRuleId;
    @Column(name = "weekdayno")
    private Integer weekdayNo;
    @JoinColumn(name = "dayTypeid", referencedColumnName = "daytypeid", nullable = true)
    @ManyToOne
    private DayType dayType;

    @JoinColumn(name = "scheduleid", referencedColumnName = "scheduleid", nullable = true)
    @ManyToOne
    private ScheduleBase scheduleParent;
    
     @Transient
    private boolean editing;

    public boolean isEditing() {
        return editing;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
    }

    public ScheduleBase getScheduleParent() {
        return scheduleParent;
    }

    public void setScheduleParent(ScheduleBase scheduleParent) {
        this.scheduleParent = scheduleParent;
    }

    public DefaultDayRule() {
    }
    public void cloneDataToNonPersited(DefaultDayRule target)
    {
        target.setWeekdayNo(this.weekdayNo);
    }

    public DefaultDayRule(Integer defaultRuleId) {
        this.defaultRuleId = defaultRuleId;
    }

    public Integer getDefaultRuleId() {
        return defaultRuleId;
    }

    public void setDefaultRuleId(Integer defaultRuleId) {
        this.defaultRuleId = defaultRuleId;
    }

    public Integer getWeekdayNo() {
        return weekdayNo;
    }

    public void setWeekdayNo(Integer weekdayNo) {
        this.weekdayNo = weekdayNo;
    }

    public DayType getDayType() {
        return dayType;
    }

    public void setDayType(DayType dayType) {
        this.dayType = dayType;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (defaultRuleId != null ? defaultRuleId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DefaultDayRule)) {
            return false;
        }
        DefaultDayRule other = (DefaultDayRule) object;
        if ((this.defaultRuleId == null && other.defaultRuleId != null) || (this.defaultRuleId != null && !this.defaultRuleId.equals(other.defaultRuleId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ScheduleEntities.DefaultDayrule[defaultruleid=" + defaultRuleId + "]";
    }

}

