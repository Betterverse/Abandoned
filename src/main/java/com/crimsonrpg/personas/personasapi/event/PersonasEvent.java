/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.crimsonrpg.personas.personasapi.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Represents a Personas-related event.
 */
public class PersonasEvent extends Event {
    private EventType type;
		private HandlerList hl = new HandlerList();
    
    public PersonasEvent(EventType type) {
        super();
    }

    public EventType getEventType() {
        return type;
    }

	@Override
	public String getEventName() {
		return "PERSONAS_" + type.name();
	}

	@Override
	public HandlerList getHandlers() {
		return hl;
	}
    
}
