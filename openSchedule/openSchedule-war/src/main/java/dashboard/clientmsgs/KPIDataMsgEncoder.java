/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dashboard.clientmsgs;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

/**
 *
 * @author jiali
 */
public class KPIDataMsgEncoder implements Encoder.Text<KPIDataMsg> {

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public String encode(KPIDataMsg kpiDataMsg) throws EncodeException {
        ArrayList<KPIDataItem> kpiDataItems = kpiDataMsg.getKpiDataItems();
        if (kpiDataItems.isEmpty()) {
            return "";
        }

        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (KPIDataItem kpiData : kpiDataItems) {
            JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
            objectBuilder.add("kpiType", kpiData.getKpiType().toString())
                .add("locationId", kpiData.getLocationId())
                .add("locationTxt", kpiData.getLocationTxt())
                .add("direction", kpiData.getDirection().toString());
            if(kpiData.getTrainId()>0)
                objectBuilder.add("trainId", kpiData.getTrainId());
            objectBuilder.add("time", dateFormat.format(kpiData.getTime()))
                .add("data", kpiData.getData());
            arrayBuilder.add(objectBuilder);
        }    
  
        JsonObject jsonObject = Json.createObjectBuilder()
                .add("msgType", kpiDataMsg.getMsgType().toString())
                .add("kpiDataItems", arrayBuilder).build();
        StringWriter writer = new StringWriter();
        try (JsonWriter jsonWriter = Json.createWriter(writer)) {
            jsonWriter.write(jsonObject);
            jsonWriter.close();
        }
        //String s = writer.toString();
        //System.out.println(s);
        return writer.toString();
    }

    @Override
    public void init(EndpointConfig config) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void destroy() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
