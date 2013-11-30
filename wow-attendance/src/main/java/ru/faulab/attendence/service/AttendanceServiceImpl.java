package ru.faulab.attendence.service;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import ru.faulab.attendence.dto.store.Attendance;
import ru.faulab.attendence.dto.store.Character;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.TemporalType;
import java.util.Date;
import java.util.List;

public class AttendanceServiceImpl implements AttendanceService {
    private final EntityManagerFactory entityManagerFactory;
    private final CharacterService characterService;

    @Inject
    public AttendanceServiceImpl(EntityManagerFactory entityManagerFactory, CharacterService characterService) {
        this.entityManagerFactory = entityManagerFactory;
        this.characterService = characterService;
    }

    @Override
    public ImmutableSet<Attendance> loadAttendanceByPeriod(Date from, Date to) {
        EntityManager em = entityManagerFactory.createEntityManager();
        List<Attendance> results = em.createNamedQuery("Attendance.findAllAttendancesInPeriod", Attendance.class).setParameter("fromDate", from, TemporalType.DATE).setParameter("toDate", to, TemporalType.DATE).getResultList();
        return ImmutableSet.copyOf(results);
    }

    @Override
    public AddAttendanceReport addAttendancies(Date day, ImmutableSet<String> nicknames) {
        EntityManager em = entityManagerFactory.createEntityManager();

        //todo Remove another at this day;
        EntityTransaction transaction = em.getTransaction();
        ImmutableSet.Builder<String> unknownPeople = ImmutableSet.builder();
        ImmutableSet.Builder<Attendance> added = ImmutableSet.builder();
        ImmutableSet.Builder<Attendance> ignored = ImmutableSet.builder();
        transaction.begin();

        for (String nickname : nicknames) {
            Character character = characterService.findCharacter(nickname);
            if (character != null) {
                Attendance entity = new Attendance(character, day);
                Attendance founded = em.find(Attendance.class, entity.getKey());
                if (founded == null) {
                    em.merge(entity);
                    added.add(entity);
                } else {
                    ignored.add(entity);
                }
            } else {
                unknownPeople.add(nickname);
            }
        }
        transaction.commit();
        em.close();
        return new AddAttendanceReport(unknownPeople.build(), ignored.build(), added.build());
    }
}
