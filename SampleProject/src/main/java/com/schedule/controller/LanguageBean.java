package com.schedule.controller;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.Font;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;

import com.schedule.facede.LocaleFacade;

import javax.servlet.ServletContext;

/**
 *
 * @author Jia Li
 */
@Named("languageBean")
@SessionScoped
public class LanguageBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private String localeCode;
    private Map<String, Locale> localeCodes;

    
    @Inject
    LocaleFacade ejbLocaleFacade;
    @Inject
    ServletContext servletContext;
    @Inject
    FacesContext facesContext;
     
    private com.schedule.base.model.Locale openScheduleLocale;

    private final StandardChartTheme chartTheme;
    private final Font extraLargeFont;
    private final Font largeFont;
    private final Font regularFont;
    private final Font smallFont;
    private String appServerInfo = "?";

    public LanguageBean() {
        chartTheme = (StandardChartTheme) org.jfree.chart.StandardChartTheme.createJFreeTheme();
        Font oldExtraLargeFont = chartTheme.getExtraLargeFont();
        Font oldLargeFont = chartTheme.getLargeFont();
        Font oldRegularFont = chartTheme.getRegularFont();
        Font oldSmallFont = chartTheme.getSmallFont();

        extraLargeFont = new Font("Sans-serif", oldExtraLargeFont.getStyle(), oldExtraLargeFont.getSize());
        largeFont = new Font("Sans-serif", oldLargeFont.getStyle(), oldLargeFont.getSize());
        regularFont = new Font("Sans-serif", oldRegularFont.getStyle(), oldRegularFont.getSize());
        smallFont = new Font("Sans-serif", oldSmallFont.getStyle(), oldSmallFont.getSize());
    }

    @PostConstruct
    void init() {
        appServerInfo = servletContext.getServerInfo();
                
        Locale defLocale = facesContext.getApplication().getDefaultLocale();
        localeCode = defLocale.getDisplayName(defLocale);
        
        localeCodes = new LinkedHashMap<>();
        localeCodes.put(defLocale.getDisplayName(defLocale), defLocale);
        Iterator<Locale> locIt = facesContext.getApplication().getSupportedLocales();
        while (locIt.hasNext()) {
            Locale locale = locIt.next();
            localeCodes.put(locale.getDisplayName(locale), locale);
            
        }
               
        initOPLocale();
    }

    private void initOPLocale() {
        LocaleFilter filter = new LocaleFilter();
        Locale locale = this.localeCodes.get(localeCode);
        filter.SetLocaleCodeFilter(locale.toString());
        List<com.schedule.base.model.Locale> openScheduleLocales = ejbLocaleFacade.findAll(filter);
        if (!openScheduleLocales.isEmpty()) {
            //textKeyController.setLocale(openScheduleLocales.get(0));
            //textKeyController.localeChanged();
            openScheduleLocale = openScheduleLocales.get(0);
        }
    }

    public com.schedule.base.model.Locale getOSLocale() {
        return openScheduleLocale;
    }

    
    /**
     * Get the value of localeCodes
     *
     * @return the value of localeCodes
     */
    public Map<String, Locale> getLocaleCodes() {
        return localeCodes;
    }

    /**
     * Set the value of localeCodes
     *
     * @param localeCodes new value of localeCodes
     */
    public void setLocaleCodes(Map<String, Locale> localeCodes) {
        this.localeCodes = localeCodes;
    }
    public String getAppServerInfo() {

        return appServerInfo;
    }
    /**
     * Get the value of localeCode
     *
     * @return the value of localeCode
     */
    public String getLocaleCode() {

        //if(FacesContext.getCurrentInstance().getViewRoot().getLocale().equals(localeCodes.get(this.localeCode)) == false)
        //FacesContext.getCurrentInstance().getViewRoot().setLocale(localeCodes.get(this.localeCode));
        return localeCode;
    }    

    /**
     * Get the value of locale
     *
     * @return the value of locale
     */
    public Locale getLocale() {
        return localeCodes.get(this.localeCode);
    }
    public String getLocaleCodeForCalendar() {
        // Addded in panic, unkown what codes moment.js uses but atleast we only nned to change one place if toLanguageTag is not ok...
        return getLocale().toLanguageTag(); 
    }

    /**
     * Set the value of localeCode
     *
     * @param localeCode new value of localeCode
     */
    public void setLocaleCode(String localeCode) {
        if (localeCodes.containsKey(localeCode)) {
            this.localeCode = localeCode;
            facesContext.getViewRoot().setLocale(localeCodes.get(this.localeCode));

            initOPLocale();
        }
    }

    public void applyChartTheme(JFreeChart chart) {
        if (getLocale().getLanguage().equals(Locale.ENGLISH.getLanguage()) == false) {

            chartTheme.setExtraLargeFont(extraLargeFont);
            chartTheme.setLargeFont(largeFont);
            chartTheme.setRegularFont(regularFont);
            chartTheme.setSmallFont(smallFont);
        }

        chartTheme.apply(chart);
    }
}
