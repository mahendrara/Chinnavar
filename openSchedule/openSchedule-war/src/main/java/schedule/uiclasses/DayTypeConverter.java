/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.uiclasses;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.inject.Inject;
import javax.inject.Named;
import schedule.entities.DayType;

/**
 *
 * @author devel1
 */
@Named("dayTypeConverter")
@RequestScoped
public class DayTypeConverter implements Converter {

    @Inject
    private schedule.sessions.DayTypeFacade ejbFacade;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String dayTypeId) {
        System.out.println(dayTypeId);
        if(dayTypeId == null || dayTypeId.isEmpty()) {
            return null;
        }
        try {
            return ejbFacade.find(Integer.valueOf(dayTypeId));
        }catch (Exception e) {
            throw new IllegalArgumentException("value not found!");
        }
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if(value == null) {
            return "";
        }
        try {
            // check if value is a dayTypeId integer
            int dayTypeId = Integer.parseInt(value.toString());
            return value.toString();
        }
        catch (NumberFormatException e) {
            DayType daytype = (DayType) value;
            if(daytype.getDayTypeId() != null) {
                return daytype.getDayTypeId().toString();
            }else {
                throw new ConverterException(new FacesMessage("Invalid daytype ID"));
            }
        }

    }
}
