//package bgu.spl.mics;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.jupiter.api.Test;
//
//import static org.junit.Assert.*;
//import bgu.spl.mics.example.messages.ExampleBroadcast;
//import bgu.spl.mics.example.messages.ExampleEvent;
//import bgu.spl.mics.example.services.ExampleBroadcastListenerService;
//import bgu.spl.mics.example.services.ExampleEventHandlerService;
//import bgu.spl.mics.example.services.ExampleMessageSenderService;
//
//
//
//
//public class MessageBusImplTest {
//
//    private ExampleEventHandlerService ms1;
//    private ExampleBroadcastListenerService ms2;
//    private ExampleMessageSenderService sender;
//    private MessageBusImpl MB;
//    private Event e;
//    private Broadcast b;
//    private String[] args;
//
//    @Before
//    public void setUp() throws Exception{
//        MB = MessageBusImpl.getInstance();
//        e = new ExampleEvent("Hadas");
//        b = new ExampleBroadcast("Gev");
//        args = new String[5];
//        args[0] = "First";
//        args[1] = "Second";
//        args[2] = "Third";
//        args[3] = "Fourth";
//        args[4] = "Fifth";
//        ms1 = new ExampleEventHandlerService("Hadas", args);
//        ms2 = new ExampleBroadcastListenerService("Gev", args);
//        sender = new ExampleMessageSenderService("sender", args);
//    }
//
////    @After
////    public void tearDown() throws Exception{
////        e = null;
////        b = null;
////        ms1 = null;
////        ms2 = null;
////        sender = null;
////    }
//
//    @Test
//    public void subscribeEvent() throws InterruptedException {
//        MB.register(ms1);
//        MB.subscribeEvent(ExampleEvent.class, ms1);
//        MB.sendEvent(e);
//        Message message = MB.awaitMessage(ms1);
//        assertNotNull(message);
//    }
//
//    @Test
//    public void subscribeBroadcast() throws InterruptedException {
//        MB.register(ms2);
//        MB.subscribeBroadcast(ExampleBroadcast.class,ms2);
//        MB.sendBroadcast(b);
//        Message message = MB.awaitMessage(ms2);
//        assertNotNull(message);
//    }
//
//    @Test
//    public void complete() throws InterruptedException {
//        MB.register(ms1);
//        MB.subscribeEvent(ExampleEvent.class, ms1);
//        Future <String> f = MB.sendEvent(e);
//        assertFalse(f.isDone());
//        MB.awaitMessage(ms1);
//        MB.complete(e,f);
//        assertTrue(f.isDone());
//    }
//
//    @Test
//    public void sendBroadcast() throws InterruptedException {
//        MB.register(ms1);
//        MB.register(ms2);
//        MB.subscribeBroadcast(b.getClass(),ms1);
//        MB.subscribeBroadcast(b.getClass(),ms2);
//        MB.sendBroadcast(b);
//        Message mm1 =MB.awaitMessage(ms1);
//        Message mm2 =MB.awaitMessage(ms2);
//        assertNotNull(mm1);
//        assertEquals(mm1,mm2);
//
//    }
//
//    @Test
//    public void sendEvent() throws InterruptedException {
//        MB.register(ms1);
//        MB.subscribeBroadcast(b.getClass(),ms1);
//        MB.sendBroadcast(b);
//        Message mess1 = MB.awaitMessage(ms1);
//        assertNotNull(mess1);
//    }
//
//    @Test
//    public void awaitMessage()throws InterruptedException {
//        MB.register(ms1);
//        MB.subscribeBroadcast(b.getClass(),ms1);
//        MB.sendBroadcast(b);
//        Message mes =MB.awaitMessage(ms1);
//        assertTrue(mes.equals(b));
//    }
//}