package io.github.rxcats.server.repository;

import io.netty.channel.Channel;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PlayerSessionRepository {
    private final Map<String, Channel> playerSessionMap = new ConcurrentHashMap<>();

    public Map<String, Channel> getPlayerSessionMap() {
        return playerSessionMap;
    }

    public Channel getPlayerSession(final String player) {
        return playerSessionMap.get(player);
    }

    public String getPlayerBySession(final Channel channel) {
        return playerSessionMap.entrySet().stream()
            .filter(Objects::nonNull)
            .filter(info -> info.getValue().equals(channel))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);
    }

    public void putPlayerChannelMap(final String player, final Channel channel) {
        playerSessionMap.put(player, channel);
    }

    public void removePlayerChannelMap(final String player) {
        Channel channel = playerSessionMap.get(player);
        if (channel != null) {
            playerSessionMap.remove(player);
        }
    }
}
