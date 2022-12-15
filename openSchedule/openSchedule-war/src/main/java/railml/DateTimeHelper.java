package railml;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.railml.schemas._2011.EArrivalDepartureTimes;
import org.railml.schemas._2011.EOcpTT;
import railml.exceptions.RailMlNullElementException;

/**
 *
 * @author Pavel
 */
public final class DateTimeHelper
{

    public static XMLGregorianCalendar createXml( Date date ) throws DatatypeConfigurationException
    {
        GregorianCalendar calendar = new GregorianCalendar();

        calendar.setTime( date );

        XMLGregorianCalendar xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar( calendar );
        xmlCalendar.setTimezone( DatatypeConstants.FIELD_UNDEFINED );

        return xmlCalendar;
    }

    public static XMLGregorianCalendar createXml( int timestamp, String timeZone ) throws DatatypeConfigurationException
    {
        int hours = timestamp / 3600;
        int minutes = timestamp / 60 % 60;
        int seconds = timestamp % 60;

        GregorianCalendar gregorianCalendar = new GregorianCalendar( 0, 0, 0, hours, minutes, seconds );
        gregorianCalendar.setTimeZone( TimeZone.getTimeZone( timeZone ) );
        XMLGregorianCalendar arrival = DatatypeFactory.newInstance().newXMLGregorianCalendar( gregorianCalendar );
        arrival.setMillisecond( 0 );

        return arrival;
    }

    public static Date toDate( XMLGregorianCalendar xmlDate, XMLGregorianCalendar xmlTime )
    {
        Date date = new Date( xmlDate.getYear() - 1900, xmlDate.getMonth() - 1, xmlDate.getDay(),
                              xmlTime.getHour(), xmlTime.getMinute(), xmlTime.getSecond() );
        
        return date;
    }

    public static int GetDayCode( Calendar calendar )
    {
        return calendar.get( Calendar.YEAR ) * 1000 + calendar.get( Calendar.DAY_OF_YEAR );
    }

    public static Integer GetStartSecs( EOcpTT eOcpTT ) throws RailMlNullElementException
    {
        EArrivalDepartureTimes eArrivalDepartureTimes = RailMlFacade.GetTimes( eOcpTT );
        Integer startSecs = (int) eArrivalDepartureTimes.getArrival().toGregorianCalendar().getTimeInMillis() / 1000;
        startSecs += (int) eArrivalDepartureTimes.getArrivalDay().longValueExact() * 24 * 60 * 60;
        return startSecs;
    }

    public static Integer GetPlannedDuration( EArrivalDepartureTimes eArrivalDepartureTimes )
    {
        Long startMs = eArrivalDepartureTimes.getArrival().toGregorianCalendar().getTimeInMillis();
        startMs += eArrivalDepartureTimes.getArrivalDay().longValueExact() * 24 * 60 * 60 * 1000;
        Long endMs = eArrivalDepartureTimes.getDeparture().toGregorianCalendar().getTimeInMillis();
        endMs += eArrivalDepartureTimes.getDepartureDay().longValueExact() * 24 * 60 * 60 * 1000;
        Integer plannedDurationSec = (int) (endMs - startMs) / 1000;
        return plannedDurationSec;
    }

    public static BigInteger GetDayFromSeconds( int seconds )
    {
        return BigInteger.valueOf( seconds / (3600 * 24) );
    }
    
    
}
