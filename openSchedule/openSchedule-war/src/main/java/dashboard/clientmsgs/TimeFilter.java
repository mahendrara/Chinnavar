/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dashboard.clientmsgs;

import dashboard.clientmsgs.DashboardMsg.TimeType;
import java.util.Date;

/**
 *
 * @author jiali
 */
public class TimeFilter {

    private Date dateStart;
    private Date dateEnd;
    private TimeType timeType;

    public TimeType getTimeType() {
        return timeType;
    }

    public void setTimeType(TimeType timeType) {
        this.timeType = timeType;
    }

    public Date getDateStart() {
        return dateStart;
    }

    public void setDateStart(Date dateStart) {
        this.dateStart = dateStart;
    }

    public Date getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(Date dateEnd) {
        this.dateEnd = dateEnd;
    }
}