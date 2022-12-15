package railml;

import java.util.List;
import javax.xml.bind.JAXBElement;
import org.purl.dc.elements._1.SimpleLiteral;
import org.railml.schemas._2011.EArrivalDepartureTimes;
import org.railml.schemas._2011.EBlock;
import org.railml.schemas._2011.EFormation;
import org.railml.schemas._2011.EOcp;
import org.railml.schemas._2011.EOcpTT;
import org.railml.schemas._2011.EOcpsTT;
import org.railml.schemas._2011.EOperatingPeriod;
import org.railml.schemas._2011.ERostering;
import org.railml.schemas._2011.ERosterings;
import org.railml.schemas._2011.ETimetablePeriod;
import org.railml.schemas._2011.ETrainPart;
import org.railml.schemas._2011.Railml;
import org.railml.schemas._2011.TBlockPart;
import org.railml.schemas._2011.TCirculation;
import railml.exceptions.RailMlNullElementException;

/**
 * Simple wrapper around RailML structure object. Provides easier access to the
 * inner elements.
 *
 * @author Pavel
 */
public class RailMlFacade extends Railml
{

    /**
     * Unfortunately, Railml object does not provide any clone() function or
     * anything similar, thus we have to copy all data to current object.
     *
     * @param railml
     */
    public RailMlFacade( Railml railml )
    {
        super.metadata = railml.getMetadata();
        super.infrastructure = railml.getInfrastructure();
        super.rollingstock = railml.getRollingstock();
        super.timetable = railml.getTimetable();
        super.version = railml.getVersion();
    }

    /**
     * Gets list of metadata element.
     *
     * @return List<JAXBElement<SimpleLiteral>>
     */
    public List<JAXBElement<SimpleLiteral>> GetMetadata()
    {
        return this.metadata.getAny();
    }

    /**
     * Gets list of operating control points from infrastructure element.
     *
     * @return List<EOcp>
     */
    public List<EOcp> GetOperatingControlPoints()
    {
        return this.infrastructure.getOperationControlPoints().getOcp();
    }

    /**
     * Gets list of formations from rollingstock element.
     *
     * @return List<EFormation>
     */
    public List<EFormation> GetFormations()
    {
        return this.rollingstock.getFormations().getFormation();
    }

    /**
     * Gets list of timetable periods from timetable element.
     *
     * @return List<ETimetablePeriod>
     */
    public List<ETimetablePeriod> GetTimetablePeriods()
    {
        return this.timetable.getTimetablePeriods().getTimetablePeriod();
    }

    /**
     * Gets first and only timetable period element from list.
     *
     * @return List<ETimetablePeriod>
     */
    public ETimetablePeriod GetTimetablePeriod()
    {
        return this.GetTimetablePeriods().get( 0 );
    }

    /**
     * Gets list of operation periods from timetable element.
     *
     * @return List<EOperatingPeriod>
     */
    public List<EOperatingPeriod> GetOperatingPeriods()
    {
        return this.timetable.getOperatingPeriods().getOperatingPeriod();
    }

    /**
     * Gets list of train parts from timetable element.
     *
     * @return List<ETrainPart>
     */
    public List<ETrainPart> GetTrainParts()
    {
        return this.timetable.getTrainParts().getTrainPart();
    }

    /**
     * Gets ERosterings from timetable element.
     *
     * @return ERosterings
     */
    public ERosterings GetERosterings()
    {
        return this.timetable.getRosterings();
    }

    /**
     * Gets list of ERostering from timetable element.
     *
     * @return List<ERostering>
     */
    public List<ERostering> GetRosterings()
    {
        return this.GetERosterings().getRostering();
    }

    /**
     * Get first ERostering of the list of rosterings in timetable element.
     *
     * @return ERostering
     */
    public ERostering GetFirstRostering()
    {
        return this.GetRosterings().get( 0 );
    }

    /**
     * Gets list of block parts from timetable element.
     *
     * @return List<TBlockPart>
     */
    public List<TBlockPart> GetBlockParts()
    {
        return this.GetFirstRostering().getBlockParts().getBlockPart();
    }

    /**
     * Gets list of blocks from timetable element.
     *
     * @return List<EBlock>
     */
    public List<EBlock> GetBlocks()
    {
        return this.GetFirstRostering().getBlocks().getBlock();
    }

    /**
     * Gets list of circulations from timetable element.
     *
     * @return List<TCirculation>
     */
    public List<TCirculation> GetCirculations()
    {
        return this.GetFirstRostering().getCirculations().getCirculation();
    }

    /**
     * Gets list of EOcpTT from train part element.
     *
     * @param eTrainPart
     *
     * @throws RailMlNullElementException if any element if absent or null.
     *
     * @return List<EOcpTT>
     */
    public static List<EOcpTT> GetEOcpTTs( ETrainPart eTrainPart ) throws RailMlNullElementException
    {
        EOcpsTT eOcpsTT = eTrainPart.getOcpsTT();

        List<EOcpTT> eOcpTTs = eOcpsTT.getOcpTT();
        if( eOcpTTs.isEmpty() )
        {
            throw new RailMlNullElementException( "List of EOcpTT is null." );
        }

        return eOcpTTs;
    }

    /**
     * Gets times from EOcpTT element.
     *
     * @param eOcpTT
     *
     * @throws RailMlNullElementException if any element if absent or null.
     *
     * @return EArrivalDepartureTimes
     */
    public static EArrivalDepartureTimes GetTimes( EOcpTT eOcpTT ) throws RailMlNullElementException
    {
        List<EArrivalDepartureTimes> eArrivalDepartureTimesList = eOcpTT.getTimes();
        if( eArrivalDepartureTimesList.isEmpty() )
        {
            throw new RailMlNullElementException( "List of EArrivalDepartureTimes is null." );
        }

        return eArrivalDepartureTimesList.get( 0 );
    }

    /**
     * Gets first EOcp from the list in train part element.
     *
     * @param eTrainPart
     *
     * @throws RailMlNullElementException if any element if absent or null.
     *
     * @return EOcp
     */
    public static EOcp getFirstOcp( ETrainPart eTrainPart ) throws RailMlNullElementException
    {
        List<EOcpTT> eOcpTTs = RailMlFacade.GetEOcpTTs( eTrainPart );

        EOcpTT eOcpTT = eOcpTTs.get( 0 );
        return (EOcp) eOcpTT.getOcpRef();
    }

    /**
     * Gets last EOcp from the list in train part element.
     *
     * @param eTrainPart
     *
     * @throws RailMlNullElementException if any element if absent or null.
     *
     * @return EOcp
     */
    public static EOcp getLastOcp( ETrainPart eTrainPart ) throws RailMlNullElementException
    {
        List<EOcpTT> eOcpTTs = RailMlFacade.GetEOcpTTs( eTrainPart );

        EOcpTT eOcpTT = eOcpTTs.get( eOcpTTs.size() - 1 );
        return (EOcp) eOcpTT.getOcpRef();
    }

}
