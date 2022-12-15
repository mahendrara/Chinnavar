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
 * @author Pavel
 */
@Named("TTHierarchyTypeConverter")
@RequestScoped
public class TTHierarchyTypeConverter implements Converter {
    @Inject
    private schedule.sessions.TTHierarchyTypeFacade ejbFacade;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value != null) {
            String temp = "[hierarchytypeid=";
            int start = value.indexOf(temp);
            int end = value.indexOf("]", start);
            String objId = value.substring(start + temp.length(), end);
            schedule.entities.TTHierarchyType ttHierarchyType = ejbFacade.find(Integer.valueOf(objId));
            return ttHierarchyType;
        }

        throw new IllegalArgumentException("value not found!");
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return value.toString();
    }
}
