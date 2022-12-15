package schedule.uiclasses;

import java.io.Serializable;
import schedule.entities.ScheduledService;
import schedule.entities.ScheduledDay;
import schedule.entities.TripAction;

import schedule.uiclasses.util.JsfUtil;
import schedule.sessions.ScheduledServiceFacade;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import schedule.entities.ActionRunTrip;
import schedule.entities.ActionTrainFormation;
import schedule.entities.BasicTrip;
import schedule.entities.Duty;
import schedule.entities.PlannedDuty;
import schedule.entities.Schedule;
import schedule.entities.ScheduledDuty;
import schedule.entities.ScheduledTrip;
import schedule.entities.TimedTrip;
import schedule.entities.TripAction.MainActionTypeEnum;
import schedule.messages.ServiceItem;
import schedule.sessions.AbstractFacade;
import schedule.sessions.ScheduledDutyFacade;
import schedule.sessions.ScheduledTripFacade;
import schedule.uiclasses.util.UiText;

@SuppressWarnings("unchecked")
@Named("scheduledServiceController")
@SessionScoped
//public class ScheduledServiceController extends BaseServiceController<ScheduledService, ScheduledServiceFilter<ScheduledService>> implements Serializable {
public class ScheduledServiceController extends BaseServiceController<ScheduledService, ScheduledServiceFilter> implements Serializable {

    @Inject
    private schedule.sessions.ScheduledServiceFacade ejbFacade;
    @Inject
    private schedule.sessions.ScheduledDayFacade ejbScheduledDayFacade;
    @Inject
    private ScheduledTripFacade ejbScheduledTripFacade;
    @Inject
    private ScheduledDutyFacade ejbScheduledDutyFacade;
    @Inject
    private UiText uiText;

    public ScheduledServiceController() {
        super(new ScheduledServiceFilter());
    }

    @PostConstruct
    protected void myInit() {
        // Initialize my local variables

        // Set Scheduled day for filter
        Calendar cal = Calendar.getInstance();
        ScheduledDay day = ejbScheduledDayFacade.find(cal);
        if (day == null) {
            // Dummy filter that not all schedules are searched
            day = new ScheduledDay(Schedule.convertToDayCode(cal), 0, 0, 0, false, 0);
        }
        getFilter().setScheduledDayFilter(day);

        getFilter().setValidFilter(Boolean.TRUE);
    }

    @Override
    protected Logger getLogger() {
        return Logger.getLogger(ScheduledServiceController.class.getName());
    }

    /**
     * This needs to be called when new day is created. That this dialog will
     * try to search given day for filtering and use
     *
     * @param date
     */
    public void newDayCreated(Date date) {
        // Externally created new day, try to get created day if given day 
        // is selected
        Calendar newCal = Calendar.getInstance();
        newCal.setTime(date);
        if (Schedule.convertToDayCode(newCal) == getFilter().getScheduledDayFilter().getScheduledDayCode()) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            ScheduledDay day = ejbScheduledDayFacade.find(cal);
            // Update only if can be found, otherwise nothing to do, as
            // dummy day already exists
            if (day != null) {
                getFilter().setScheduledDayFilter(day);
            }
        } else {
        }
    }

    @Override
    protected ScheduledServiceFacade getFacade() {
        return ejbFacade;
    }

    @Override
    public ScheduledService constructNewItem() {
        ScheduledService service = new ScheduledService();
        service.setEditing(true);
        service.setCreating(true);
        service.setTripId(0);
        service.setTimesAreValid(false);
        service.setUtcTimes(true);
        service.setConsumed(false);
        service.setOrigoSecs(0);
        service.setValid(Boolean.TRUE);
        service.setActionsFromTemplate(false);

        return service;
    }

    @Override
    public String addNew() {
        // Dummy scheduled day has 0's everything but daycode
        if (getFilter().getScheduledDayFilter().getStartDay() == 0) {
            JsfUtil.addErrorMessage(uiText.get("Error_ScheduledDayNotExist"));
            return null;
        }

        ScheduledService service = constructNewItem();
        service.setDay(getFilter().getScheduledDayFilter());
        service.setTripType(this.serviceTripType);

        List<ScheduledService> oldArray = (List<ScheduledService>) getItems().getWrappedData();
        oldArray.add(0, service);
        getItems().setWrappedData(oldArray);
        return null;
    }

    /**
     * Called from XHTML
     *
     * @param service
     * @return
     */
    @Override
    public String addNewServiceAction(ScheduledService service) {
        TripAction tripAction = null;
        switch (MainActionTypeEnum.parse(getSelectedMainActionType().getMactionTypeId())) {
            case RUN_TRIP:
                tripAction = new ActionRunTrip();
                break;
            case TRAIN_FORMATION:
                tripAction = new ActionTrainFormation();
                break;
        }

        if (tripAction != null) {
            tripAction.setTimeFromTripStart(super.getLastActionRunTripEndTime(service));
            tripAction.setTimesValid(Boolean.TRUE);
            tripAction.setConsumed(false);
            tripAction.setCreating(true);
            tripAction.setEditing(true);
            tripAction.setActionType(getSelectedActionType());
            tripAction.setMinSecs(tripAction.getActionType().getDefaultMinSecs());
            tripAction.setPlannedSecs(0);
            if (tripAction instanceof ActionRunTrip) {
                ScheduledTrip scheduledTrip = new ScheduledTrip();
                scheduledTrip.setConsumed(false);
                scheduledTrip.setTimesAreValid(false);
                scheduledTrip.setUtcTimes(true);
                scheduledTrip.setAreaObj(service.getAreaObj());
                scheduledTrip.setVersion(1);
                // Make sure, that not dummy day is added
                if (getFilter().getScheduledDayFilter().getStartDay() != 0) {
                    scheduledTrip.setDay(getFilter().getScheduledDayFilter()); // This is Scheduled specified
                }
                scheduledTrip.setValid(Boolean.TRUE);
                scheduledTrip.setPlannedState(service.getPlannedState());

                /*if ((getSelectedActionType().getActionSubtype() == ActionRunTrip.ActionRunTripTypeEnum.RUN_COMMERCIAL_TRIP.getValue())
                        || (getSelectedActionType().getActionSubtype() == ActionRunTrip.ActionRunTripTypeEnum.TURN_AND_RUN_COMMERCIAL_TRIP.getValue())) {
                    scheduledTrip.setTripType(this.commercialTripType);
                } else if ((getSelectedActionType().getActionSubtype() == ActionRunTrip.ActionRunTripTypeEnum.RUN_NONCOMMERCIAL_TRIP.getValue()
                        || (getSelectedActionType().getActionSubtype() == ActionRunTrip.ActionRunTripTypeEnum.TURN_AND_RUN_NONCOMMERCIAL_TRIP.getValue()))) {
                    scheduledTrip.setTripType(this.nonCommercialTripType);
                }*/
                ((ActionRunTrip) tripAction).setRefTrip(scheduledTrip);
                scheduledTrip.setServiceAction((ActionRunTrip) tripAction);

                if (this.getPossibleActionLocations().isEmpty() == false) {
                    tripAction.setTimetableObject(this.getPossibleActionLocations().get(0));
                    if (tripAction.hasSecondObject()) {
                        ((ActionRunTrip) tripAction).setTimetableObject2(getPossibleActionLocations2().get(0));
                    }
                    BasicTrip template = scheduledTrip.getTripTemplate();
                    if (template != null) {
                        scheduledTrip.setTripType(GetToTripType(template.getTripType()));
                        if (template.getNumberOfActions() == 1) {
                            scheduledTrip.setDescription(template.getTripAction(1).getTimetableObject().getText(languageBean.getOSLocale()));
                        } else if (template.getNumberOfActions() > 1) {
                            scheduledTrip.setDescription(template.getTripAction(1).getTimetableObject().getText(languageBean.getOSLocale())
                                    + " - " + template.getTripAction(template.getNumberOfActions()).getTimetableObject().getText(languageBean.getOSLocale()));
                        } else {
                            scheduledTrip.setDescription(" - ");
                        }
                    }
                }
            }

            service.addTripAction(tripAction, 0);
            tripAction.setTrip(service);

            // Set first template
            if (tripAction instanceof ActionRunTrip) {
                List<BasicTrip> templates = getTripTemplates(tripAction);
                if (templates != null && templates.size() > 0) {
                    ((TimedTrip) ((ActionRunTrip) tripAction).getRefTrip()).setTripTemplate(templates.get(0));
                    templateChanged(tripAction); // Update tripaction times
                }
            }

            recreateModel();
        }

        return null;
    }

    public boolean canScheduledServiceBeDeleted(ScheduledService ss) {
        // Loop through ScheduledService's ScheduledDays and if there are any 
        // which are archived or active, ScheduledService is not allowed to be deleted
        //if (ss.getDay() != null && (/*ss.getDay().getArchived() ||*/ ss.getDay().isActive())) {
        //    return false;
        //}

        // ScheduledService can be deleted
        return true;
    }

    @Override
    public String destroy(ScheduledService service) {
        if (!service.isCreating()) {
            performDestroy(service);
            updateState(service);
        }
        recreateModel();
        return "List";
    }

    private void performDestroy(ScheduledService scheduledService) {
        try {
            List<Duty> duties = null;
            userTransaction.begin();
            entityManager.joinTransaction();
            if (supportDuty) {
                duties = deleteFullTripDriveDuties(scheduledService);
                //This has to be done after service is removed
                //this.getDutyFacade().evictAll();
            }
            //see if to remove sourcetrip
            /*if (scheduledService.getSourceTrip() != null && !scheduledService.getSourceTrip().isValid()) {
                ScheduledServiceFilter filter = new ScheduledServiceFilter();
                filter.setSourceTripFilter(scheduledService.getSourceTrip());
                if (getFacade().findAll(filter).size() == 1) {
                    getFacade().remove(scheduledService);
                    //planned service might be multiple scheduled services used, we should not delete it?
                    //schedule.entities.PlannedService pS = scheduledService.getSourceTrip();
                    //ejbPlannedServiceFacade.remove(pS);
                }
            }*/

 /*Iterator<TripAction> iterator = scheduledService.getTripActions().iterator();
            TripAction tripAction;
            while (iterator.hasNext()) {
                tripAction = iterator.next();
                if (tripAction instanceof ActionRunTrip && (((ActionRunTrip) tripAction).getRefTrip() != null)) {
                    ((ActionRunTrip) tripAction).getRefTrip().setValid(Boolean.FALSE);
                }
            }*/
            //scheduledService.setValid(Boolean.FALSE);
            //getFacade().remove(scheduledService);
            scheduledService = entityManager.merge(scheduledService);
            entityManager.remove(scheduledService);
            scheduledService.setRemoving(true);
            userTransaction.commit();
            this.getDutyFacade().evictAll();
            updateState();
            JsfUtil.addSuccessMessage(uiText.get("ServiceDeleted"));
            xmlMessageSender.sendServiceDutyChangeMsg(scheduledService, duties);

        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            try {
                userTransaction.rollback();
            } catch (IllegalStateException | SecurityException | SystemException ex1) {
                getLogger().log(Level.SEVERE, null, ex1);
            }
            getLogger().log(Level.SEVERE, null, ex);
            JsfUtil.addErrorMessage(ex, uiText.get("PersistenceErrorOccured"));
        }
    }

    // Offer hook for ScheduledServiceController to fill DAY
    @Override
    protected void fillServiceAction(ScheduledService service, TimedTrip trip) {
        ((ScheduledTrip) trip).setDay(service.getDay());
    }

    public Date getDateFilter() {
        Date d = getFilter().getScheduledDayFilter().getDateOfDay();
        return d;
    }

    public void setDateFilter(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        ScheduledDay day = this.ejbScheduledDayFacade.find(cal);
        if (day == null) {
            // Dummy item that there is some filter
            day = new ScheduledDay(Schedule.convertToDayCode(cal), 0, 0, 0, false, 0);
        }

        getFilter().setScheduledDayFilter(day);
    }

    @Override
    protected void updateState(ScheduledService service) {
        ejbFacade.evict(service);
        ejbScheduledTripFacade.evictAll();

        // Be cunning and update selected item!
        service = ejbFacade.find(service.getTripId());
        prepareView(service);
        if (service != null) {
            service.updateActionTimes();
        }
    }

    @Override
    protected void updateState() {
        ejbFacade.evictAll();
        ejbScheduledTripFacade.evictAll();
        //ejbTripFacade.evictAll();
    }

    /*@Override
    public void destroyRefTrip(Trip trip) {
        if (!(trip instanceof ScheduledTrip)) {
            return;
        }

        ScheduledTrip refTrip = (ScheduledTrip) trip;
        ejbScheduledTripFacade.remove(refTrip);
        //see if to remove sourcetrip
        if (refTrip.getSourceTrip() != null && !refTrip.getSourceTrip().isValid()) {
            ScheduledTripFilter filter = new ScheduledTripFilter();
            filter.setSourceTripFilter(refTrip.getSourceTrip());
            if (this.ejbScheduledTripFacade.findAll(filter).isEmpty()) {
                PlannedTrip pS = refTrip.getSourceTrip();
                ejbPlannedTripFacade.remove(pS);
            }
        }
    }*/
    protected ServiceItem.ServiceType getServiceType() {
        return ServiceItem.ServiceType.SCHEDULED;
    }

    @Override
    public ScheduledDuty getDuty(TimedTrip timedTrip, String description) {
        ScheduledDutyFilter filter = new ScheduledDutyFilter();
        filter.setDescription(description);
        filter.setDayType(timedTrip.getDayTypeList().get(0));
        filter.setScheduledDay(((ScheduledTrip) timedTrip).getDay());
        filter.setValid(timedTrip.isValid());
        ScheduledDuty duty = ejbScheduledDutyFacade.findFirst(filter);
        if (duty == null) {
            duty = new ScheduledDuty();
            duty.setCreating(true);
            duty.setDescription(description);
            duty.setDayTypeList(timedTrip.getDayTypeList());
            duty.setTripType(this.normalDutyTripType);
            duty.setActionsFromTemplate(false);
            duty.setAreaObj(timedTrip.getAreaObj());
            duty.setDurationSecs(0);
            duty.setConsumed(false);
            duty.setUtcTimes(timedTrip.getUtcTimes());
            duty.setOrigoSecs(0);
            duty.setTimesAreValid(false);
            duty.setValid(timedTrip.isValid());
            if (timedTrip.getFullTripDriveDutyAction() != null) {
                duty.setSourceTrip((PlannedDuty) timedTrip.getFullTripDriveDutyAction().getTrip());
            }
            duty.setDay(((ScheduledTrip) timedTrip).getDay());
        }
        return duty;
    }

    @Override
    protected AbstractFacade getDutyFacade() {
        return this.ejbScheduledDutyFacade;
    }
}
