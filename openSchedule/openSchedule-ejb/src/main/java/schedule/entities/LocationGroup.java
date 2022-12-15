/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.entities;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;

/**
 *
 * @author Pavel
 */
@Entity
@DiscriminatorValue("G")
public class LocationGroup extends TTObject {
    
    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "tthierarchy",
            joinColumns = @JoinColumn(name = "parentid"),
            inverseJoinColumns = @JoinColumn(name = "childid"))
    private List<TTObject> _childObjects;
    
    public LocationGroup() 
    {
        this._childObjects = new ArrayList<>();
    }
    
    public String getName()
    {
        return this.getDescription();
    }
    
    public void setName(String NewName)
    {
        this.setDescription(NewName);
        this.setExternalName(NewName);
        this.setScheduleName(NewName);
    }
    
    @Override
    public List<TTObject> getChildObjects() 
    {
        return this._childObjects;
    }
    
    @Override
    public int getChildCount() 
    {
        return (this._childObjects != null) ? this._childObjects.size() : 0;
    }
    
    @Override
    public void setChildObjects(List<TTObject> childObjects) 
    {
        this._childObjects = childObjects;
    }
}
