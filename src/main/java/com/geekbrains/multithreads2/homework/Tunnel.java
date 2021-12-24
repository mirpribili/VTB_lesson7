package com.geekbrains.multithreads2.homework;

import java.util.concurrent.Semaphore;

public class Tunnel extends Stage {
    private final Semaphore smp;

    public Tunnel() {
        this.smp = new Semaphore(MainClass.CARS_COUNT / 2); // !!! ограниечение тунеля на кол-во машин
        this.length = 80;
        this.description = "Тоннель " + length + " метров";
    }

    @Override
    public void go(Car c) {
        try {
            if (!smp.tryAcquire()) {
                System.out.println(c.getName() + " готовится к этапу(ждет): " + description);
                smp.acquire();
            }
            // на этом этапе благодаря tryAcquire если машина подъедет к тунелю, а он свободен она не будет
                // готовиться к этапу(ждет) - а сразу поедет
            System.out.println(c.getName() + " начал этап: " + description);
            Thread.sleep(length / c.getSpeed() * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println(c.getName() + " закончил этап: " + description);
            smp.release();
        }
    }
}
