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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 *
 * @author Jia Li
 */
@Entity
@Table(name = "tthierarchy")
@NamedQueries({
    @NamedQuery(name = "TTHierarchy.deleteChild", query = "DELETE FROM TTHierarchy t WHERE t.parentObject.ttObjId = :parentid AND t.childObject.ttObjId = :childid")
})
public class TTHierarchy extends EditableEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    
    @NotNull
    @ManyToOne
    @JoinColumn(name = "hierarchyid", referencedColumnName = "hierarchyid")
    private TTHierarchyType hierarchyType;
    
    @Column(name = "seqno")
    private Integer seqNo;
    
    @ManyToOne
    @JoinColumn(name = "parentid", referencedColumnName = "ttobjid")
    private TTObject parentObject;

    @ManyToOne
    @JoinColumn(name = "childid", referencedColumnName = "ttobjid")
    private TTObject childObject;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public TTHierarchyType getHierarchyType() {
        return hierarchyType;
    }

    public void setHierarchyType(TTHierarchyType hierarchyType) {
        this.hierarchyType = hierarchyType;
    }

    public Integer getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(Integer seqNo) {
        this.seqNo = seqNo;
    }

    public TTObject getChildObject() {
        return childObject;
    }

    public void setChildObject(TTObject childObject) {
        this.childObject = childObject;
    }

    public TTObject getParentObject() {
        return parentObject;
    }

    public void setParentObject(TTObject parentObject) {
        this.parentObject = parentObject;
    }
    
    @Override
    public String toString() {
        return "schedule.entities.MainActionType[hierarchytypeid=" + id + "]";
    }
}
