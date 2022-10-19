package bgu.spl.mics.application.services;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.CPU;
import bgu.spl.mics.application.objects.DataBatch;

/**
 * CPU service is responsible for handling the {@link //DataPreProcessEvent}.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class CPUService extends MicroService {
    private CPU cpu;
    private int startingTickProcess;//the tick in which the process of a batch started
    private int tickCounter;//the number of ticks we "saved"
    public CPUService(String name, CPU cpu) {
        super(name);
        this.cpu = cpu;
        this.tickCounter = 0;
        this.startingTickProcess = 0;
    }

    @Override
    protected void initialize() {
        MB.register(this);
        subscribeBroadcast(TerminateBroadcast.class, terminateBroadcast -> {
            terminate();
        });
        subscribeBroadcast(TickBroadcast.class, new Callback<TickBroadcast>(){
            @Override
            public void call(TickBroadcast tickBroadcast) throws InterruptedException {
               // System.out.println("CPU received a tick");
                if (! cpu.getBlockedDataBatches().isEmpty()){//double check that we sent the batches correctly, probably not entering the loop
                    for(DataBatch batch : cpu.getBlockedDataBatches()){
                        if(cpu.getCluster().returnBatchToGPU( batch )){
                            cpu.getCluster().incrementTotalProcessedBatch();
                            cpu.getBlockedDataBatches().remove(batch);
                        }
                    }
                }
                if (cpu.getCurBatch() == null){
                    if (! cpu.getCluster().getBatchesToTrain().isEmpty()) {
                        cpu.getNextBatch();
                    }
                }
                if (cpu.getCurBatch() != null){ //the CPU processing
                    tickCounter++;
                    cpu.getCluster().incrementCPUsed();
                    if (tickCounter >= cpu.getProcessTime()){//finish the batch process
                        tickCounter = tickCounter - cpu.getProcessTime();
                        if (cpu.getCluster().returnBatchToGPU( cpu.getCurBatch() )){ //can you return the batch to the gpu?
                            cpu.getCluster().incrementTotalProcessedBatch();
//                            System.out.println("cpu processed a batch");
                        }
                        else{
                            cpu.getBlockedDataBatches().add(cpu.getCurBatch());
                        }
                        cpu.setCurBatch(null);//ready for the next batch
                    }
                }
            }
        });
    }
}
