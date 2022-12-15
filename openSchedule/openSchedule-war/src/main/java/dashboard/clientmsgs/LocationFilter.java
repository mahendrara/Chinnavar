/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dashboard.clientmsgs;

import dashboard.clientmsgs.DashboardMsg.Direction;

/**
 *
 * @author jiali
 */
public class LocationFilter {

    //private LocationFilter.LocationType locationType;
    private Direction direction;
    private Integer locationId;

    /*public LocationFilter.LocationType getLocationType() {
     return locationType;
     }

     public void setLocationType(LocationFilter.LocationType locationType) {
     this.locationType = locationType;
     }*/
    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public Integer getLocationId() {
        return locationId;
    }

    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }
}
