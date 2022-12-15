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
public class KPIHistoryDataMsg extends DashboardMsg {

    //private boolean groupByKpi;
    private ArrayList<KPIHistoryDataItem> kpiHistoryDataItems;

    /*public KPIHistoryDataMsg(boolean groupByKpi) {
        this.groupByKpi = groupByKpi;
    }*/

    
    public ArrayList<KPIHistoryDataItem> getKpiHistoryDataItems() {
        return kpiHistoryDataItems;
    }

    public void setKpiHistoryDataItems(ArrayList<KPIHistoryDataItem> kpiHistoryDataItems) {
        this.kpiHistoryDataItems = kpiHistoryDataItems;
    }

    /*public boolean isGroupByKpi() {
        return groupByKpi;
    }

    public void setGroupByKpi(boolean groupByKpi) {
        this.groupByKpi = groupByKpi;
    }*/
    
}

