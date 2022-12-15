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
import schedule.entities.DegradedPlanGroup;

/**
 *
 * @author Jia Li
 */
@Named("degradedPlanGroupConverter")
@RequestScoped
public class DegradedPlanGroupConverter implements Converter{
    @Inject
    private schedule.sessions.DegradedPlanGroupFacade ejbFacade;
    
    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
        if (value != null) {
            String temp = "[ groupId=";
            int start = value.indexOf(temp);
            int end = value.indexOf(" ]", start);
            String groupId = value.substring(start + temp.length(), end);
            DegradedPlanGroup degradedPlanGroup = ejbFacade.find(Integer.valueOf(groupId));
            return degradedPlanGroup;
        }

        throw new IllegalArgumentException("value not found!");
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object o) {
        return o.toString();
    }
}
