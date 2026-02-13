package dev.anarchy.waifuhax.api.events;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface EventListener {

    Class<? extends Event> event();
}