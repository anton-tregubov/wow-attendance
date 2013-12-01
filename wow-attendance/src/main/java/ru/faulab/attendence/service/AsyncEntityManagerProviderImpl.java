package ru.faulab.attendence.service;

import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Singleton
public class AsyncEntityManagerProviderImpl implements AsyncEntityManagerProvider {

    private final ListeningExecutorService listeningExecutorService;
    private final Lock lock;
    private ListenableFuture<EntityManagerFactory> entityManagerFactory;

    @Inject
    public AsyncEntityManagerProviderImpl(ListeningExecutorService listeningExecutorService) {
        this.listeningExecutorService = listeningExecutorService;
        lock = new ReentrantLock();
    }

    @Override
    public ListenableFuture<EntityManager> requestEntityManager() {

        if (entityManagerFactory == null) {
            lock.lock();
            try {
                if (entityManagerFactory == null)
                    entityManagerFactory = listeningExecutorService.submit(new Callable<EntityManagerFactory>() {
                        @Override
                        public EntityManagerFactory call() throws Exception {
                            EntityManagerFactory factory = Persistence.createEntityManagerFactory("WoWAttendance");
                            //todo add Data Migration here
                            return factory;
                        }
                    });
            } finally {
                lock.unlock();
            }
        }
        return Futures.transform(entityManagerFactory, new AsyncFunction<EntityManagerFactory, EntityManager>() {
            @Override
            public ListenableFuture<EntityManager> apply(final EntityManagerFactory input) throws Exception {
                return listeningExecutorService.submit(new Callable<EntityManager>() {
                    @Override
                    public EntityManager call() throws Exception {
                        return input.createEntityManager();
                    }
                });
            }
        }, listeningExecutorService);
    }
}
