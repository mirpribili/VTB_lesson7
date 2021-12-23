package com.geekbrains.multithreads2;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// попытка прокинуть переменную i  в тред = BAD
public class TheoryExampleClass {

    public static void example1(String[] args) {
        for (int i = 0; i <10; i++){
            new Thread((new Runnable() {
                @Override
                public void run() {
                    System.out.println(/*i*/);
                }
            }));
        }
    }
// Внимание вопрос почему вывод будет? как будто нет синхронизации
    /*
    1-start
    2-start
    3-start
    1-end
    2-end
    3-end
     */
    private static Integer monitor = 1;
    // из-за того что обертка делает final int, а при (++) создается копия, как со строками!
    public static void main(String[] args) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (monitor){
                    System.out.println("1-start");
                    monitor++;
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("1-end");
                }
            }
        }).start();
        new Thread(() -> {
            synchronized (monitor){
                System.out.println("2-start");
                monitor++;
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("2-end");
            }
        }).start();
        new Thread(() -> {
            synchronized (monitor){
                System.out.println("3-start");
                monitor++;
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("3-end");
            }
        }).start();
    }
}
// менеджер, пул потоков
/*
 * SingleThreadPool  - для множества краткосрочных задач, для редких задач
 * Fixed..           - пул с ограничением (100) например
 * Cached..          - пул без потолка может попытаться и миллион тредов создать и упадет +через 60 сек безделия выключает поток.
 * shutdown()        - без этого приложение будет работать и дальше. А так service  большеее не принимает задачи и переходит постепенно в terminate
 * shutdownNow()     - для всех потоков посылает Interrupt а если вы не обработали это исключение то потоки продолжат работу
 * awaitTermination()- что то на подобие join() = я подожду(можно время указать) и потоммм иду дальше
 * isShutdown()      - проверка состояния
 * isTerminated()    - проверка состояния
 * execute()         - заставляет нашу стандартную Runnable задачу отдать пулу на исполнение
 * submit()          - можно получить ответ из задачи
 * * stringFuture
 * * get()           - получаю данные из треда
 * * get(longtimeout)- жду объект с ответом
 * * isCansel isDone - проверка состояния
 */
class ExecutorServiceApp{
    public static void main(String[] args){
        ExecutorService service = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 10; i++){
            final int w = i + 1;
            // w -- !!!
            service.execute(()->{
                System.out.println(w + " - Begin");
                try{
                    Thread.sleep(1000 + (int) (2000 * Math.random()));
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                System.out.println(w + " - End");
            });
        }
        service.shutdown();
    }
}
//  пытаемся дать задачу по которой хотим получить ответ
class ExecutorServiceApp2{
    public static void main(String[] args) {
        ExecutorService service = Executors.newFixedThreadPool(4);
        //Если требуется использовать кэширующий пул потоков, который создает потоки по мере необходимости,
        // но переиспользует неактивные потоки (и подчищает потоки, которые были неактивные некоторое время),
        // то это задается следующим образом:
        // Executors.newCachedThreadPool()

        // submit return объект типа Future(информация о выполнениии задачи)
        Future<String> stringFuture = service.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Thread.sleep(2000); // this is analog join() !
                //int x = 10 / 0;
                return "Java";
            }
        });

        // Future это информация о выполняемой нами задачи
        // service.submit(Runnable task)  = вернет только выполнена или нет задача
        // invokeAll(Collection<? extends Callable<T>> task) = позволяет не пользуясь циклом отдать в сервис целую коллекцию задач
        // invokeAny() = берет коллекцию задач выбирает какуюто решает ее ивозвращает результат хммм... только 1 ответ будет. 
        try {
            String result = stringFuture.get();
            System.out.println(result);
            // Why is two exceptions?
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        service.shutdown();
    }
}
// Как перехватывать "снаружи" те в главном потоке исключения вторстеп потоков?
class ThreadExceptions{
    public static void main(String[] args) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                int x = 10;
                System.out.println(1);
                x = x / 0;
                System.out.println(2);
            }
        });
        // можно повесить на все треды разом Thread.setDefaultUncaughtExceptionHandler(...)
        // for concrete thread  exceptions
        t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
                System.out.println("Catched");
            }
        });
        t.start();
    }
}
// покажем фабрике как из  задачи получить новый тред
class ThreadFactoryExample{
    public static void main(String[] args) throws Exception{
        ExecutorService service = Executors.newFixedThreadPool(4, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setPriority(10);
                t.setDaemon(true);
                t.setName("ABC");
                System.out.println("created");
                // теперь когда сервис будет запрашивать у фабрике новый поток мы будем видеть это в консоле
                return t;
            }
        });
        service.execute(() -> System.out.println(1));
        Thread.sleep(2000);
        service.execute(() -> System.out.println(2));
        Thread.sleep(2000);
        service.execute(() -> System.out.println(3));
        Thread.sleep(2000);
        service.execute(() -> System.out.println(4));
        Thread.sleep(2000);
        service.execute(() -> System.out.println(5));
        Thread.sleep(2000);
        service.execute(() -> System.out.println(6));
        service.shutdown();
        // как итог только 4 поток созданы а еще 2 обрабатываются повторно имеющимися из созданных 4х
        // еще и наглядно в конось выводится этап создания потока

// Scheduled для задач по расписанию - раз в час или любой другой период
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        //scheduledExecutorService.schedule(); // тут указать можно выполнить не сейчас а через время

        // нужны для задач с переодичностью
        //scheduledExecutorService.scheduleAtFixedRate();
        // но разница в том
        //           5 сек       5 сек       5 сек          - длительность работы над заданиями
        //       .===--------.===--------.===--------.
        //        0          20          40          60 сек - период между запусками задач 20 сек
        // и второй
        //          5сек
        //       .===----------.===----------.===---------.
        //        0 5         25 30         50 55         75 сек - период между запусками задач 20+5 сек
        //scheduledExecutorService.scheduleWithFixedDelay();
    }
}

//  замки
//  сначала аналог, что и с монитором
class SimpleLockApp{
    public static void main(String[] args) {
        final Lock lock = new ReentrantLock();

        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("BEFORE-LOCK-1");
                lock.lock(); /// второй тред подойдя к этой операции зависнит и будет ждать пока не освободится
                // этот лок
                System.out.println("GET-LOCK-1");
                try{
                    Thread.sleep(8000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }finally {
                    System.out.println("END-1");
                    lock.unlock(); /// !
                }
            }
        }).start();

        new Thread(() -> {
            System.out.println("BEFORE-LOCK-2");
            lock.lock(); /// второй тред подойдя к этой операции зависнит и будет ждать пока не освободится
            // этот лок
            System.out.println("GET-LOCK-2");
            try{
                Thread.sleep(8000);
            }catch (InterruptedException e){
                e.printStackTrace();
            }finally {
                System.out.println("END-2");
                lock.unlock(); /// !
            }
        }).start();
// в чем идея lock.tryLock? это не захват замка, а попытка с тайм-аутом его захватить!
// правило для try lock - если сделали то делайте try catch + finally
        // try catch так как используя Thread.sleep() надо обработать InterruptedException
        //  и обязательно final с unlock тк не важно поймали или нет исключение - надо освободить замок
        // и еще lock.lockInterruptibly();
        new Thread(() -> {
            System.out.println("BEGIN-3");
            try {
                if(lock.tryLock(4,TimeUnit.SECONDS)){
                    try {
                        System.out.println("LOCK-SECTION-3");
                        try{
                            Thread.sleep(13000);
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }finally {
                            lock.unlock();
                        }
                    }finally {
                        lock.unlock();
                        System.out.println("END-3");
                    }
                }else{
                    System.out.println("не очень то и нужно было...");
                }
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }).start();
    }
}
class RRWLocksApp{
// ReentrantReadWriteLock = может давать треду доступ на чтение или на запись
    // правило такое - пока кто-то читает ресурс под этим замком то его не получится записать
    // запись тоже по очереди
    // ReentrantReadWriteLock проводит огромную работу по упорядочиванию запросов на чтение и запись
    public static void main(String[] args) {
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        new Thread(() -> {
            rwl.readLock().lock(); /// !
            try{
                System.out.println("READING-start-a");
                Thread.sleep(3000);
                System.out.println("READING-end-a");
            }catch (InterruptedException e){
                e.printStackTrace();
            }finally {
                rwl.readLock().unlock();/// !
            }
        }).start();

        new Thread(() -> {
            rwl.writeLock().lock();/// !
            try{
                System.out.println("WRITING-start-b");
                Thread.sleep(5000);
                System.out.println("WRITING-end-b");
            }catch (InterruptedException e){
                e.printStackTrace();
            }finally {
                rwl.writeLock().unlock();/// !
            }
        }).start();

        new Thread(() -> {
            rwl.readLock().lock();/// !
            try{
                System.out.println("READING-start-c");
                Thread.sleep(5000);
                System.out.println("READING-end-c");
            }catch (InterruptedException e){
                e.printStackTrace();
            }finally {
                rwl.readLock().unlock();/// !
            }
        }).start();

        new Thread(() -> {
            rwl.readLock().lock();/// !
            try{
                System.out.println("READING-start-d");
                Thread.sleep(3000);
                System.out.println("READING-end-d");
            }catch (InterruptedException e){
                e.printStackTrace();
            }finally {
                rwl.readLock().unlock();/// !
            }
        }).start();
    }
}
// теперь коллекции и на методы add \ put in //HashMap//LinkedList//ArrayList
    // то там нигде нет слова synchronized
    // поэтому для многопоточки будут проблемы
// Как сделать так, чтобы несколько тредов могли работать с одной и той же коллекцией?
    // Если ArrayList то для доступов(модификации) для двух тредов лучше не использовать
        // тк для любого метода модификации придется ставить synchronized.
            //ВЫХОД: если не хочется то лучше применять Vector. тк внутри его методы уже синхронизированы некоторые
    // Если HashMap(не синхр.) -> то старенький вариант синхронизированный Hashtable. Проблема в том что блокируется по объекту
        // те lock идет на всю таблицу разом = падает производительность тк даже чтение последовательное.
            // ВЫХОД: ConcurrentHashMap = на чтение нет блокировки. На запись блокируется только 1 ячейка!
    // Если хочется стандартный лист в синхронизацию засунуть то: Collections.synchronizedMap() \ synchronizedList()
        // эти методы принимают мэп или лист и возвращ синхр. мэп или лист НО они проигрывают по производ-ти. ConcurrentHashMap
// Пакет Concurrent включает и для спец задач
        // например если нужно работать с листом на чтение в том виде в котором он был на момент обращения
            // ТО: CopyOnWriteArrayList но память ...
        // ArrayBlockingQueue ...
            //  имеет 4 метода на помещение в коллекцию
                //  put() - переведет поток в реж ожид если не будет места в массиве для помещения объекта.
                //  offer()A - вернет true или false когда попытается поместить объект в массив
                //  offer()B - вторая прегрузка связана с тайм аутом
                //  add() - бросит исключение если не сможет поместить объект в коллекцию
            // имеет на получение объекта из коллекции
                // take() - достает и удаляет его и освобождает коллекцию. + ждет если очередь пустая
                // poll()A - возвращает элемент но если очередь пустая то null вернет
                // poll()B - вторая прегрузка связана с тайм аутом - я жду минуту и если нет то null
                // peek() - вернет объект но оставит его в очереди

// ситуация - есть N  потоков разбросанных по программе и НЕТ возможности получить ССЫЛКИ
    // на ВСЕ треды и мэин поток ХОЧЕТ ждать пока все треды закончат работу.
    // Как быть? столько join мы не напишем
        //  может помочь защелка: CountDownLatch и указываем сколько раз она должна щелкнуть
// CountDown нужен когда задача выбранному треду подождать завершения других каких-то не знакомых тредов
class CountDownLatchApp {
    public static void main(String[] args) {
        final int THREADS_COUNT = 6; // or 1000
        final CountDownLatch countDownLatch = new CountDownLatch(THREADS_COUNT);
        System.out.println("Начинаем");
        for (int i = 0; i < THREADS_COUNT; i++){
            final int w = i;
            new Thread(()->{
                try {
                    Thread.sleep(200 * w + (int)(500 * Math.random()));
                    countDownLatch.countDown(); // своего рода продвинутый вариант join
                    System.out.println("Поток №" + w + " - готов");
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }).start();
        }
        try {
            countDownLatch.await(); // ждем всех 6 щелчков + можно ждать ограниченное время по тайм оут
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        System.out.println("Работа завершена");

    }
}
// семафор
// ситуация: мы считаем что обрабатывая 2 параллельно мы выигрываем по скорости и 4 тоже выигрываем,
    // но 500 в параллель уже будет невыгодно по скорости.
    // Synhronize нам не подошел бы потому что он по 1му будет пускать а ннам нужно например по 4
class SimpleSemaphoreApp{
    public static void main(String[] args) {
        final Semaphore smp = new Semaphore(4); // 4 щелчка
        Runnable limitCall = new Runnable() {
            int count = 0;
            @Override
            public void run() {
                int time = 3 + (int)(Math.random() * 4);
                int num = count++;
                try {
                    smp.acquire(); // тред заполучает семафор и тут уменьшается на 1 количество доступов
                    System.out.println("Поток №" + num + " начинает выполнять задачу");
                    Thread.sleep(time * 1000);
                    System.out.println("Поток №" + num + " завершил задачу");
                }catch (InterruptedException intEx){
                    intEx.printStackTrace();
                }finally {
                    smp.release();
                }
            }
        };
        for (int i = 0; i < 10; i++){
            new Thread(limitCall).start();
        }
    }
}
// CyclicBarrier - идея как в скачках 
    // сначала лошади подходят к барьеру и готовятся
    // а потом когда они готовы барьер поднимается и лошади одновременно скачут    
public class CyclicBarrierApp {
    public static void main(String[] args) {
        final int THREAD_COUNT = 5;
        CyclicBarrier cyclicBarrierApp = new CyclicBarrier(THREAD_COUNT);
        for(int i = 0; i < THREAD_COUNT; i++){
            int w = i;
                new Thread(new Runnable(){
                @Override
                public void run(){
                    System.out.println("Подготавливается " + w);
                    try{
                        Thread.sleep(2000 + 500 * (int)(Match.random() * 10));
                        System.out.println("Готов " + w);
                        cyclicBarrier.await(); // как только счетчик сдесь станет 0 то потоки сначнут работу
                        System.out.println("Поехал " + w);
                        // есть опасность кругового сброса счетчика именно у БАРЬЕРА!
                        // cyclicBarrier.await();
                    }catch(InterruptedException | BrokenBarrierException e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }//for
// атомарные переменные
        // гарантируют что никакой тред не сможет вклиниться между
        // увеличением(уменьш. и тд) счетчика и между запросом результата.
        AtomicInteger ai = new AtomicInteger (10);
        ai.addAndGet();
        getAndAdd();
        decrementAndGet();
    }
}
