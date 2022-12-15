/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.entities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author EBIScreen
 */
@Entity
@DiscriminatorValue("S")
public class Schedule extends ScheduleBase {

    private static final long serialVersionUID = 1L;
    
    @Basic(optional = false)
    @Column(name = "starttime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;
    
    @Basic(optional = false)
    @Column(name = "endtime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;

    @JoinColumn(name = "templateid", referencedColumnName = "scheduleid", nullable = false)
    @ManyToOne
    private ScheduleTemplate scheduleTemplate;

    @OneToMany(mappedBy = "scheduleParent", targetEntity = DayInSchedule.class, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy(value = "dayCodeNo")
    private final List<DayInSchedule> dayInSchedules;

    public Schedule() {
        this.dayInSchedules = new ArrayList<>();
    }

    public int getLoadDayWidth() {
        return loadDayWidth;
    }

    public void setLoadDayWidth(int loadDayWidth) {
        this.loadDayWidth = loadDayWidth;
    }

    public ScheduleTemplate getScheduleTemplate() {
        return scheduleTemplate;
    }

    public void setScheduleTemplate(ScheduleTemplate scheduleTemplate) {
        this.scheduleTemplate = scheduleTemplate;
    }

    public List<DayInSchedule> getDayInSchedules() {
        return dayInSchedules;
    }

    /*public void setDayInSchedules(List<DayInSchedule> dayInSchedules) {
     this.dayInSchedules = dayInSchedules;
     }*/
    public boolean addDayInSchedules(DayInSchedule dayInSchedule) {
        Iterator<DayInSchedule> ite = dayInSchedules.iterator();
        DayInSchedule entity;
        boolean exist = false;
        while (ite.hasNext()) {
            entity = ite.next();
            int temp1 = entity.getDayCodeNo();
            int temp2 = dayInSchedule.getDayCodeNo();
            if (temp1 == temp2) {
                if (entity.getScheduleParent().equals(dayInSchedule.getScheduleParent())) {
                    exist = true;
                    break;
                }
            }
        }

        if (!exist) {
            this.dayInSchedules.add(dayInSchedule);
        }

        return !exist;
    }

    public void removeDayInSchedules(Calendar start, Calendar end) {
        //should we check parent?
        int startDayCode = convertToDayCode(start);
        int endDayCode = convertToDayCode(end);
        Iterator<DayInSchedule> ite = dayInSchedules.iterator();
        DayInSchedule entity;
        while (ite.hasNext()) {
            entity = ite.next();
            int daycode = entity.getDayCodeNo();
            if (daycode < startDayCode || daycode > endDayCode) {
                ite.remove();
                entity.setScheduleParent(null);
            }
        }

        /**/
    }

    @SuppressWarnings("deprecation")
    public Date getStartTime() {
        if (startTime != null) {
            startTime.setHours(0);
            startTime.setMinutes(0);
            startTime.setSeconds(0);
        }
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    @SuppressWarnings("deprecation")
    public Date getEndTime() {
        if (startTime != null) {
            endTime.setHours(23);
            endTime.setMinutes(59);
            endTime.setSeconds(59);
        }
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /*public boolean isScheduleSpanValid(Calendar start, Calendar end) {
        start.setTime(startTime);
        end.setTime(endTime);

        if (start.after(end)) {
            return false;
        }
        return true;
    }*/
    public int getNumberOfDays() {
        return dayInSchedules.size();
    }

    public static int convertToDayCode(Calendar dayInCalendar) {

        return dayInCalendar.get(Calendar.DAY_OF_YEAR) + 1000 * dayInCalendar.get(Calendar.YEAR);
    }

    public int generateDaysInSchedule(Calendar start, Calendar end) {
        removeDayInSchedules(start, end);
        Calendar curDay = (Calendar) start.clone();
        int iDaysGenerated = 0;

        while (!curDay.after(end)) {
            int dayCode = convertToDayCode(curDay);

            int javaDayCode = curDay.get(Calendar.DAY_OF_WEEK);
            // Later just correct with mathematics when I know the numbers
            int weekdayCode = 0;

            switch (javaDayCode) {
                case Calendar.MONDAY: {
                    weekdayCode = 1;
                }
                break;
                case Calendar.TUESDAY: {
                    weekdayCode = 2;
                }
                break;
                case Calendar.WEDNESDAY: {
                    weekdayCode = 3;
                }
                break;
                case Calendar.THURSDAY: {
                    weekdayCode = 4;
                }
                break;
                case Calendar.FRIDAY: {
                    weekdayCode = 5;
                }
                break;
                case Calendar.SATURDAY: {
                    weekdayCode = 6;
                }
                break;
                case Calendar.SUNDAY: {
                    weekdayCode = 7;
                }
                break;
            }
            // All clear let's generate new dayin schedule...

            DayInSchedule newDay = new DayInSchedule();
            newDay.setDayCodeNo(dayCode);
            newDay.setWeekDayNo(weekdayCode);
            newDay.setWeekNumber(curDay.get(Calendar.WEEK_OF_YEAR));

            iDaysGenerated++;
            newDay.setDaySeqNo(iDaysGenerated);

            DayType dayType = FindDefault(weekdayCode);
            //newDay.setDayType(dayType);
            newDay.addDayTypeInList(dayType);
            newDay.setScheduleParent(this);
            addDayInSchedules(newDay);

            curDay.add(Calendar.DAY_OF_MONTH, 1);
        }

        /*Collections.sort(dayInSchedules, new Comparator<DayInSchedule>() {
            @Override
            public int compare(DayInSchedule day1, DayInSchedule day2) {
                return day1.getDayCodeNo().compareTo(day2.getDayCodeNo());
            }
        });*/
        sort(dayInSchedules);
        Iterator<DayInSchedule> ite = dayInSchedules.iterator();
        int seqNo = 1;
        DayInSchedule entity;
        while (ite.hasNext()) {
            entity = ite.next();
            entity.setDaySeqNo(seqNo++);
        }
        return iDaysGenerated;
    }
    
    protected void sort(List<DayInSchedule> days) {
        DayInSchedule temp;

        int n = days.size();

        for (int i = 0; i < n; i++) {
            for (int j = 1; j < (n - i); j++) {

                if (days.get(j - 1).getDayCodeNo() > days.get(j).getDayCodeNo()) {
                    //days the elements!
                    temp = days.get(j - 1);
                    days.set(j - 1, days.get(j));
                    days.set(j, temp);
                }

            }
        }
    }
}
