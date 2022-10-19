package bgu.spl.mics.application.objects;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.*;

public class GPUTest {
    private GPU gpu;
    private Model model;
    private Cluster cluster;

    @Before
    public void setUp() throws Exception {
        Student student = new Student("student name", "department", Student.Degree.PhD);
        Data data = new Data(Data.Type.Images, 20000);
        model = new Model("test", student);
        model.setData(data);
        gpu = new GPU(GPU.Type.GTX1080);
        cluster = Cluster.getInstance();
    }

    @After
    public void tearDown() throws Exception {
        Student student = null;
        Data data = null;
        model = null;
        gpu = null;
    }
    @Test
    public void sendBatchesToClusterTest(){
        int batchesSent = gpu.getBatchesSent();
        if (!gpu.isDone()){
            gpu.sendBatchToCluster();
            assertEquals(Model.Status.Training, gpu.getModel().getStatus());
            assertEquals(batchesSent + 1, gpu.getBatchesSent());
        }
    }

    @Test
    public void getTypeTest(){
        assertEquals(GPU.Type.GTX1080, gpu.getType());
    }
    @Test
    public void setModelTest(){
        gpu.setModel(model);
        assertEquals(model, gpu.getModel());
    }
    @Test
    public void addToMaxBatchesArrayTest(){
        assertEquals(0, gpu.getMaxBatchesIndex());
        DataBatch batch = new DataBatch(new Data(Data.Type.Images,1000),0);
        gpu.addToMaxBatchesArray(batch);
        assertEquals(1, gpu.getMaxBatchesIndex());
    }
    @Test
    public void processBatchTest(){
        DataBatch batch = new DataBatch(new Data(Data.Type.Images,1000),0);
        gpu.addToMaxBatchesArray(batch);
        assertEquals(1, gpu.getMaxBatchesIndex());
        assertEquals(0, gpu.getProcessedBatches());
        gpu.processBatch();
        assertEquals(0, gpu.getMaxBatchesIndex());
        assertEquals(1, gpu.getProcessedBatches());
    }
}