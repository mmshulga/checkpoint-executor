package my.mmshulga.test;

import my.mmshulga.internal.IWithPhaser;
import my.mmshulga.internal.annotation.CheckPoint;
import my.mmshulga.internal.annotation.CheckPointSyncedJob;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CheckPointTest {

    @Test
    public void simpleTest() throws Exception {

        int TEST_SIZE = 20;

        ConcurrentLinkedQueue<Integer> terminationOrder = new ConcurrentLinkedQueue<>();

        List<Callable<Void>> callables = new ArrayList<>(TEST_SIZE);
        Phaser phaser = new Phaser();
        for (int i = 0; i < TEST_SIZE; i++) {
            callables.add(new IExampleImpl(phaser, terminationOrder));
            phaser.register();
        }

        ExecutorService es = Executors.newFixedThreadPool(TEST_SIZE);
        es.invokeAll(callables);

        for (int i = 0; i < TEST_SIZE; i++) {
            int methodNumber = terminationOrder.poll();
            assertEquals(1, methodNumber);
        }

        for (int i = 0; i < TEST_SIZE; i++) {
            int methodNumber = terminationOrder.poll();
            assertEquals(2, methodNumber);
        }

        for (int i = 0; i < TEST_SIZE; i++) {
            int methodNumber = terminationOrder.poll();
            assertEquals(3, methodNumber);
        }
    }

    @CheckPointSyncedJob
    private class IExampleImpl implements Callable<Void>, IWithPhaser {

        private final Phaser phaser;
        private final ConcurrentLinkedQueue<Integer> terminationOrder;

        public IExampleImpl(Phaser phaser, ConcurrentLinkedQueue<Integer> terminationOrder) {
            this.phaser = phaser;
            this.terminationOrder = terminationOrder;
        }

        @Override
        public Void call() {
            method1();
            method2();
            method3();
            return null;
        }

        @CheckPoint(order = 0)
        private void method1() {
            try {
                Thread.sleep((long)(Math.random() * 1000));
                terminationOrder.add(1);
            } catch (InterruptedException ignored) { }
        }

        @CheckPoint(order = 1)
        private void method2() {
            try {
                Thread.sleep((long)(Math.random() * 1000));
                terminationOrder.add(2);
            } catch (InterruptedException ignored) { }
        }

        @CheckPoint(order = 2)
        private void method3() {
            try {
                Thread.sleep((long)(Math.random() * 1000));
                terminationOrder.add(3);
            } catch (InterruptedException ignored) { }
        }

        @Override
        public Phaser __getPhaser() {
            return this.phaser;
        }
    }
}
