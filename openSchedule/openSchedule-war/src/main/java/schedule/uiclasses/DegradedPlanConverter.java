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
import schedule.entities.DegradedPlan;

/**
 *
 * @author Jia Li
 */
@Named("degradedPlanConverter")
@RequestScoped
public class DegradedPlanConverter implements Converter {

    @Inject
    private schedule.sessions.DegradedPlanFacade ejbFacade;
    
    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
        if (value != null) {
            String temp = "[ planid=";
            int start = value.indexOf(temp);
            int end = value.indexOf(" ]", start);
            String degradedPlanId = value.substring(start + temp.length(), end);
            DegradedPlan degradedPlan = ejbFacade.find(Integer.valueOf(degradedPlanId));
            return degradedPlan;
        }

        throw new IllegalArgumentException("value not found!");
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object o) {
        return o.toString();
    }
}
