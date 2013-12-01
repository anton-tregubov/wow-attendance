package ru.faulab.attendence.service;

import com.google.common.util.concurrent.ListenableFuture;

import javax.persistence.EntityManager;

public interface AsyncEntityManagerProvider {
    ListenableFuture<EntityManager> requestEntityManager();
}
