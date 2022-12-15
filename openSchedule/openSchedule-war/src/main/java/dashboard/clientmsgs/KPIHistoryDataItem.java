/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dashboard.clientmsgs;

import dashboard.clientmsgs.DashboardMsg.Direction;
import dashboard.clientmsgs.DashboardMsg.KPIType;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author jiali
 */
public class KPIHistoryDataItem {
    private KPIType kpiType;
    private Date time;
    private Direction direction;
    private int trainId;
    private ArrayList<HistoryDataPoint> kpiDataPoints;

    public int getTrainId() {
        return trainId;
    }

    public void setTrainId(int trainId) {
        this.trainId = trainId;
    }

    public ArrayList<HistoryDataPoint> getKpiDataPoints() {
        return kpiDataPoints;
    }

    public void setKpiDataPoints(ArrayList<HistoryDataPoint> kpiDataPoints) {
        this.kpiDataPoints = kpiDataPoints;
    }

    public KPIHistoryDataItem(KPIType kpiType, Date time, Direction direction, int trainId) {
        this.kpiType = kpiType;
        this.time = time;
        this.direction = direction;
        this.trainId = trainId;
    }

    public KPIType getKpiType() {
        return kpiType;
    }

    public void setKpiType(KPIType kpiType) {
        this.kpiType = kpiType;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }


    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }
}

