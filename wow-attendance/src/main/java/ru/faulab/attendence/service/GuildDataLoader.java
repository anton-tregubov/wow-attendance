package ru.faulab.attendence.service;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListenableFuture;

public interface GuildDataLoader {
    ListenableFuture<GuildData> loadActualGuildInfo();

    public static final class GuildData {
        public final ImmutableSet<String> newComers;
        public final ImmutableSet<String> candidates;
        public final ImmutableSet<String> raidMembers;

        public GuildData(ImmutableSet<String> newComers, ImmutableSet<String> candidates, ImmutableSet<String> raidMembers) {
            this.newComers = newComers;
            this.candidates = candidates;
            this.raidMembers = raidMembers;
        }
    }
}
