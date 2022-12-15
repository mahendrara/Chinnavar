package schedule.uiclasses;

import org.primefaces.component.selectcheckboxmenu.SelectCheckboxMenu;
import schedule.uiclasses.util.UiText;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;

@FacesValidator("limitCheckboxMenuValidator")
public class LimitCheckboxMenuValidator implements Validator {

    @Inject
    private UiText uiText;

    public LimitCheckboxMenuValidator() {

    }

    @Override
    public void validate(FacesContext context, UIComponent component,
                         Object value) throws ValidatorException {
        //get limit
        Integer maxLimit = Integer.parseInt((String)component.getAttributes().get("maxLimit"));
        SelectCheckboxMenu myComponent = (SelectCheckboxMenu)component;

        if (((String[])myComponent.getSubmittedValue()).length > maxLimit) {
            FacesMessage msg = new FacesMessage("Select upto " + maxLimit + " items");
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            //myComponent.resetValue();
            throw new ValidatorException(msg);
        }
    }

}
