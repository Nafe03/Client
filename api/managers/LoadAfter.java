package dev.anarchy.waifuhax.api.managers;

import dev.anarchy.waifuhax.api.BaseManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface LoadAfter {

    Class<? extends BaseManager> manager();
}
