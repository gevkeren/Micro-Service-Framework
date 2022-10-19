package bgu.spl.mics.application.objects;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class CPU {
    private int numOfCores;
    private DataBatch curBatch;
    private Cluster cluster = Cluster.getInstance();
    private LinkedBlockingQueue<DataBatch> blockedDataBatches;

    public CPU(int numOfCores) {
        this.numOfCores = numOfCores;
        this.cluster = Cluster.getInstance();
        this.curBatch = null;
        blockedDataBatches = new LinkedBlockingQueue<>();
    }

    public DataBatch getCurBatch(){
        return curBatch;
    }
    public int getProcessTime(){
        if(curBatch.getData().getType() == Data.Type.Images)
            return (32/numOfCores)*4;
        else if(curBatch.getData().getType() == Data.Type.Text)
            return (32/numOfCores)*2;
        else //curBatch.getData().getType() == Data.Type.Tabular
            return (32/numOfCores);
    }
    public void setCurBatch(DataBatch batch){
        curBatch = batch;
    }
    public Cluster getCluster() {
        return cluster;
    }

    public LinkedBlockingQueue<DataBatch> getBlockedDataBatches() {
        return blockedDataBatches;
    }
    public void getNextBatch(){
        curBatch = cluster.getBatchesToTrain().poll();
    }
}
