package bgu.spl.mics.application.objects;


import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.services.GPUService;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing the cluster.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */

public class Cluster {


	/**
     * Retrieves the single instance of this class.
     */
	//we need to create an object that represent a group of gpus and cpus
	private LinkedBlockingQueue<GPU> gpus;
	private LinkedBlockingQueue<CPU> cpus;
	private ConcurrentLinkedQueue<DataBatch> batchesToTrain;
	Object lockNumOfProcessed;
	Object lockNames;
	//statistics
	private LinkedList<String> names; //names of all models trained
	private AtomicInteger totalBatchesProcessed; //total number of data batches processed by the CPU's
	private AtomicInteger CPUsed;// number of CPU time unit used
	private AtomicInteger GPUsed;// number of CPU time unit used




	private static class ClusterHolder{// Singleton
		private static Cluster instance = new Cluster();
	}
	public Cluster(){
		this.gpus = new LinkedBlockingQueue<GPU>();
		this.cpus = new LinkedBlockingQueue<CPU>();
		this.batchesToTrain = new ConcurrentLinkedQueue<DataBatch>();
		this.names = new LinkedList<String>();
		this.totalBatchesProcessed = new AtomicInteger();
		lockNumOfProcessed = new Object();
		lockNames = new Object();
		this.CPUsed = new AtomicInteger();
		this.GPUsed = new AtomicInteger();
	}
	public void incrementCPUsed(){
		CPUsed.incrementAndGet();
	}
	public void incrementGPUsed(){
		GPUsed.incrementAndGet();
	}
	public int getCpuTimeUsed(){
		return CPUsed.intValue();
	}
	public int getGpuTimeUsed(){
		return GPUsed.intValue();
	}
	public synchronized void addTrainedModelName(String name){
		names.add(name);
	}

	public int getTotalBatchesProcessed() {
		return totalBatchesProcessed.intValue();
	}

	public static Cluster getInstance() {
		if(Cluster.ClusterHolder.instance == null){
			Cluster.ClusterHolder.instance = new Cluster();
		}
		return Cluster.ClusterHolder.instance;
	}
	public void incrementTotalProcessedBatch(){
		totalBatchesProcessed.incrementAndGet();
	}
	public synchronized void addBatchToCluster(DataBatch batch) {
		batchesToTrain.add(batch);
	}
	public synchronized boolean returnBatchToGPU(DataBatch batch){
		GPU gpu = batch.getData().getGpu();
		if (! gpu.isFull()){//not expected to be full
			gpu.addToMaxBatchesArray(batch);
			return true;
		}
		return false;
	}

	public ConcurrentLinkedQueue<DataBatch> getBatchesToTrain() {
		return batchesToTrain;
	}
}
