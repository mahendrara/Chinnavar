/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule.entities;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Size;
import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author spirttin
 */
@Entity
@Table(name = "users")
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.CHAR)

/*@NamedQueries({
    @NamedQuery(name = "BaseUser.findAll", query = "SELECT u FROM BaseUser u")})
*/
public class BaseUser extends EditableEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "userid")
    private Integer userid;
    @Size(max = 254)
    @Column(name = "name")
    private String name;
    @Size(max = 254)
    @Column(name = "username")
    private String username;
    @Size(max = 254)
    @Column(name = "password")
    private String password; // This is SHA-256 digested, UTF-8, printed to Base64 password
    @Column(name = "type")
    private Character type;
    @Column(name = "active")
    private boolean active;
    @JoinTable(name = "join_users_usergroups", 
            inverseJoinColumns = {@JoinColumn(name = "groupid", referencedColumnName = "groupid", updatable = true )}, 
            joinColumns = {@JoinColumn(name = "userid", referencedColumnName = "userid", updatable = true )})
    @ManyToMany
    private Collection<UserGroup> userGroupCollection;

    public BaseUser() {
    }

    public BaseUser(Integer userid) {
        this.userid = userid;
    }

    public Integer getUserid() {
        return userid;
    }

    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (userid != null ? userid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof BaseUser)) {
            return false;
        }
        BaseUser other = (BaseUser) object;
        if ((this.userid == null && other.userid != null) || (this.userid != null && !this.userid.equals(other.userid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "schedule.entities.User[ userid=" + userid + " ]";
    }
 
    public Character getType() {
        return type;
    }

    public void setType(Character type) {
        this.type = type;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @XmlTransient
    public Collection<UserGroup> getUserGroupCollection() {
        if (userGroupCollection == null)
            userGroupCollection = new ArrayList<>();

        return userGroupCollection;
    }

    public void setUserGroupCollection(Collection<UserGroup> userGroupCollection) {
        this.userGroupCollection = userGroupCollection;
    }
    
    /**
     * Creates from normal string to hashed password to be saved in DB
     * @param password 
     * @return  
     */
    public boolean createPassword(String password) {
        try {
            MessageDigest m = MessageDigest.getInstance("SHA-256");
            m.update(password.getBytes("UTF-8"));
            byte[] passwordDigest = m.digest();
            this.password = DatatypeConverter.printBase64Binary(passwordDigest);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            Logger.getLogger(BaseUser.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
        return true;
    }
    
    @Transient
    private UserGroup createUserGroup = null;
    /**
     * Access to temporal variable to keep in store newly create usergroup
     * @return 
     */
    public UserGroup getCreateUserGroup() {
        return createUserGroup;
    }
    /**
     * Access to temporal variable to keep in store newly create usergroup
     * @param createUserGroup
     */
    public void setCreateUserGroup(UserGroup createUserGroup) {
        this.createUserGroup = createUserGroup;
    }

    public void addUserGroup(UserGroup usergroup) {
        userGroupCollection.add(usergroup);
    }

}
