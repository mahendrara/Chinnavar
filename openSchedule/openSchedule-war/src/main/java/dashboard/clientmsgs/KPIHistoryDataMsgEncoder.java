/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dashboard.clientmsgs;

import dashboard.clientmsgs.DashboardMsg.DashboardMsgType;
import java.io.StringWriter;
import java.math.BigDecimal;
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
public class KPIHistoryDataMsgEncoder implements Encoder.Text<KPIHistoryDataMsg> {

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public String encode(KPIHistoryDataMsg kpiDataMsg) throws EncodeException {
        ArrayList<KPIHistoryDataItem> kpiDataItems = kpiDataMsg.getKpiHistoryDataItems();
        if (kpiDataItems.isEmpty()) {
            return "";
        }

        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (KPIHistoryDataItem kpiData : kpiDataItems) {
            JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
            
            //if(kpiDataMsg.isGroupByKpi() == false)
                //objectBuilder.add("locationId", ((KPIHistoryDataItemByLocation)kpiData).getLocationId());
            objectBuilder.add("kpiType", kpiData.getKpiType().toString())
                    .add("time", dateFormat.format(kpiData.getTime()))
                    .add("direction", kpiData.getDirection().toString());
            if (kpiData.getTrainId() > 0) {
                objectBuilder.add("trainId", kpiData.getTrainId());
            }
            JsonArrayBuilder arrayBuilder2 = Json.createArrayBuilder();
            HistoryDataPoint kpiDataPoint;
            for (int j = 0; j < kpiData.getKpiDataPoints().size(); j++) {
                kpiDataPoint = kpiData.getKpiDataPoints().get(j);
                arrayBuilder2.add(Json.createObjectBuilder()//must add object according to user's filter setting
                        .add("kpiType", kpiDataPoint.getKpiType().toString())
                        .add("locationId", kpiDataPoint.getLocationId())
                        .add("locationTxt", kpiDataPoint.getLocationTxt())
                        .add("data", kpiDataPoint.getData()));
            }
            objectBuilder.add("dataPoints", arrayBuilder2);
            arrayBuilder.add(objectBuilder);
        }
        /*JsonObject jsonObject =Json.createObjectBuilder()
         .add("kpiType", item.getKPIType().toString())
         .add("location",item.getKPILocation().toString())
         .add("data",item.getItemData())
         .build();*/
        JsonObject jsonObject = Json.createObjectBuilder()
                .add("msgType", DashboardMsgType.KPI_HISTORY_DATA.toString())
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void destroy() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
