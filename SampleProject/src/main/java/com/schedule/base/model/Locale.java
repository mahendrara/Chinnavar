package com.schedule.base.model;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.schedule.model.TextKey;

/**
 *
 * @author Jia Li
 */
@Entity
@Table(name = "locales")
public class Locale implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "lid")
    private Integer lId;
    
    @Basic(optional = false)
    @Column(name = "javalocalecode")
    private String localeCode;
    
    @Basic(optional = false)
    @Column(name = "description")
    private String description;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "locale")
    private List<TextKey> textKeys;

    public Locale() {
    }

    public Locale(Integer lId) {
        this.lId = lId;
    }

    public Locale(Integer lId, String localeCode, String description) {
        this.lId = lId;
        this.localeCode = localeCode;
        this.description = description;
    }

    public Integer getLId() {
        return lId;
    }

    public void setLId(Integer lId) {
        this.lId = lId;
    }

    public String getLocaleCode() {
        return localeCode;
    }

    public void setLocaleCode(String localeCode) {
        this.localeCode = localeCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<TextKey> getTextKeys() {
        return textKeys;
    }

    public void setTextKeys(List<TextKey> textKeys) {
        this.textKeys = textKeys;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (lId != null ? lId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Locale)) {
            return false;
        }
        Locale other = (Locale)object;
        if ((this.lId == null && other.lId != null) || (this.lId != null && !this.lId.equals(other.lId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "schedule.entities.Locales[ lId=" + lId + " ]";
    }
    
}
