package org.example.interfaces;

public interface ObservableOnSubscribe<T> {
    void subscribe(Emitter<T> emitter) throws Exception;
}
