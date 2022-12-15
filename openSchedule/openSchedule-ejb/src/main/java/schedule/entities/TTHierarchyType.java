/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.entities;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author Jia Li
 */
@Entity
@Table(name = "tthierarchytypes")
@NamedQueries({
    @NamedQuery(name = "TTHierarchyType.findAllByParentType", query = "SELECT t FROM TTHierarchyType t WHERE t.parentType = :parenttype")
})
public class TTHierarchyType implements Serializable {

    public static enum TTHierarchyEnum {
        stationparent,
        lineparent,
        alternativeto
    }

    @Id
    @NotNull
    @Basic(optional = false)
    @Column(name = "hierarchyid")
    private Integer hierarchyId;

    @NotNull
    @Column(name = "hierarchytype")
    private String hierarchyType;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "parenttype", referencedColumnName = "ttobjtypeid")
    private TTObjectType parentType;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "childtype", referencedColumnName = "ttobjtypeid")
    private TTObjectType childType;

    @NotNull
    @Column(name = "mandatory")
    private Boolean mandatory;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 254)
    @Column(name = "description")
    private String description;

    public Integer getHierarchyId() {
        return hierarchyId;
    }

    public void setHierarchyId(Integer hierarchyId) {
        this.hierarchyId = hierarchyId;
    }

    public String getHierarchyType() {
        return hierarchyType;
    }

    public void setHierarchyType(String hierarchyType) {
        this.hierarchyType = hierarchyType;
    }

    public TTObjectType getParentType() {
        return parentType;
    }

    public void setParentType(TTObjectType parentType) {
        this.parentType = parentType;
    }

    public TTObjectType getChildType() {
        return childType;
    }

    public void setChildType(TTObjectType childType) {
        this.childType = childType;
    }

    public Boolean getMandatory() {
        return mandatory;
    }

    public void setMandatory(Boolean mandatory) {
        this.mandatory = mandatory;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
