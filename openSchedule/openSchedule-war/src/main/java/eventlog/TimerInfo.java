/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eventlog;

import javax.ejb.Timer;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Ari
 */
public class TimerInfo {

    public String getInfo() {
        return info;
    }

    public String getNextTimeout() {
        return nextTimeout;
    }

    String info;
    String nextTimeout;
    
    TimerInfo(Timer containerEJBTimer)
    {
        if(containerEJBTimer!=null)
        {
            try {
                Serializable sinfo = containerEJBTimer.getInfo();
                if(sinfo!=null)
                {
                   info = sinfo.toString();   

                }
            } 
            catch (NullPointerException e) {
               info = "NullPointerException APP server error";
            }
            try {
                Date timeOut = containerEJBTimer.getNextTimeout();

                if(timeOut!=null)
                {
                    nextTimeout = timeOut.toString();
                }   
            }
            catch (NullPointerException e) {
               nextTimeout = "NullPointerException APP server error";
            }
        }
        if(info==null)
        {
            info = "null";
        }
        if(nextTimeout==null)
        {
            nextTimeout = "null";
        }        
    }
    
}
