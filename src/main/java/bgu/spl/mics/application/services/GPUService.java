package bgu.spl.mics.application.services;

import bgu.spl.mics.*;
import bgu.spl.mics.application.CRMSRunner;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * GPU service is responsible for handling the
 * {@link TrainModelEvent} and {@link TestModelEvent},
 * in addition to sending the {@link //DataPreProcessEvent}.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class GPUService extends MicroService {
    private GPU gpu;
    private Event<Model> currentEvent;
    private int startingTickProcess;//the tick in which the process of a batch started
    private int tickCounter;//the number of ticks we "saved"
    private LinkedBlockingQueue<Event<Model>> waitingEvents;

    public GPUService(String name, GPU gpu) {
        super(name);
        this.gpu = gpu;
        this.tickCounter = 0;
        this.startingTickProcess = 0;
        this.currentEvent = null;
        this.waitingEvents = new LinkedBlockingQueue<Event<Model>>();
    }
    public void setEvent(Event<Model> event){
        currentEvent = event;
    }
    @Override
    protected synchronized void initialize() throws InterruptedException {
        MB.register(this);//move to run

        subscribeEvent(TrainModelEvent.class, trainEvent -> { //Train
            setEvent(trainEvent);
            gpu.setModel(trainEvent.getModel());
            trainEvent.getModel().setStatus(Model.Status.Training);
            gpu.createBatches();
//            System.out.println(getName() + " received the TrainModelEvent " + gpu.getModel().getName());
        });

        subscribeEvent(TestModelEvent.class, new Callback<TestModelEvent>() { //Test
            public void call(TestModelEvent testEvent) throws InterruptedException {
                setEvent(testEvent);
                gpu.test(testEvent);
                complete(testEvent, gpu.getModel());
                ((TestModelEvent) currentEvent).getModel().changeToTested();
//                System.out.println("model " + ((TestModelEvent) currentEvent).getModel().getName() + " tested");
                gpu.clean();
                currentEvent = null;
            }
        });

        subscribeBroadcast(TickBroadcast.class, new Callback<TickBroadcast>() { //Tick
            @Override
            public void call(TickBroadcast c) throws InterruptedException {
                //System.out.println("GPU received a tick");
                if (currentEvent != null) {
                    Model model;
                    if (currentEvent instanceof TestModelEvent) {
                        model = ((TestModelEvent) currentEvent).getModel();
                    } else {//TrainModelEvent
                        model = ((TrainModelEvent) currentEvent).getModel();
                        gpu.getCluster().incrementGPUsed();
                    }
                    if (!model.isTrained()) {
                        gpu.train((TrainModelEvent) currentEvent); //sending the batches to the cluster
                        if (gpu.getMaxBatchesIndex() > 0) {//there is a batch to process
                            // System.out.println("there is a batch for the gpu to process");
                            tickCounter++;
                            if (tickCounter >= gpu.getTicksToTrain()) {
                                gpu.processBatch();
                                tickCounter = tickCounter - gpu.getTicksToTrain();
                                //                        System.out.println("gpu processed");
                                if (gpu.isDone()) {
//                                    System.out.println("model " + gpu.getModel().getName() + " trained by " + getName());
                                    gpu.changeToTrained();
                                    ((TrainModelEvent) currentEvent).getModel().setStatus(Model.Status.Trained);
                                    complete(currentEvent, gpu.getModel());
                                    gpu.getCluster().addTrainedModelName(gpu.getModel().getName());
                                    gpu.getModel().getStudent().getTrainedModels().add(gpu.getModel().getName());
                                    //more statistics
                                    ((TrainModelEvent) currentEvent).getModel().changeToTrained();
                                    gpu.clean();
                                    currentEvent = null;
                                }
                            }
                        }
                    }
                    else {
                        Event nextEvent = waitingEvents.poll();
                        if (nextEvent instanceof TrainModelEvent) {
                            gpu.setModel(((TrainModelEvent) nextEvent).getModel());
                            gpu.createBatches();
                        }
                        else {
                            if (nextEvent instanceof TestModelEvent) {
                                gpu.setModel(((TestModelEvent) nextEvent).getModel());
                                gpu.createBatches();
                            }
                        }
                    }
                }
            }
        });

        subscribeBroadcast(TerminateBroadcast.class, terminateBroadcast -> {
            terminate();
        });
        CRMSRunner.CDL.countDown();
    }
    public Message awaitMessage(MicroService m) throws InterruptedException {
        if (currentEvent != null){//gpu in process
            Message msg = MB.awaitMessage(m);
            while (msg instanceof Event){
                waitingEvents.add((Event<Model>) msg);
                msg = MB.awaitMessage(m);
            }
            return msg;
        }
        else{ // not in process
            if (! waitingEvents.isEmpty()){
                return waitingEvents.poll();
            }
        }
        return MB.awaitMessage(m);
    }
}
