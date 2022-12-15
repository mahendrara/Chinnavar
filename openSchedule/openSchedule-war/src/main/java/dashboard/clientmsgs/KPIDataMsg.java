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

public class KPIDataMsg extends DashboardMsg {

    private ArrayList<KPIDataItem> kpiDataItems;

    public ArrayList<KPIDataItem> getKpiDataItems() {
        return kpiDataItems;
    }

    public void setKpiDataItems(ArrayList<KPIDataItem> kpiDataItems) {
        this.kpiDataItems = kpiDataItems;
    }
}

