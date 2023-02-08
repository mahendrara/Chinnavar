package com.schedule.base.model;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.schedule.model.TextKey;
import com.schedule.model.TripAction;
import com.schedule.model.TripAction.MainActionTypeEnum;

/**
 *
 * @author Jia Li
 */
@Entity
@Table(name = "mainactiontypes")
public class MainActionType implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "mactiontypeid")
    private Integer mactionTypeId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 254)
    @Column(name = "description")
    private String description;
    @Column(name = "used")
    private Boolean used;
    
    @Column(name = "userName")
    private String userName;

    @OneToMany
    @JoinColumn(name = "textKey", referencedColumnName = "userName")
    @MapKey(name = "locale")
    private Map<Locale, TextKey> textKeys;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "mainActionType")
    private List<ActionType> actionTypes;

    public MainActionType() {
    }

    public MainActionType(Integer mactionTypeId) {
        this.mactionTypeId = mactionTypeId;
    }

    public MainActionType(Integer mactionTypeId, String description) {
        this.mactionTypeId = mactionTypeId;
        this.description = description;
    }

    public Integer getMactionTypeId() {
        return mactionTypeId;
    }

    public void setMactionTypeId(Integer mactionTypeId) {
        this.mactionTypeId = mactionTypeId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getText(Locale locale) {
        if (textKeys.containsKey(locale)) {
            return textKeys.get(locale).getText();
        }
        return getDescription();
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (mactionTypeId != null ? mactionTypeId.hashCode() : 0);
        return hash;
    }

    public List<ActionType> getActionTypes() {
        return actionTypes;
    }

    public void setActionSubTypes(List<ActionType> actionTypes) {
        this.actionTypes = actionTypes;
    }

    public Boolean isUsed() {
        return used;
    }

    public void setUsed(Boolean used) {
        this.used = used;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MainActionType)) {
            return false;
        }
        MainActionType other = (MainActionType) object;
        if ((this.mactionTypeId == null && other.mactionTypeId != null) || (this.mactionTypeId != null && !this.mactionTypeId.equals(other.mactionTypeId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "schedule.entities.MainActionType[mactiontypeid=" + mactionTypeId + "]";
    }
    
    public MainActionTypeEnum getMainActionTypeEnum() {
        return TripAction.MainActionTypeEnum.parse(mactionTypeId);
    }
}

