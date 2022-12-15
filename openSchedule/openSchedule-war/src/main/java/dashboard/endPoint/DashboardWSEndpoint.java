/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dashboard.endPoint;

import dashboard.clientmsgs.DashboardMsg;
import dashboard.clientmsgs.DashboardMsg.DashboardMsgType;
import dashboard.clientmsgs.DashboardMsg.Direction;
import dashboard.clientmsgs.DashboardMsg.KPILocation;
import dashboard.clientmsgs.DashboardMsg.KPIType;
import dashboard.clientmsgs.DashboardMsgDecoder;
import dashboard.clientmsgs.HistoryDataPoint;
import dashboard.clientmsgs.KPIHistoryDataItem;
import dashboard.clientmsgs.KPIHistoryDataMsg;
import dashboard.clientmsgs.KPIHistoryDataMsgEncoder;
import dashboard.clientmsgs.KPIDataItem;
import dashboard.clientmsgs.KPIDataMsg;
import dashboard.clientmsgs.KPIDataMsgEncoder;
import dashboard.clientmsgs.KPIFilterMsg;
import dashboard.clientmsgs.KPISettingMsg;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

/**
 *
 * @author jiali
 */
@ServerEndpoint(value = "/dashboardendpoint/{groupBy}", encoders = {KPIDataMsgEncoder.class, KPIHistoryDataMsgEncoder.class},
        decoders = {DashboardMsgDecoder.class/*KPIFilterMsgDecoder.class, KPISettingMsgDecoder.class*/})
public class DashboardWSEndpoint {

    static final Logger logger = Logger.getLogger("DashboardWSEndpoint");
    private static Set<Session> sessions = Collections.synchronizedSet(new HashSet<Session>());
    Timer timer = null;
    TimerTask timerTask;
    int delay = 1000;
    int period = 10000;
    Date date;
    Random random = new Random();
    //cached data
    ArrayList<KPIDataItem> items = new ArrayList<>();
    KPIHistoryDataMsg historyDataMsgByKpi;
    //KPIHistoryDataMsgByLocation historyDataMsgByLocation;
    static boolean needHistoryMsgByKPI = false;
    static boolean needHistoryMsgByLocation = false;

    public DashboardWSEndpoint() {
    }

    @OnMessage
    public void onMessage(Session session, DashboardMsg msg) {
        if (msg instanceof KPIFilterMsg) {
            KPIFilterMsg filterMsg = (KPIFilterMsg) msg;
         //session.getUserProperties().put("sessionid",filterMsg.getUserId)
            //send msg according to filter settings

        } else if (msg instanceof KPISettingMsg) {
            //KPIFilterMsg filterMsg =  (KPIFilterMsg)msg;
            //temp code
            KPIDataMsg kpiDataMsg = generateKPIDataMsg();
            broadcastKPI(kpiDataMsg);
            GenerateHistoryDataFromInstanceData();
            System.out.println("onMessage: " + session.getId());
        }
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig c, @PathParam("groupBy") String groupBy) {
        session.getUserProperties().put("groupBy", groupBy);
        if (needHistoryMsgByKPI == false && groupBy.equals("groupByKPI")) {
            needHistoryMsgByKPI = true;
        }

        if (needHistoryMsgByLocation == false && groupBy.equals("groupByLocation")) {
            needHistoryMsgByLocation = true;
        }

        System.out.println("onOpen: " + session.getId());
        //synchronized (sessions) {
        sessions.add(session);
        //}
        if (sessions.size() == 1) {
            timer = new Timer();
            timerTask = new GenerateKPIData();
            timer.scheduleAtFixedRate(timerTask, delay, period);
            //generateHistoryData();
        }
    }

    class GenerateKPIData extends TimerTask {

        @Override
        public void run() {
            KPIDataMsg kpiDataMsg = generateKPIDataMsg();
            broadcastKPI(kpiDataMsg);
            GenerateHistoryDataFromInstanceData();
        }
    }

    private KPIDataMsg generateKPIDataMsg() {
        KPIDataMsg kpiDataMsg = new KPIDataMsg();
        kpiDataMsg.setMsgType(DashboardMsgType.KPI_INSTANT_DATA);
        //ArrayList<KPIInstantDataItem> items = new ArrayList<>();
        date = new Date();
        //int nextInt;// = random.nextInt(5);
        for (int j = 0; j < 5; j++) {

            KPIType kpiType = KPIType.values()[j];
            float sum = 0.0f;
            for (int i = 0; i < 5; i++) {
                KPILocation kpiLocation = KPILocation.values()[i + 1];
                int cachedIndex = findCachedItem(kpiType, kpiLocation);
                Float cachedData = 0.0f;
                if (cachedIndex > -1) {
                    cachedData = items.get(cachedIndex).getData();
                    items.remove(cachedIndex);
                }
                Float itemData = generateRandomData(kpiType, cachedData);
                sum += itemData;
                items.add(new KPIDataItem(kpiType, kpiLocation.ordinal(), kpiLocation.toString(), Direction.NORTH, 0, date, itemData));
            }
            int cachedIndex = findCachedItem(kpiType, KPILocation.values()[0]);
            if (cachedIndex > -1) {
                items.remove(cachedIndex);
            }
            sum = sum / 5;
            items.add(new KPIDataItem(kpiType, KPILocation.values()[0].ordinal(), KPILocation.values()[0].toString(), Direction.NORTH, 0, date, sum));
        }
        kpiDataMsg.setKpiDataItems(items);
        return kpiDataMsg;
    }

    private int findCachedItem(KPIType kpiType, KPILocation kpiLocation) {
        int index = -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getKpiType() == kpiType && items.get(i).getLocationId() == kpiLocation.ordinal()) {
                index = i;
                break;
            }
        }
        return index;
    }

    private Float generateRandomData(KPIType kpiType, Float oldData) {
        Float itemData = 0.0f;
        boolean add = random.nextInt(3) % 2 == 0;
        switch (kpiType) {
            case AVERAGE_LATENESS:

                if (oldData > 0.0f) {

                    int rand = random.nextInt(3);
                    if (add) {
                        itemData = oldData + rand > 10 ? oldData - rand / 2 - 0.125f : oldData + rand / 2 - 0.125f;
                    } else {
                        itemData = oldData - rand < 5 ? oldData + rand / 2 + 0.125f : oldData - rand / 2 + 0.125f;
                    }

                } else {
                    if (add) {
                        itemData = 5.0f + random.nextInt(9 - 5 + 1);
                    } else {
                        itemData = 5.0f - random.nextInt(9 - 5 + 1);
                    }
                }

                break;
            case PERCENTAGE_OF_HEADWAYS:
                if (oldData > 0.0f) {

                    int rand = random.nextInt(3);
                    if (add) {
                        itemData = oldData + rand > 95 ? oldData - rand / 2 - 0.125f : oldData + rand / 2 - 0.125f;
                    } else {
                        itemData = oldData - rand < 75 ? oldData + rand / 2 + 0.125f : oldData - rand / 2 + 0.125f;
                    }
                } else {
                    if (add) {
                        itemData = 80.0f + random.nextInt(90 - 89 + 1);
                    } else {
                        itemData = 80.0f - random.nextInt(90 - 89 + 1);
                    }
                }
                //itemData = random.nextInt(100);
                break;
            case KILOMETERS_DELIVERED:

                //itemData = 1000 + random.nextInt(10);
                //itemData = 1000.0f + 10 + random.nextInt(15 - 10 + 1);
                if (oldData > 0.0f) {

                    int rand = random.nextInt(3);
                    if (add) {
                        itemData = oldData + rand > 10 ? oldData - rand / 2 - 0.125f : oldData + rand / 2 - 0.125f;
                    } else {
                        itemData = oldData - rand < 5 ? oldData + rand / 2 + 0.125f : oldData - rand / 2 + 0.125f;
                    }

                } else {
                    if (add) {
                        itemData = 5.0f + random.nextInt(9 - 5 + 1);
                    } else {
                        itemData = 5.0f - random.nextInt(9 - 5 + 1);
                    }
                }
                break;
            case JOURNEY_TIME_STATISTICS:
                if (oldData > 0.0f) {

                    int rand = random.nextInt(3);
                    if (add) {
                        itemData = oldData + rand > 10 ? oldData - rand / 2 - 0.125f : oldData + rand / 2 - 0.125f;
                    } else {
                        itemData = oldData - rand < 5 ? oldData + rand / 2 + 0.125f : oldData - rand / 2 + 0.125f;
                    }

                } else {
                    if (add) {
                        itemData = 5.0f + random.nextInt(9 - 5 + 1);
                    } else {
                        itemData = 5.0f - random.nextInt(9 - 5 + 1);
                    }
                }
                //itemData = 1000.0f + 50 + random.nextInt(70 - 50 + 1);
                break;
            case PLATFORM_WAIT_TIME:
                if (oldData > 0.0f) {

                    int rand = random.nextInt(3);
                    if (add) {
                        itemData = oldData + rand > 10 ? oldData - rand / 2 - 0.125f : oldData + rand / 2 - 0.125f;
                    } else {
                        itemData = oldData - rand < 5 ? oldData + rand / 2 + 0.125f : oldData - rand / 2 + 0.125f;
                    }

                } else {
                    if (add) {
                        itemData = 5.0f + random.nextInt(9 - 5 + 1);
                    } else {
                        itemData = 5.0f - random.nextInt(9 - 5 + 1);
                    }
                }
                //itemData = 10.0f + random.nextInt(5);
                break;
        }
        return itemData;
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        System.out.println("onClose: " + session.getId());
        if (sessions.isEmpty()) {
            timerTask.cancel();
            timer.cancel();
            timer = null;
            timerTask = null;
        }
    }

    @OnError
    public void onError(Session session, Throwable t) {
        //queue.remove(session);
        System.out.println("onError: " + session.getId() + ": " + t.toString());
    }

    public synchronized void broadcastKPI(DashboardMsg kpiDataMsg) {

        //synchronized (sessions) {
        try {
            for (Session session : sessions) {// synchronized by SynchronizedSet 
                if (session.isOpen()) {
                    /*if((kpiDataMsg.getMsgType() ==  DashboardMsgType.KPI_HISTORY_DATA && session.getUserProperties().get("groupBy").equals("groupByKPI"))
                     ||(kpiDataMsg.getMsgType() == DashboardMsgType.KPI_HISTORY_DATA && session.getUserProperties().get("groupBy").equals("groupByLocation")))
                     {
                     session.getBasicRemote().sendObject(kpiDataMsg); //not synchronized
                     }
                     else if(kpiDataMsg.getMsgType() == DashboardMsgType.KPI_INSTANT_DATA)*/
                    session.getBasicRemote().sendObject(kpiDataMsg);
                }
            }
        } catch (IOException | EncodeException e) {
            logger.log(Level.SEVERE, null, e);
            //throws ex;
        }
        //}
    }

    //generate history data for testing
    /*private void generateHistoryData() {
     KPIHistoryDataMsg kpiDataMsg = new KPIHistoryDataMsg();
     kpiDataMsg.setMsgType(DashboardMsgType.KPI_HISTORY_DATA);
     int historyDuriation  =  60;//60*period/1000 minutes
     ArrayList<KPIHistoryDataItem> historyItems = new ArrayList<>();
     Date date1 = new Date();
     Date date2 = new Date(date1.getTime() - 1000*historyDuriation);
     KPIHistoryDataItem item;
     HistoryDataPoint dataPoint;
     //items.clear();
     for (int i = 0; i < historyDuriation; i++) {
            
     for (int j = 0; j < 5; j++) {
     item = new KPIHistoryDataItem(KPIType.values()[j],new Date(date2.getTime() + 1000*i),Direction.NORTH,0);
     item.kpiDataPoints = new ArrayList<>();
     for(int n = 0; n < 6; n++) {
     Float itemData = generateRandomData(KPIType.values()[j],KPILocation.values()[n]);
     dataPoint = new HistoryDataPoint(KPILocation.values()[n].ordinal(),KPILocation.values()[n].toString(),itemData);
     item.kpiDataPoints.add(dataPoint);
     }
     historyItems.add(item);
     }
     }
     kpiDataMsg.setKpiHistoryDataItems(historyItems);
     DashboardWSEndpoint.broadcastKPI(kpiDataMsg);
     }*/
    private void GenerateHistoryDataFromInstanceData() {
        //KPIHistoryDataMsg kpiDataMsg = new KPIHistoryDataMsg();

        HistoryDataPoint dataPoint;
        if (needHistoryMsgByKPI) {
            ArrayList<KPIHistoryDataItem> historyItems = new ArrayList<>();
            historyDataMsgByKpi = new KPIHistoryDataMsg();
            historyDataMsgByKpi.setMsgType(DashboardMsgType.KPI_HISTORY_DATA);

            KPIHistoryDataItem item = null;
            for (int i = 0; i < items.size(); i++) {
                KPIDataItem instantItem = items.get(i);
                if (i % 6 == 0) {
                    item = new KPIHistoryDataItem(instantItem.getKpiType(), instantItem.getTime(), instantItem.getDirection(), 0);
                    item.setKpiDataPoints(new ArrayList<HistoryDataPoint>());
                }
                dataPoint = new HistoryDataPoint(instantItem.getLocationId(), instantItem.getLocationTxt(), instantItem.getData(), instantItem.getKpiType());
                item.getKpiDataPoints().add(dataPoint);
                historyItems.add(item);
            }
            historyDataMsgByKpi.setKpiHistoryDataItems(historyItems);
            broadcastKPI(historyDataMsgByKpi);
        }

        if (needHistoryMsgByLocation) {
            KPIDataMsg kpiDataMsg = new KPIDataMsg();
            kpiDataMsg.setMsgType(DashboardMsgType.KPI_HISTORY_DATA);
            ArrayList<KPIDataItem> dataItems = new ArrayList<>();

            for (int i = 0; i < items.size(); i++) {
                KPIDataItem instantItem = items.get(i);
                dataItems.add(new KPIDataItem(instantItem.getKpiType(), instantItem.getLocationId(), instantItem.getLocationTxt(), instantItem.getDirection(), 0, instantItem.getTime(), instantItem.getData()));
            }
            kpiDataMsg.setKpiDataItems(dataItems);
            broadcastKPI(kpiDataMsg);
        }
    }

}
