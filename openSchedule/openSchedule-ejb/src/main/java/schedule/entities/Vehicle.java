/*
 * To change this template, choose Tools | Templates
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
import javax.persistence.Table;

/**
 *
 * @author EBIScreen
 */
@Entity
@Table(name = "vehicles")
/*@NamedQueries({
    @NamedQuery(name = "Vehicle.findAll", query = "SELECT v FROM Vehicle v"),
    @NamedQuery(name = "Vehicle.findByVehicleid", query = "SELECT v FROM Vehicle v WHERE v.vehicleid = :vehicleid"),
    @NamedQuery(name = "Vehicle.findByVehicletype", query = "SELECT v FROM Vehicle v WHERE v.vehicletype = :vehicletype"),
    @NamedQuery(name = "Vehicle.findByVehiclesubtype", query = "SELECT v FROM Vehicle v WHERE v.vehiclesubtype = :vehiclesubtype"),
    @NamedQuery(name = "Vehicle.findByVehiclestate", query = "SELECT v FROM Vehicle v WHERE v.vehiclestate = :vehiclestate"),
    @NamedQuery(name = "Vehicle.findByCommtype", query = "SELECT v FROM Vehicle v WHERE v.commtype = :commtype"),
    @NamedQuery(name = "Vehicle.findByShortname", query = "SELECT v FROM Vehicle v WHERE v.shortname = :shortname"),
    @NamedQuery(name = "Vehicle.findBySerialno", query = "SELECT v FROM Vehicle v WHERE v.serialno = :serialno"),
    @NamedQuery(name = "Vehicle.findByAtoid", query = "SELECT v FROM Vehicle v WHERE v.atoid = :atoid"),
    @NamedQuery(name = "Vehicle.findByRadioserialno", query = "SELECT v FROM Vehicle v WHERE v.radioserialno = :radioserialno"),
    @NamedQuery(name = "Vehicle.findByDescripition", query = "SELECT v FROM Vehicle v WHERE v.descripition = :descripition"),
    @NamedQuery(name = "Vehicle.findByUiimage", query = "SELECT v FROM Vehicle v WHERE v.uiimage = :uiimage"),
    @NamedQuery(name = "Vehicle.findByVehiclelength", query = "SELECT v FROM Vehicle v WHERE v.vehiclelength = :vehiclelength"),
    @NamedQuery(name = "Vehicle.findByUsercomment", query = "SELECT v FROM Vehicle v WHERE v.usercomment = :usercomment"),
    @NamedQuery(name = "Vehicle.findByDeicingdevice", query = "SELECT v FROM Vehicle v WHERE v.deicingdevice = :deicingdevice"),
    @NamedQuery(name = "Vehicle.findByConnectionorder", query = "SELECT v FROM Vehicle v WHERE v.connectionorder = :connectionorder"),
    @NamedQuery(name = "Vehicle.findByConsistid", query = "SELECT v FROM Vehicle v WHERE v.consistid = :consistid"),})
*/
public class Vehicle implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "vehicleid")
    private Integer vehicleid;
    @Column(name = "vehicletype")
    private Integer vehicletype;
    @Column(name = "vehiclesubtype")
    private Integer vehiclesubtype;
    @Basic(optional = false)
    @Column(name = "vehiclestate")
    private int vehiclestate;
    @Column(name = "commtype")
    private Integer commtype;
    @Basic(optional = false)
    @Column(name = "shortname")
    private String shortname;
    @Basic(optional = false)
    @Column(name = "serialno")
    private String serialno;
    @Basic(optional = false)
    @Column(name = "atoid")
    private String atoid;
    @Basic(optional = false)
    @Column(name = "radioserialno")
    private String radioserialno;
    @Basic(optional = false)
    @Column(name = "descripition")
    private String descripition;
    @Basic(optional = false)
    @Column(name = "uiimage")
    private String uiimage;
    @Column(name = "vehiclelength")
    private Integer vehiclelength;
    @Column(name = "usercomment")
    private String usercomment;
    @Column(name = "deicingdevice")
    private Boolean deicingdevice;
    @Column (name="connectionorder")
    private Integer connectionorder; 
    @ManyToOne
    @JoinColumn(name="consistid", nullable=true)
    private TrainConsist trainConsist;

    public void setTrainConsist(TrainConsist trainConsist) {
        this.trainConsist = trainConsist;
    }
    
    public TrainConsist getTrainConsist() { return trainConsist; }

    public Integer getConnectionorder() {
        return connectionorder;
    }

    public void setConnectionorder(Integer vehicleOrder) {
        this.connectionorder = vehicleOrder;
    }
 
    public Vehicle() {
    }

    public Vehicle(Integer vehicleid) {
        this.vehicleid = vehicleid;
    }

    public Vehicle(Integer vehicleid, int vehiclestate, String shortname, String serialno, String atoid, String radioserialno, String descripition, String uiimage) {
        this.vehicleid = vehicleid;
        this.vehiclestate = vehiclestate;
        this.shortname = shortname;
        this.serialno = serialno;
        this.atoid = atoid;
        this.radioserialno = radioserialno;
        this.descripition = descripition;
        this.uiimage = uiimage;
    }

    public Integer getVehicleid() {
        return vehicleid;
    }

    public void setVehicleid(Integer vehicleid) {
        this.vehicleid = vehicleid;
    }

    public Integer getVehicletype() {
        return vehicletype;
    }

    public void setVehicletype(Integer vehicletype) {
        this.vehicletype = vehicletype;
    }

    public Integer getVehiclesubtype() {
        return vehiclesubtype;
    }

    public void setVehiclesubtype(Integer vehiclesubtype) {
        this.vehiclesubtype = vehiclesubtype;
    }

    public int getVehiclestate() {
        return vehiclestate;
    }

    public void setVehiclestate(int vehiclestate) {
        this.vehiclestate = vehiclestate;
    }

    public Integer getCommtype() {
        return commtype;
    }

    public void setCommtype(Integer commtype) {
        this.commtype = commtype;
    }

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public String getSerialno() {
        return serialno;
    }

    public void setSerialno(String serialno) {
        this.serialno = serialno;
    }

    public String getAtoid() {
        return atoid;
    }

    public void setAtoid(String atoid) {
        this.atoid = atoid;
    }

    public String getRadioserialno() {
        return radioserialno;
    }

    public void setRadioserialno(String radioserialno) {
        this.radioserialno = radioserialno;
    }

    public String getDescripition() {
        return descripition;
    }

    public void setDescripition(String descripition) {
        this.descripition = descripition;
    }

    public String getUiimage() {
        return uiimage;
    }

    public void setUiimage(String uiimage) {
        this.uiimage = uiimage;
    }

    public Integer getVehiclelength() {
        return vehiclelength;
    }

    public void setVehiclelength(Integer vehiclelength) {
        this.vehiclelength = vehiclelength;
    }
    
       public Boolean getDeicingdevice() {
        return deicingdevice;
    }

    public void setDeicingdevice(Boolean deicingdevice) {
        this.deicingdevice = deicingdevice;
    }

    public String getUsercomment() {
        return usercomment;
    }

    public void setUsercomment(String usercomment) {
        this.usercomment = usercomment;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (vehicleid != null ? vehicleid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Vehicle)) {
            return false;
        }
        Vehicle other = (Vehicle) object;
        if ((this.vehicleid == null && other.vehicleid != null) || (this.vehicleid != null && !this.vehicleid.equals(other.vehicleid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ScheduleEntities.Vehicle[vehicleid=" + vehicleid + "]";
    }

}
