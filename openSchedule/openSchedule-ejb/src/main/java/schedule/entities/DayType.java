/*
 * To change this template, choose Tools | Templates
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
import javax.persistence.Transient;

/**
 *
 * @author EBIScreen
 */
@Entity
@Table(name = "daytypes")
public class DayType extends EditableEntity implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "daytypeid")
    private Integer dayTypeId;

    @Column(name = "scheduleid", updatable = false, insertable = false)
    private Integer scheduleid;
    
    @Basic(optional = false)
    @Column(name = "abbr")
    private String abbr;

    @Basic(optional = false)
    @Column(name = "description")
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "scheduleid", referencedColumnName = "scheduleid")
    private ScheduleBase scheduleParent;

    @Transient
    private DayType clonedFrom;

    @Transient
    private int helperCounter;

    public int getHelperCounter()
    {
        return helperCounter;
    }

    public void incHelperCounter()
    {
        helperCounter++;
    }

    public void setHelperCounter( int helperCounter )
    {
        this.helperCounter = helperCounter;
    }

    public DayType getClonedFrom()
    {
        return clonedFrom;
    }

    public void setClonedFrom( DayType copiedFrom )
    {
        this.clonedFrom = copiedFrom;
    }

    public ScheduleBase getScheduleParent()
    {
        return scheduleParent;
    }

    public void setScheduleParent( ScheduleBase scheduleParent )
    {
        this.scheduleParent = scheduleParent;
    }

    public DayType()
    {
    }

    public DayType( Integer dayTypeId )
    {
        this.dayTypeId = dayTypeId;
    }

    public DayType( Integer dayTypeId, String abbr, String description )
    {
        this.dayTypeId = dayTypeId;
        this.abbr = abbr;
        this.description = description;
    }

    public Integer getDayTypeId()
    {
        return dayTypeId;
    }

    public void setDayTypeId( Integer dayTypeId )
    {
        this.dayTypeId = dayTypeId;
    }

    public String getAbbr()
    {
        return abbr;
    }

    public void setAbbr( String abbr )
    {
        this.abbr = abbr;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public void cloneDataToNonPersited( DayType cloned )
    {
        cloned.setAbbr( this.abbr );
        cloned.setDescription( this.description );
        cloned.setClonedFrom( this );

    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (dayTypeId != null ? dayTypeId.hashCode() : 0);
        return hash;
    }

    private Integer getScheduleId()
    {
        return this.scheduleid;
    }
    
    @Override
    public boolean equals( Object object )
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if( !(object instanceof DayType) )
        {
            return false;
        }
        DayType other = (DayType) object;
        if( (this.dayTypeId == null && other.dayTypeId != null) || (this.dayTypeId != null && !this.dayTypeId.equals( other.dayTypeId )) )
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "ScheduleEntities.Daytype[daytypeid=" + dayTypeId + "]";
    }
}
