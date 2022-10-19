package bgu.spl.mics;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class FutureTest {

    private Future<String> future;
    @Before
    public void setUp() throws Exception {
        future = new Future<String>();
    }

    @After
    public void tearDown() throws Exception {
        future = null;
    }

    @Test
    public void TestGet() {

    }
    @Test
    public void testIsDone() {
        assertFalse(future.isDone());
        future.resolve("result");
        assertTrue(future.isDone());
    }
    @Test
    public void testResolve () {
        //TODO: implement this.
        future.resolve("result");
        assertTrue(future.isDone());
        assertEquals("result", future.get());
    }

    @Test
        //getter with timeout
    public void testGet() throws InterruptedException {
        assertNull(future.get(1000, TimeUnit.MILLISECONDS));
        future.resolve("result");
        assertEquals("result", future.get(1000, TimeUnit.MILLISECONDS));
    }
}