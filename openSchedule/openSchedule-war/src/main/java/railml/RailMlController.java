package railml;

import eventlog.EventLogBean;
import java.io.InputStream;
import java.io.Serializable;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.railml.schemas._2011.Railml;
import railml.exceptions.RailMlConversionException;
import railml.exceptions.RailMlExportException;
import railml.exceptions.RailMlFormatCastingException;
import railml.exceptions.RailMlImportException;
import railml.exceptions.RailMlIvuFileFormat;
import railml.exceptions.RailMlServiceImportException;
import railml.exceptions.RailMlUnsupportedFileFormatException;
import railml.exceptions.RailMlVersionInformationException;

/**
 * Controls the work flow of whole RailML import / export process.
 *
 * @author Pavel
 */
@Named("RailMlController")
@SessionScoped
@TransactionManagement(TransactionManagementType.BEAN)
public class RailMlController implements Serializable
{

    @Inject
    private RailMlConversionController conversionController;

    @Inject
    private RailMlDataController dataController;
    @Inject
    private EventLogBean eventLog;
    /**
     * Carries out and controls operations of import process.
     *
     * @param inputStream
     *
     * @throws RailMlImportException when any step of the process failed.
     *
     * @return true if import succeeded, otherwise false.
     */
    public boolean Import( InputStream inputStream ) throws RailMlImportException
    {
        boolean importOk = false;
        
        try
        {
            Railml importedRailMlFile = RailMlConverter.cast( inputStream );
            if( importedRailMlFile != null )
            {
                ScheduleData data = this.conversionController.convert( importedRailMlFile );
                
                if( data!=null)
                {
                    eventLog.addEvent("RailMlController", "Persisting parsed schedule...");
                    importOk = this.dataController.setScheduleData(eventLog,data );
                    if(importOk)
                    {
                        String obj = data.getInfoString();
                        eventLog.addEvent(obj, "Persisted ok");
                    }
                    else
                    {
                        eventLog.addEvent("RailMlController", "Storing to db failed");
                    }
                    
                }
                else
                {
                    eventLog.addEvent("RailMlController", "Conversion to schedule failed"); 
                }
            }
            else
            {
               eventLog.addEvent("RailMlController", "Parsing inputStream failed"); 
            }
            
        } catch( RailMlFormatCastingException | RailMlServiceImportException |
                 RailMlUnsupportedFileFormatException |
                 RailMlConversionException exception )
        {
            eventLog.addEvent("RailMlController", exception.toString());
            throw new RailMlImportException( exception );
        } catch( RailMlIvuFileFormat exception )
        {
            /**
             * This is workaround that should be removed when stable IVU parser
             * is written.
             */
            importOk = true;
        }
        return importOk;
    }

    /**
     * Carries out and controls operations of export process.
     *
     * @param ScheduleId
     *
     * @throws RailMlExportException when any step of the process failed.
     *
     * @return InputStream if succeeded, otherwise null.
     */
    public InputStream Export( Integer ScheduleId ) throws RailMlExportException
    {
        try
        {
            RailMlConverter converter = new RailMlConverter();

            ScheduleData data = this.dataController.getScheduleData( ScheduleId );

            Railml railmlData = RailMlStructureBuilder.CreateEmptyStructure();
            RailMlVersionController.AddOurMetadata( railmlData );

            converter.scheduledata_to_railml( railmlData, data );

            return RailMlConverter.cast( railmlData );
        } catch( RailMlFormatCastingException | RailMlConversionException |
                 RailMlVersionInformationException exception )
        {
            throw new RailMlExportException( exception );
        }
    }

}
