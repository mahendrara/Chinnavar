/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.uiclasses.util.config;

import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Jia Li
 */
@XmlRootElement(name = "config")
@XmlAccessorType(XmlAccessType.FIELD)
public class Config {

    //@XmlElement(name = "pages")
    //@XmlJavaTypeAdapter(MapAdapter.class)
    @XmlElementWrapper(name = "pages")
    @XmlElements({
        @XmlElement(name = "timeBlock", type = TimeBlockPage.class),
        @XmlElement(name = "duty", type = PageItem.class)
    })
    private ArrayList<PageItem> pages;

    @XmlElementWrapper(name = "functions")
    @XmlElements({
        @XmlElement(type = FunctionItem.class)})
    private ArrayList<FunctionItem> functions;

    @XmlElementWrapper(name = "formats")
    @XmlElements({
        @XmlElement(name = "format", type = FormatItem.class)
    })

    private ArrayList<FormatItem> formats;

    @XmlElementWrapper(name = "settings")
    @XmlElements({
        @XmlElement(name = "setting", type = SettingItem.class)
    })

    private ArrayList<SettingItem> settings;

    public ArrayList<PageItem> getPages() {
        return pages;
    }

    public void setPages(ArrayList<PageItem> pages) {
        this.pages = pages;
    }

    public PageItem getPage(String name) {
        Iterator<PageItem> it = this.pages.iterator();
        while (it.hasNext()) {
            PageItem page = it.next();
            if (page.getName() == null ? name == null : page.getName().equals(name)) {
                return page;
            }
        }
        return null;
    }

    public ArrayList<FunctionItem> getFunctions() {
        return functions;
    }

    public void setFunctions(ArrayList<FunctionItem> functions) {
        this.functions = functions;
    }

    public FunctionItem getFunction(String name) {
        Iterator<FunctionItem> it = this.functions.iterator();
        while (it.hasNext()) {
            FunctionItem function = it.next();
            if (function.getName() == null ? name == null : function.getName().equals(name)) {
                return function;
            }
        }
        return null;
    }

    public FormatItem getFormat(String name) {
        Iterator<FormatItem> it = this.formats.iterator();
        while (it.hasNext()) {
            FormatItem format = it.next();
            if (format.getName() == null ? name == null : format.getName().equals(name)) {
                return format;
            }
        }
        return null;
    }
    
    public SettingItem getSetting(String name) {
        Iterator<SettingItem> it = this.settings.iterator();
        while (it.hasNext()) {
            SettingItem setting = it.next();
            if (setting.getName() == null ? name == null : setting.getName().equals(name)) {
                return setting;
            }
        }
        return null;
    }
}
