package schedule.uiclasses;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import schedule.entities.BaseUser;
import schedule.sessions.UserFacade;
import schedule.uiclasses.util.JsfUtil;
import schedule.uiclasses.util.UiText;

/**
 *
 * @author EBIScreen
 */
@Named("loginBean")
@SessionScoped
public class LoginBean implements Serializable {
    
    /**
     * Creates a new instance of LoginBean
     */
    public LoginBean() {
        Calendar now = Calendar.getInstance();
        starttime = now.getTime();
        
        Enumeration resEnum;
        try {
            resEnum = Thread.currentThread().getContextClassLoader().getResources(JarFile.MANIFEST_NAME);
            while (resEnum.hasMoreElements()) {
                try {
                    URL url = (URL) resEnum.nextElement();
                    InputStream is = url.openStream();
                    if (is != null) {
                        Manifest manifest = new Manifest(is);
                        Attributes mainAttribs = manifest.getMainAttributes();
                        if(mainAttribs.getValue("Implementation-Title").contains("Open Schedule")) {
                            version = mainAttribs.getValue("Implementation-Version");
                            break;
                        }
                        
                    }
                } catch (Exception e) {
                    // Silently ignore wrong manifests on classpath?
                }
            }
        } catch (IOException e1) {
            // Silently ignore wrong manifests on classpath?
        }

    }
    private Date starttime;
    private Date loginTime;
    private String userName;
    private String passWord;
    private String version="";

    @Inject
    private UserFacade ejbUserFacade;

    // This is logged in user
    private BaseUser user = null;

    @Inject
    private UiText uiText;

    @Inject
    FacesContext facesContext;
    //@Inject
    //ServletContext servletContext;
     
    public Date getLoginTime() {
        return loginTime;
    }

    public void setLogintime(Date loginTime) {
        this.loginTime = loginTime;
    }

    public Date getStarttime() {
        return starttime;
    }

    public void setStarttime(Date starttime) {
        this.starttime = starttime;
    }

    /**
     * Login password -- To be used in login view only
     */
    public String getPassWord() {
        return passWord;
    }

    /**
     * Login password -- To be used in login view only
     */
    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    /**
     * Login username -- To be used in login view only
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Login username -- To be used in login view only
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public BaseUser getUser() {
        return user;
    }

    public void setUser(BaseUser user) {
        this.user = user;
    }

    public boolean isUserLoggedIn() {
        return user != null;
    }

    public String loginAction() {
        try {
            HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
            request.login(userName, passWord);

            user = ejbUserFacade.findUserByUserName(userName);
            passWord = null; // Invalidate password

            if (user != null) {
                return "login-ok";
            } else {
                JsfUtil.addErrorMessage(uiText.get("LoginFailed"));
            }
        } catch (ServletException e) {
            JsfUtil.addErrorMessage(e, uiText.get("LoginFailed"));
        }

        return "";
    }

    public String logoutAction() {
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
        if (session != null) {
            session.invalidate();
        }

        this.user = null;
        return "logout-ok";
    }

    public Date getServerTime() {
        return Calendar.getInstance().getTime();
    }

    public String getVersion() {
        return version;
    }
}
