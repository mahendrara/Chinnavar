/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dashboard.clientmsgs;

import dashboard.clientmsgs.DashboardMsg.Direction;
import dashboard.clientmsgs.DashboardMsg.KPIType;
import java.util.Date;

/**
 *
 * @author jiali
 */
public class KPIDataItem {

    public KPIDataItem(KPIType kpiType, int locationId, String locationTxt, Direction direction, int trainId, Date time, Float data) {
        this.kpiType = kpiType;
        this.locationId = locationId;
        this.locationTxt = locationTxt;
        this.direction = direction;
        this.trainId = trainId;
        this.time = time;
        this.data = data;
    }

    private KPIType kpiType;
    private int locationId;
    private String locationTxt;
    private Direction direction;
    private int trainId;
    private Date time;
    private Float data;

    public KPIType getKpiType() {
        return kpiType;
    }

    public void setKpiType(KPIType kpiType) {
        this.kpiType = kpiType;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public String getLocationTxt() {
        return locationTxt;
    }

    public void setLocationTxt(String locationTxt) {
        this.locationTxt = locationTxt;
    }

    public int getTrainId() {
        return trainId;
    }

    public void setTrainId(int trainId) {
        this.trainId = trainId;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public Float getData() {
        return data;
    }

    public void setData(Float data) {
        this.data = data;
    }

   

    /*public ArrayList<LocationFilter> getLocations() {
     return Locations;
     }
     public void setLocations(ArrayList<LocationFilter> Locations) {
     this.Locations = Locations;
     }*/
    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
