package ru.faulab.attendence.service;

import com.google.common.collect.ImmutableSet;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.json.impl.provider.entity.JSONRootElementProvider;
import ru.faulab.attendence.dto.wow.Guild;
import ru.faulab.attendence.dto.wow.Member;

import javax.ws.rs.core.MediaType;

public class GuildDataLoaderImpl implements GuildDataLoader {

    @Override
    public GuildData loadGuildMembers() {
        ClientConfig config = new DefaultClientConfig(JSONRootElementProvider.App.class);
        Client client = Client.create(config);
        WebResource webResource = client
                .resource("http://eu.battle.net/api/wow/guild/%D0%93%D0%BE%D1%80%D0%B4%D1%83%D0%BD%D0%BD%D0%B8/%D0%A2%D0%B5%D0%BE%D1%80%D0%B8%D1%8F%20%D0%A5%D0%B0%D0%BE%D1%81%D0%B0?fields=members");

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

}
