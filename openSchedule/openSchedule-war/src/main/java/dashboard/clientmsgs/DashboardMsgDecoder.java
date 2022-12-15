/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dashboard.clientmsgs;

/**
 *
 * @author jiali
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import dashboard.clientmsgs.DashboardMsg.DashboardMsgType;
import dashboard.clientmsgs.DashboardMsg.Direction;
import dashboard.clientmsgs.DashboardMsg.KPIType;
import dashboard.clientmsgs.DashboardMsg.TimeType;
import dashboard.clientmsgs.KPISetting.KPIPresentation;
import java.io.StringReader;
import java.util.Date;
import java.util.ArrayList;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

/**
 *
 * @author jiali
 */
public class DashboardMsgDecoder implements Decoder.Text<DashboardMsg> {
    //Stores name-vale pairs from a Json Message
    //private Map<String, String> messageMap;

    JsonObject jsonObject;

    @Override
    public void init(EndpointConfig ec) {
    }

    @Override
    public void destroy() {
    }

    /**
     *
     * @param msg
     * @return KPIFilterMsg which is decoded from Json filter msg from client
     * @throws DecodeException
     */
    @Override
    public DashboardMsg decode(String msg) throws DecodeException {
        if (jsonObject.getInt("msgType") == DashboardMsgType.SET_FILTERS.ordinal()) {
            return decodeFilterMsg(msg);
        } else {
            return decodeSettingMsg(msg);
        }
    }

    @Override
    public boolean willDecode(String string) {
        boolean canDecode = false;
        try {
            jsonObject = Json.createReader(new StringReader(string)).readObject();
            Integer msgType = jsonObject.getInt("msgType");
            if (msgType == DashboardMsgType.CHANGE_SETTINGS.ordinal() || msgType == DashboardMsgType.SET_FILTERS.ordinal()) {
                canDecode = true;
            }
        } catch (JsonException ex) {
            return false;
        }

        return canDecode;
    }

    private ArrayList<LocationFilter> parseLocations(JsonObject jsonObject, String key) {
        if (jsonObject.containsKey(key)) {
            return null;
        }

        JsonArray jsonArray = jsonObject.getJsonArray(key);
        ArrayList<LocationFilter> locationFilters = new ArrayList<>();
        for (int j = 0; j < jsonArray.size(); j++) {
            JsonObject jsonLocation = jsonArray.getJsonObject(j);
            LocationFilter locationFilter = new LocationFilter();
            //locationFilter.setLocationType(LocationFilter.LocationType.LINES);
            locationFilter.setDirection(Direction.values()[jsonLocation.getInt("direction")]);
            locationFilter.setLocationId(jsonLocation.getInt("locationId"));
            locationFilters.add(locationFilter);
        }
        return locationFilters;
    }

    @SuppressWarnings("deprecation")
    private KPIFilterMsg decodeFilterMsg(String msg) {
        JsonArray filterArray = jsonObject.getJsonArray("filters");
        if (filterArray == null) {
            return null;
        }

        KPIFilterMsg filterMessage = new KPIFilterMsg();
        ArrayList<KPIFilter> filters = new ArrayList<>();
        for (int i = 0; i < filterArray.size(); i++) {
            JsonObject jsonFilter = filterArray.getJsonObject(i);
            KPIFilter filter = new KPIFilter();
            filter.setKpiType(KPIType.values()[jsonFilter.getInt("kpitype")]);

            JsonObject jsonLocations = jsonFilter.getJsonObject("locations");
            filter.setLineLocations(parseLocations(jsonLocations, "lines"));
            filter.setSectionLocations(parseLocations(jsonLocations, "sections"));
            filter.setPointLocations(parseLocations(jsonLocations, "points"));

            if (jsonFilter.containsKey("time")) {
                TimeFilter timeFilter = new TimeFilter();
                JsonObject jsonTime = jsonFilter.getJsonObject("time");
                if (jsonTime.getInt("timeType") == TimeType.Now.ordinal()) {
                    timeFilter.setTimeType(TimeType.Now);
                } else {
                    timeFilter.setTimeType(TimeType.DURATION);
                    timeFilter.setDateStart(new Date(jsonTime.getString("From")));
                }
                filter.setTimeFilter(timeFilter);
            }

            if (jsonFilter.containsKey("trains")) {
                JsonArray jsonTrains = jsonFilter.getJsonArray("trains");
                if (jsonTrains != null) {
                    ArrayList<Integer> trains = new ArrayList<>();
                    for (int j = 0; j < jsonTrains.size(); j++) {
                        trains.add(jsonTrains.getInt(j));
                    }
                    filter.setTrains(trains);
                }
            }
            filters.add(filter);

        }
        filterMessage.setKpiFilters(filters);
        return filterMessage;
    }

    private KPISettingMsg decodeSettingMsg(String msg) {
        JsonArray settingArray = jsonObject.getJsonArray("settings");
        KPISettingMsg kpiSettingMsg = new KPISettingMsg();
        if (settingArray == null) {
            return kpiSettingMsg;
        }

        
        ArrayList<KPISetting> settings = new ArrayList<>();
        for (int i = 0; i < settingArray.size(); i++) {
            JsonObject jsonSetting = settingArray.getJsonObject(i);
            KPISetting setting = new KPISetting();
            setting.setKpiType(KPIType.values()[jsonSetting.getInt("kpiType")]);
            setting.setKpiPresentation(KPIPresentation.values()[jsonSetting.getInt("presentations")]);

            JsonObject thresholds = jsonSetting.getJsonObject("thresholds");

            setting.setUpperRed(Float.valueOf(thresholds.getString("upperRed")));
            setting.setUpperAmber(Float.valueOf(thresholds.getString("upperAmber")));
            setting.setLowerAmber(Float.valueOf(thresholds.getString("lowerAmber")));
            setting.setLowerGreen(Float.valueOf(thresholds.getString("lowerGreen")));
            settings.add(setting);
        }
        kpiSettingMsg.setKpiSettings(settings);
        return kpiSettingMsg;
    }
}
