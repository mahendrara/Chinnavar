/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule.messages;

import java.util.concurrent.CountDownLatch;
import javax.jms.CompletionListener;
import javax.jms.Message;

/**
 *
 * @author Jia Li
 */
class TTCompletionListener implements CompletionListener {

    CountDownLatch latch;
    Exception exception;

    public TTCompletionListener(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onCompletion(Message message) {
        latch.countDown();
        //System.out.println("ack received");
    }

    @Override
    public void onException(Message message, Exception exception) {
        latch.countDown();
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }
}