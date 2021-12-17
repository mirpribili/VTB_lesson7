package com.geekbrains.multithreads2;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
 */
class ExecutorServiceApp{
    public static void main(String[] args){
        ExecutorService service = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 10; i++){
            final int w = i + 1;
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
