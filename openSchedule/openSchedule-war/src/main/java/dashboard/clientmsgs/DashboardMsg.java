/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dashboard.clientmsgs;

/**
 *
 * @author jiali
 */
public class DashboardMsg {

    public enum DashboardMsgType {

        SET_FILTERS,
        CHANGE_SETTINGS,
        KPI_INSTANT_DATA,
        KPI_HISTORY_DATA//,
        //KPI_HISTORY_DATA_BY_LOCATION
    }

    public enum KPIType {

        AVERAGE_LATENESS,
        PERCENTAGE_OF_HEADWAYS,
        KILOMETERS_DELIVERED,
        JOURNEY_TIME_STATISTICS,
        PLATFORM_WAIT_TIME
    }

    public enum TimeType {

        Now,
        DURATION
    }

    public enum Direction {

        NORTH,
        SOUTH
    }

    public enum KPILocation {

        ALL_LINES,
        METROPOLITAN,
        HAMMERSMITH_CITY,
        CIRCLE,
        DISTRICT,
        PICCADILLY
    }
    private DashboardMsgType msgType;

    public DashboardMsgType getMsgType() {
        return msgType;
    }

    public void setMsgType(DashboardMsgType msgType) {
        this.msgType = msgType;
    }
}
