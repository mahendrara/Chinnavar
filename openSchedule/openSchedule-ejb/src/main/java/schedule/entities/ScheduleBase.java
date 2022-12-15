/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

/**
 *
 * @author EBIScreen
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "objectclass", discriminatorType = DiscriminatorType.CHAR)
@Table(name = "schedules")
/*@NamedQueries({
    @NamedQuery(name = "Schedule.findAll", query = "SELECT s FROM Schedule s"),
    @NamedQuery(name = "Schedule.findByDescription", query = "SELECT s FROM Schedule s WHERE s.description = :description"),
    @NamedQuery(name = "Schedule.findByStarttime", query = "SELECT s FROM Schedule s WHERE s.starttime = :starttime"),
    @NamedQuery(name = "Schedule.findByEndtime", query = "SELECT s FROM Schedule s WHERE s.endtime = :endtime"),
    @NamedQuery(name = "Schedule.findByValid", query = "SELECT s FROM Schedule s WHERE s.valid = :valid"),
    @NamedQuery(name = "Schedule.findByScheduleid", query = "SELECT s FROM Schedule s WHERE s.scheduleid = :scheduleid")})*/
public class ScheduleBase extends EditableEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scheduleid")
    private Integer scheduleid;
    @Basic(optional = false)
    @Column(name = "description")
    private String description;
    @Basic(optional = false)
    @Column(name = "valid")
    private boolean valid;
    @Basic(optional = false)
    @Column(name = "loaddaywidth")
    protected int loadDayWidth;

    @OneToMany(mappedBy = "scheduleParent", targetEntity = DayType.class, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DayType> dayTypes;

    @OneToMany(mappedBy = "scheduleParent", targetEntity = DefaultDayRule.class, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy(value = "weekdayNo")
    private List<DefaultDayRule> defaultDayRules;

    public List<DefaultDayRule> getDefaultDayRules() {
        return defaultDayRules;
    }

    public void setDefaultDayRules(List<DefaultDayRule> defaultDayRules) {
        this.defaultDayRules = defaultDayRules;
    }

    public List<DayType> getDayTypes() {
        return dayTypes;
    }

    public void setDaytypes(List<DayType> dayTypes) {
        this.dayTypes = dayTypes;
    }

    public ScheduleBase() {
        this.dayTypes = new ArrayList<>();
        this.defaultDayRules = new ArrayList<>();
        this.loadDayWidth = 0;
    }

    public ScheduleBase(Integer scheduleid) {
        this.dayTypes = new ArrayList<>();
        this.defaultDayRules = new ArrayList<>();
        this.loadDayWidth = 0;
        this.scheduleid = scheduleid;
    }

    public ScheduleBase(Integer scheduleid, String description, boolean valid) {
        this.scheduleid = scheduleid;
        this.description = description;
        this.valid = valid;
        this.loadDayWidth = 0;
        this.dayTypes = new ArrayList<>();
        this.defaultDayRules = new ArrayList<>();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public Integer getScheduleId() {
        return scheduleid;
    }

    public void setScheduleId(Integer scheduleid) {
        this.scheduleid = scheduleid;
    }

    public void add(DayType dayType) {
        dayTypes.add(dayType);
        dayType.setScheduleParent(this);
    }
    
    public void remove(DayType dayType) {
        
        if (dayType.isCreating() == false) {
            Iterator<DayType> iterator = this.dayTypes.iterator();//sorted list
            while (iterator.hasNext()) {
                DayType temp = iterator.next();
                if (dayType.getDayTypeId() == temp.getDayTypeId()) {
                    iterator.remove();
                } 
                
        //dayTypes.remove(dayType);
        //dayType.setScheduleParent(null);
            }
        }
    }

    public void add(DefaultDayRule dayrule) {
        defaultDayRules.add(dayrule);
        dayrule.setScheduleParent(this);
    }

    public void cloneDayCodesTo(ScheduleBase target) {
        //This is actually clone daycodes from schedule(id=1) template: finish oneToMany Relationship between schedule and DayType
        Iterator<DayType> iteDaytypes = this.dayTypes.iterator();

        while (iteDaytypes.hasNext()) {
            DayType cur = iteDaytypes.next();
            DayType cloned = new DayType();

            target.add(cloned);

            cur.cloneDataToNonPersited(cloned);
            cloned.setScheduleParent(target);
        }

        //This is actually clone daycodes from schedule(id=1) template: finish oneToMany Relationship between schedule and DefaultDayRule
        Iterator<DefaultDayRule> iteRules = this.defaultDayRules.iterator();

        while (iteRules.hasNext()) {
            DefaultDayRule curRule = iteRules.next();
            DefaultDayRule clonedRule = new DefaultDayRule();

            target.add(clonedRule);

            curRule.cloneDataToNonPersited(clonedRule);
            clonedRule.setScheduleParent(target);

            // To connect created rule to right instance we
            // we have to help here..
            DayType myEntity = curRule.getDayType();
            DayType clonedEntity = target.FindCloneOf(myEntity);
            clonedRule.setDayType(clonedEntity);
        }
    }
    
    public void createEmptyDayCodes(ScheduleBase target) {
        for(int i= 1; i< 8; i++) {
            DefaultDayRule dayRule = new DefaultDayRule();
            dayRule.setWeekdayNo(i);
            target.add(dayRule);
        }
    }

    public DayType FindDayType(int dayTypeId) {
        Iterator<DayType> iteDaytypes = this.dayTypes.iterator();

        while (iteDaytypes.hasNext()) {
            DayType cur = iteDaytypes.next();
            if (cur.getDayTypeId() == dayTypeId) {
                return cur;
            }
        }
        return null;
    }

    public DayType FindCloneOf(DayType orginal) {

        Iterator<DayType> iteDaytypes = this.dayTypes.iterator();

        while (iteDaytypes.hasNext()) {

            DayType cur = iteDaytypes.next();

            if (cur.getClonedFrom() == orginal) {
                return cur;
            }

        }
        return null;
    }

    public DayType FindDefault(int weekday) {
        Iterator<DefaultDayRule> iteRules = this.defaultDayRules.iterator();

        while (iteRules.hasNext()) {
            DefaultDayRule curRule = iteRules.next();

            if (curRule.getWeekdayNo() == weekday) {
                return curRule.getDayType();
            }
        }
        return null;
    }

    /*public DayType FindDefault() {
        Iterator<DefaultDayRule> iteRules = this.defaultDayRules.iterator();

        while (iteRules.hasNext()) {
            DefaultDayRule curRule = iteRules.next();

            //if(curRule.getWeekdayno()==weekday)
            {
                return curRule.getDayType();
            }
        }
        return null;
    }*/

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (scheduleid != null ? scheduleid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Schedule)) {
            return false;
        }
        ScheduleBase other = (ScheduleBase) object;
        if ((this.scheduleid == null && other.scheduleid != null) || (this.scheduleid != null && !this.scheduleid.equals(other.scheduleid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ScheduleEntities.Schedule[scheduleid=" + scheduleid + "]";
    }
}
