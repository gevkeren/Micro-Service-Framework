package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Event;
import bgu.spl.mics.Message;
import bgu.spl.mics.application.objects.Model;

import java.util.concurrent.atomic.AtomicInteger;

public class TickBroadcast implements Broadcast {
    private AtomicInteger time;

    public TickBroadcast(AtomicInteger time){
        this.time = time;
    }
    public AtomicInteger getTime(){
        return time;
    }

}
