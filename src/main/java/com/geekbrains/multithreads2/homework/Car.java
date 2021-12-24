package com.geekbrains.multithreads2.homework;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class Car implements Runnable{


    private static int CARS_COUNT;
    private static final AtomicInteger ai;

    static {
        CARS_COUNT = 0 ;
        ai = new AtomicInteger();
    }
    private Race race;
    private int speed;
    private String name;
    private CyclicBarrier barrier;

    public String getName () {
        return name;
    }
    public int getSpeed () {
        return speed;
    }
    public Car (Race race, int speed, CyclicBarrier barrier) {
        this .race = race;
        this .speed = speed;
        CARS_COUNT++;
        this .name = "Участник #" + CARS_COUNT;
        this.barrier = barrier;
    }
    @Override
    public void run() {
        try {
            System.out.println( this .name + " готовится" );
            Thread.sleep( 500 + ( int )(Math.random() * 800 ));
            System.out.println( this .name + " готов" );
            barrier.await(); // первый await сразу по готовности к старту - по идее все await'ы синхрониз. с мэином.
            barrier.await(); // затем сразу же происходит второй await и в мэине и в машине
            // Зачем?
                // Защищает от раннего старта машин тем что препятствует машине попасть в цикл гонки.... СМ №8 4:20
                // сообщение в MainClass System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Гонка началась!!!");
                    // не успеет напечататься
            for ( int i = 0 ; i < race.getStages().size(); i++) {
                race.getStages().get(i).go( this );
            }
            // находим победителя множество способов!
            // static Object monitor =...;
            // static boolean haveWinner = false;

            // syncronized(monitor) {
            //   if(!haveWinner){
            //      haveWinner = true;
            //      System.out.println(name + " - WIN");
            //   }
            // }
            // lock.tryLock();

            if(ai.incrementAndGet() == 1){
                System.out.println(name + " - WIN");
            }
            barrier.await(); // машина на финише, но ждет подсчета результатов гонки.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
