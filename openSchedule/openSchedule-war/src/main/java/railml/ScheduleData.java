package railml;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import schedule.entities.BasicTrip;
import schedule.entities.DayType;
import schedule.entities.PlannedService;
import schedule.entities.Schedule;
import schedule.entities.ScheduleTemplate;

/**
 * Wrapper class for TMS data.
 * 
 * @author Pavel
 */
public class ScheduleData
{
    private Schedule schedule;
    private List<DayType> dayTypes;
    private List<PlannedService> plannedServices;
    private List<BasicTrip> templates;

    public ScheduleData()
    {
        this.dayTypes = new ArrayList<>();
        this.plannedServices = new ArrayList<>();
        this.templates = new ArrayList<>();
    }
    
    public Schedule getSchedule()
    {
        return this.schedule;
    }

    public void setSchedule( Schedule schedule )
    {
        this.schedule = schedule;
    }

    public List<DayType> getDayTypes()
    {
        return this.dayTypes;
    }

    public void setDayTypes( List<DayType> dayTypes )
    {
        this.dayTypes = dayTypes;
    }
    
    public void addDayType( DayType dayType )
    {
        this.dayTypes.add( dayType );
    }
    
    public List<PlannedService> getPlannedServices()
    {
        return this.plannedServices;
    }
    
    public void setPlannedServices( List<PlannedService> plannedServices )
    {
        this.plannedServices = plannedServices;
    }

    void setTemplates( List<BasicTrip> templates )
    {
        this.templates = templates;
    }
    public String getInfoString()
    {
        StringBuilder sb = new StringBuilder(500);    
        if(schedule!=null)
        {
            sb.append("Schedule : ");
            sb.append(schedule.getDescription());
            sb.append(" Start : ");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            String startDate = dateFormat.format(schedule.getStartTime()); //2016/11/16

            sb.append(startDate);
            
            sb.append(" Days : ");
            sb.append(schedule.getNumberOfDays());
            
            ScheduleTemplate template = schedule.getScheduleTemplate();
            sb.append(" Template : ");        
            if(template==null)
            {
                sb.append("none");     
            }
            else
            {
                sb.append(template.getDescription());
            }
            return sb.toString();            
        }
        return "null";
    }    
    List<BasicTrip> getTemplates()
    {
        return this.templates;
    }
}
