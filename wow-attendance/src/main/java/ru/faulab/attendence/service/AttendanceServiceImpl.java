package ru.faulab.attendence.service;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Inject;
import ru.faulab.attendence.dto.store.Attendance;
import ru.faulab.attendence.dto.store.Character;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.TemporalType;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

public class AttendanceServiceImpl implements AttendanceService {
    private final AsyncEntityManagerProvider asyncEntityManagerProvider;
    private final CharacterService characterService;
    private final ListeningExecutorService listeningExecutorService;

    @Inject
    public AttendanceServiceImpl(AsyncEntityManagerProvider asyncEntityManagerProvider, CharacterService characterService, ListeningExecutorService listeningExecutorService) {
        this.asyncEntityManagerProvider = asyncEntityManagerProvider;
        this.characterService = characterService;
        this.listeningExecutorService = listeningExecutorService;
    }

    @Override
    public ListenableFuture<ImmutableSet<Attendance>> loadAttendanceFromPeriod(final Date from, final Date to) {
        ListenableFuture<EntityManager> entityManagerAsync = asyncEntityManagerProvider.requestEntityManager();
        return Futures.transform(entityManagerAsync, new AsyncFunction<EntityManager, ImmutableSet<Attendance>>() {
            @Override
            public ListenableFuture<ImmutableSet<Attendance>> apply(final EntityManager entityManager) throws Exception {
                return listeningExecutorService.submit(new Callable<ImmutableSet<Attendance>>() {
                    @Override
                    public ImmutableSet<Attendance> call() throws Exception {
                        try {
                            return ImmutableSet.copyOf(entityManager.createNamedQuery("Attendance.findAllAttendancesInPeriod", Attendance.class).setParameter("fromDate", from, TemporalType.DATE).setParameter("toDate", to, TemporalType.DATE).getResultList());
                        } finally {
                            entityManager.close();
                        }
                    }
                });
            }
        }, listeningExecutorService);
    }

    @Override
    public ListenableFuture<AddAttendanceReport> addAttendances(final Date day, final ImmutableSet<String> nicknames) {
        ListenableFuture<EntityManager> entityManagerAsync = asyncEntityManagerProvider.requestEntityManager();
        ListenableFuture<ImmutableSet<Character>> characters = characterService.loadCharacters(nicknames);

        ListenableFuture<List<Object>> joinPoint = Futures.allAsList(entityManagerAsync, characters);
        return Futures.transform(joinPoint, new AsyncFunction<List<Object>, AddAttendanceReport>() {
            @Override
            public ListenableFuture<AddAttendanceReport> apply(List<Object> input) throws Exception {
                final EntityManager entityManager = (EntityManager) Iterables.get(input, 0);
                final ImmutableSet<Character> characters = (ImmutableSet<Character>) Iterables.get(input, 1);
                //todo Remove another attendance at this day;
                final PersistenceUnitUtil persistenceUnitUtil = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
                return listeningExecutorService.submit(new Callable<AddAttendanceReport>() {
                    @Override
                    public AddAttendanceReport call() throws Exception {
                        EntityTransaction transaction = entityManager.getTransaction();
                        ImmutableSet.Builder<String> unknownPeople = ImmutableSet.builder();
                        ImmutableSet.Builder<Attendance> added = ImmutableSet.builder();
                        ImmutableSet.Builder<Attendance> ignored = ImmutableSet.builder();
                        transaction.begin();
                        Set<String> nicknamesCopy = Sets.newHashSet(nicknames);
                        for (Character character : characters) {
                            //todo add Report for not Active users
                            Attendance entity = new Attendance(character, day);
                            Attendance founded = entityManager.find(Attendance.class, persistenceUnitUtil.getIdentifier(entity));
                            if (founded == null) {
                                entityManager.merge(entity);
                                added.add(entity);
                            } else {
                                ignored.add(entity);
                            }
                            nicknamesCopy.remove(character.getNickname());
                        }
                        unknownPeople.addAll(nicknames);
                        transaction.commit();
                        entityManager.close();
                        return new AddAttendanceReport(unknownPeople.build(), ignored.build(), added.build());
                    }
                });
            }
        }, listeningExecutorService);


    }
}
