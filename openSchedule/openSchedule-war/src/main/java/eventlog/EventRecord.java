/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eventlog;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 *
 * @author Ari
 */
public class EventRecord
{
    EventRecord(String server,String object,String text) {
        serverName = server;
        eventText = text;
        objectName = object;
        time = LocalDateTime.now();
    }
    public LocalDateTime getEventTime() {
        return time; 
    }
    public String getServerName() {
        return serverName; 
    }
    public String getName() {
        return objectName; 
    }
    public String getEventText() {
        return eventText; 
    }
    LocalDateTime time;
    String serverName;
    String objectName;
    String eventText;
}
