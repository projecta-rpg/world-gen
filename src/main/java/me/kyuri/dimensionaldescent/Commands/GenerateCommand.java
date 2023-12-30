package me.kyuri.dimensionaldescent.Commands;

import me.kyuri.dimensionaldescent.DimensionalDescent;
import me.kyuri.dimensionaldescent.Generation.Populator.TreePopulator;
import me.kyuri.dimensionaldescent.Generation.ChunkGenerators.CustomChunkGenerator;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import revxrsal.commands.annotation.Command;

import java.util.Random;

public class GenerateCommand {

    @Command("generate")
    public void generate(Player player, String value){
        World world = new WorldCreator(value).generator(new CustomChunkGenerator()).createWorld();

        player.sendMessage("World " + value + " was generated successfully.");
        Location loc = new Location(world, 0, 64, 0);
        //runnable to teleport player after world is generated
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                player.setBedSpawnLocation(loc, true);
                player.teleport(loc);
            }
        };
        runnable.runTaskLater(DimensionalDescent.getInstance(), 100L);
    }
}
