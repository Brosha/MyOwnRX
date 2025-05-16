# MyOwnRX: Упрощенная реализация реактивных потоков на Java

Реализация основных концепций реактивного программирования, включая Observable, операторы (map, filter, flatMap), управление потоками (Schedulers) и отмену подписок.

---

## Дерево проекта
```
src/
├── main/
│ └── java/
│ └── org/
│ └── example/
│ ├── Observable.java # Ядро реактивного потока
│ ├── Observer.java # Интерфейс подписчика
│ ├── Emitter.java # Генератор событий
│ ├── DefaultEmitter.java # Реализация Emitter
│ ├── Disposable.java # Управление подписками
│ ├── CompositeDisposable.java # Групповая отмена подписок
│ ├── Scheduler.java # Интерфейс планировщика
│ ├── IOScheduler.java # Пул потоков для I/O
│ ├── ComputationScheduler.java # Пул для CPU-задач
│ └── SingleThreadScheduler.java # Один поток
└── test/
└── java/
└── org/
└── example/
└── ObservableTest.java # Юнит-тесты
```


---

## Архитектура системы

### Основные компоненты:
1. **Observable**  
   Источник данных. Создается через `create()`, поддерживает цепочки операторов (`map`, `filter`, `flatMap`).
2. **Observer**  
   Получатель событий с методами `onNext()`, `onError()`, `onComplete()`.
3. **Emitter**  
   Генерирует события для Observer. Контролирует жизненный цикл через `Disposable`.
4. **Disposable**  
   Позволяет отменить подписку. `CompositeDisposable` управляет группой подписок.
5. **Schedulers**  
   Управляют потоками выполнения. Поддерживают `subscribeOn()` и `observeOn()`.

---

## Принципы работы Schedulers

### Доступные планировщики:
| Тип                | Описание                                      | Использование                     |
|---------------------|-----------------------------------------------|-----------------------------------|
| `IOScheduler`       | CachedThreadPool для асинхронных I/O-задач.  | Сеть, файловые операции.          |
| `ComputationScheduler` | FixedThreadPool (размер = ядра CPU).       | Вычисления, параллельная обработка. |
| `SingleThreadScheduler` | Один поток для последовательных задач.  | UI-обновления, синхронизация.     |

### Пример:
```java
Observable.create(emitter -> {
    // Выполнится в IO-потоке
    emitter.onNext(1);
})
.subscribeOn(new IOScheduler())
.observeOn(new SingleThreadScheduler())
.subscribe(item -> {
    // Обработка в SingleThread
});
```
---

## Процесс тестирования

### Технологии:
- **JUnit 4** для юнит-тестов.
- `CountDownLatch` для синхронизации потоков.

### Основные сценарии:
1. **Базовые операции**  
   Проверка создания Observable, передачи элементов, завершения.
2. **Операторы**  
   Тестирование преобразований (`map`), фильтрации (`filter`), асинхронности (`flatMap`).
3. **Многопоточность**  
   Проверка работы `subscribeOn()` и `observeOn()`.
4. **Отмена подписок**  
   Тесты для `Disposable` и `CompositeDisposable`.

Пример теста:
```java
@Test
public void testMapOperator() {
    Observable.create(emitter -> {
        emitter.onNext(1);
    })
    .map(x -> x * 2)
    .subscribe(result -> {
        assertEquals(2, result);
    });
}
```

---

## Примеры использования

### 1. Создание Observable
```java
Observable<Integer> observable = Observable.create(emitter -> {
    emitter.onNext(1);
    emitter.onNext(2);
    emitter.onComplete();
});
```

### 2. Цепочка операторов
```java
observable
    .map(x -> x * 2)
    .filter(x -> x > 2)
    .subscribe(
        item -> System.out.println("Received: " + item),
        error -> error.printStackTrace()
    );
// Вывод: Received: 4
```

### 3. Многопоточная обработка
```java
observable
    .subscribeOn(new IOScheduler())
    .observeOn(new ComputationScheduler())
    .flatMap(x -> Observable.create(e -> {
        e.onNext(x * 10);
    }))
    .subscribe(item -> {
        System.out.println(Thread.currentThread().getName() + ": " + item);
    });
```


