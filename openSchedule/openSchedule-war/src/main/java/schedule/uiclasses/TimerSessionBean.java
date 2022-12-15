/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.uiclasses;

import eventlog.EventLogBean;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.TimerService;
import javax.inject.Inject;
import schedule.messages.MaintenanceItem;
import schedule.messages.XmlMessageSender;

/**
 *
 * @author Jia Li
 */
//@Singleton
public class TimerSessionBean {

    @Resource
    TimerService timerService;

    @Inject
    ScheduleCreatorBean scheduleCreatorBean;
    @Inject
    ScheduleCleaningBean scheduleCleaningBean;
    @Inject
    private XmlMessageSender xmlMessageSender;
    @Inject
    private EventLogBean eventLog;

    static final Logger logger = Logger.getLogger("TimerSessionBean");


    //@Schedule(hour = "*", minute = "*/5")
    //@Schedule(dayOfWeek = "*", hour = "3", persistent=false)
    public void timer_ScheduledDayCreator() {

        Date now = new Date();
        logger.log(Level.INFO, String.format("TimerSessionBean.timer_ScheduledDayCreator STARTED: [%s]", now));

        try {
            scheduleCreatorBean.createScheduledDay(null);
            logger.log(Level.INFO, String.format("TimerSessionBean.timer_ScheduledDayCreator NO EXCEPTIONS"));
        } catch (Exception e) {
            logger.log(Level.SEVERE, String.format("TimerSessionBean.timer_ScheduledDayCreator ENCOUNTERED ERROR: %s", e.getMessage()));
        }
        now = new Date();
        logger.log(Level.INFO, String.format("TimerSessionBean.createScheduledDay FINISHED: [%s]", now));

    }
    
    //@Schedule(dayOfWeek = "*", hour = "3", minute="30",persistent=false)
    public void timer_ScheduleMaintenance() {

        Date now = new Date();
        logger.log(Level.INFO, String.format("TimerSessionBean.timer_ScheduledMaintenanceBean STARTED: [%s]", now));

        try {
            scheduleCleaningBean.cleanHistoricalSchedules();
            logger.log(Level.INFO, String.format("TimerSessionBean.timer_ScheduledMaintenanceBean NO EXCEPTIONS"));
        } catch (Exception e) {
            logger.log(Level.SEVERE, String.format("TimerSessionBean.timer_ScheduledMaintenanceBean ENCOUNTERED ERROR: %s", e.getMessage()));
        }
        now = new Date();
        logger.log(Level.INFO, String.format("TimerSessionBean.createScheduledDay FINISHED: [%s]", now));

    }
    public void timer_PingScheduler() {

        Date now = new Date();
        logger.log(Level.INFO, String.format("TimerSessionBean.timer_PingScheduler STARTED: [%s]", now));

        try {
            
            if(xmlMessageSender.sendMaintenanceMsg(MaintenanceItem.Operation.TEST_JMS_LINK)==false)
            {
                eventLog.addEventFromObject(this,true,"JMS link test sending failed");
            }
            else
            {
                //out commnet if need for dev enivronment...
                //eventLog.addEventFromObject(this,true,"JMS Link test message sent");
            }
        } 
        catch (Exception e) {
            String msg = "JMS link test sending failed : " + e.getMessage();             
            eventLog.addEventFromObject(this,true,msg);
            logger.log(Level.SEVERE, String.format("TimerSessionBean.timer_PingScheduler ENCOUNTERED ERROR: %s", e.getMessage()));
        }
        now = new Date();
        // Let's log this, so we can notice problem with transaction timing out etc..
        logger.log(Level.INFO, String.format("TimerSessionBean.timer_PingScheduler FINISHED: [%s]", now));        
    }
}
