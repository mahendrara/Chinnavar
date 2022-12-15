/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trafficNotification.clientmsgs;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.TimeZone;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import trafficnotification.servermsgs.TrafficNotificationItem;
import trafficnotification.servermsgs.TrafficNotifications;
import trafficnotification.servermsgs.TrafficNotifications.NotificationType;

/**
 *
 * @author jiali
 */
public class TrafficNotificationMsgEncoder implements Encoder.Text<TrafficNotifications> {

    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    static Calendar  echoTime; 

    public TrafficNotificationMsgEncoder() {
        echoTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")); 
        echoTime.set(1970,0,1,0,0,0);
    }

    @Override
    public String encode(TrafficNotifications object) throws EncodeException {
        LinkedList<TrafficNotificationItem> dataItems = object.getTrafficNotificationList();
        if (dataItems.isEmpty()) {
            return "";
        }

        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        Calendar temp;
        for (TrafficNotificationItem item : dataItems) {
            temp = echoTime;
            temp.add(Calendar.SECOND, (int)(item.getUtcTimeSec()));
            JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
            objectBuilder.add("service", item.getService())
                    .add("atrNodeId", item.getAtrNodeId())
                    .add("lineId", item.getLineId())
                    .add("time", dateFormat.format(temp.getTime()))
                    .add("typeclass",NotificationType.values()[item.getTypeClass()].toString())
                    .add("text",item.getText());
            arrayBuilder.add(objectBuilder);
        }
        JsonObject jsonObject = Json.createObjectBuilder()
                //.add("msgType", DashboardMsgType.KPI_INSTANT_DATA.toString())
                .add("dataItems", arrayBuilder).build();
        StringWriter writer = new StringWriter();
        try (JsonWriter jsonWriter = Json.createWriter(writer)) {
            jsonWriter.write(jsonObject);
            jsonWriter.close();
        }
        return writer.toString();
    }

    @Override
    public void init(EndpointConfig config) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void destroy() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}

