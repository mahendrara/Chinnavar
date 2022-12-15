/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package railml;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;
import org.railml.schemas._2011.EArrivalDepartureTimes;
import org.railml.schemas._2011.EOcp;
import org.railml.schemas._2011.EOcpTT;
import org.railml.schemas._2011.ETrainPart;

/**
 * ImporterTrip is a wrapper class for RailML::ETrainPart. It offers easy
 * accesses to some elements. It creates hashes
 *
 * @author spirttin
 */
public class ImporterTrip
{
    // Line+platforms 
    private String pathHash;
    // arrival + departure times
    private String timeHash;
    // template hash, which contains pathHash + timeHash
    private String templateHash;
    private ETrainPart trainpart;

    private String id;
//    private ArrayList<Integer> times;
    private Integer origoTime;

    //private Integer direction = 0;
    public ImporterTrip( ETrainPart tp )
    {
        ConstructTrainPartNameHash( tp );
        ConstructTrainPartTimesHash( tp );

        id = tp.getId();
        trainpart = tp;
        templateHash = /*tp.getCategoryRef()+ ": " + */ pathHash + " == " + timeHash;
    }

    public String getPathHash()
    {
        return pathHash;
    }

    public String getTimeHash()
    {
        return timeHash;
    }

    public String getTemplateHash()
    {
        return templateHash;
    }

    public String getRailMLTripId()
    {
        return id;
    }

    /*    public ArrayList<Integer> getTimes() {
        return times;
    }
     */
    public EOcp getStartPlatform()
    {
        return (EOcp) trainpart.getOcpsTT().getOcpTT().get( 0 ).getOcpRef();
    }

    public EOcp getStopPlatform()
    {
        int size = trainpart.getOcpsTT().getOcpTT().size();
        return (EOcp) trainpart.getOcpsTT().getOcpTT().get( size - 1 ).getOcpRef();
    }

    public String getLine()
    {
        return trainpart.getLine();
    }

    public String getTemplateName()
    {
        return "RailML: [" + getStartPlatform().getCode() + "]-[" + getStopPlatform().getCode() + "]";
    }

    private void ConstructTrainPartNameHash( ETrainPart tp )
    {
        if( tp != null )
        {
            pathHash = tp.getLine() + ": ";
            for( EOcpTT ocpTT : tp.getOcpsTT().getOcpTT() )
            {
                pathHash += ((EOcp) ocpTT.getOcpRef()).getId() + "-";
            }
        } else
        {
            System.out.println( "error" );
        }
    }

    private void ConstructTrainPartTimesHash( ETrainPart tp )
    {
//        times = new ArrayList<>();
        timeHash = "";

        if( tp.getOcpsTT().getOcpTT().isEmpty() )
        {
            return;
        }

        origoTime = ConvertTimeInSeconds( tp.getOcpsTT().getOcpTT().get( 0 ).getTimes().get( 0 ).getDeparture(), tp.getOcpsTT().getOcpTT().get( 0 ).getTimes().get( 0 ).getDepartureDay(), 0 );

        // Convert times to start from 0
        for( EOcpTT ocpTT : tp.getOcpsTT().getOcpTT() )
        {
            Integer timeArr = ConvertTimeInSeconds( ocpTT.getTimes().get( 0 ).getArrival(), ocpTT.getTimes().get( 0 ).getArrivalDay(), origoTime );
            Integer timeDep = ConvertTimeInSeconds( ocpTT.getTimes().get( 0 ).getDeparture(), ocpTT.getTimes().get( 0 ).getDepartureDay(), origoTime );

            timeHash += "[" + timeArr + ",";
            timeHash += timeDep + "]";
        }
        return;
    }

    public static Integer ConvertTimeInSeconds( XMLGregorianCalendar xmlCal, BigInteger days, Integer origoTime )
    {
        if( xmlCal == null )
        {
            return null;
        }

        Calendar cal = xmlCal.toGregorianCalendar();
        return cal.get( Calendar.SECOND ) + 60 * cal.get( Calendar.MINUTE ) + 3600 * cal.get( Calendar.HOUR_OF_DAY ) + 24 * 3600 * days.intValue() - origoTime;
    }

    List<EOcpTT> getOcpTT()
    {
        return trainpart.getOcpsTT().getOcpTT();
    }

    Integer getOrigoTime()
    {
        return origoTime;
    }

    /**
     * Absolute time for first DEPARTURE in seconds. Note that this time should
     * be reduced with default dwelltime for taking first passengers
     *
     * @return
     */
    Integer getStartSecs()
    {
        EArrivalDepartureTimes firstTime = trainpart.getOcpsTT().getOcpTT().get( 0 ).getTimes().get( 0 );
        if( firstTime.getArrival() != null )
        {
            return ConvertTimeInSeconds( firstTime.getArrival(), firstTime.getArrivalDay(), 0 );
        } else
        {
            return ConvertTimeInSeconds( firstTime.getDeparture(), firstTime.getDepartureDay(), 0 );
        }

    }

    /**
     * Absolute time for last ARRIVAL in seconds. Note that this time should be
     * added with default dwelltime for leaving last passengers
     *
     * @return
     */
    Integer getStopSecs()
    {
        int size = trainpart.getOcpsTT().getOcpTT().size();

        return ConvertTimeInSeconds( trainpart.getOcpsTT().getOcpTT().get( size - 1 ).getTimes().get( 0 ).getArrival(),
                                     trainpart.getOcpsTT().getOcpTT().get( size - 1 ).getTimes().get( 0 ).getArrivalDay(), 0 );
    }

    public ETrainPart getTrainpart()
    {
        return trainpart;
    }
}
