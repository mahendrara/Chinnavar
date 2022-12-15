package schedule.uiclasses;

import java.io.Serializable;
import java.util.Collection;
import schedule.uiclasses.util.JsfUtil;
import java.util.Objects;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import schedule.entities.Employee;
import schedule.entities.TTObjectState;
import schedule.entities.TTObjectType;
import schedule.messages.Operation;
import schedule.messages.XmlMessageSender;
import schedule.sessions.EmployeeFacade;
import schedule.uiclasses.util.UiText;

@Named("driverController")
@SessionScoped
public class DriverController extends FilterController<Employee, EmployeeFilter> implements Serializable {

    @Inject
    private schedule.sessions.EmployeeFacade ejbFacade;
    @Inject
    private schedule.sessions.TTObjectTypeFacade ejbTTObjectTypeFacade;
    @Inject
    private schedule.sessions.TTObjectStateFacade ejbTTObjectStateFacade;
    @Inject
    private UiText uiText;
    @Inject
    private XmlMessageSender xmlMessageSender;
    
    // Filter selections in UI
    private TTObjectState objectStateAll = null;
    private Collection<TTObjectState> ttObjectStates;

    public DriverController() {
        super(new EmployeeFilter());
        // Filter 'all'-item
        objectStateAll = new TTObjectState(0);
    }
    
    @PostConstruct
    public void init() {
        // Generate items for ObjectType-menu
        ttObjectStates = ejbTTObjectStateFacade.findAll();
    }

    @Override
    protected EmployeeFacade getFacade() {
        return ejbFacade;
    }
    
    @Override
    public void clearFilters() {
        getFilter().setObjectStateFilter(null);
    }
        
    @Override
    public String destroy(Employee emp) {
        try {
            // Do not try to delete item which is not stored yet
            if (!emp.isCreating())
            {
                getFacade().remove(emp);
                JsfUtil.addSuccessMessage(uiText.get("DriverDeleted"));
            }
            recreateModel();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
        }
        return "List";
    }
        
    @Override
    public String save(Employee emp) {
        if (!isUniqueDriverName(emp)) {
            JsfUtil.addErrorMessage(uiText.get("Error_DriverNameMustBeUnique"));
            return "List";
        }
        
        try {
            if (emp.isCreating()) {
                getFacade().create(emp);
                JsfUtil.addSuccessMessage(uiText.get("DriverCreated"));
                xmlMessageSender.sendEmployeeMsg(emp, Operation.CREATE);
            } 
            else {
                getFacade().edit(emp);
                JsfUtil.addSuccessMessage(uiText.get("DriverUpdated"));
                xmlMessageSender.sendEmployeeMsg(emp, Operation.MODIFY);
            }
            emp.setCreating(false);
            emp.setEditing(false);
            recreateModel();
            return "List";
            
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
            return null;
        }
    }
    
    @Override
    public Employee constructNewItem() {
        // Create new item and fill data to it
        Employee current = new Employee();
        TTObjectType ot = ejbTTObjectTypeFacade.find(TTObjectType.TTObjectTypeEnum.DRIVER.getDBCode());
        current.setTTObjectType(ot);
        current.setEditing(true);
        current.setCreating(true);
        current.setExtPhysicalId(0);
        current.setExtPhysicalId2(0);
        current.setExtLogicalId(0);
        return current;
    }
    
    private boolean isUniqueDriverName(Employee emp) {
        for (Employee e : ejbFacade.findAll()) {
            if (emp.getDriverName().equals(e.getDriverName()) && !Objects.equals(e.getTTObjId(), emp.getTTObjId()))
                return false;
        }
        return true;
    }
    
    public TTObjectState getObjectStateAll() {
        objectStateAll.setDescription(uiText.get("FilterAll"));
        return this.objectStateAll;
    }
   
    public TTObjectState getSelectedTTObjectState() {
        return getFilter().getObjectStateFilter();
    }
    
    public void setSelectedTTObjectState(TTObjectState selectedTTObjectState) {
        getFilter().setObjectStateFilter(selectedTTObjectState);
    }
    
    public Collection<TTObjectState> getTtObjectStates() {
        return ttObjectStates;
    }
    
    public void setTtObjectTypes(Collection<TTObjectState> ttObjectStates) {
        this.ttObjectStates = ttObjectStates;
    }
}
