package com.schedule.model;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.List;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

/**
 *
 * @author EBIScreen
 */
@Entity
@DiscriminatorValue("T")
public class ScheduleTemplate extends ScheduleBase {
    
    @OneToMany(mappedBy = "scheduleTemplate", targetEntity = Schedule.class)
    private List<Schedule> schedules;

    public List<Schedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<Schedule> schedules) {
        this.schedules = schedules;
    }
    
    public ScheduleTemplate() {

    }
    
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ScheduleTemplate)) {
            return false;
        }
        ScheduleTemplate other = (ScheduleTemplate) object;
        if ((this.getScheduleId() == null && other.getScheduleId() != null) || (this.getScheduleId() != null && this.getScheduleId()!=other.getScheduleId())) {
            return false;
        }
        return true;
    }
}

