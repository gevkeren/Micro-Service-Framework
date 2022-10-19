package bgu.spl.mics;

import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.example.messages.ExampleBroadcast;

import bgu.spl.mics.example.messages.ExampleEvent;

import java.util.concurrent.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	private static MessageBusImpl messageBusImplSingleton;

	private ConcurrentHashMap<Class<? extends Message>, ConcurrentLinkedQueue<MicroService>> HashMapTypeToMS; //Hash Map of message types with Queue of MicroServices
	private ConcurrentHashMap< MicroService, LinkedBlockingQueue<Message>> HashMapMStoMsg;//Hash Map of MicroServices with Queue of Messages
	private ConcurrentHashMap<Event, Future> HashMapEventToFuture;// Hash Map of events with all the future values of the events
	private ConcurrentHashMap< MicroService, LinkedBlockingQueue<Message>> HashMapMStoMsg2;


	protected MessageBusImpl(){
		HashMapTypeToMS = new ConcurrentHashMap<>();
		HashMapMStoMsg = new ConcurrentHashMap<>();
		HashMapEventToFuture = new ConcurrentHashMap<>();
	}
	private static class MessageBusImplHolder {// Singleton
		private static MessageBusImpl instance = new MessageBusImpl();
	}
	protected boolean isSubscribedBroadcast(Class<ExampleBroadcast> exampleBroadcastClass, MicroService m) {
		if (getHashMapTypeToMS().containsKey(exampleBroadcastClass)){
			return getHashMapTypeToMS().get(exampleBroadcastClass).contains(m);
		}
		return false;
	}

	protected boolean isSubscribedEvent(Class<ExampleEvent> exampleEventClass, MicroService m) {
		if (getHashMapTypeToMS().containsKey(exampleEventClass)){
			return getHashMapTypeToMS().get(exampleEventClass).contains(m);
		}
		return false;
	}

	protected boolean isRegister(MicroService m) {
		if (getHashMapMStoMsg().containsKey(m)){
			return true;
		}
		return false;
	}
	@Override

	public synchronized <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		if(isRegister(m)) {
			HashMapTypeToMS.putIfAbsent(type, new ConcurrentLinkedQueue<>());
			HashMapTypeToMS.get(type).add(m);
		}
	}

	/**
	 *
	 * @post: @pre.HashMapMsgToMS + 1 = HashMapMsgToMS
	 */
	public synchronized void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		if(isRegister(m)) {
			HashMapTypeToMS.putIfAbsent(type, new ConcurrentLinkedQueue<>());
			HashMapTypeToMS.get(type).add(m);
		}
	}
	/**
	 *
	 * @post: @pre.HashMapMsgToMS + 1 = HashMapMsgToMS
	 */

	public ConcurrentHashMap< MicroService, LinkedBlockingQueue<Message>>  getHashMapMStoMsg(){
		return HashMapMStoMsg;
	}

	public ConcurrentHashMap<Event, Future> getHashMapEventToFuture() {
		return HashMapEventToFuture;
	}

	public ConcurrentHashMap<Class<? extends Message>, ConcurrentLinkedQueue<MicroService>> getHashMapTypeToMS(){

		return HashMapTypeToMS;
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		if (e != null && result != null) {
			HashMapEventToFuture.get(e).resolve(result);
		}
	}

	@Override
	/**
	 *
	 * @post: @pre.HashMapMsgToMS + 1 = HashMapMsgToMS for all the microServices that  subscribe this kind of message
	 */
	public synchronized void sendBroadcast(Broadcast b) {
		if (b != null) {
			if (HashMapTypeToMS.get(b.getClass()) != null) {
				for (MicroService ms : HashMapTypeToMS.get(b.getClass())) {
					synchronized (ms) {//not sure
						if (HashMapMStoMsg.get(ms) != null) {
							HashMapMStoMsg.get(ms).add(b);
						}
					}
				}
			}
		}
	}


	@Override
	/**
	 * @pre: this Event contains to HashMapMsgToMS
	 * @post: @pre.HashMapMStoMsg + 1 = HashMapMStoMsg
	 */
	public <T> Future<T> sendEvent(Event<T> e) {
		MicroService ms = null;
		if (e != null) {
			if (HashMapTypeToMS.containsKey(e.getClass())) {//checking if the event exists
				synchronized (e.getClass()) {
					if (! HashMapTypeToMS.get(e.getClass()).isEmpty()) {//checking if there is a microservice in that queue
						ms = HashMapTypeToMS.get(e.getClass()).poll();
						HashMapTypeToMS.get(e.getClass()).add(ms);
						if (e instanceof PublishResultsEvent){
							int numOfConf = HashMapTypeToMS.get(e.getClass()).size();
//							for (int i = 0; i < numOfConf - 1; i++){
//								HashMapTypeToMS.get(e.getClass()).add(HashMapTypeToMS.get(e.getClass()).poll());
//							}
						}
						if (HashMapMStoMsg.keySet().contains(ms)) {
							Future<T> future = new Future<>();
							HashMapEventToFuture.put(e, future);
							HashMapMStoMsg.get(ms).add(e);
							return future;
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	/**
	 * @pre: m not contains to HashMapMStoMsg
	 * @post: @pre.HashMapMStoMsg + 1 = HashMapMStoMsg
	 */
	public synchronized void register(MicroService m) {
		HashMapMStoMsg.put(m, new LinkedBlockingQueue<>());
	}

	@Override
	/**
	 * @pre: m  contains to HashMapMStoMsg
	 * @post: m not contains to HashMapMStoMsg
	 */
	public synchronized void unregister(MicroService m) {
		HashMapMStoMsg.remove(m);
		for(Class<? extends Message> type : HashMapTypeToMS.keySet()){
			if (HashMapTypeToMS.get(type).contains(m))
				HashMapTypeToMS.get(type).remove(m);
		}

	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException{
		if(isRegister(m) && m != null){
			Message message = null;
			message = HashMapMStoMsg.get(m).take();
			return message;
		}
		throw new IllegalArgumentException("m is not registered");
	}
	public static MessageBusImpl getInstance(){
		if(MessageBusImplHolder.instance == null){
			MessageBusImplHolder.instance = new MessageBusImpl();
		}
		return MessageBusImplHolder.instance;
	}



}