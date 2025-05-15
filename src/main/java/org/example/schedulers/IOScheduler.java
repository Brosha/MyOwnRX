package org.example.schedulers;

import org.example.interfaces.Disposable;
import org.example.interfaces.Scheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class IOScheduler implements Scheduler {

    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public Disposable schedule(Runnable task) {
        Future<?> future = executor.submit(task);
        return new FutureDisposable(future);
    }

    static class FutureDisposable implements Disposable {
        private final Future<?> future;

        FutureDisposable(Future<?> future) {
            this.future = future;
        }

        @Override
        public void dispose() {
            future.cancel(true);
        }

        @Override
        public boolean isDisposed() {
            return future.isDone() || future.isCancelled();
        }
    }
}