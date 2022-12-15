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
public class KPISetting {
    public enum KPIPresentation {

        INSTANT_NUMBER,
        INSTANT_GAUGE,
        INSTANT_SIMPLE_GAUGE,
        HISTORY_CHART
    }
    private KPIType kpiType;
    private KPIPresentation kpiPresentation;
    private Float upperRed;
    private Float upperAmber;
    private Float lowerAmber;
    private Float lowerGreen;

    public KPIType getKpiType() {
        return kpiType;
    }

    public void setKpiType(KPIType kpiType) {
        this.kpiType = kpiType;
    }

    public KPIPresentation getKpiPresentation() {
        return kpiPresentation;
    }

    public void setKpiPresentation(KPIPresentation kpiPresentation) {
        this.kpiPresentation = kpiPresentation;
    }

    public Float getUpperRed() {
        return upperRed;
    }

    public void setUpperRed(Float upperRed) {
        this.upperRed = upperRed;
    }

    public Float getUpperAmber() {
        return upperAmber;
    }

    public void setUpperAmber(Float upperAmber) {
        this.upperAmber = upperAmber;
    }

    public Float getLowerAmber() {
        return lowerAmber;
    }

    public void setLowerAmber(Float lowerAmber) {
        this.lowerAmber = lowerAmber;
    }

    public Float getLowerGreen() {
        return lowerGreen;
    }

    public void setLowerGreen(Float lowerGreen) {
        this.lowerGreen = lowerGreen;
    }
}
