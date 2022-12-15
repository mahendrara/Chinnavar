/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.uiclasses;

import javax.enterprise.context.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.inject.Inject;
import javax.inject.Named;
import schedule.entities.TimeBlock;

/**
 *
 * @author Jia Li
 */
@Named("timeBlockConverter")
@RequestScoped
public class TimeBlockConverter implements Converter {

    @Inject
    private schedule.sessions.TimeBlockFacade ejbFacade;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value != null) {
            String temp = "[blockId=";
            int start = value.indexOf(temp);
            int end = value.indexOf("]", start);
            String blockId = value.substring(start + temp.length(), end);
            TimeBlock timeBlock = ejbFacade.find(Integer.valueOf(blockId));
            return timeBlock;
        }

        throw new IllegalArgumentException("value not found!");
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return value.toString();
    }
}
