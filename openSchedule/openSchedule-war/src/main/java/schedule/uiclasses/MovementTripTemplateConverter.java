/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule.uiclasses;

import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.inject.Inject;
import javax.inject.Named;
import schedule.entities.MovementTripTemplate;

/**
 *
 * @author Jia Li
 */
@Named("movementTripTemplateConverter")
@SessionScoped
public class MovementTripTemplateConverter implements Converter,Serializable {
    @Inject
    private schedule.sessions.MovementTripTemplateFacade ejbFacade;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value != null) {
            String temp = "[tripid=";
            int start = value.indexOf(temp);
            int end = value.indexOf("]", start);
            String tripId = value.substring(start + temp.length(), end);
            MovementTripTemplate trip = ejbFacade.find(Integer.valueOf(tripId));
            return trip;
        }

        throw new IllegalArgumentException("value not found!");
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return value != null ? value.toString() : null;
    }
}
