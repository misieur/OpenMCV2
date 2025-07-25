package fr.openmc.core.features.homes.events;

import fr.openmc.core.features.homes.models.Home;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class HomeCreateEvent extends Event {

    private final Home home;
    private final Player owner;

    private static final HandlerList HANDLERS = new HandlerList();

    public HomeCreateEvent(Home home, Player owner) {
        this.home = home;
        this.owner = owner;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

}
