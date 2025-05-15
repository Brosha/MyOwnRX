package org.example;

import org.example.interfaces.Disposable;
import org.example.interfaces.Emitter;
import org.example.interfaces.Observer;

class DefaultEmitter<T> implements Emitter<T>, Disposable {
    private final Observer<T> observer;
    private Disposable disposable;
    private volatile boolean disposed = false;

    public DefaultEmitter(Observer<T> observer) {
        this.observer = observer;
    }

    @Override
    public void onNext(T value) {
        if (!isDisposed()) { // Используем метод isDisposed()
            observer.onNext(value);
        }
    }

    @Override
    public void onError(Throwable error) {
        if (!disposed) {
            observer.onError(error);
            dispose();
        }
    }

    @Override
    public void onComplete() {
        if (!disposed) {
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
        disposed = true;
        if (disposable != null) {
            disposable.dispose();
        }
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }
}
