/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dashboard.clientmsgs;

import java.util.ArrayList;

/**
 *
 * @author jiali
 */
public class KPIFilter {

    private DashboardMsg.KPIType kpiType;
    private ArrayList<LocationFilter> lineLocations;
    private ArrayList<LocationFilter> sectionLocations;
    private ArrayList<LocationFilter> pointLocations;
    private TimeFilter timeFilter;
    private ArrayList<Integer> trains;

    public DashboardMsg.KPIType getKpiType() {
        return kpiType;
    }

    public void setKpiType(DashboardMsg.KPIType kpiType) {
        this.kpiType = kpiType;
    }

    public ArrayList<LocationFilter> getLineLocations() {
        return lineLocations;
    }

    public void setLineLocations(ArrayList<LocationFilter> lineLocations) {
        this.lineLocations = lineLocations;
    }

    public ArrayList<LocationFilter> getSectionLocations() {
        return sectionLocations;
    }

    public void setSectionLocations(ArrayList<LocationFilter> sectionLocations) {
        this.sectionLocations = sectionLocations;
    }

    public ArrayList<LocationFilter> getPointLocations() {
        return pointLocations;
    }

    public void setPointLocations(ArrayList<LocationFilter> pointLocations) {
        this.pointLocations = pointLocations;
    }

    /*public ArrayList<LocationFilter> getLocations() {
     return Locations;
     }
     public void setLocations(ArrayList<LocationFilter> Locations) {
     this.Locations = Locations;
     }*/
    public TimeFilter getTimeFilter() {
        return timeFilter;
    }

    public void setTimeFilter(TimeFilter timeFilter) {
        this.timeFilter = timeFilter;
    }

    public ArrayList<Integer> getTrains() {
        return trains;
    }

    public void setTrains(ArrayList<Integer> trains) {
        this.trains = trains;
    }
}
