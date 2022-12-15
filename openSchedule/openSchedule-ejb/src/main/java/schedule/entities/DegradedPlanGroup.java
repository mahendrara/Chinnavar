/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.entities;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;

/**
 *
 * @author Jia Li
 */
@Entity
@Table(name = "degradedplangroup")
@NamedQueries({
    @NamedQuery(name = "DegradedPlanGroup.findAll", query = "SELECT d FROM DegradedPlanGroup d")})
public class DegradedPlanGroup implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "groupid")
    private Integer groupId;
    @Size(max = 2048)
    @Column(name = "description")
    private String description;
    @Size(max = 254)
    @Column(name = "username")
    private String userName;  
    @OneToMany(mappedBy = "degradedPlanGroup")
    private List<DegradedPlan> degradedPlans;

    public DegradedPlanGroup() {
    }

    public DegradedPlanGroup(Integer groupId) {
        this.groupId = groupId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<DegradedPlan> getDegradedPlans() {
        return degradedPlans;
    }

    public void setDegradedPlans(List<DegradedPlan> degradedPlans) {
        this.degradedPlans = degradedPlans;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (groupId != null ? groupId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DegradedPlanGroup)) {
            return false;
        }
        DegradedPlanGroup other = (DegradedPlanGroup) object;
        if ((this.groupId == null && other.groupId != null) || (this.groupId != null && !this.groupId.equals(other.groupId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "schedule.entities.DegradedPlanGroup[ groupId=" + groupId + " ]";
    }
    
}
