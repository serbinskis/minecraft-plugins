package me.serbinskis.smptweaks.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark methods that are PaperMC-specific.
 * It helps differentiate features that rely on the PaperMC server API
 * from those that are purely Bukkit/Spigot.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Paper {}