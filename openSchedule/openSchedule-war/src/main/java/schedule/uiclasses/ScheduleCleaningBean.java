/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.uiclasses;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.ParameterMode;
import javax.persistence.Persistence;
import javax.persistence.StoredProcedureQuery;
import schedule.entities.ScheduledDay;
import schedule.messages.Operation;
import schedule.messages.XmlMessageSender;
import schedule.sessions.PlannedServiceFacade;
import schedule.sessions.ScheduledDayFacade;
import schedule.sessions.ScheduledDutyFacade;
import schedule.sessions.ScheduledServiceFacade;
import schedule.sessions.TripFacade;

/**
 *
 * @author Jia Li
 */
@Stateless
public class ScheduleCleaningBean {

    //@Inject
    //private ScheduleFacade scheduleFacade;

    @Inject
    private ScheduledDayFacade scheduledDayFacade;

    @Inject
    private PlannedServiceFacade plannedServiceFacade;
    
    @Inject
    private ScheduledServiceFacade scheduledServiceFacade;
    
    @Inject
    private ScheduledDutyFacade scheduledDutyFacade;

    //@Inject
    //private ScheduledServiceController scheduledServiceController;

    @Inject
    private TripFacade basicTripFacade;

    @Inject
    private XmlMessageSender xmlMessageSender;

    static final Logger logger = Logger.getLogger("ScheduleCreatorBean");
    private static final String PERSISTENCE_UNIT_NAME = "openSchedule-warPU";
    private static EntityManagerFactory factory;

    //Cleaning history might take hours or even longer time for the first time. Transaction timeout will rollback all the changes.
    //@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void cleanHistoricalSchedules() {

        logger.log(Level.INFO, String.format("ScheduleCleaningBean.cleanHistoricalSchedules STARTED: [%s]", new Date()));
  
        try {
            ScheduledDayFilter scheduledDayFilter = new ScheduledDayFilter();
            scheduledDayFilter.setArchived(true);
            ScheduledDay lastArchivedScheduledDay = scheduledDayFacade.findFirst(scheduledDayFilter);
            factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
            EntityManager em = factory.createEntityManager();
            StoredProcedureQuery storedProcedure = em.createStoredProcedureQuery("deletehistoricaldata");
            storedProcedure.registerStoredProcedureParameter(1,  Integer.class, ParameterMode.OUT);
            storedProcedure.execute();
            Integer earliestDay = (Integer) storedProcedure.getOutputParameterValue(1);
            System.out.println("ScheduleCleaningBean.cleanHistoricalSchedules: before "+ earliestDay + " history kept in the database");
            
            storedProcedure = em.createStoredProcedureQuery("deleteinvalidplanneddata");
            storedProcedure.execute();
            System.out.println("ScheduleCleaningBean.cleanHistoricalSchedules: invalid planned data cleaned");
            
            storedProcedure = em.createStoredProcedureQuery("deleteinvalidtriptemplates");
            storedProcedure.execute();
            System.out.println("ScheduleCleaningBean.cleanHistoricalSchedules: invalid trip templates cleaned");
            em.close();                         

            //Maintenance script might clean the invalid templates and planned services, we need to see the changes from open schedule
            ScheduledDayFilter scheduledDayFilter2 = new ScheduledDayFilter();
            if(lastArchivedScheduledDay != null)
                scheduledDayFilter2.setStartScheduledDayCode(lastArchivedScheduledDay.getScheduledDayCode());
            scheduledDayFilter2.setEndScheduledDayCode(earliestDay);
            List<ScheduledDay> scheduledDays = scheduledDayFacade.findAll(scheduledDayFilter2);
            if(scheduledDays != null && !scheduledDays.isEmpty()) {
                xmlMessageSender.sendScheduledDayMsg(scheduledDays, Operation.ARCHIVE);
                System.out.println("ScheduleCleaningBean.cleanHistoricalSchedules: delete history before " + (earliestDay-1));
            }
            plannedServiceFacade.evictAll();
            scheduledServiceFacade.evictAll();
            scheduledDayFacade.evictAll();
            basicTripFacade.evictAll();
            scheduledDutyFacade.evictAll();
        } catch (Exception e) {
            logger.log(Level.SEVERE, String.format("ScheduleMaintenanceBean.deleteHistoricalSchedules ENCOUNTERED ERROR: %s", e.getMessage()));
        }
        logger.log(Level.INFO, String.format("ScheduleMaintenanceBean.deleteHistoricalSchedules FINISHED: [%s]", new Date()));
    }
}
