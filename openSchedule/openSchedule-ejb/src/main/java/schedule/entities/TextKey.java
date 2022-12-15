/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule.entities;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author Jia Li
 */
@Entity
@Table(name = "textKey")
public class TextKey implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "tId")
    private Integer tId;
    
    @Basic(optional = false)
    @Column(name = "textKey")
    private String textKey;
    
    @Column(name = "pValue")
    private Integer pValue;
    
    @Basic(optional = false)
    @Column(name = "text")
    private String text;
    
    @Basic(optional = false)
    @Column(name = "shortText")
    private String shortText;
    
    @JoinColumn(name = "lId", referencedColumnName = "lId")
    @ManyToOne(optional = false)
    private Locale locale;

    public TextKey() {
    }

    public TextKey(Integer tId) {
        this.tId = tId;
    }

    public TextKey(Integer tId, String textKey, String text, String shortText) {
        this.tId = tId;
        this.textKey = textKey;
        this.text = text;
        this.shortText = shortText;
    }

    public Integer getTId() {
        return tId;
    }

    public void setTId(Integer tId) {
        this.tId = tId;
    }

    public String getTextKey() {
        return textKey;
    }

    public void setTextKey(String textKey) {
        this.textKey = textKey;
    }

    public Integer getPValue() {
        return pValue;
    }

    public void setPValue(Integer pValue) {
        this.pValue = pValue;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getShortText() {
        return shortText;
    }

    public void setShortText(String shortText) {
        this.shortText = shortText;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLid(Locale locale) {
        this.locale = locale;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (tId != null ? tId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TextKey)) {
            return false;
        }
        TextKey other = (TextKey) object;
        if ((this.tId == null && other.tId != null) || (this.tId != null && !this.tId.equals(other.tId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "schedule.entities.TextKey[ tId=" + tId + " ]";
    }
    
}
