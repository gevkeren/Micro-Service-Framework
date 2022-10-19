package bgu.spl.mics.application.services;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import sun.swing.SwingUtilities2;

/**
 * TimeService is the global system timer There is only one instance of this micro-service.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other micro-services about the current time tick using {@link TickBroadcast}.
 * This class may not hold references for objects which it is not responsible for.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends MicroService{
	private int tickDef;
	private int duration;
	public AtomicInteger timeSinceStart;

	public TimeService(int duration, int tickDef) {
		super("Time Service");
		this.duration = duration;
		this.tickDef = tickDef;
		timeSinceStart = new AtomicInteger();


	}
	private void endProgram(){

	}
	@Override
	protected void initialize() {
		MB.register(this);
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask(){

			@Override
			public void run() {
				if (! (timeSinceStart.intValue() >= duration)){
					TickBroadcast tickToBroadcast = new TickBroadcast(timeSinceStart);
					sendBroadcast(tickToBroadcast);
					timeSinceStart.incrementAndGet();
//					if(timeSinceStart.intValue() % 100 == 0)
//						System.out.println(timeSinceStart);
				}
				else{
					TerminateBroadcast terminateBroadcast = new TerminateBroadcast();
					sendBroadcast(terminateBroadcast);
					timer.cancel();
					terminate();
				}
			}
		},0,tickDef);

//		TimerTask changeTick = new TimerTask(){
//			@Override
//			public void run() {
//
//			}
//		};
		subscribeBroadcast(TerminateBroadcast.class, terminateBroadcast -> {
			terminate();
		});
		//timer.schedule(changeTick,0,tickDef);
	}


}
