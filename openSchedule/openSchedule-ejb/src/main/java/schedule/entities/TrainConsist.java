/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.entities;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 *
 * @author EBIScreen
 */
@Entity
@Table(name = "trainconsists")
/*@NamedQueries({
 @NamedQuery(name = "TrainConsist.findAll", query = "SELECT t FROM TrainConsist t"),
 @NamedQuery(name = "TrainConsist.findByConsistid", query = "SELECT t FROM TrainConsist t WHERE t.consistid = :consistid"),
 @NamedQuery(name = "TrainConsist.findByTrainstate", query = "SELECT t FROM TrainConsist t WHERE t.trainstate = :trainstate"),
 @NamedQuery(name = "TrainConsist.findByPositionid", query = "SELECT t FROM TrainConsist t WHERE t.positionid = :positionid"),
 @NamedQuery(name = "TrainConsist.findByCommtype", query = "SELECT t FROM TrainConsist t WHERE t.commtype = :commtype"),
 @NamedQuery(name = "TrainConsist.findByCurrentcommtype", query = "SELECT t FROM TrainConsist t WHERE t.currentcommtype = :currentcommtype"),
 @NamedQuery(name = "TrainConsist.findByLogicallength", query = "SELECT t FROM TrainConsist t WHERE t.logicallength = :logicallength"),
 @NamedQuery(name = "TrainConsist.findByCrewid", query = "SELECT t FROM TrainConsist t WHERE t.crewid = :crewid"),
 @NamedQuery(name = "TrainConsist.findByComment", query = "SELECT t FROM TrainConsist t WHERE t.comment = :comment"),
 @NamedQuery(name = "TrainConsist.findByTraindescriber", query = "SELECT t FROM TrainConsist t WHERE t.traindescriber = :traindescriber"),
 @NamedQuery(name = "TrainConsist.findByTraintypeid", query = "SELECT t FROM TrainConsist t WHERE t.traintypeid = :traintypeid")})
 //@NamedQuery(name = "TrainConsist.findByReportsubsystemid", query = "SELECT t FROM TrainConsist t WHERE t.reportsubsystemid = :reportsubsystemid")})
 //@NamedQuery(name = "TrainConsist.findByReporttime", query = "SELECT t FROM TrainConsist t WHERE t.reporttime = :reporttime")})*/
public class TrainConsist extends EditableEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "consistid")
    private Integer consistId;
    @JoinColumn(name = "trainstate", referencedColumnName = "stateid")
    @ManyToOne(optional = false)
    private ConsistState trainState;
    @Column(name = "positionid")
    private Integer positionId;
    @Basic(optional = false)
    @Column(name = "commtype")
    private int commType;
    @Basic(optional = false)
    @Column(name = "currentcommtype")
    private int currentCommType;
    /*@Column(name = "logicallength")
    private Integer logicalLength;
    @Column(name = "crewid")
    private Integer crewId;*/
    @Basic(optional = false)
    @Column(name = "comment")
    private String comment;
    @Basic(optional = false)
    @Column(name = "traindescriber")
    private String trainDescriber;
    // @Basic(optional = false)
    //@Column(name = "traintypeid")
    //private Integer trainTypeId;
    @JoinColumn(name = "traintypeid", referencedColumnName = "traintypeid")
    @ManyToOne(optional = false)
    private TrainType trainType;
    
    @Basic(optional = false)
    @Column(name = "reporttime")
    private Timestamp reportTime;

    @Basic(optional = false)
    @Column(name = "onboardid")
    private String onboardId;

    @Basic(optional = false)
    @Column(name = "systemgen")
    private boolean systemGen;
    
    @OneToMany(mappedBy = "trainConsist")
    @OrderBy("connectionorder asc")
    private List<Vehicle> vehicles;
    
    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public Timestamp getReportTime() {
        return reportTime;
    }

    public void setReportTime(Timestamp reportTime) {
        this.reportTime = reportTime;
    }
    
    public String getOnboardId() {
        return onboardId;
    }

    public void setOnboardId(String onboardId) {
        this.onboardId = onboardId;
    }

    /*@OneToMany
     @JoinTable(name = "trainhierarchy",
     joinColumns = {@JoinColumn(name = "consistid")},
     inverseJoinColumns = {@JoinColumn(name = "vehicleid")})
     @OrderBy("vehicleOrder asc")*/
    /*public void setVehicles(List<Vehicle> vehicles) {
     this.vehicles = vehicles;
     }*/
    public void addVehicle(Vehicle vehicle) {
        if (!vehicles.contains(vehicle)) {
            vehicles.add(vehicle);
            if (vehicle.getTrainConsist() != null) {
                vehicle.getTrainConsist().getVehicles().remove(vehicle);
            }
            vehicle.setTrainConsist(this);
        }
    }

    /*public void addVehicles(List<Vehicle> newVehicles) {      
     for(Vehicle vehicle : newVehicles){            
     if(this.vehicles.contains(vehicle))
     break;              
     else
     this.vehicles.add(vehicle);
     }
     }*/
    @JoinColumn(name = "areaobj", referencedColumnName = "ttobjid")
    @ManyToOne(optional = false)
    private TTObject areaObj;

    public TTObject getAreaObj() {
        return areaObj;
    }

    public void setAreaObj(TTObject areaObj) {
        this.areaObj = areaObj;
    }

    @JoinColumn(name = "reportsubsystemid", referencedColumnName = "ttobjid")
    @ManyToOne(optional = false)
    private TTObject reportSubsystem;

    public TTObject getReportSubsystem() {
        return reportSubsystem;
    }

    public void setReportSubsystem(TTObject reportSubsystem) {
        this.reportSubsystem = reportSubsystem;
    }

    public String getReportSubsystemName() {
        if (reportSubsystem == null) {
            return "";
        } else {
            return reportSubsystem.getDescription();
        }
    }

    public TrainConsist() {
    }

    public TrainConsist(Integer consistId) {
        this.consistId = consistId;
    }

    public TrainConsist(Integer consistId, ConsistState trainState, int commType, int currentCommType, String comment, String trainDescriber) {
        this.consistId = consistId;
        this.trainState = trainState;
        this.commType = commType;
        this.currentCommType = currentCommType;
        this.comment = comment;
        this.trainDescriber = trainDescriber;
    }

    public Integer getConsistId() {
        return consistId;
    }

    public void setConsistId(Integer consistId) {
        this.consistId = consistId;
    }

    public ConsistState getTrainState() {
        return trainState;
    }

    public void setTrainState(ConsistState trainstate) {
        this.trainState = trainstate;
    }

    public Integer getPositionId() {
        return positionId;
    }

    public void setPositionId(Integer positionId) {
        this.positionId = positionId;
    }

    public int getCommType() {
        return commType;
    }

    public void setCommType(int commType) {
        this.commType = commType;
    }

    public int getCurrentCommType() {
        return currentCommType;
    }

    public void setCurrentCommType(int currentCommType) {
        this.currentCommType = currentCommType;
    }

    /*public Integer getLogicalLength() {
        return logicalLength;
    }

    public void setLogicalLength(Integer logicalLength) {
        this.logicalLength = logicalLength;
    }

    public Integer getCrewId() {
        return crewId;
    }

    public void setCrewId(Integer crewId) {
        this.crewId = crewId;
    }*/

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTrainDescriber() {
        return trainDescriber;
    }

    public void setTrainDescriber(String trainDescriber) {
        this.trainDescriber = trainDescriber;
    }

    public TrainType getTrainType() {
        return trainType;
    }

    public void setTrainType(TrainType trainType) {
        this.trainType = trainType;
    }
    
    public boolean getSystemGen() {
        return this.systemGen;
    }
    
    public void setSystemGen(boolean systemGen) {
        this.systemGen = systemGen;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (consistId != null ? consistId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TrainConsist)) {
            return false;
        }
        TrainConsist other = (TrainConsist) object;
        if ((this.consistId == null && other.consistId != null) || (this.consistId != null && !this.consistId.equals(other.consistId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ScheduleEntities.TrainConsist[consistid=" + consistId + "]";
    }

}
