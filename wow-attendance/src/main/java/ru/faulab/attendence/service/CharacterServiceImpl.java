package ru.faulab.attendence.service;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import ru.faulab.attendence.dto.store.Character;
import ru.faulab.attendence.dto.store.Rang;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Singleton
public class CharacterServiceImpl implements CharacterService {

    private final EntityManagerFactory entityManagerFactory;
    private final GuildDataLoader guildDataLoader;
    private final Map<String, Character> characterMap;

    @Inject
    public CharacterServiceImpl(EntityManagerFactory entityManagerFactory, GuildDataLoader guildDataLoader) {
        this.entityManagerFactory = entityManagerFactory;
        this.guildDataLoader = guildDataLoader;
        this.characterMap = Maps.newHashMap();
    }

    private static void addNewMembers(EntityManager entityManager, Iterable<String> newRaidMembers, Rang rang, ImmutableSet.Builder<Report.RangChange> reportBuilder, Map<String, Character> tempMapping) {
        for (String member : newRaidMembers) {
            Character character = tempMapping.get(member);
            if (character != null) {
                Rang oldRang = character.getRang();
                character.setRang(rang);
                reportBuilder.add(new Report.RangChange(member, oldRang, rang));
            } else {
                character = new Character(member, rang, new Date());
                entityManager.persist(character);
                tempMapping.put(member, character);
                reportBuilder.add(new Report.RangChange(member, null, rang));
            }
        }
    }

    @Override
    public Report sync() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        List<Character> resultList = entityManager.createNamedQuery("Character.findAllCharacters", ru.faulab.attendence.dto.store.Character.class).getResultList();
        Map<String, Character> tempMapping = Maps.newHashMap();
        tempMapping.putAll(Maps.uniqueIndex(resultList, new Function<Character, String>() {
            @Override
            public String apply(Character input) {
                return input.getNickname();
            }
        }));


        GuildDataLoaderImpl.GuildData guildData = guildDataLoader.loadGuildMembers();
        Set<String> storedNicknames = tempMapping.keySet();
        Set<String> battleNetNickNames = ImmutableSet.<String>builder().addAll(guildData.raidMembers).addAll(guildData.candidates).addAll(guildData.newComers).build();
        Sets.SetView<String> newRaidMembers = Sets.difference(guildData.raidMembers, Sets.newHashSet(Iterables.transform(Iterables.filter(resultList, new CharacterPredicate(Rang.RAID_MEMBER)), new CharacterNick())));
        Sets.SetView<String> newCandidates = Sets.difference(guildData.candidates, Sets.newHashSet(Iterables.transform(Iterables.filter(resultList, new CharacterPredicate(Rang.CANDIDATE)), new CharacterNick())));
        Sets.SetView<String> newNewComer = Sets.difference(guildData.newComers, Sets.newHashSet(Iterables.transform(Iterables.filter(resultList, new CharacterPredicate(Rang.NEW_COMER)), new CharacterNick())));
        Set<String> deadBodies = Sets.difference(storedNicknames, battleNetNickNames).immutableCopy();


        ImmutableSet.Builder<Report.RangChange> reportBuilder = ImmutableSet.builder();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        for (String deadBody : deadBodies) {
            Character remove = tempMapping.remove(deadBody);
//            entityManager.remove(remove);
            reportBuilder.add(new Report.RangChange(remove.getNickname(), remove.getRang(), null));
        }
        addNewMembers(entityManager, newRaidMembers, Rang.RAID_MEMBER, reportBuilder, tempMapping);
        addNewMembers(entityManager, newCandidates, Rang.CANDIDATE, reportBuilder, tempMapping);
        addNewMembers(entityManager, newNewComer, Rang.NEW_COMER, reportBuilder, tempMapping);
        transaction.commit();
        entityManager.close();

        characterMap.putAll(tempMapping);

        return new Report(reportBuilder.build());
    }

    @Override
    public Character findCharacter(String nickname) {
        return characterMap.get(nickname);
    }

    @Override
    public ImmutableSet<Character> allRegisteredCharacters() {
        return ImmutableSet.copyOf(characterMap.values());
    }

    private static class CharacterPredicate implements Predicate<Character> {
        private final Rang rang;

        private CharacterPredicate(Rang rang) {
            this.rang = rang;
        }

        public boolean apply(Character input) {
            return input.getRang() == rang;
        }
    }

    private static class CharacterNick implements Function<Character, String> {
        @Override
        public String apply(ru.faulab.attendence.dto.store.Character input) {
            return input.getNickname();
        }
    }
}
