package schedule.uiclasses;

import java.io.Serializable;
import schedule.entities.Vehicle;
import schedule.uiclasses.util.JsfUtil;
import schedule.uiclasses.util.PaginationHelper;
import schedule.sessions.VehicleFacade;

import java.util.ResourceBundle;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.inject.Inject;
import javax.inject.Named;
import schedule.uiclasses.util.UiText;


// @TODO: No functionality/accessability 
// @TODO: Needs to extend BaseController

@Named ("vehicleController")
@SessionScoped
public class VehicleController implements Serializable{

    private Vehicle current;
    private DataModel items = null;
    @Inject 
    private schedule.sessions.VehicleFacade ejbFacade;
    @Inject
    private UiText uiText;
    
    private PaginationHelper<Vehicle> pagination;
    private int selectedItemIndex;
    private int itemsPerPage = 50;

    public VehicleController() {
    }

    public Vehicle getSelected() {
        if (current == null) {
            current = new Vehicle();
            selectedItemIndex = -1;
        }
        return current;
    }

    private VehicleFacade getFacade() {
        return ejbFacade;
    }

    public PaginationHelper<Vehicle> getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper<Vehicle>(itemsPerPage) {

                @Override
                public int getItemsCount() {
                    return getFacade().count();
                }

                @Override
                public DataModel<Vehicle> createPageDataModel() {
                    return new ListDataModel<>(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem()+getPageSize()}));
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        recreateModel();
        return "List";
    }

    public String prepareView() {
        current = (Vehicle)getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new Vehicle();
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            getFacade().create(current);
            JsfUtil.addSuccessMessage(uiText.get("VehicleCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        current = (Vehicle)getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(uiText.get("VehicleUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (Vehicle)getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreateModel();
        return "List";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "List";
        }
    }

    private void performDestroy() {
        try {
            getFacade().remove(current);
            JsfUtil.addSuccessMessage(uiText.get("VehicleDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getFacade().count();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count-1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = getFacade().findRange(new int[]{selectedItemIndex, selectedItemIndex+1}).get(0);
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }

    private void recreateModel() {
        items = null;
    }

    public String next() {
        getPagination().nextPage();
        recreateModel();
        return "List";
    }

    public String previous() {
        getPagination().previousPage();
        recreateModel();
        return "List";
    }
}
