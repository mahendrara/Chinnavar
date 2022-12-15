/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dashboard.clientmsgs;

import dashboard.clientmsgs.DashboardMsg.KPIType;

/**
 *
 * @author jiali
 */
public class HistoryDataPoint {
    private KPIType kpiType;
    private int locationId;
    private String locationTxt;
    private Float data;

    public HistoryDataPoint(int locationId, String locationTxt, Float data, KPIType kpiType) {
        this.locationId = locationId;
        this.locationTxt = locationTxt;
        this.data = data;
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

    public Float getData() {
        return data;
    }

    public void setData(Float data) {
        this.data = data;
    }

    public KPIType getKpiType() {
        return kpiType;
    }

    public void setKpiType(KPIType kpiType) {
        this.kpiType = kpiType;
    }
    
    
}

