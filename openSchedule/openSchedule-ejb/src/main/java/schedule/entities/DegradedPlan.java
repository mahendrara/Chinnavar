/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.entities;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author Jia Li
 */
@Entity
@Table(name = "degradedplan")
public class DegradedPlan implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "planid")
    private Integer planId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "objectiveid")
    private int objectiveId;
    @Size(max = 2048)
    @Column(name = "description")
    private String description;
    @Size(max = 254)
    @Column(name = "username")
    private String userName;
    @ManyToOne(optional = false)
    @JoinColumn(name = "groupid", referencedColumnName = "groupid")
    private DegradedPlanGroup degradedPlanGroup;

    @OneToMany
    @JoinColumn(name = "textKey", referencedColumnName = "username")
    @MapKey(name = "locale")
    private Map<Locale, TextKey> textKeys;

    @ManyToMany(fetch=FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "degradedjoin",
            joinColumns = @JoinColumn(name = "planid",referencedColumnName="planid"),
            inverseJoinColumns = @JoinColumn(name = "serviceid",referencedColumnName="tripid"))
    private List<ScheduledService> degradedServices;

    public DegradedPlan() {
    }

    public DegradedPlan(Integer planId) {
        this.planId = planId;
    }

    public DegradedPlan(Integer planId, int objectiveId) {
        this.planId = planId;
        this.objectiveId = objectiveId;
    }

    public Integer getPlanId() {
        return planId;
    }

    public void setPlanid(Integer planId) {
        this.planId = planId;
    }

    public int getObjectiveId() {
        return objectiveId;
    }

    public void setObjectiveId(int objectiveId) {
        this.objectiveId = objectiveId;
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

    public DegradedPlanGroup getDegradedPlanGroup() {
        return degradedPlanGroup;
    }

    public void setDegradedPlanGroup(DegradedPlanGroup degradedPlanGroup) {
        this.degradedPlanGroup = degradedPlanGroup;
    }

    public List<ScheduledService> getDegradedServices() {
        degradedServices.size();
        return degradedServices;
    }

    public void setDegradedService(List<ScheduledService> degradedServices) {
        this.degradedServices = degradedServices;
    }
    
    public void removeDegradedService(ScheduledService degradedService) {
        Iterator<ScheduledService> it = this.degradedServices.iterator();
        while(it.hasNext()) {
            if(Objects.equals(it.next().getTripId(), degradedService.getTripId())) {
                it.remove();
                break;
            }
        }
    }

    public String getText(Locale locale) {
        if (textKeys != null && textKeys.containsKey(locale)) {
            return textKeys.get(locale).getText();
        }
        return getDescription();
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (planId != null ? planId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DegradedPlan)) {
            return false;
        }
        DegradedPlan other = (DegradedPlan) object;
        if ((this.planId == null && other.planId != null) || (this.planId != null && !this.planId.equals(other.planId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "schedule.entities.DegradedPlan[ planid=" + planId + " ]";
    }
}
