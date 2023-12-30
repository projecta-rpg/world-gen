package me.kyuri.dimensionaldescent;

import me.kyuri.dimensionaldescent.Commands.GenerateCommand;
import me.kyuri.dimensionaldescent.Generation.ChunkGenerators.CustomChunkGenerator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.bukkit.BukkitCommandHandler;

import java.util.logging.Level;

public final class DimensionalDescent extends JavaPlugin implements Listener {
    private static BukkitCommandHandler handler;
    private static DimensionalDescent instance;
    private CustomChunkGenerator chunkGenerator;
    @Override
    public void onEnable() {
        instance = this;
        chunkGenerator = new CustomChunkGenerator();
        getLogger().log(Level.INFO, "WorldGenerator was enabled successfully.");
        handler = BukkitCommandHandler.create(this);
        handler.register(new GenerateCommand());
        getServer().getPluginManager().registerEvents(this, this);

    }
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        chunkGenerator.logNoiseValues(event.getPlayer());
    }


    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, "WorldGenerator was disabled successfully.");
    }
    public static BukkitCommandHandler getCommandHandler() {
        return handler;
    }

    public static DimensionalDescent getInstance() {
        return instance;
    }
}
