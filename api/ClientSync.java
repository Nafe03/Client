package dev.anarchy.waifuhax.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ClientSync {

    String clientid();
}
