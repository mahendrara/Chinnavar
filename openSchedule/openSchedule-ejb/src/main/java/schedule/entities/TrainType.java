package schedule.entities;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author jiali
 */
@Entity
@Table(name = "traintypes")
public class TrainType extends EditableEntity implements Serializable
{

    @OneToMany
    @JoinTable(name = "traintypes",
               joinColumns = @JoinColumn(name = "derivedfrom"),
               inverseJoinColumns = @JoinColumn(name = "traintypeid"))
    private List<TrainType> trainSubTypes;

    @JoinColumn(name = "derivedfrom", referencedColumnName = "traintypeid")
    @ManyToOne
    private TrainType derivedFrom;

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @NotNull
    @Column(name = "traintypeid")
    private Integer trainTypeId;

    @Size(max = 254)
    @Column(name = "description")
    private String description;

    @Column(name = "userName")
    private String username;

    @Column(name = "obiftypeid")
    private Integer obiftypeid;

    @Column(name = "defaultlength")
    private Integer defaultlength;

    @Column(name = "useasvehicle")
    private boolean useAsVehicle;

    @Column(name = "assumetraction")
    private boolean assumeTraction;

    @Column(name = "onboardradio")
    private boolean onboardRadio;

    @Column(name = "movingblockcapable")
    private boolean movingBlockCapable;

    @Column(name = "gpsposcapable")
    private boolean gpsPosCapable;

    @Column(name = "dynamicconsist")
    private boolean dynamicConsist;

    @Column(name = "canbeconsist")
    private boolean canBeConsist;

    @Column(name = "canbechild")
    private boolean canBeChild;

    @Column(name = "hasspeedprofile")
    private boolean hasSpeedProfile;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "traintypeid", referencedColumnName = "traintypeid")
    private List<TrainTypeProperty> trainTypeProperties;

    @OneToMany
    @JoinColumn(name = "traintypeid", referencedColumnName = "traintypeid")
    private List<TrainProperty> trainProperties;

    @Column(name = "simulate")
    @Convert(converter = DriveTimeEstimationConverter.class)
    private DriveTimeEstEnum simulate;

    @Column(name = "globaldefault")
    private boolean globalDefault;

    @OneToMany
    @JoinColumn(name = "textKey", referencedColumnName = "userName")
    @MapKey(name = "locale")
    private Map<Locale, TextKey> textKeys;

    public enum DriveTimeEstEnum
    {
        onlystatistics,
        validstatistics,
        nostatistics;
    }

    public TrainType()
    {
    }

    public TrainType( Integer trainTypeId )
    {
        this.trainTypeId = trainTypeId;
    }

    public TrainType( Integer trainTypeId, String description, String userName )
    {
        this.trainTypeId = trainTypeId;
        this.description = description;
        this.username = userName;
    }

    public Integer getTrainTypeId()
    {
        return trainTypeId;
    }

    public void setTrainTypeId( Integer trainTypeId )
    {
        this.trainTypeId = trainTypeId;
    }

    public String getDescription()
    {
        return description;
    }

    public String getText( Locale locale )
    {
        if( textKeys.containsKey( locale ) )
        {
            return textKeys.get( locale ).getText();
        }
        return getDescription();
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (trainTypeId != null ? trainTypeId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals( Object object )
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if( !(object instanceof TrainType) )
        {
            return false;
        }
        TrainType other = (TrainType) object;
        if( (this.trainTypeId == null && other.trainTypeId != null) || (this.trainTypeId != null && !this.trainTypeId.equals( other.trainTypeId )) )
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "ScheduleEntities.Traintype[traintypeid=" + trainTypeId + "]";
    }

    public List<TrainType> getTrainSubTypes()
    {
        return trainSubTypes;
    }

    public void setTrainSubTypes( List<TrainType> trainSubTypes )
    {
        this.trainSubTypes = trainSubTypes;
    }

    public TrainType getDerivedFrom()
    {
        return derivedFrom;
    }

    public void setDerivedFrom( TrainType derivedFrom )
    {
        this.derivedFrom = derivedFrom;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername( String username )
    {
        this.username = username;
    }

    public boolean isDynamicConsist()
    {
        return dynamicConsist;
    }

    public void setDynamicConsist( boolean dynamicConsist )
    {
        this.dynamicConsist = dynamicConsist;
    }

    public boolean isCanBeConsist()
    {
        return canBeConsist;
    }

    public void setCanBeConsist( boolean canBeConsist )
    {
        this.canBeConsist = canBeConsist;
    }

    public Integer getObiftypeid()
    {
        return obiftypeid;
    }

    public void setObiftypeid( Integer obiftypeid )
    {
        this.obiftypeid = obiftypeid;
    }

    public Integer getDefaultlength()
    {
        return defaultlength;
    }

    public void setDefaultlength( Integer defaultlength )
    {
        this.defaultlength = defaultlength;
    }

    public boolean isUseAsVehicle()
    {
        return useAsVehicle;
    }

    public boolean isAssumeTraction()
    {
        return assumeTraction;
    }

    public void setAssumeTraction( boolean assumeTraction )
    {
        this.assumeTraction = assumeTraction;
    }

    public void setUseAsVehicle( boolean useAsVehicle )
    {
        this.useAsVehicle = useAsVehicle;
    }

    public List<TrainProperty> getTrainProperties()
    {
        if( trainProperties == null )
        {
            trainProperties = new LinkedList<>();
        }

        return trainProperties;
    }

    public void setTrainProperties( List<TrainProperty> trainProperties )
    {
        this.trainProperties = trainProperties;
    }

    public List<TrainProperty> getInheritedTrainProperties()
    {
        List<TrainProperty> list = new LinkedList<>();
        if( derivedFrom != null )
        {
            list.addAll( derivedFrom.getInheritedTrainProperties() );
            list.addAll( derivedFrom.getTrainProperties() );
        }
        return list;
    }

    /**
     * gets all train properties which can be added to this traintype
     *
     * @return
     */
    public List<TrainProperty> getValidTrainProperties()
    {
        List<TrainProperty> list = getInheritedTrainProperties();
        list.addAll( getTrainProperties() );
        return list;
    }

    /**
     * Gets all train properties which are not yet set to this traintype
     *
     * @return
     */
    public List<TrainProperty> getUnsetTrainProperties()
    {
        List<TrainProperty> list = getValidTrainProperties();
        if( trainTypeProperties != null )
        {
            for( TrainTypeProperty ttp : trainTypeProperties )
            {
                list.remove( ttp.getPropid() );
            }
        }
        //Don't support other types so far
        Iterator<TrainProperty> it = list.iterator();

        TrainProperty next;
        while( it.hasNext() )
        {
            next = it.next();
            switch( next.getPropertyType() )
            {
                case "integer":
                    break;
                case "string":
                    break;
                default:
                    it.remove();
                    break;
            }
        }

        return list;
    }

    public List<TrainTypeProperty> getTrainTypeProperties()
    {
        if( trainTypeProperties == null )
        {
            trainTypeProperties = new LinkedList<>();
        }

        return trainTypeProperties;
    }

    public void setTrainTypeProperties( List<TrainTypeProperty> trainTypeProperties )
    {
        this.trainTypeProperties = trainTypeProperties;
    }

    public List<TrainTypeProperty> getInheritedTrainTypeProperties()
    {
        List<TrainTypeProperty> list = new LinkedList<>();
        if( derivedFrom != null )
        {
            list.addAll( derivedFrom.getInheritedTrainTypeProperties() );
            list.addAll( derivedFrom.getTrainTypeProperties() );
        }
        return list;
    }

    @Transient
    private TrainProperty createProp = null;

    /**
     * Access to temporal variable to keep in store newly create property's type
     *
     * @return
     */
    public TrainProperty getCreateProp()
    {
        return createProp;
    }

    /**
     * Access to temporal variable to keep in store newly create property's type
     *
     * @param createProp
     */
    public void setCreateProp( TrainProperty createProp )
    {
        this.createProp = createProp;
    }

    public boolean isOnboardRadio()
    {
        return onboardRadio;
    }

    public void setOnboardRadio( boolean onboardRadio )
    {
        this.onboardRadio = onboardRadio;
    }

    public boolean isMovingBlockCapable()
    {
        return movingBlockCapable;
    }

    public void setMovingBlockCapable( boolean movingBlockCapable )
    {
        this.movingBlockCapable = movingBlockCapable;
    }

    public boolean isGpsPosCapable()
    {
        return gpsPosCapable;
    }

    public void setGpsPosCapable( boolean gpsPosCapable )
    {
        this.gpsPosCapable = gpsPosCapable;
    }

    public boolean isCanBeChild()
    {
        return canBeChild;
    }

    public void setCanBeChild( boolean canBeChild )
    {
        this.canBeChild = canBeChild;
    }

    public boolean isHasSpeedProfile()
    {
        return hasSpeedProfile;
    }

    public void setHasSpeedProfile( boolean hasSpeedProfile )
    {
        this.hasSpeedProfile = hasSpeedProfile;
    }

    public DriveTimeEstEnum getSimulate()
    {
        return simulate;
    }

    public void setSimulate( DriveTimeEstEnum simulate )
    {
        this.simulate = simulate;
    }

    public boolean getGlobalDefault()
    {
        return this.globalDefault;
    }

    public void setGlobalDefault( boolean globalDefault )
    {
        this.globalDefault = globalDefault;
    }

}
