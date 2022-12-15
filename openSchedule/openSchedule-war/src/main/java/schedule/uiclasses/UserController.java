/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.uiclasses;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import schedule.entities.BaseUser;
import schedule.entities.User;
import schedule.entities.UserGroup;
import schedule.sessions.AbstractFacade;
import schedule.sessions.UserFacade;
import schedule.sessions.UserGroupFacade;
import schedule.uiclasses.util.JsfUtil;
import schedule.uiclasses.util.UiText;

/**
 *
 * @author spirttin
 */
//@DeclareRoles("admin")
@Named("UserController")
@SessionScoped
public class UserController extends FilterController<BaseUser, UserFilter> implements Serializable {

    @Inject
    UserFacade ejbFacade;
    @Inject
    UiText uiText;
    @Inject
    UserGroupFacade ejbUserGroupFacade;

    // BaseUser filters
    UserController() {
        super(new UserFilter('U')); // Use filter which access only to real users
    }

    @PostConstruct
    public void init() {
    }

    @Override
    protected AbstractFacade<BaseUser> getFacade() {
        return ejbFacade;
    }

    @Override
    public BaseUser constructNewItem() {
        // Create new item and fill data to it
        User current = new User();
        current.setEditing(true);
        current.setCreating(true);
        return current;
    }

    private boolean isUniqueUserName(BaseUser user) {
        for (BaseUser u : ejbFacade.findAll()) {
            if (user.getUsername().equals(u.getUsername()) && !Objects.equals(u.getUserid(), user.getUserid())) {
                return false;
            }
        }
        return true;
    }

    static private final String PASSWORD_CHAR = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final Random PASSWORD_RANDOM = new Random();

    private String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(PASSWORD_CHAR.charAt(PASSWORD_RANDOM.nextInt(PASSWORD_CHAR.length())));
        }
        return sb.toString();
    }

    @Override
    public String save(BaseUser item) {
        if (!isUniqueUserName(item)) {
            JsfUtil.addErrorMessage(uiText.get("Error_UserNameMustBeUnique"));
            return "List";
        }

        try {
            if (item.isCreating()) {
                // Create password
                String newPassword = randomString(8);
                if (!item.createPassword(newPassword)) {
                    JsfUtil.addErrorMessage(uiText.get("Error_PasswordGenerationFailed"));
                    return "List";
                }
                item.setCreating(false);
                item.setEditing(false);
                getFacade().create(item);
                // Add user to groups
//                for(UserGroup ug : item.getUserGroupCollection()) {
                //                  ug.getUserCollection().add(item);
                //                ejbUserGroupFacade.edit(ug);
                //          }

                JsfUtil.addSuccessMessage(uiText.get("UserCreated"));
                JsfUtil.addSuccessMessage(uiText.get("NewPassword") + " " + newPassword);
            } else {
                item.setEditing(false);
                getFacade().edit(item);
                JsfUtil.addSuccessMessage(uiText.get("UserModified"));
            }

            recreateModel();
            getFacade().evictAll();
            return "List";

        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
            return null;
        }
    }

    @Override
    public String destroy(BaseUser item) {
        if (item.isCreating()) {
            recreateModel();
            return "List";
        } else {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    public List<BaseUser> getUserTemplates() {
        return getFacade().findAll(new UserFilter('T'));
    }

    public List<UserGroup> getUnsetUserGroups(BaseUser user) {
        List<UserGroup> usergroups = ejbUserGroupFacade.findAll();
        usergroups.removeAll(user.getUserGroupCollection());
        return usergroups;
    }

    public boolean hasUnsetUserGroups(BaseUser user) {
        return !getUnsetUserGroups(user).isEmpty();
    }

    public void addNewUserGroup(BaseUser user) {
        // New usergroup to be added is within user
        user.addUserGroup(user.getCreateUserGroup());

        try {
            // Make DB
            getFacade().edit(user);
            JsfUtil.addSuccessMessage(uiText.get("UserModified"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
        }
    }

    public void removeUserGroup(BaseUser user, UserGroup group) {
        user.getUserGroupCollection().remove(group);
        try {
            getFacade().edit(user);
            JsfUtil.addSuccessMessage(uiText.get("UserModified"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
        }
    }

    public void resetPassword(BaseUser user) {
        // Create password
        String newPassword = randomString(8);
        if (!user.createPassword(newPassword)) {
            JsfUtil.addErrorMessage(uiText.get("PasswordGenerationFailed"));
            return;
        }
        try {
            // Make DB
            getFacade().edit(user);
            JsfUtil.addSuccessMessage(uiText.get("UserModified"));
            JsfUtil.addSuccessMessage(uiText.get("NewPassword") + " " + newPassword);
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, uiText.get("PersistenceErrorOccured"));
        }
    }
}
