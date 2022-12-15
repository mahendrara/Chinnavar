package railml;

import java.io.Serializable;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.railml.schemas._2011.Railml;
import railml.RailMlVersionController.RailMlFormat;
import railml.exceptions.RailMlConversionException;
import railml.exceptions.RailMlIvuFileFormat;
import railml.exceptions.RailMlServiceImportException;
import railml.exceptions.RailMlUnsupportedFileFormatException;

/**
 * Determines most suitable converter for the provided RailML file and returns
 * data converted by it.
 *
 * @author Pavel
 */
@Named("RailMlConversionController")
@SessionScoped
@TransactionManagement(TransactionManagementType.BEAN)
public class RailMlConversionController implements Serializable
{

    @Inject
    private RailMlConverter converter;

    @Inject
    private Importer oldConverter;

    /**
     * Determines most suitable converter for the provided RailML file and
     * returns data converted by it.
     *
     * @param importedFile
     *
     * @throws RailMlUnsupportedFileFormatException in case of unrecognized file
     *                                              format.
     * @throws RailMlServiceImportException         if no planned service was
     *                                              imported.
     * @throws RailMlIvuFileFormat                  in case of successful
     *                                              parsing of IVU format. This
     *                                              is workaround that should be
     *                                              removed when stable IVU
     *                                              parser is written.
     * @throws RailMlConversionException            if an exception occurred in
     *                                              conversion process.
     *
     * @return ScheduleData
     */
    public ScheduleData convert( Railml importedFile ) throws RailMlUnsupportedFileFormatException,
                                                              RailMlIvuFileFormat,
                                                              RailMlServiceImportException,
                                                              RailMlConversionException
    {
        RailMlFormat version = RailMlVersionController.getVersion( importedFile );
        switch( version )
        {
            case Tms:
                return this.converter.scheduledata_from_railml( importedFile );

            case Ivu:
                /**
                 * This is workaround that should be removed when stable IVU
                 * parser is written.
                 */
                if( this.oldConverter.scheduledata_from_railml( importedFile ) )
                {
                    throw new RailMlIvuFileFormat();
                } else
                {
                    throw new RailMlUnsupportedFileFormatException();
                }

            case Unsupported:
            default:
                throw new RailMlUnsupportedFileFormatException();
        }
    }

}
