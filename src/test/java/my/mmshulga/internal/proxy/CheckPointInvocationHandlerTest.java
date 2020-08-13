package my.mmshulga.internal.proxy;

import my.mmshulga.internal.IWithPhaser;
import my.mmshulga.internal.annotation.CheckPoint;
import my.mmshulga.internal.annotation.CheckPointSyncedJob;
import my.mmshulga.processor.SyncedJobsProcessor;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class CheckPointInvocationHandlerTest {

    @Test
    public void simpleTest() throws Exception {

        ConcurrentMap<String, Instant> terminationOrder = new ConcurrentHashMap<>();

        Phaser phaser = new Phaser();
        phaser.register();
        phaser.register();

        IExample obj1 = new IExampleImpl(1, phaser, terminationOrder);
        IExample obj2 = new IExampleImpl(2, phaser, terminationOrder);

        SyncedJobsProcessor<Void> processor = new SyncedJobsProcessor<>();
        processor.addJob(() -> {
            obj1.run();
            return null;
        });

        processor.addJob(() -> {
            obj2.run();
            return null;
        });

        processor.launch();
        Thread.sleep(5000L);
        processor.shutdownNow();

        Instant i1m1 = terminationOrder.get("1.method1");
        Instant i1m2 = terminationOrder.get("1.method2");
        Instant i2m1 = terminationOrder.get("2.method1");
        Instant i2m2 = terminationOrder.get("2.method2");

        assertFalse(i1m2.isBefore(i2m1) || i2m2.isBefore(i1m1));
    }

    private interface IExample {
        void run();
    }

    @CheckPointSyncedJob
    private class IExampleImpl implements IExample, IWithPhaser {

        private final int id;
        private final Phaser phaser;
        private final ConcurrentMap<String, Instant> terminationOrder;

        public IExampleImpl(int id, Phaser phaser, ConcurrentMap<String, Instant> terminationOrder) {
            this.id = id;
            this.phaser = phaser;
            this.terminationOrder = terminationOrder;
        }

        @Override
        public Phaser __getPhaser() {
            return phaser;
        }

        @Override
        public void run() {
            method1();
            method2();
        }

        @CheckPoint(order = 0)
        private void method1() {
            System.out.println("Method1 from " + id + " is executing ...");
            try {
                Thread.sleep((long)(Math.random() * 1000));

                String key = String.format("%d.method1", id);
                terminationOrder.put(key, Instant.now());
            } catch (InterruptedException ignored) { }
            System.out.println("Method1 from " + id + " completed ...");
        }

        @CheckPoint(order = 1)
        private void method2() {
            System.out.println("Method2 from " + id + " is executing ...");
            try {
                Thread.sleep((long)(Math.random() * 1000));
                String key = String.format("%d.method2", id);
                terminationOrder.put(key, Instant.now());
            } catch (InterruptedException ignored) { }
            System.out.println("Method1 from " + id + " completed ...");
        }
    }
}
