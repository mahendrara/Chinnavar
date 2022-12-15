/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dashboard.entities;

import schedule.entities.TTObject;
import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author jiali
 */
@Entity
@Table(name = "dashboardKPI")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="kpiClass", discriminatorType=DiscriminatorType.CHAR)
@XmlRootElement
/*@NamedQueries({
    @NamedQuery(name = "DashboardKPI.findAll", query = "SELECT d FROM DashboardKPI d"),
    @NamedQuery(name = "DashboardKPI.findById", query = "SELECT d FROM DashboardKPI d WHERE d.id = :id"),
    @NamedQuery(name = "DashboardKPI.findByKpiClass", query = "SELECT d FROM DashboardKPI d WHERE d.kpiClass = :kpiClass"),
    @NamedQuery(name = "DashboardKPI.findBySampleWindowSecs", query = "SELECT d FROM DashboardKPI d WHERE d.sampleWindowSecs = :sampleWindowSecs"),
    @NamedQuery(name = "DashboardKPI.findByKpiValue", query = "SELECT d FROM DashboardKPI d WHERE d.kpiValue = :kpiValue")})*/
public abstract class DashboardKPI implements Serializable {
    public enum KPIType {

        AVERAGE_LATENESS,
        PERCENTAGE_OF_HEADWAYS,
        KILOMETERS_DELIVERED,
        JOURNEY_TIME_STATISTICS,
        PLATFORM_WAIT_TIME
    }

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Column(name = "kpiClass")
    private Character kpiClass;
    @Column(name = "sampleWindowSecs")
    private Integer sampleWindowSecs;
    @Column(name = "kpiValue")
    private Integer kpiValue;
    
    @JoinTable(name = "join_dashboardKPI_ttobjects", joinColumns = {
        @JoinColumn(name = "kpiid", referencedColumnName = "id")}, inverseJoinColumns = {
        @JoinColumn(name = "ttobjid", referencedColumnName = "ttobjid")})
    @ManyToMany
    private List<TTObject> tTObjectList;

    public DashboardKPI() {
    }

    public DashboardKPI(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Character getKpiClass() {
        return kpiClass;
    }

    public void setKpiClass(Character kpiClass) {
        this.kpiClass = kpiClass;
    }

    public Integer getSampleWindowSecs() {
        return sampleWindowSecs;
    }

    public void setSampleWindowSecs(Integer sampleWindowSecs) {
        this.sampleWindowSecs = sampleWindowSecs;
    }

    public Integer getKpiValue() {
        return kpiValue;
    }

    public void setKpiValue(Integer kpiValue) {
        this.kpiValue = kpiValue;
    }

    @XmlTransient
    public List<TTObject> getTTObjectList() {
        return tTObjectList;
    }

    public void setTTObjectList(List<TTObject> tTObjectList) {
        this.tTObjectList = tTObjectList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DashboardKPI)) {
            return false;
        }
        DashboardKPI other = (DashboardKPI) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "dashboard.entities.DashboardKPI[ id=" + id + " ]";
    }
    
}
