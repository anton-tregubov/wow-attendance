package ru.faulab.attendence;

import com.google.common.collect.ImmutableSet;
import ru.faulab.attendence.store.Attendance;
import ru.faulab.attendence.store.Character;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.TemporalType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import java.util.Date;
import java.util.List;

public class AttendanceService {
    private final EntityManagerFactory entityManagerFactory;
    private final CharacterService characterService;

    public AttendanceService(EntityManagerFactory entityManagerFactory, CharacterService characterService) {
        this.entityManagerFactory = entityManagerFactory;
        this.characterService = characterService;
    }

    public ImmutableSet<Attendance> loadAttendanceByPeriod(Date from, Date to) {
        EntityManager em = entityManagerFactory.createEntityManager();
        List<Attendance> results = em.createNamedQuery("Attendance.findAllAttendancesInPeriod", Attendance.class).setParameter("fromDate",from, TemporalType.DATE).setParameter("toDate",to, TemporalType.DATE).getResultList();
        return ImmutableSet.copyOf(results);
    }

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

    public static class AddAttendanceReport {
        public final ImmutableSet<String> unknownPersons;
        public final ImmutableSet<Attendance> alreadyExistedAttendance;
        public final ImmutableSet<Attendance> addedExistedAttendance;

        public AddAttendanceReport(ImmutableSet<String> unknownPersons, ImmutableSet<Attendance> alreadyExistedAttendance, ImmutableSet<Attendance> addedExistedAttendance) {
            this.unknownPersons = unknownPersons;
            this.alreadyExistedAttendance = alreadyExistedAttendance;
            this.addedExistedAttendance = addedExistedAttendance;
        }
    }
}
