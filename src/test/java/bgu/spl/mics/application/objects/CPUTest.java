package bgu.spl.mics.application.objects;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CPUTest {
    private Cluster cluster;
    private CPU cpu;

    @Before
    public void setUp() throws Exception {
        cpu = new CPU(4);
        cluster = Cluster.getInstance();
    }

    @After
    public void tearDown() throws Exception {
        cpu = null;
    }

    @Test
    public void getProcessTimeTest(){
        cpu.setCurBatch(new DataBatch(new Data(Data.Type.Images, 1000), 0));
        assertEquals(32, cpu.getProcessTime());
        cpu.setCurBatch(new DataBatch(new Data(Data.Type.Text, 1000), 0));
        assertEquals(16, cpu.getProcessTime());
        cpu.setCurBatch(new DataBatch(new Data(Data.Type.Tabular, 1000), 0));
        assertEquals(8, cpu.getProcessTime());
    }
    @Test
    public void getCurBatchTest() throws InterruptedException {
        assertNull(cpu.getCurBatch());
        cpu.setCurBatch(new DataBatch(new Data(Data.Type.Images, 1000), 0));
        assertNotNull(cpu.getCurBatch());
        cpu.getNextBatch();
        assertNull(cpu.getCurBatch());
    }
    @Test
    public void getNextBatchTest(){
        DataBatch batch = new DataBatch(new Data(Data.Type.Images, 2000),0);
        cluster.getBatchesToTrain().add(batch);
        assertNull(cpu.getCurBatch());
        cpu.getNextBatch();
        assertNotNull(cpu.getCurBatch());
        assertEquals(batch, cpu.getCurBatch());
    }
}