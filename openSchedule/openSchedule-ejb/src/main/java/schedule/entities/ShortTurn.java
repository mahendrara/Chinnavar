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
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author spirttin
 */
@Entity
@Table(name = "shortturns")
@XmlRootElement
/*@NamedQueries({
    @NamedQuery(name = "Shortturns.findAll", query = "SELECT s FROM Shortturns s"),
    @NamedQuery(name = "Shortturns.findById", query = "SELECT s FROM Shortturns s WHERE s.id = :id"),
    @NamedQuery(name = "Shortturns.findByFromcurrent", query = "SELECT s FROM Shortturns s WHERE s.fromcurrent = :fromcurrent")})
*/
public class ShortTurn extends EditableEntity implements Serializable {
    @Basic(optional = false)
    @NotNull
    @Column(name = "shorturntype")
    private int shorturntype;
    @Basic(optional = false)
    @NotNull
    @Column(name = "allowpermanent")
    private boolean allowpermanent;
    @Basic(optional = false)
    @NotNull
    @Column(name = "maxtrips")
    private int maxtrips;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Column(name = "fromcurrent")
    private boolean fromCurrent;
    @JoinColumn(name = "destinationid", referencedColumnName = "ttobjid")
    @ManyToOne(optional = false)
    private TTObject destinationId;
    @JoinColumn(name = "locationid", referencedColumnName = "ttobjid")
    @ManyToOne(optional = false)
    private TTObject locationId;
    @JoinColumn(name = "toid", referencedColumnName = "ttobjid")
    @ManyToOne(optional = false)
    private TTObject toId;
    @JoinColumn(name = "fromid", referencedColumnName = "ttobjid")
    @ManyToOne(optional = false)
    private TTObject fromId;
    @JoinColumn(name = "backid", referencedColumnName = "ttobjid")
    @ManyToOne(optional = false)
    private TTObject backId;
    @JoinColumn(name = "traintypeid", referencedColumnName="traintypeid")
    @ManyToOne(optional = false)
    private TrainType trainTypeId;
    
    public ShortTurn() {
        // Default values
        allowpermanent = true;
        maxtrips = 2;
    }

    public ShortTurn(Integer id) {
        this.id = id;
        // Default values
        allowpermanent = true;
        maxtrips = 2;
    }

    public ShortTurn(Integer id, boolean fromcurrent) {
        this.id = id;
        this.fromCurrent = fromcurrent;
        // Default values
        allowpermanent = true;
        maxtrips = 2;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean getFromCurrent() {
        return fromCurrent;
    }

    public void setFromCurrent(boolean fromcurrent) {
        this.fromCurrent = fromcurrent;
    }

    public TTObject getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(TTObject destinationid) {
        this.destinationId = destinationid;
    }

    public TTObject getLocationId() {
        return locationId;
    }

    public void setLocationId(TTObject locationid) {
        this.locationId = locationid;
    }

    public TTObject getToId() {
        return toId;
    }

    public void setToId(TTObject toid) {
        this.toId = toid;
    }

    public TTObject getFromId() {
        return fromId;
    }

    public void setFromId(TTObject fromid) {
        this.fromId = fromid;
    }
    
    public TTObject getBackId() {
        return backId;
    }

    public void setBackId(TTObject backid) {
        this.backId = backid;
    }
    public TrainType getTrainTypeId() {
        return trainTypeId;
    }

    public void setTrainTypeId(TrainType trainTypeId) {
        this.trainTypeId = trainTypeId;
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
        if (!(object instanceof ShortTurn)) {
            return false;
        }
        ShortTurn other = (ShortTurn) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "schedule.entities.Shortturns[ id=" + id + " ]";
    }

    public int getShorturntype() {
        return shorturntype;
    }

    public void setShorturntype(int shorturntype) {
        this.shorturntype = shorturntype;
    }

    public boolean getAllowpermanent() {
        return allowpermanent;
    }

    public void setAllowpermanent(boolean allowpermanent) {
        this.allowpermanent = allowpermanent;
    }

    public int getMaxtrips() {
        return maxtrips;
    }

    public void setMaxtrips(int maxtrips) {
        this.maxtrips = maxtrips;
    }
}
