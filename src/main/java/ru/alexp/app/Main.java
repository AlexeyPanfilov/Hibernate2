package ru.alexp.app;

import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import javax.persistence.OptimisticLockException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class Main {

    private static final int THREADS_COUNT = 8;

    public static void main(String[] args) {
//        prepareData();
//        calculateDataInDb();
        sumOfVal();
    }

    public static void sumOfVal() {
        SessionFactory factory = new Configuration()
                .configure("hibernate.cfg.xml")
                .buildSessionFactory();
        Session session = factory.getCurrentSession();
        session.beginTransaction();
        Object o = session.createNativeQuery("SELECT SUM(val) FROM items;").getSingleResult();
        System.out.println(o);
        session.getTransaction().commit();
        factory.close();
    }

    public static void calculateDataInDb() {
        CountDownLatch countDownLatch = new CountDownLatch(THREADS_COUNT);
        SessionFactory factory = new Configuration()
                .configure("hibernate.cfg.xml")
                .buildSessionFactory();
        for (int i = 0; i < THREADS_COUNT; i++) {
            final int n = i;
            new Thread(() -> {
                System.out.println("Thread " + n + " started");
                for (int j = 0; j < 20_000; j++) {
                    Long id = (long) ((Math.random() * 39) + 1);
                    Session session = factory.getCurrentSession();
                    session.beginTransaction();
                    Item item = session.get(Item.class, id, LockMode.PESSIMISTIC_WRITE);
                    int val = item.getVal();
                    item.setVal(++val);
                    threadSleep(5);
                    try {
                        session.save(item);
                        session.getTransaction().commit();
                        System.out.println("Thread " + n + " commited");
                    } catch (OptimisticLockException e) {
//                        session.getTransaction().rollback();
//                        System.out.println("Thread " + n + " rollback");
                        e.printStackTrace();
                    }
                    if (session != null) {
                        session.close();
                    }
                }
                countDownLatch.countDown();
            }).start();
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        factory.close();
    }

    public static void prepareData() {
        SessionFactory factory = new Configuration()
                .configure("hibernate.cfg.xml")
                .buildSessionFactory();
        Session session = null;
        try {
            String sql = Files.lines(Paths.get("drop_and_create.sql")).collect(Collectors.joining(" "));
            session = factory.getCurrentSession();
            session.beginTransaction();
            session.createNativeQuery(sql).executeUpdate();
            for (int i = 0; i < 40; i++) {
                session.save(new Item(0));
            }
            session.getTransaction().commit();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            factory.close();
            if (session != null) {
                session.close();
            }
        }
    }

    public static void threadSleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
