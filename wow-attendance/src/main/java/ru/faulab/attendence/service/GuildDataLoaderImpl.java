package ru.faulab.attendence.service;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Inject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.json.impl.provider.entity.JSONRootElementProvider;
import ru.faulab.attendence.dto.wow.Guild;
import ru.faulab.attendence.dto.wow.Member;

import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.Callable;

public class GuildDataLoaderImpl implements GuildDataLoader {
    private static final String SERVER = "Гордунни";
    private static final String GUILD_NAME = "Теория Хаоса";
    private final ListeningExecutorService listeningExecutorService;

    @Inject
    public GuildDataLoaderImpl(ListeningExecutorService listeningExecutorService) {
        this.listeningExecutorService = listeningExecutorService;
    }

    private static String oldSchoolEncoding(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public ListenableFuture<GuildData> loadActualGuildInfo() {
        return listeningExecutorService.submit(new Callable<GuildData>() {
            @Override
            public GuildData call() throws Exception {
                ClientConfig config = new DefaultClientConfig(JSONRootElementProvider.App.class);
                WebResource webResource = Client.create(config).resource("http://eu.battle.net/api/wow/guild/" + oldSchoolEncoding(SERVER) + "/" + oldSchoolEncoding(GUILD_NAME)).queryParam("fields", "members");

                Guild guild = webResource.accept(MediaType.APPLICATION_JSON_TYPE).get(Guild.class);

                ImmutableSet.Builder<String> newComers = ImmutableSet.builder();
                ImmutableSet.Builder<String> candidates = ImmutableSet.builder();
                ImmutableSet.Builder<String> raidMembers = ImmutableSet.builder();
                for (Member member : guild.members) {
                    switch (member.rank) {
                        case 4:
                            raidMembers.add(member.character.name);
                            break;
                        case 5:
                            candidates.add(member.character.name);
                            break;
                        case 9:
                            newComers.add(member.character.name);
                            break;
                    }
                }
                return new GuildData(newComers.build(), candidates.build(), raidMembers.build());
            }
        });
    }

}
