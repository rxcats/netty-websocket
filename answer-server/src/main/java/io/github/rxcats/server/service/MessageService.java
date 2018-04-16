package io.github.rxcats.server.service;

import io.github.rxcats.core.netty.ws.parser.PacketParser;
import io.github.rxcats.server.message.message.SendMessageRes;
import io.github.rxcats.server.repository.PlayerSessionRepository;
import io.netty.channel.Channel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map.Entry;

@Service
public class MessageService {

	@Autowired
	private PacketParser packetParser;

	@Autowired
	private PlayerSessionRepository playerSessionRepository;

	public void sendBroadcast(String method, String fromPlayer, Object message) {
		sendBroadcast(method, fromPlayer, message, false);
	}

	public void sendBroadcast(String method, String fromPlayer, Object message, boolean ignoreSender) {
		for (Entry<String, Channel> entry : playerSessionRepository.getPlayerSessionMap().entrySet()) {
			if (entry.getValue() != null || entry.getValue().isActive()) {
				if (ignoreSender && entry.getKey().equals(fromPlayer)) {
					continue;
				}

				entry.getValue().writeAndFlush(
					packetParser.convertWebSocketFrame(
						packetParser.convertResponse(method, new SendMessageRes<>(fromPlayer, message))
					)
				);
			}
		}
	}

	public void sendMessage(String method, String player, String targetPlayer, Object message) {
		Channel channel = playerSessionRepository.getPlayerSessionMap().get(targetPlayer);
		if (channel != null && channel.isActive()) {
			channel.writeAndFlush(
				packetParser.convertWebSocketFrame(
					packetParser.convertResponse(method, new SendMessageRes<>(player, targetPlayer, message))
				)
			);
		}
	}
}
