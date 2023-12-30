package me.kyuri.dimensionaldescent.Generation.Populator;

import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PopulatorProvider {
    public @NotNull List<BlockPopulator> getDefaultPopulators(@NotNull World world) {
        List<BlockPopulator> populators = new ArrayList<>();
        //Add populators here
        populators.add(new TreePopulator());
        populators.add(new GrassPopulator());
        return populators;
    }
}
