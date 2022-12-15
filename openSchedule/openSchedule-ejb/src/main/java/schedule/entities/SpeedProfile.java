package schedule.entities;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author Pavel
 */
@Entity
@Table(name = "speedprofiles")
public class SpeedProfile extends EditableEntity implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @NotNull
    @Column(name = "profileid")
    private Integer profileid;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "traintype")
    private TrainType traintype;
    
    @Column(name = "trainspeedprofile")
    private Integer trainspeedprofile;
    
    @Column(name = "percentagefrommax")
    private Integer percentagefrommax;
    
    @Size(max = 254)
    @Column(name = "description")
    private String description;
    
    @Size(max = 254)
    @Column(name = "username")
    private String username;

    public Integer getProfileid()
    {
        return profileid;
    }

    public void setProfileid( Integer profileid )
    {
        this.profileid = profileid;
    }

    public TrainType getTraintype()
    {
        return traintype;
    }

    public void setTraintype( TrainType traintype )
    {
        this.traintype = traintype;
    }

    public Integer getTrainspeedprofile()
    {
        return trainspeedprofile;
    }

    public void setTrainspeedprofile( Integer trainspeedprofile )
    {
        this.trainspeedprofile = trainspeedprofile;
    }

    public Integer getPercentagefrommax()
    {
        return percentagefrommax;
    }

    public void setPercentagefrommax( Integer percentagefrommax )
    {
        this.percentagefrommax = percentagefrommax;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername( String username )
    {
        this.username = username;
    }
    
}
