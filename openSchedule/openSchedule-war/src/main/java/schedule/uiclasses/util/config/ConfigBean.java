/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.uiclasses.util.config;

import java.io.File;
import java.net.URL;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.LocalBean;
import javax.ejb.Startup;
import javax.inject.Named;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author Jia Li
 */
@Singleton
@LocalBean
@Startup
@Named("configBean")
public class ConfigBean {

    private static Config config;

    @PostConstruct
    void init() {
        try {

            JAXBContext jaxbContext = JAXBContext.newInstance(Config.class);
            Unmarshaller unmashaller = jaxbContext.createUnmarshaller();
            URL url = Thread.currentThread().getContextClassLoader().getResource("OpenSchedule-Config.xml");//getResourceAsStream("Config.xml");
            if (url != null) {
                File file = new File(url.toURI());
                config = (Config) unmashaller.unmarshal(file);
            }else
                System.err.println("ConfigBean init fails: cannot find file!");
        } catch (JAXBException ex) {
            System.err.println("ConfigBean init fails: " + ex.getMessage());
        } catch (Exception ex) {

        }
    }

    public static final Config getConfig() {
        //System.out.println(config.getPageItems().get("timeBlock").visible);
        return config;
    }
}
