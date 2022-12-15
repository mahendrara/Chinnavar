/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eventlog;

import java.time.LocalTime;
import java.util.List;
import javax.annotation.PostConstruct;
//import javax.enterprise.context.ApplicationScoped;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
//import javax.inject.Named;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import javax.annotation.Resource;
import javax.ejb.Timer;
import javax.ejb.TimerService;

/**
 *
 * @author Ari
 */
//@Named("eventLogBean")
//@ApplicationScoped
@Startup
@Singleton
public class EventLogBean {

    int bufSize;
    int curHead;
    int curTail;
    boolean bufferFull;
    String myServerHostName = "?";
    
    public boolean isEmpty() {
        return !this.bufferFull && (this.curHead == this.curTail);
    }
    @PostConstruct
    public void afterCreate() {
        initWithSize(20);
        
        InetAddress ip;
        try {
            ip = InetAddress.getLocalHost();
            myServerHostName = ip.getHostName();            
        } 
        catch (UnknownHostException e) {
 
            e.printStackTrace();
        }        
        addEvent("eventLogBean", "Event log started.");
    }

    EventRecord[] eventsInMemory;
    @Resource
    TimerService timerService;
     
    void initWithSize(int sizeOfRingBuffer) {
        bufSize = sizeOfRingBuffer;
        bufferFull = false;   
        eventsInMemory = new EventRecord[sizeOfRingBuffer];
	curHead = 0;
        curTail = 0;
	
    }
    public int getItemsCount()
    {
        if(bufferFull) {
            return bufSize;
        }
        else {
            return ((curHead > curTail) ? (curTail + bufSize) - curHead : curTail - curHead);
        }
	
    }
    public void addEvent(String objectName,String eventString) {
         
         EventRecord event = new EventRecord(myServerHostName,objectName,eventString);
         addEvent(event);         
    }
    public void addEventFromObject(Object objectInstance,boolean useClassName,String eventString) {
         
         String objectName;
         if(useClassName)
         {
            objectName = objectInstance.getClass().toString();   
         }   
         else
         {
            objectName = objectInstance.toString();
         }
         EventRecord event = new EventRecord(myServerHostName,objectName,eventString);
         addEvent(event);         
    }
    public void addEvent(EventRecord event) {
        if(bufferFull) curHead = (curHead + 1) % bufSize;
		
	eventsInMemory[curTail] = event;
	curTail = (curTail + 1) % bufSize;
        
	if (curTail == curHead) {
            bufferFull = true;
        }
    }
    public void getEvents(List<EventRecord> targetList,String filter) {
     
        int numElements = getItemsCount();
	for (int i = 0; i < numElements; i++)
        {
            int index = (curHead + i) % bufSize;
            targetList.add(eventsInMemory[index]);
            //sb.append(this.buffer[(this.head + i) % this.n] + "\n");
        }
    }
    public List<TimerInfo> getEJBTimers() {
        
        Collection<Timer> allTimers = timerService.getAllTimers();
        
        List<TimerInfo> timerList = new ArrayList<TimerInfo>();
        
        if(allTimers!=null)
        {
            
            for(Timer timer : allTimers)
            {
                TimerInfo info = new TimerInfo(timer);
                timerList.add(info);
            }                
        }        
        return timerList;
    }
            
    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
}
