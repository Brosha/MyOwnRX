package org.example.interfaces;

public interface Scheduler {
    Disposable schedule(Runnable task);
}
