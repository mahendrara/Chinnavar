package schedule.uiclasses;

import java.io.IOException;
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
import javax.servlet.http.Part;
import org.primefaces.model.DefaultStreamedContent;
import railml.Exporter;
import railml.RailMlController;
import railml.exceptions.RailMlExportException;
import railml.exceptions.RailMlImportException;
import schedule.entities.Schedule;
import schedule.sessions.ScheduleFacade;
import schedule.uiclasses.util.UiText;

@Named("RailMLFormController")
@SessionScoped
public class RailMlFormController implements Serializable
{

    @Inject
    private UiText uiText;

    @Inject
    private ScheduleFacade ejbScheduleFacade;

    @Inject
    private Exporter exporter;

    private Schedule selectedSchedule;
    private Part uploadFile;

    @Inject
    private RailMlController railMlController;

    @PostConstruct
    public void init()
    {
        List<Schedule> schedules = getAvailableSchedules();
        if( schedules != null && !schedules.isEmpty() )
        {
            this.selectedSchedule = schedules.get( 0 );
        }
    }

    public List<Schedule> getAvailableSchedules()
    {
        ScheduleFilter filter = new ScheduleFilter();
        filter.setValid( Boolean.TRUE );

        return this.ejbScheduleFacade.findAll( filter );
    }

    public void setSelectedSchedule( Schedule schedule )
    {
        this.selectedSchedule = schedule;
    }

    public Schedule getSelectedSchedule()
    {
        return this.selectedSchedule;
    }

    public Part getUploadFile()
    {
        return this.uploadFile;
    }

    public void setUploadFile( Part file )
    {
        this.uploadFile = file;
    }

    public void upload()
    {
        try
        {
            if( this.uploadFile == null )
            {
                JsfUtil.addErrorMessage( uiText.get( "ViewMaintenanceRailMLChooseFile" ) );
                return;
            }

            System.out.println( "Uploaded File Name Is :: "
                                + this.uploadFile.getName()
                                + " :: Uploaded File Size :: "
                                + this.uploadFile.getSize() );

            InputStream inputStream = this.uploadFile.getInputStream();
            boolean importOK = this.railMlController.Import( inputStream );
            if(importOK) {
                JsfUtil.addSuccessMessage(uiText.get("RailMLFileUploadSuccess"));
            }else {
                JsfUtil.addSuccessMessage(uiText.get("RailMLFileUploadFailed"));
            }
        } catch( IOException exception )
        {
            JsfUtil.addErrorMessage( uiText.get( "RailMLFileUploadFailed" ) );
            Logger.getLogger( RailMlFormController.class.getName() ).log( Level.SEVERE, null, exception );
        } catch( NullPointerException | RailMlImportException exception )
        {
            JsfUtil.addErrorMessage( uiText.get( "RailMlImportExceptionHappened" ) );
            Logger.getLogger( RailMlFormController.class.getName() ).log( Level.SEVERE, null, exception );
        }
    }

    public DefaultStreamedContent download()
    {
        try
        {
            Integer scheduleId = this.getSelectedSchedule().getScheduleId();
            String scheduleName = this.getSelectedSchedule().getDescription();

            String fileName = "RailMl OpenSchedule Export " + scheduleName + ".xml";
            String fileFormat = "Railml";

            //System.out.println( "Export started: " + Instant.now().toString() );
            //InputStream inputStream = new FileInputStream( this.exporter.generateDownload( getSelectedSchedule() ) );
            InputStream inputStream = this.railMlController.Export( scheduleId );

            //System.out.println( "Export finished: " + Instant.now().toString() );
            return new DefaultStreamedContent( inputStream, fileFormat, fileName );
        } catch( NullPointerException | RailMlExportException exception )
        {
            JsfUtil.addErrorMessage( uiText.get( "RailMlExportExceptionHappened" ) );
            Logger.getLogger( RailMlFormController.class.getName() ).log( Level.SEVERE, null, exception );

            return null;
        }
    }

}
