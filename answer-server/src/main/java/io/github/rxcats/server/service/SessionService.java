package io.github.rxcats.server.service;

import io.github.rxcats.server.repository.PlayerSessionRepository;
import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SessionService {

    @Autowired
    private PlayerSessionRepository playerSessionRepository;

    public void addPlayerSessionInfo(Channel channel, String player) {
        playerSessionRepository.putPlayerChannelMap(player, channel);
    }

    public void removePlayerSessionInfo(String player) {
        playerSessionRepository.getPlayerSession(player).closeFuture();
        playerSessionRepository.removePlayerChannelMap(player);
    }
}
