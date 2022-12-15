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
import schedule.entities.ScheduleTemplate;

/**
 *
 * @author Jia Li
 */
@Named("scheduleTemplateConverter")
@RequestScoped
public class ScheduleTemplateConverter implements Converter{

    @Inject
    private schedule.sessions.ScheduleTemplateFacade ejbFacade;
    
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        
        if(value != null) {
            String temp = "[scheduleid=";
            int start = value.indexOf(temp);
            int end = value.indexOf("]", start);
            String scheduleId = value.substring(start + temp.length(), end);
            ScheduleTemplate scheduleTemplate = ejbFacade.find(Integer.valueOf(scheduleId));
            return scheduleTemplate;
        }
         throw new IllegalArgumentException("value not found!");
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if(value !=  null)
            return value.toString();
        else 
            return null;
    }
}