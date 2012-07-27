package net.betterverse.towns.chat;

import net.betterverse.towns.chat.event.TownsChatEvent;
import net.betterverse.towns.chat.util.ReplacerCallable;

public abstract class TownsChatReplacerCallable implements ReplacerCallable<TownsChatEvent> {
	public TownsChatReplacerCallable() {
	}

	public abstract String call(String match, TownsChatEvent event) throws Exception;
}
