package bgu.spl.mics.application.objects;

import bgu.spl.mics.Message;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TrainModelEvent;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Passive object representing a single GPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class GPU {
    /**
     * Enum representing the type of the GPU.
     */
    public enum Type {RTX3090, RTX2080, GTX1080}
    private Type type;
    private Model model;
    private Cluster cluster = Cluster.getInstance();
    private int numOfBatches;
    private ConcurrentLinkedQueue<DataBatch> nonProcessed;
    private int batchesSent; //batches the GPU has already sent to the cluster
    private int processedBatches; //batches both the gpu and the cpu had processed
    private int maxBatchesIndex; //index of the next empty spot int the maxBatches array
    private DataBatch[] maxBatches; //array of the batches the cpu had processed and the gpu didn't
    private int ticksToTrain;
    private MessageBusImpl MB = MessageBusImpl.getInstance();
    private MicroService gpuService;



    public GPU(Type type){
        this.type = type;
        model = null;
        batchesSent = 0;
        processedBatches = 0;
        maxBatchesIndex = 0;
        numOfBatches = 0;
        nonProcessed = new ConcurrentLinkedQueue<DataBatch>();
        if (type == Type.GTX1080) {
            maxBatches = new DataBatch[8];
            ticksToTrain = 4;
        }
        else if (type == Type.RTX2080) {
            maxBatches = new DataBatch[16];
            ticksToTrain = 2;
        }
        else {
            maxBatches = new DataBatch[32];
            ticksToTrain = 1;
        }
        gpuService = null;
    }
    public void createBatches(){
        int numOfBatches = model.getData().getSize();
        for (int i = 0; i < numOfBatches; i = i + 1000){
            nonProcessed.add(new DataBatch(model.getData(), i));
        }
    }

    public MicroService getGpuService() {
        return gpuService;
    }

    public void setGpuService(MicroService gpuService) {
        this.gpuService = gpuService;
    }

    public Cluster getCluster() {
        return cluster;
    }
    public void clean() {
        batchesSent = 0;
        processedBatches = 0;
        maxBatchesIndex = 0;
        nonProcessed = new ConcurrentLinkedQueue<DataBatch>();
        for (int i = 0; i < maxBatches.length; i++){//could be replaced with arrays.fill
            maxBatches[i] = null;
        }
    }
    public int getNumOfUnprocessedByGPU() {
        return maxBatchesIndex;
    }
    public synchronized void train(TrainModelEvent event){
        while ((batchesSent - processedBatches < maxBatches.length - maxBatchesIndex) && (batchesSent != numOfBatches)){
            sendBatchToCluster();
        }
    }
    public int getTicksToTrain(){
        return ticksToTrain;
    }
    public void processBatch(){
        maxBatches[maxBatchesIndex - 1] = null;
        processedBatches++;
        maxBatchesIndex--;
    }
    public void changeToTrained(){
        model.setStatus(Model.Status.Trained);
    }
    public void test(TestModelEvent event){
        double probability = Math.random(); //will get a value between 0-1
        synchronized (event.getModel()) {
            if (event != null) {
                if (event.getModel() != null) {
                    if (event.getModel().getStudent() != null) {
                        Student.Degree degree = event.getModel().getStudent().getStatus();
                        if (degree != null) {
                            if (degree.equals(Student.Degree.MSc)) {//MSc - 0.6 to be good
                                if (probability <= 0.6) {
                                    event.getModel().setResult(Model.Result.Good);
                                } else {
                                    event.getModel().setResult(Model.Result.Bad);
                                }
                            } else {//PhD - 0.8 to be good
                                if (probability <= 0.8) {
                                    event.getModel().setResult(Model.Result.Good);
                                } else {
                                    event.getModel().setResult(Model.Result.Bad);
                                }
                            }
                        }
                    }
                }
            }
        }
        event.getModel().setStatus(Model.Status.Tested);
    }
    public Type getType(){
        return type;
    }
    public boolean isDone(){
        return processedBatches == numOfBatches;
    }

    public Model getModel(){
        return model;
    }
    public int getProcessedBatches(){
        return processedBatches;
    }
    public int getBatchesSent(){
        return batchesSent;
    }
    /**
     *
     * @post: this.model = model
     */
    public void setModel(Model model){
        this.model = model;
        setNumOfBatches(model.getData().getSize()/1000);
        this.model.getData().setGpu(this);
    }
    /**
     * @pre: isDone() = true
     * @post: this.model = model
     */
    public void receiveModelFromMB(Model model){
        if (isDone()){
            setModel(model);
        }
    }
    /**
     *
     * @post: @pre.batchesSent + 1 = batchesSent
     */
    public synchronized void sendBatchToCluster(){
        if (batchesSent - processedBatches < maxBatches.length){//probably wasteful
            batchesSent++;
            cluster.addBatchToCluster(nonProcessed.poll());
        }

    }
    public boolean isFull(){
        return (maxBatchesIndex >= maxBatches.length);
    }
    public void addToMaxBatchesArray(DataBatch batch) {
        maxBatches[maxBatchesIndex] = batch;
        maxBatchesIndex++;
    }
    public void setNumOfBatches(int numOfBatches){
        this.numOfBatches = numOfBatches;
    }

    public MessageBusImpl getMB() {
        return MB;
    }
//    public void awaitTestModel() {
//        Message msg = (Message)(MB.getHashMapMStoMsg().get(this).poll());
//        while (! (msg instanceof TestModelEvent)){
//            MB.getHashMapMStoMsg().get(this);
//        }
//    }
    public int getMaxBatchesIndex(){
        return maxBatchesIndex;
    }

}
