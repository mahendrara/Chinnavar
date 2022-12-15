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

/**
 *
 * @author spirttin
 */
@Named("trainPropertyConverter")
@RequestScoped
public class TrainPropertyConverter implements Converter {
    @Inject
    private schedule.sessions.TrainPropertyFacade ejbFacade;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value != null) {
            String pattern = "(.*?propid=)(\\d+)(.*)";
            String id = value.replaceAll(pattern, "$2");
            return ejbFacade.find(Integer.valueOf(id));
        }

        throw new IllegalArgumentException("value not found!");
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return value.toString();
    }
}
