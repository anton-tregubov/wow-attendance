package ru.faulab.attendence.service;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListenableFuture;
import ru.faulab.attendence.dto.store.Character;
import ru.faulab.attendence.dto.store.Rang;

public interface CharacterService {
    ListenableFuture<UpdateCharactersReport> synchronizeCharacters();

    ListenableFuture<ImmutableSet<Character>> loadAllActiveCharacters();

    ListenableFuture<ImmutableSet<Character>> loadCharacters(Iterable<String> nicknames);

    public static final class UpdateCharactersReport {
        public final ImmutableSet<UpdateCharactersReport.RangChange> rangChanges;

        public UpdateCharactersReport(ImmutableSet<UpdateCharactersReport.RangChange> rangChanges) {
            this.rangChanges = rangChanges;
        }

        public static final class RangChange {
            public final String nickName;
            public final Rang oldRang;
            public final Rang newRang;

            public RangChange(String nickName, Rang oldRang, Rang newRang) {
                this.nickName = nickName;
                this.oldRang = oldRang;
                this.newRang = newRang;
            }
        }
    }
}
