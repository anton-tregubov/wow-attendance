package ru.faulab.attendence.service;

import com.google.common.collect.ImmutableSet;
import ru.faulab.attendence.dto.store.Character;
import ru.faulab.attendence.dto.store.Rang;

public interface CharacterService {
    Report sync();

    ru.faulab.attendence.dto.store.Character findCharacter(String nickname);

    ImmutableSet<Character> allRegisteredCharacters();

    public static class Report {
        public final ImmutableSet<CharacterServiceImpl.Report.RangChange> rangChanges;

        public Report(ImmutableSet<CharacterServiceImpl.Report.RangChange> rangChanges) {
            this.rangChanges = rangChanges;
        }

        public static class RangChange {
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
