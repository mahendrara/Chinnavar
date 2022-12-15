package schedule.uiclasses;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import schedule.uiclasses.util.JsfUtil;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import railml.Exporter;
import schedule.entities.Schedule;
import schedule.sessions.ScheduleFacade;
import schedule.uiclasses.util.UiText;

// This does NOT inherit BaseController, as this dialog does not access to
// any entity
@Named("ReportController")
@SessionScoped
public class ReportController implements Serializable {
    static final Logger logger = Logger.getLogger("ReportController");

    // <editor-fold defaultstate="collapsed" desc="Injects">
    @Inject
    private UiText uiText;
    
    @Inject 
    private ScheduleFacade ejbScheduleFacade;
        
    @Inject
    private Exporter exp;
    // </editor-fold>
    
    // Selected schedule for report
    private Schedule schedule;
    private String report;
    
    public ReportController() {
    }

    @PostConstruct
    public void init() {
        List<Schedule> schedules = getSchedules();
        if (schedules != null && schedules.size() > 0)
            schedule = schedules.get(0);
        
        report = "";
    }
 
    // <editor-fold defaultstate="collapsed" desc="Callbacks from WEB-pages">
    public List<Schedule> getSchedules() {
        ScheduleFilter filter = new ScheduleFilter();
        filter.setValid(Boolean.TRUE);
        
        return ejbScheduleFacade.findAll(filter);
    }
    public void setSchedule(Schedule sched) {
        schedule = sched;
    }
    /**
     * Gets selected schedule
     * @return 
     */
    public Schedule getSchedule() {
        return schedule;
    }
    
    /**
     * Gets generated report
     * @return 
     */
    public String getReport() {
        return report;
    }
    
    public void GenerateReport() {
        try {
            long startTime = System.currentTimeMillis();
            File f = exp.generateDownload(schedule);
            report = "";
            report = fillReport(f);
            if (f != null)
                f.delete();
            
            long endTime = System.currentTimeMillis();

            long duration = (endTime - startTime);

            JsfUtil.addSuccessMessage("Took: "+ duration  + " ms");
        } catch (Exception ex) {
            JsfUtil.addErrorMessage(uiText.get("ErrorGeneratingScheduleReport"));
            logger.log(Level.SEVERE,
                    "ReportController.GenerateReport:  {0}", ex.getMessage());
        }
        
    }
    
            
    /**
     * Generates railML file and converts it to report structured string
     * 
     * @param fileRailML
     * @return
     * @throws Exception 
     */
    public String fillReport(File fileRailML) throws Exception {
        

        ClassLoader loader = this.getClass().getClassLoader();
        InputStream stream = loader.getResourceAsStream("/xml-resources/report/OpenSched_report.xsl");
        
        TransformerFactory factory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl",null);
        
        Source stylesheetSource = new StreamSource(stream);    
        Transformer transformer = factory.newTransformer(stylesheetSource);
        Source inputSource = new StreamSource(fileRailML);
        File reportFile = File.createTempFile("reportFile", ".xml");
        reportFile.deleteOnExit();
        Result outputResult = new StreamResult(reportFile);

        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"yes");
        
        transformer.transform(inputSource, outputResult);

        BufferedReader br = new BufferedReader(new FileReader(reportFile));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            String everything = sb.toString();
            return everything;
        } finally {
            br.close();
        }

    }
    // </editor-fold>
    
}
