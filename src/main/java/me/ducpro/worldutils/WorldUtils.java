package me.ducpro.worldutils;

import org.apache.commons.io.FileUtils;

import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class WorldUtils {
    private static Plugin plugin;
    private static List<String> disableKeepSpawnWorlds = new ArrayList<>();

    public static void setPlugin(Plugin plugin) {
        WorldUtils.plugin = plugin;
    }

    public static void registerWorldInitListener() {
        plugin.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onWorldInit(WorldInitEvent event) {
                if (disableKeepSpawnWorlds.contains(event.getWorld().getName())) {
                    event.getWorld().setKeepSpawnInMemory(false);
                    disableKeepSpawnWorlds.remove(event.getWorld().getName());
                }
            }
        }, plugin);
    }

    public static CompletableFuture<Boolean> copyWorldFolderAsync(File originalDirectory, File newDirectory) {
        var result = new CompletableFuture<Boolean>();
        var noCopy = Arrays.asList("uid.dat", "session.lock");
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    FileUtils.copyDirectory(originalDirectory, newDirectory, file -> !noCopy.contains(file.getName()));
                } catch (IOException exception) {
                    result.complete(false);
                    exception.printStackTrace();
                }
                result.complete(true);
            }
        }.runTaskAsynchronously(plugin);
        return result;
    }

    public static CompletableFuture<Boolean> copyWorldAsync(String originalName, String newName) {
        var originalDirectory = new File(Bukkit.getWorldContainer(), originalName);
        var newDirectory = new File(Bukkit.getWorldContainer(), newName);
        return copyWorldFolderAsync(originalDirectory, newDirectory);
    }

    public static CompletableFuture<Boolean> copyAndLoadWorldAsync(String originalName, String newName,
                                                                   boolean keepSpawnInMemory) {
        var originalDirectory = new File(Bukkit.getWorldContainer(), originalName);
        var newDirectory = new File(Bukkit.getWorldContainer(), newName);
        var result = new CompletableFuture<Boolean>();
        copyWorldFolderAsync(originalDirectory, newDirectory).thenAccept(success -> new BukkitRunnable() {
            @Override
            public void run() {
                if (!success) {
                    result.complete(false);
                    return;
                }
                if (!keepSpawnInMemory) disableKeepSpawnWorlds.add(newName);
                Bukkit.createWorld(new WorldCreator(newName));
                result.complete(true);
            }
        }.runTask(plugin));
        return result;
    }

    public static CompletableFuture<Boolean> copyAndLoadWorldAsync(String originalName, String newName) {
        return copyAndLoadWorldAsync(originalName, newName, false);
    }

    public static CompletableFuture<Boolean> removeWorldFolderAsync(File directory) {
        var result = new CompletableFuture<Boolean>();
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    FileUtils.deleteDirectory(directory);
                    result.complete(true);
                } catch (IOException exception) {
                    result.complete(false);
                    exception.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
        return result;
    }

    public static CompletableFuture<Boolean> removeWorldAsync(String worldName) {
        boolean unloadSuccess = Bukkit.unloadWorld(worldName, false);
        if (!unloadSuccess) {
            Bukkit.getLogger().log(Level.WARNING, String.format("Unable to unload world %s.", worldName));
            return CompletableFuture.completedFuture(false);
        }
        var worldDirectory = new File(Bukkit.getWorldContainer(), worldName);
        return removeWorldFolderAsync(worldDirectory);
    }
}
