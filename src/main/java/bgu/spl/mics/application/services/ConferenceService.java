package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.CRMSRunner;
import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.ConfrenceInformation;
import bgu.spl.mics.application.objects.Model;

/**
 * Conference service is in charge of
 * aggregating good results and publishing them via the {@link //PublishConfrenceBroadcast},
 * after publishing results the conference will unregister from the system.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ConferenceService extends MicroService {
    private ConfrenceInformation confrenceInformation;
    private int tickCounter;
    private int startPublishTime;//will not publish or save any events that arrived before
    private Event<Model> currentEvent;

    public ConferenceService(String name, ConfrenceInformation confrenceInformation) {
        super(name);
        this.confrenceInformation = confrenceInformation;
        this.tickCounter = 0;
        this.currentEvent = null;
    }
    public void terminateConference(){
        MB.unregister(this);
    }

    @Override
    protected void initialize() {
        getMB().register(this);
        subscribeBroadcast(TerminateBroadcast.class, terminateBroadcast -> {
            terminate();
        });
        subscribeBroadcast(TickBroadcast.class, new Callback<TickBroadcast>() {
            @Override
            public void call(TickBroadcast tickBroadcast) throws InterruptedException {
                tickCounter++;
//                System.out.println("Conf received the tick");
                if (tickCounter == confrenceInformation.getDate()) {
//                    System.out.println("Conf date arrived");

                    for (PublishResultsEvent publish: confrenceInformation.getPublishResultsEvents()){
//                        System.out.println("publish event: "+ publish.getModel().getName());
                    }
                    while (! confrenceInformation.getPublishResultsEvents().isEmpty()){
                        Model model = confrenceInformation.getPublishResultsEvents().peek().getModel();
                        Future future = confrenceInformation.getPublishResultsEvents().poll().getFuture();
                        PublishConferenceBroadcast publishBroadcast = new PublishConferenceBroadcast(model, future);
                        sendBroadcast(publishBroadcast);
                        //save in the output file
                    }
//                    for (PublishResultsEvent event : confrenceInformation.getPublishResultsEvents()) {
//                        System.out.println(event.getModel().getName());
//                    }
//                    System.out.println(getName() + " is done!");
                    terminate();
                    terminateConference();
                }
            }
        });
        subscribeEvent(PublishResultsEvent.class, publishResultsEvent -> {
            confrenceInformation.getPublishResultsEvents().add(publishResultsEvent);
            confrenceInformation.getModels().add(publishResultsEvent.getModel());
//            System.out.println(getName() + " received the PublishResultEvent");

        });
        CRMSRunner.CDL.countDown();
    }
}
