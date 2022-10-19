package bgu.spl.mics.application.services;

import bgu.spl.mics.*;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.ConfrenceInformation;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

/**
 * Student is responsible for sending the {@link TrainModelEvent},
 * {@link TestModelEvent} and {@link PublishResultsEvent}.
 * In addition, it must sign up for the conference publication broadcasts.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class StudentService extends MicroService {
    private Student student;
    private Future<Model> future;
    private boolean isSent;
    private int currentTime;


    public StudentService(Student student) {
        super(student.getName());
        this.student = student;
        this.future = new Future<>();
        this.isSent = false;
        this.currentTime = 0;
    }


    @Override
    protected void initialize() throws InterruptedException {
        MB.register(this);

        subscribeBroadcast(TerminateBroadcast.class, terminateBroadcast -> {
            terminate();
        });

        subscribeBroadcast(TickBroadcast.class, new Callback<TickBroadcast>(){
            @Override
            public void call(TickBroadcast c) throws InterruptedException {
                currentTime++;
                if (!student.getConfs().isEmpty()){
                    if (currentTime >= student.getConfs().peek().getDate()) {
                        student.getConfs().poll();
                    }
                }
                //System.out.println("student service received the tick");
                if (! isSent) {
                    Model currentModel = student.getCurrentModel();
                    if (currentModel == null) {
                        if (!student.getModels().isEmpty()) {
                            student.setCurrentModel(student.getModels().poll());
                        }
                    }
                    if (currentModel != null) {
                        if (student.getCurrentModel().getStatus().equals(Model.Status.PreTrained)) {
                            TrainModelEvent trainModelEvent = new TrainModelEvent(currentModel, future);
                            future = sendEvent(trainModelEvent);
                            trainModelEvent.setFuture(future);
                            student.getCurrentModel().setStatus(Model.Status.Training);
                            //   } else if (student.getCurrentModel().getStatus().equals(Model.Status.Training)) {

                        } else if (student.getCurrentModel().getStatus().equals(Model.Status.Training)) {
                            return;
                        } else if (student.getCurrentModel().getStatus().equals(Model.Status.Trained)) {
                            if (!currentModel.isTested()) {
                                TestModelEvent testModelEvent = new TestModelEvent(currentModel, future);
                                future = sendEvent(testModelEvent);
                                testModelEvent.setFuture(future);
                            }
                        } else {//Tested
//                            System.out.println(getName() + " event was tested");
                            if (currentModel.getResult().equals(Model.Result.Good)) {
//                                if (currentModel != null)
//                                    student.getTrainedModels().add(currentModel.getName());
                                PublishResultsEvent publishResultsEvent = new PublishResultsEvent(currentModel, future);
                                future = sendEvent(publishResultsEvent);
//                                System.out.println(publishResultsEvent.getModel().getName() + " event was good");
                            }
                            if (!student.getModels().isEmpty()) {
                                student.setNextCurrentModel();
                            } else {
                                student.setCurrentModel(null);
                            }
                        }
                    }
                }
            }
        });
        subscribeBroadcast(PublishConferenceBroadcast.class, new Callback<PublishConferenceBroadcast>(){
            @Override
            public void call(PublishConferenceBroadcast publishBroadcast) throws InterruptedException {
                if (publishBroadcast.getModel().getStudent().equals(student)){
                    student.incrementPublications();
                }
                else{
                    student.incrementPapersRead();
                }
            }
        });
    }
//    MicroService ms = null;
//		if (e != null) {
//        if (HashMapTypeToMS.containsKey(e.getClass())) {//checking if the event exists
//            synchronized (e.getClass()) {
//                if (! HashMapTypeToMS.get(e.getClass()).isEmpty()) {//checking if there is a microservice in that queue
//                    ms = HashMapTypeToMS.get(e.getClass()).poll();
//                    HashMapTypeToMS.get(e.getClass()).add(ms);
//                    if (HashMapMStoMsg.keySet().contains(ms)) {
//                        Future<T> future = new Future<>();
//                        HashMapEventToFuture.put(e, future);
//                        HashMapMStoMsg.get(ms).add(e);
//                        return future;
//                    }
//                }
//            }
//        }
    public <T> Future<T> sendPublishEvent(PublishResultsEvent p) {
//        if (p != null) {
//            if (MB.getHashMapTypeToMS().containsKey(p.getClass())) {//checking if the event exists
//                synchronized (p.getClass()) {
//                    if (!MB.getHashMapTypeToMS().get(p.getClass()).isEmpty()) {//checking if there is a microservice in that queue
//                        ConfrenceInformation currConf = student.getConfs().peek();
//                        ConferenceService currConfService = currConf.getService();
//                        MB.getHashMapTypeToMS().get(p.getClass()).add(currConfService);
//                        if (MB.getHashMapTypeToMS().keySet().contains(currConfService)) {
//                            Future<T> future = new Future<>();
//                            MB.getHashMapEventToFuture().put(p, future);
//                            MB.getHashMapMStoMsg().get(currConfService).add(p);
//                            return future;
//                        }
//                    }
//                }
//            }
//        }
        return null;
    }
}
