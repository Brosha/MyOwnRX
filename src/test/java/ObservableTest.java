import org.example.CompositeDisposable;
import org.example.Observable;
import org.example.interfaces.Disposable;
import org.example.interfaces.Emitter;
import org.example.interfaces.Observer;
import org.example.schedulers.IOScheduler;
import org.example.schedulers.SingleThreadScheduler;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class ObservableTest {
// ----------------------------
// 1. Базовые сценарии
// ----------------------------

    @Test
    public void testCreateAndSubscribe() {
        List<Integer> results = new ArrayList<>();
        Observable<Integer> obs = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onComplete();
        });

        obs.subscribe(new Observer<Integer>() {
            @Override
            public void onNext(Integer item) {
                results.add(item);
            }

            @Override
            public void onError(Throwable t) {
                fail("Unexpected error");
            }

            @Override
            public void onComplete() {
            }
        });

        assertEquals(Arrays.asList(1, 2), results);
    }

// ----------------------------
// 2. Тесты операторов
// ----------------------------

    @Test
    public void testMapOperator() {
        List<Integer> results = new ArrayList<>();
        Observable.create((Emitter<Integer> emitter) -> {
                    emitter.onNext(1);
                    emitter.onNext(2);
                    emitter.onComplete();
                })
                .map(x -> x * 2)
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onNext(Integer item) {
                        results.add(item);
                    }

                    @Override
                    public void onError(Throwable t) {
                        fail("Unexpected error");
                    }

                    @Override
                    public void onComplete() {
                    }
                });

        assertArrayEquals(new Integer[]{2, 4}, results.toArray());
    }

// ----------------------------
// 3. Тесты многопоточности
// ----------------------------

    @Test
    public void testObserveOn() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        String[] threadName = new String[1];

        Observable.create((Emitter<Integer> emitter) -> {
                    emitter.onNext(1);
                    emitter.onComplete();
                })
                .observeOn(new SingleThreadScheduler())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onNext(Integer item) {
                        threadName[0] = Thread.currentThread().getName();
                        latch.countDown();
                    }

                    @Override
                    public void onError(Throwable t) {
                        fail("Unexpected error");
                    }

                    @Override
                    public void onComplete() {
                    }
                });

        latch.await(2, TimeUnit.SECONDS);
        assertTrue(threadName[0].contains("single"));
    }

// ----------------------------
// 4. Тесты отмены подписок
// ----------------------------

    @Test
    public void testDisposable() throws InterruptedException {
        List<Integer> results = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        Observable<Integer> obs = Observable.create(emitter -> {
            emitter.onNext(1);
            latch.countDown();
            Thread.sleep(200);
            emitter.onNext(2);
        });

        Disposable disposable = obs.subscribe(new Observer<Integer>() {
            @Override
            public void onNext(Integer item) {
                results.add(item);
            }

            @Override
            public void onError(Throwable t) {
                fail("Unexpected error");
            }

            @Override
            public void onComplete() {}
        });

        latch.await(2, TimeUnit.SECONDS);
        disposable.dispose();
        Thread.sleep(300);

        assertEquals(1, results.size());
    }
}
