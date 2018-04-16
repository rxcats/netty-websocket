package io.github.rxcats.server.message.message;

import lombok.Data;

@Data
public class SendMessageRes<T> {
	private String player;
	private String targetPlayer;
	private T message;

	public SendMessageRes(final String player, final T message) {
		this.player = player;
		this.message = message;
	}

	public SendMessageRes(final String player, final String targetPlayer, final T message) {
		this.player = player;
		this.targetPlayer = targetPlayer;
		this.message = message;
	}
}
