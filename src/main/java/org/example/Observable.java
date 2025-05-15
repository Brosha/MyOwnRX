package org.example;

import org.example.interfaces.Disposable;
import org.example.interfaces.ObservableOnSubscribe;
import org.example.interfaces.Observer;
import org.example.interfaces.Scheduler;

import java.util.function.Function;
import java.util.function.Predicate;

public class Observable<T> {
    private final ObservableOnSubscribe<T> source;

    private Observable(ObservableOnSubscribe<T> source) {
        this.source = source;
    }


    public static <T> Observable<T> create(ObservableOnSubscribe<T> source) {
        return new Observable<>(source);
    }


    public Disposable subscribe(Observer<T> observer) {
        DefaultEmitter<T> emitter = new DefaultEmitter<T>(observer);
        try {
            source.subscribe(emitter);
        } catch (Exception e) {
            emitter.onError(e);
        }
        return emitter;
    }


    public <R> Observable<R> map(Function<? super T, ? extends R> mapper) {
        return new Observable<>(emitter ->
                this.subscribe(new Observer<T>() {
                    @Override
                    public void onNext(T item) {
                        try {
                            emitter.onNext(mapper.apply(item));
                        } catch (Throwable t) {
                            emitter.onError(t);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        emitter.onError(t);
                    }

                    @Override
                    public void onComplete() {
                        emitter.onComplete();
                    }
                })
        );
    }

    public Observable<T> filter(Predicate<? super T> predicate) {
        return new Observable<>(emitter ->
                this.subscribe(new Observer<T>() {
                    @Override
                    public void onNext(T item) {
                        try {
                            if (predicate.test(item)) {
                                emitter.onNext(item);
                            }
                        } catch (Throwable t) {
                            emitter.onError(t);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        emitter.onError(t);
                    }

                    @Override
                    public void onComplete() {
                        emitter.onComplete();
                    }


                })
        );
    }

    public <R> Observable<R> flatMap(Function<T, Observable<R>> mapper) {
        return new Observable<>(emitter -> {
            CompositeDisposable composite = new CompositeDisposable();
            emitter.setDisposable(composite);

            Disposable upstreamDisposable = this.subscribe(
                    new Observer<T>() {
                        @Override
                        public void onNext(T item) {
                            Observable<R> inner = mapper.apply(item);

                            Disposable innerDisposable = inner.subscribe(
                                    new Observer<R>() {
                                        @Override
                                        public void onNext(R value) {
                                            emitter.onNext(value);
                                        }

                                        @Override
                                        public void onError(Throwable t) {
                                            emitter.onError(t);
                                        }

                                        @Override
                                        public void onComplete() {
                                        }
                                    }
                            );
                            composite.add(innerDisposable);
                        }

                        @Override
                        public void onError(Throwable t) {
                            emitter.onError(t);
                        }

                        @Override
                        public void onComplete() {
                            emitter.onComplete();
                        }
                    }
            );
            composite.add(upstreamDisposable);
        });
    }
    public Observable<T> subscribeOn(Scheduler scheduler) {
        return new Observable<>(emitter ->
                scheduler.schedule(() ->
                        this.subscribe(new Observer<T>() {
                            @Override
                            public void onNext(T item) {
                                emitter.onNext(item);
                            }

                            @Override
                            public void onError(Throwable t) {
                                emitter.onError(t);
                            }

                            @Override
                            public void onComplete() {
                                emitter.onComplete();
                            }
                        })
                )
        );
    }

    public Observable<T> observeOn(Scheduler scheduler) {
        return new Observable<>(emitter ->
                this.subscribe(new Observer<T>() {
                    @Override
                    public void onNext(T item) {
                        scheduler.schedule(() -> emitter.onNext(item));
                    }

                    @Override
                    public void onError(Throwable t) {
                        scheduler.schedule(() -> emitter.onError(t));
                    }

                    @Override
                    public void onComplete() {
                        scheduler.schedule(() -> emitter.onComplete());
                    }
                })
    );
    }

}
