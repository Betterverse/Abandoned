package net.betterverse.protect;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class PolygonDebug {
	
	private static boolean log = false;
	
	public static void log(Event event, Cancellable cancel) {
		if(!log)
			return;
		System.out.println(event.getEventName()+"_CANCELLED:"+cancel.isCancelled());
	}

}
