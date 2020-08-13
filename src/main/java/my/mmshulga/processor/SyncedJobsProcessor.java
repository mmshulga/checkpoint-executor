package my.mmshulga.processor;

import my.mmshulga.internal.IWithPhaser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class SyncedJobsProcessor<T> {

    private final List<Callable<T>> jobs;
    private final Phaser phaser;
    private volatile ExecutorService es;

    public SyncedJobsProcessor(List<Callable<T>> jobs) {
        this();
        for (Callable<T> job : jobs) {
            addJob(job);
            phaser.register();
        }
    }

    public SyncedJobsProcessor() {
        this.phaser = new Phaser();
        this.jobs = new ArrayList<>();
    }

    public void addJob(Callable<T> job) {
        jobs.add(new CallableWithPhaserContainer(job, phaser));
    }

    public List<Future<T>> launch() {
        es = Executors.newFixedThreadPool(jobs.size());
        try {
            return es.invokeAll(jobs);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            es.shutdown();
        }
    }

    public void shutdownNow() {
        if (es != null) {
            es.shutdownNow();
        }
    }

    private class CallableWithPhaserContainer implements Callable<T>, IWithPhaser {

        private final Callable<T> wrapped;
        private final Phaser phaser;

        CallableWithPhaserContainer(Callable<T> toWrap, Phaser phaser) {
            this.wrapped = toWrap;
            this.phaser = phaser;
        }

        @Override
        public T call() throws Exception {
            return wrapped.call();
        }

        @Override
        public Phaser __getPhaser() {
            return this.phaser;
        }
    }
}
