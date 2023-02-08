package com.schedule.base.model;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

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

import com.schedule.model.TextKey;

/**
 *
 * @author Jia Li
 */
@Entity
@Table(name = "schedulingstate")

public class SchedulingState implements Serializable {

    public enum State {

        UNKNOWN(0),
        PLANNED_TO_RUN(1),
        PLANNED_SPARE(2),
        CANCELLED(3),
        PARTLY_CANCELLED(4),
        FINISHED(5),
        FINISHED_DEGRADED(6),
        
        SYSTEM(100),
        USER(101);
        
        private final int stateValue;
        private State(int stateValue){
            this.stateValue = stateValue;
        };
        public int getStateValue() {
            return stateValue;
        }
    }

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "schedulingstateid")
    private Integer schedulingStateId;
    @Size(max = 254)
    @Column(name = "state")
    private String state;
    @Size(max = 254)
    @Column(name = "description")
    private String description;
    @Column(name = "userName")
    private String userName;

    @OneToMany
    @JoinColumn(name = "textKey", referencedColumnName = "userName")
    @MapKey(name = "locale")
    private Map<Locale, TextKey> textKeys;

    public SchedulingState() {
    }

    public SchedulingState(Integer schedulingStateId) {
        this.schedulingStateId = schedulingStateId;
    }

    public SchedulingState(Integer schedulingStateId, String description) {
        this.schedulingStateId = schedulingStateId;
        this.description = description;
    }

    public Integer getSchedulingStateId() {
        return schedulingStateId;
    }

    public void setSchedulingStateId(Integer schedulingStateId) {
        this.schedulingStateId = schedulingStateId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getText(Locale locale) {
        if (textKeys.containsKey(locale)) {
            return textKeys.get(locale).getText();
        }
        return getDescription();
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (schedulingStateId != null ? schedulingStateId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof SchedulingState)) {
            return false;
        }
        SchedulingState other = (SchedulingState) object;
        if ((this.schedulingStateId == null && other.schedulingStateId != null) || (this.schedulingStateId != null && !this.schedulingStateId.equals(other.schedulingStateId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "schedule.entities.Schedulingstate[ schedulingstateid=" + schedulingStateId + " ]";
    }

}
