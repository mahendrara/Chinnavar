package com.schedule.model;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 *
 * @author EBIScreen
 */
@Entity
@DiscriminatorValue("A")
public class TTArea extends TTObject{
    @Column(name = "colorref")
    private String colorRef;

    public TTArea() {
    }

    
    public TTArea(Integer ttObjId, String description) {
        super(ttObjId, description);
    }

    public String getColorRef() {
        return colorRef;
    }

    public void setColorRef(String colorRef) {
        this.colorRef = colorRef;
    }

    public String getTextColorRef() {
        return textColorRef;
    }

    public void setTextColorRef(String textColorRef) {
        this.textColorRef = textColorRef;
    }

    @Column(name = "textcolorref")
    private String textColorRef;
}
