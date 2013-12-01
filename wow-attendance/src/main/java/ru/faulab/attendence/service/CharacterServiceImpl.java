package ru.faulab.attendence.service;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Inject;
import ru.faulab.attendence.dto.store.Character;
import ru.faulab.attendence.dto.store.Rang;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

public class CharacterServiceImpl implements CharacterService {

    private final AsyncEntityManagerProvider entityManagerFactory;
    private final GuildDataLoader guildDataLoader;
    private final ListeningExecutorService listeningExecutorService;

    @Inject
    public CharacterServiceImpl(AsyncEntityManagerProvider entityManagerFactory, GuildDataLoader guildDataLoader, ListeningExecutorService listeningExecutorService) {
        this.entityManagerFactory = entityManagerFactory;
        this.guildDataLoader = guildDataLoader;
        this.listeningExecutorService = listeningExecutorService;
    }

    private static void addNewMembers(EntityManager entityManager, Iterable<String> newRaidMembers, Rang rang, ImmutableSet.Builder<UpdateCharactersReport.RangChange> reportBuilder, Map<String, Character> tempMapping) {
        for (String member : newRaidMembers) {
            Character character = tempMapping.get(member);
            if (character != null) {
                Rang oldRang = character.getRang();
                character.setRang(rang);
                reportBuilder.add(new UpdateCharactersReport.RangChange(member, oldRang, rang));
            } else {
                character = new Character(member, rang, new Date());
                entityManager.persist(character);
                tempMapping.put(member, character);
                reportBuilder.add(new UpdateCharactersReport.RangChange(member, null, rang));
            }
        }
    }

    @Override
    public ListenableFuture<UpdateCharactersReport> synchronizeCharacters() {
        ListenableFuture<GuildDataLoader.GuildData> guildDataAsync = guildDataLoader.loadActualGuildInfo();
        ListenableFuture<EntityManager> entityManagerAsync = entityManagerFactory.requestEntityManager();
        ListenableFuture<ImmutableSet<Character>> charactersAsync = loadAllActiveCharacters();

        ListenableFuture<List<Object>> deferredTrio = Futures.allAsList(guildDataAsync, entityManagerAsync, charactersAsync);

        return Futures.transform(deferredTrio, new AsyncFunction<List<Object>, UpdateCharactersReport>() {
            @Override
            public ListenableFuture<UpdateCharactersReport> apply(List<Object> trio) throws Exception {
                final GuildDataLoader.GuildData guildData = (GuildDataLoader.GuildData) Iterables.get(trio, 0);
                final EntityManager entityManager = (EntityManager) Iterables.get(trio, 1);
                final ImmutableSet<Character> characters = (ImmutableSet<Character>) Iterables.get(trio, 2);

                return listeningExecutorService.submit(new Callable<UpdateCharactersReport>() {
                    @Override
                    public UpdateCharactersReport call() throws Exception {
                        Map<String, Character> tempMapping = Maps.newHashMap();
                        tempMapping.putAll(Maps.uniqueIndex(characters, new CharacterNickname()));

                        Set<String> storedNicknames = tempMapping.keySet();
                        Set<String> battleNetNickNames = ImmutableSet.<String>builder().addAll(guildData.raidMembers).addAll(guildData.candidates).addAll(guildData.newComers).build();
                        Sets.SetView<String> newRaidMembers = Sets.difference(guildData.raidMembers, Sets.newHashSet(Iterables.transform(Iterables.filter(characters, new CharacterRang(Rang.RAID_MEMBER)), new CharacterNickname())));
                        Sets.SetView<String> newCandidates = Sets.difference(guildData.candidates, Sets.newHashSet(Iterables.transform(Iterables.filter(characters, new CharacterRang(Rang.CANDIDATE)), new CharacterNickname())));
                        Sets.SetView<String> newNewComer = Sets.difference(guildData.newComers, Sets.newHashSet(Iterables.transform(Iterables.filter(characters, new CharacterRang(Rang.NEW_COMER)), new CharacterNickname())));
                        Set<String> deadBodies = Sets.difference(storedNicknames, battleNetNickNames).immutableCopy();

                        ImmutableSet.Builder<UpdateCharactersReport.RangChange> reportBuilder = ImmutableSet.builder();
                        EntityTransaction transaction = entityManager.getTransaction();
                        transaction.begin();
                        for (String deadBody : deadBodies) {
                            Character remove = tempMapping.remove(deadBody);
                            remove.disable();
                            entityManager.merge(remove);
                            reportBuilder.add(new UpdateCharactersReport.RangChange(remove.getNickname(), remove.getRang(), null));
                        }
                        addNewMembers(entityManager, newRaidMembers, Rang.RAID_MEMBER, reportBuilder, tempMapping);
                        addNewMembers(entityManager, newCandidates, Rang.CANDIDATE, reportBuilder, tempMapping);
                        addNewMembers(entityManager, newNewComer, Rang.NEW_COMER, reportBuilder, tempMapping);
                        transaction.commit();
                        entityManager.close();
                        return new UpdateCharactersReport(reportBuilder.build());
                    }
                });
            }
        }, listeningExecutorService);


    }

    @Override
    public ListenableFuture<ImmutableSet<Character>> loadAllActiveCharacters() {
        ListenableFuture<EntityManager> entityManagerAsync = entityManagerFactory.requestEntityManager();
        return Futures.transform(entityManagerAsync, new AsyncFunction<EntityManager, ImmutableSet<Character>>() {
            @Override
            public ListenableFuture<ImmutableSet<Character>> apply(final EntityManager entityManager) throws Exception {
                return listeningExecutorService.submit(new Callable<ImmutableSet<Character>>() {
                    @Override
                    public ImmutableSet<Character> call() throws Exception {
                        return ImmutableSet.copyOf(entityManager.createNamedQuery("Character.findAllActiveCharacters", Character.class).getResultList());
                    }
                });
            }
        }, listeningExecutorService);
    }

    @Override
    public ListenableFuture<ImmutableSet<Character>> loadCharacters(final Iterable<String> nicknames) {
        ListenableFuture<EntityManager> entityManagerAsync = entityManagerFactory.requestEntityManager();
        return Futures.transform(entityManagerAsync, new Function<EntityManager, ImmutableSet<Character>>() {
            @Override
            public ImmutableSet<Character> apply(EntityManager entityManager) {
                return ImmutableSet.copyOf(entityManager.createNamedQuery("Character.findCharactersByNames", Character.class).setParameter("nicknames", nicknames).getResultList());
            }
        }, listeningExecutorService);
    }

    private static final class CharacterRang implements Predicate<Character> {
        private final Rang rang;

        private CharacterRang(Rang rang) {
            this.rang = rang;
        }

        public boolean apply(Character input) {
            return input.getRang() == rang;
        }
    }

    private static final class CharacterNickname implements Function<Character, String> {
        @Override
        public String apply(ru.faulab.attendence.dto.store.Character input) {
            return input.getNickname();
        }
    }
}
