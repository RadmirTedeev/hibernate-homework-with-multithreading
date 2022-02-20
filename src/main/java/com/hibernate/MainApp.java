package com.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import javax.persistence.OptimisticLockException;
import java.util.concurrent.CountDownLatch;

public class MainApp {
    private static final int NUMBER_OF_THREADS = 8;

    public static void main(String[] args) {
        long time = System.currentTimeMillis();
        increase();
        System.out.println("Time is: " + (System.currentTimeMillis() - time));
        countSumOfVal();
    }

    public static void increase() {
        CountDownLatch countDownLatch = new CountDownLatch(NUMBER_OF_THREADS);
        Thread[] threads = new Thread[NUMBER_OF_THREADS];

        try (SessionFactory factory = new Configuration()
                .configure("hibernate.cfg.xml")
                .addAnnotatedClass(Item.class)
                .buildSessionFactory()) {
            for (int i = 0; i < NUMBER_OF_THREADS; i++) {
                final int u = i;
                threads[i] = new Thread(() -> {
                    System.out.println("Thread " + u + " started");

                    for (int j = 0; j < 1000; j++) {
                        boolean updated = false;
                        Long randomRow = (long) (Math.random() * 40) + 1;

                        while (!updated) {
                            Session session = factory.getCurrentSession();
                            session.beginTransaction();
                            Item item = session.get(Item.class, randomRow);
                            int temp = item.getVal();
                            item.setVal(++temp);
                            threadSleep();

                            try {
                                session.save(item);
                                session.getTransaction().commit();
                                updated = true;
                            } catch (OptimisticLockException e) {
                                session.getTransaction().rollback();
                            }

                            if (session != null) {
                                session.close();
                            }
                        }
                    }
                    countDownLatch.countDown();
                });
                threads[i].start();
            }

            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("END");
        }
    }

    public static void threadSleep() {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void countSumOfVal() {
        try (SessionFactory factory = new Configuration()
                .configure("hibernate.cfg.xml")
                .addAnnotatedClass(Item.class)
                .buildSessionFactory()) {
            Session session = factory.getCurrentSession();
            session.beginTransaction();
            Object sum = session.createNativeQuery("select sum (val) from items;").getSingleResult();
            System.out.println(sum);

            if (session != null) {
                session.close();
            }
        }
    }
}