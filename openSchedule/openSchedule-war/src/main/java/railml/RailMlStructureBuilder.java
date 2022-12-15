package railml;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import org.purl.dc.elements._1.SimpleLiteral;
import org.railml.schemas._2011.EBlockParts;
import org.railml.schemas._2011.EBlocks;
import org.railml.schemas._2011.ECirculations;
import org.railml.schemas._2011.EFormations;
import org.railml.schemas._2011.EOperatingPeriods;
import org.railml.schemas._2011.EOperationControlPoints;
import org.railml.schemas._2011.ERostering;
import org.railml.schemas._2011.ERosterings;
import org.railml.schemas._2011.ETimetablePeriods;
import org.railml.schemas._2011.ETrainParts;
import org.railml.schemas._2011.Infrastructure;
import org.railml.schemas._2011.Railml;
import org.railml.schemas._2011.Rollingstock;
import org.railml.schemas._2011.Timetable;

/**
 * Creates an empty RailML structure with all required to use elements.
 *
 * @author Pavel
 */
public final class RailMlStructureBuilder
{

    /**
     * Creates an empty RailML structure with all required to use elements.
     *
     * @return Railml
     */
    public static Railml CreateEmptyStructure()
    {
        Railml data = new Railml();

        AddEmptyInfrastructureElement( data );
        AddEmptyRollingStockElement( data );
        AddEmptyTimetableElement( data );

        return data;
    }

    /**
     * Creates empty element.
     *
     * @param data
     */
    private static void AddEmptyInfrastructureElement( Railml data )
    {
        Infrastructure infrastructure = new Infrastructure();
        infrastructure.setId( "infra1" );

        AddEmptyOperatingControlPointsElement( infrastructure );

        data.setInfrastructure( infrastructure );
    }

    /**
     * Creates empty element.
     *
     * @param infrastructure
     */
    private static void AddEmptyOperatingControlPointsElement( Infrastructure infrastructure )
    {
        EOperationControlPoints ocps = new EOperationControlPoints();
        infrastructure.setOperationControlPoints( ocps );
    }

    /**
     * Creates empty element.
     *
     * @param data
     */
    private static void AddEmptyRollingStockElement( Railml data )
    {
        Rollingstock rollingStock = new Rollingstock();
        rollingStock.setId( "roll1" );

        AddEmptyFormationsElement( rollingStock );

        data.setRollingstock( rollingStock );
    }

    /**
     * Creates empty element.
     *
     * @param rollingStock
     */
    private static void AddEmptyFormationsElement( Rollingstock rollingStock )
    {
        EFormations formations = new EFormations();
        rollingStock.setFormations( formations );
    }

    /**
     * Creates empty element.
     *
     * @param data
     */
    private static void AddEmptyTimetableElement( Railml data )
    {
        Timetable timetable = new Timetable();
        timetable.setId( "time1" );

        AddEmptyTimetablePeriodsElement( timetable );
        AddEmptyOperatingPeriodsElement( timetable );
        AddEmptyTrainPartsElement( timetable );
        AddEmptyRosteringsElement( timetable );

        data.setTimetable( timetable );
    }

    /**
     * Creates empty element.
     *
     * @param timetable
     */
    private static void AddEmptyTimetablePeriodsElement( Timetable timetable )
    {
        ETimetablePeriods eTimetablePeriods = new ETimetablePeriods();
        timetable.setTimetablePeriods( eTimetablePeriods );
    }

    /**
     * Creates empty element.
     *
     * @param timetable
     */
    private static void AddEmptyOperatingPeriodsElement( Timetable timetable )
    {
        EOperatingPeriods eOperatingPeriods = new EOperatingPeriods();
        timetable.setOperatingPeriods( eOperatingPeriods );
    }

    /**
     * Creates empty element.
     *
     * @param timetable
     */
    private static void AddEmptyTrainPartsElement( Timetable timetable )
    {
        ETrainParts trainParts = new ETrainParts();
        timetable.setTrainParts( trainParts );
    }

    /**
     * Creates empty element.
     *
     * @param timetable
     */
    private static void AddEmptyRosteringsElement( Timetable timetable )
    {
        ERosterings rosterings = new ERosterings();

        AddEmptyRosteringElement( rosterings );

        timetable.setRosterings( rosterings );
    }

    /**
     * Creates empty element.
     *
     * @param rosterings
     */
    private static void AddEmptyRosteringElement( ERosterings rosterings )
    {
        ERostering rostering = new ERostering();
        rostering.setId( "rostering1" );

        AddEmptyBlockPartsElement( rostering );
        AddEmptyBlocksElement( rostering );
        AddEmptyCirculationsElement( rostering );

        rosterings.getRostering().add( rostering );
    }

    /**
     * Creates empty element.
     *
     * @param rostering
     */
    private static void AddEmptyBlockPartsElement( ERostering rostering )
    {
        EBlockParts eBlockParts = new EBlockParts();
        rostering.setBlockParts( eBlockParts );
    }

    /**
     * Creates empty element.
     *
     * @param rostering
     */
    private static void AddEmptyBlocksElement( ERostering rostering )
    {
        EBlocks blocks = new EBlocks();
        rostering.setBlocks( blocks );
    }

    /**
     * Creates empty element.
     *
     * @param rostering
     */
    private static void AddEmptyCirculationsElement( ERostering rostering )
    {
        ECirculations eCirculations = new ECirculations();
        rostering.setCirculations( eCirculations );
    }

    /**
     * Creates valid RailML element with specified name and value.
     *
     * @param name
     * @param value
     *
     * @return JAXBElement<SimpleLiteral>
     */
    static JAXBElement<SimpleLiteral> CreateElement( String name, String value )
    {
        QName elementName = new QName( "http://purl.org/dc/elements/1.1/", name );
        SimpleLiteral simpleLiteral = new SimpleLiteral();
        simpleLiteral.getContent().add( value );
        JAXBElement<SimpleLiteral> element = new JAXBElement<>( elementName, SimpleLiteral.class, simpleLiteral );
        return element;
    }
}
