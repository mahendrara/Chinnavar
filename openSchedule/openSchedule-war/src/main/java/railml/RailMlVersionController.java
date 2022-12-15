package railml;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import org.purl.dc.elements._1.ElementContainer;
import org.purl.dc.elements._1.SimpleLiteral;
import org.railml.schemas._2011.EOcp;
import org.railml.schemas._2011.EOperationControlPoints;
import org.railml.schemas._2011.Infrastructure;
import org.railml.schemas._2011.Railml;
import org.railml.schemas._2011.TAdditionalName;
import railml.exceptions.RailMlVersionInformationException;

/**
 * Reads and writes version information to / from RailML file.
 *
 * @author Pavel
 */
public final class RailMlVersionController
{

    /**
     * Contains name of the creating application.
     */
    private static final String Creator = "OpenSchedule";

    /**
     * Contains conversion version number. Increment this number accordingly
     * when the conversion changes in any way.
     */
    private static final String VersionIdentifier = "1.0.0";

    /**
     * Contains version about RailML standard used.
     */
    private static final String RailMlVersion = "2.1";

    /**
     * Describes all possible file formats that conversion code could meet.
     */
    public static enum RailMlFormat
    {
        /**
         * Unsupported format. Could be an old TMS format or any other.
         */
        Unsupported,
        /**
         * Format used by external timetable compilation application.
         */
        Ivu,
        /**
         * Format created and used by our system.
         */
        Tms
    }

    /**
     * Reads RailML meta data for information about version. In case when no
     * meta data was found, tries to parse the file structure and find any
     * mentions or attributes of IVU format, since the IVU format does not
     * provide any identifying information.
     *
     * @param importedFile
     *
     * @return RailMlFormat describing determined format of the file
     *
     * @see RailMlFormat enumeration for details
     */
    public static RailMlFormat getVersion( Railml importedFile )
    {
        RailMlFormat version = RailMlFormat.Unsupported;

        if( IsTmsFormat( importedFile ) )
        {
            version = RailMlFormat.Tms;
        } else if( IsIvuFormat( importedFile ) )
        {
            version = RailMlFormat.Ivu;
        }

        System.out.println( "[RailMlVersionController] Determined format of the uploaded file: " + version.name() );

        return version;
    }

    /**
     * Adds version information as a RailML's metadata element. Version
     * information includes the name of creator application, version of
     * conversion and date of export.
     *
     * @param exportingFile
     *
     * @throws RailMlVersionInformationException in case when meta data was not
     *                                           added to the RailML file for
     *                                           some reason.
     */
    public static void AddOurMetadata( Railml exportingFile ) throws RailMlVersionInformationException
    {
        ElementContainer newMetadata = new ElementContainer();
        List<JAXBElement<SimpleLiteral>> elementAny = newMetadata.getAny();

        JAXBElement<SimpleLiteral> creator = RailMlStructureBuilder.CreateElement( "creator", Creator );
        JAXBElement<SimpleLiteral> identifier = RailMlStructureBuilder.CreateElement( "identifier", VersionIdentifier );

        // Check that the vital elements were added to the metadata.
        if( !elementAny.add( creator ) || !elementAny.add( identifier ) )
        {
            throw new RailMlVersionInformationException( "No metadata was added to the exproted file." );
        }

        // Date is not a vital element to check.
        JAXBElement<SimpleLiteral> date = RailMlStructureBuilder.CreateElement( "date", Instant.now().toString() );
        elementAny.add( date );

        exportingFile.setMetadata( newMetadata );
        exportingFile.setVersion( RailMlVersion );
    }

    /**
     * Parses provided RailML file and searches for any mentions or attributes
     * of IVU format.
     *
     * @param importedFile
     *
     * @return true if any mention or attribute was found, otherwise false
     */
    private static boolean IsIvuFormat( Railml importedFile )
    {
        Infrastructure infrastructure = importedFile.getInfrastructure();
        if( infrastructure == null )
        {
            return false;
        }

        EOperationControlPoints eOperationControlPoints = infrastructure.getOperationControlPoints();
        if( eOperationControlPoints == null )
        {
            return false;
        }

        List<EOcp> eOcps = eOperationControlPoints.getOcp();
        if( eOcps == null || eOcps.isEmpty() )
        {
            return false;
        }

        for( EOcp eOcp : eOcps )
        {
            if( eOcp == null )
            {
                continue;
            }

            List<TAdditionalName> additionalNames = eOcp.getAdditionalName();
            if( additionalNames == null || additionalNames.isEmpty() )
            {
                continue;
            }

            for( TAdditionalName tAdditionalName : additionalNames )
            {
                if( tAdditionalName == null )
                {
                    continue;
                }

                Map<QName, String> otherAttributes = tAdditionalName.getOtherAttributes();
                if( otherAttributes == null || otherAttributes.isEmpty() )
                {
                    continue;
                }

                for( Map.Entry<QName, String> attribute : otherAttributes.entrySet() )
                {
                    QName attributeName = attribute.getKey();
                    if( attributeName == null )
                    {
                        continue;
                    }

                    String prefix = attributeName.getPrefix();
                    if( prefix != null
                        && !prefix.isEmpty()
                        && prefix.equals( "ivu" ) )
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Reads RailML's metadata element and validates it against expected TMS
     * version information.
     *
     * @param importedFile
     *
     * @return true if file format was validated as the TMS, otherwise false.
     */
    private static boolean IsTmsFormat( Railml importedFile )
    {
        if( !IsCorrectRailMlVersion( importedFile ) )
        {
            return false;
        }

        ElementContainer metadata = importedFile.getMetadata();
        if( metadata == null )
        {
            return false;
        }

        List<JAXBElement<SimpleLiteral>> elementAny = metadata.getAny();
        if( elementAny == null || elementAny.isEmpty() )
        {
            return false;
        }

        return HasValidElements( elementAny );
    }

    /**
     * Validates version against expected information.
     *
     * @param importedFile
     *
     * @return boolean
     */
    private static boolean IsCorrectRailMlVersion( Railml importedFile )
    {
        String version = importedFile.getVersion();
        return version != null && !version.isEmpty() && version.equals( RailMlVersion );
    }

    /**
     * Validates provided element to have expected information about creator and
     * identifier elements.
     *
     * @param elementAny
     *
     * @return true if elements are present and have correct information,
     *         otherwise false.
     */
    private static boolean HasValidElements( List<JAXBElement<SimpleLiteral>> elementAny )
    {
        boolean HasValidCreator = false;
        boolean HasValidVersionIdentifier = false;

        for( JAXBElement<SimpleLiteral> element : elementAny )
        {
            QName name = element.getName();
            SimpleLiteral value = element.getValue();

            if( name == null || value == null )
            {
                continue;
            }

            String actualName = name.getLocalPart();
            if( actualName == null || actualName.isEmpty() )
            {
                continue;
            }

            List<String> valueContent = value.getContent();
            if( valueContent == null || valueContent.isEmpty() )
            {
                continue;
            }

            String actualValue = valueContent.get( 0 );
            if( actualValue == null || actualValue.isEmpty() )
            {
                continue;
            }

            if( !HasValidCreator )
            {
                HasValidCreator
                        = actualName.equals( "creator" ) && actualValue.equals( Creator );
            }

            if( !HasValidVersionIdentifier )
            {
                HasValidVersionIdentifier
                        = actualName.equals( "identifier" ) && actualValue.equals( VersionIdentifier );
            }
        }

        return HasValidCreator && HasValidVersionIdentifier;
    }

}
