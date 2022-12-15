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
public class KPIFilterMsg extends DashboardMsg {

    private ArrayList<KPIFilter> kpiFilters;

    public ArrayList<KPIFilter> getKpiFilters() {
        return kpiFilters;
    }

    public void setKpiFilters(ArrayList<KPIFilter> kpiFilters) {
        this.kpiFilters = kpiFilters;
    }
}



