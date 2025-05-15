package org.example.schedulers;

import org.example.interfaces.Disposable;
import org.example.interfaces.Scheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SingleThreadScheduler implements Scheduler {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public Disposable schedule(Runnable task) {
        Future<?> future = executor.submit(task);
        return new IOScheduler.FutureDisposable(future);
    }
}