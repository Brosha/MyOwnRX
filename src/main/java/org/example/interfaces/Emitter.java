package org.example.interfaces;

public interface Emitter<T> {
    void onNext(T value);
    void onError(Throwable error);
    void onComplete();
    void setDisposable(Disposable disposable);
}