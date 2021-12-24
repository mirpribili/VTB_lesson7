package com.geekbrains.multithreads2.homework;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

// Задание организовать гонки.
// Правила:
    // Старт участников одновременный, при разном времени подготовки к гонке участников.
    // В туннель не может заехать одновременно больше половины участников.
public class MainClass {
    public  static final int CARS_COUNT = 4;
    public static void main(String[] args) {
        System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Подготовка!!!");
        CyclicBarrier barrier = new CyclicBarrier(CARS_COUNT + 1); // синхронизируем треды машин и основной(+1)
        Race race = new Race (new Road(60), new Tunnel(), new Road(40));
        Car[] cars = new Car[CARS_COUNT];
        for (int i = 0; i < cars.length; i++){
            cars[i] = new Car(race, 20 + (int) (Math.random() * 10), barrier);
            new Thread(cars[i]).start();
        }
        try{
            barrier.await(); // тут машины подготавливаются
            System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Гонка началась!!!");
            barrier.await(); // зачем еще один?
            barrier.await(); // этот когда все машины доедут до конца
            System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Гонка закончилась!!!");
        }catch (Exception e){
            e.printStackTrace();
        }

//   ошибки
    // не атомарная можно не успеть поймать условие в if
        /*
        countDownLatch.countDown();
        if(countDownLatch.getCount() == CARS_COUNT - 1){
            System.out.println(name + " WIN");
        }*/
    // тоже не атомарное решение
        //if(stage.getFinish() != null) stage.getFinish().makeFinish(this);
            // два потока могут успеть сделать гет гет и сет сет
    // ошибка с захватом tryLock
        //if(start.tryLock())
        // start.lock(); // забыли разблокировать? // рекомендуют закомментировать эту строку
    }
}

