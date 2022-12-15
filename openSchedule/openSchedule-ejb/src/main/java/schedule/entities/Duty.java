/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.entities;

import java.util.Iterator;
import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public abstract class Duty extends Trip {

    @Column(name = "plannedstartsecs")
    private Integer plannedStartSecs;
    @Column(name = "plannedstopsecs")
    private Integer plannedStopSecs;

    public Integer getPlannedStartSecs() {
        return plannedStartSecs;
    }

    public Integer getPlannedStopSecs() {
        return plannedStopSecs;
    }

    public void setPlannedStartSecs(Integer plannedStartSecs) {
        this.plannedStartSecs = plannedStartSecs;
    }

    public void setPlannedStopSecs(Integer plannedStopSecs) {
        this.plannedStopSecs = plannedStopSecs;
    }

    @Override
    public void removeTripAction(TripAction tripAction) {
        //this.getTripActions().remove(tripAction);
        if (tripAction.isCreating() == false) {
            Iterator<TripAction> iterator = this.getTripActions().iterator();//sorted list
            while (iterator.hasNext()) {
                TripAction temp = iterator.next();
                if (tripAction.getActionId() == temp.getActionId()) {
                    iterator.remove();
                } else if (temp.getSeqNo() != null && temp.getSeqNo() > tripAction.getSeqNo()) {
                    temp.setSeqNo(temp.getSeqNo() - 1);
                    temp.setTimeFromTripStart(temp.getTimeFromTripStart() - tripAction.getPlannedSecs());
                    //temp.setMinTimeFromTripStart(temp.getMinTimeFromTripStart() - tripAction.getPlannedSecs());
                }
            }
        }
    }

    public void addTripActionInOrder(TripAction tripAction) {
        int seqNo = 1;

        Iterator<TripAction> iterator = this.getTripActions().iterator();//sorted list
        while (iterator.hasNext()) {
            TripAction action = iterator.next();
            if (action instanceof ActionFullTripDriveDuty) {
                if (((ActionFullTripDriveDuty) action).getRefTrip().getPlannedStartSecs() >= ((ActionFullTripDriveDuty) tripAction).getRefTrip().getPlannedStartSecs()) {
                    action.setSeqNo(action.getSeqNo() + 1);
                    action.setTimeFromTripStart(action.getTimeFromTripStart() + tripAction.getPlannedSecs());
                } else {
                    seqNo++;
                }
            }
        }

        tripAction.setSeqNo(seqNo);
        this.tripActions.add(seqNo - 1, tripAction);
        tripAction.setTrip(this);
    }

    public void actionChanged() {
        if (tripActions.size() > 0) {
            TripAction tripAction1 = this.getTripAction(1);
            if (tripAction1 instanceof ActionFullTripDriveDuty) {
                this.setPlannedStartObj(((ActionFullTripDriveDuty) tripAction1).getRefTrip().getPlannedStartObj());
                this.setPlannedStartSecs(((ActionFullTripDriveDuty) tripAction1).getRefTrip().getPlannedStartSecs());
            }
            TripAction tripAction2 = this.getTripAction(tripActions.size());
            if (tripAction2 instanceof ActionFullTripDriveDuty) {
                this.setPlannedStopObj(((ActionFullTripDriveDuty) tripAction2).getRefTrip().getPlannedStopObj());
                this.setPlannedStopSecs(((ActionFullTripDriveDuty) tripAction2).getRefTrip().getPlannedStopSecs());
            }

            this.setDurationSecs(this.getPlannedStopSecs() - this.getPlannedStartSecs());
        } else {
            this.plannedStopSecs = plannedStartSecs;
            this.setDurationSecs(0);
            this.setPlannedStopObj(this.getPlannedStartObj());
        }
        this.updateActionTimes();
    }

    public void sortByStartTime() {
        TripAction temp;

        int n = tripActions.size();

        for (int i = 0; i < n; i++) {
            for (int j = 1; j < (n - i); j++) {

                if (((ActionFullTripDriveDuty) tripActions.get(j - 1)).getRefTrip().getPlannedStopSecs() > ((ActionFullTripDriveDuty) tripActions.get(j)).getRefTrip().getPlannedStopSecs()) {
                    //swap the elements!
                    temp = tripActions.get(j - 1);
                    tripActions.set(j - 1, tripActions.get(j));
                    tripActions.set(j, temp);
                }

            }
            tripActions.get(n - i - 1).setSeqNo(n - i);
        }
    }

    @Override
    public void updateActionTimes() {
        if (tripActions != null && tripActions.size() > 0) {
            //Collections.sort(tripActions);
            sort(tripActions);
            Iterator<TripAction> ite = tripActions.iterator();

            //int totalSecs = getOrigoSecs();
            //int totalMin = getOrigoSecs();
            while (ite.hasNext()) {
                TripAction action = ite.next();
                if (action instanceof ActionFullTripDriveDuty) {
                    ActionFullTripDriveDuty curAction = (ActionFullTripDriveDuty) action;

                    curAction.setTimeFromTripStart(curAction.getRefTrip().getPlannedStartSecs() - this.plannedStartSecs);
                    //curAction.setMinTimeFromTripStart(totalMin);

                    /*if (curAction.getPlannedSecs() != null) {
                    totalSecs += curAction.getPlannedSecs();
                }
                    if (curAction.getMinSecs() != null) {
                        totalMin += curAction.getMinSecs();
                    }*/
                }
            }
            setActionTimesValid(true);
        }
    }
}
