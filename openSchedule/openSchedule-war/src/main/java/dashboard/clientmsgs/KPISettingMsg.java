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
public class KPISettingMsg extends DashboardMsg{
    String instantPres;
    String historyPres;
    private ArrayList<KPISetting> kpiSettings;

    public ArrayList<KPISetting> getKpiSettings() {
        return kpiSettings;
    }

    public void setKpiSettings(ArrayList<KPISetting> kpiSettings) {
        this.kpiSettings = kpiSettings;
    }

    public String getInstantPres() {
        return instantPres;
    }

    public void setInstantPres(String instantPres) {
        this.instantPres = instantPres;
    }

    public String getHistoryPres() {
        return historyPres;
    }

    public void setHistoryPres(String historyPres) {
        this.historyPres = historyPres;
    }
    
}

