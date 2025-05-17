package org.example;

import org.example.interfaces.Disposable;
import org.example.interfaces.Emitter;
import org.example.interfaces.Observer;

import java.util.concurrent.atomic.AtomicBoolean;

class DefaultEmitter<T> implements Emitter<T>, Disposable {
    private final Observer<T> observer;
    private Disposable disposable;
    private final AtomicBoolean disposed = new AtomicBoolean(false);

    public DefaultEmitter(Observer<T> observer) {
        this.observer = observer;
    }

    @Override
    public void onNext(T value) {
        if (!disposed.get()) {
            observer.onNext(value);
        }
    }

    @Override
    public void onError(Throwable error) {
        if (!disposed.get()) {
            observer.onError(error);
            dispose();
        }
    }

    @Override
    public void onComplete() {
        if (!disposed.get()) {
            observer.onComplete();
            dispose();
        }
    }

    @Override
    public void setDisposable(Disposable disposable) {
        this.disposable = disposable;
    }

    @Override
    public void dispose() {
        disposed.set(true);
    }

    @Override
    public boolean isDisposed() {
        return disposed.get();
    }
}
