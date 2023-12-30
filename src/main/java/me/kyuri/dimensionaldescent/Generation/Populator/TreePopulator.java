package me.kyuri.dimensionaldescent.Generation.Populator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import java.util.*;

public class TreePopulator extends BlockPopulator {
    private final HashMap<Biome, List<TreeType>> biomeTrees = new HashMap<Biome, List<TreeType>>() {{
        put(Biome.PLAINS, Arrays.asList());
        put(Biome.FOREST, Arrays.asList(TreeType.BIRCH));
        put(Biome.DARK_FOREST, Arrays.asList(TreeType.DARK_OAK));
    }};

    private final Set<String> generatedTreeLocations = new HashSet<>();

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion limitedRegion) {
        int x = random.nextInt(16) + chunkX * 16;
        int z = random.nextInt(16) + chunkZ * 16;
        int y = 319;

        while (limitedRegion.getType(x, y, z).isAir() && y > -64) y--;

        Location location = new Location(Bukkit.getWorld(worldInfo.getUID()), x, y, z);

        // Check if a tree has already been generated at this location with any type
        if (generatedTreeLocations.contains(getLocationKey(location))) {
            return; // Skip tree generation at this location
        }

        List<TreeType> trees = biomeTrees.getOrDefault(limitedRegion.getBiome(location), Arrays.asList(TreeType.TREE, TreeType.BIRCH));

        if (!trees.isEmpty() && limitedRegion.getType(x, y - 1, z).isSolid() && limitedRegion.isInRegion(location)) {
            Material blockBelow = limitedRegion.getType(x, y - 1, z);
            Material blockAt = limitedRegion.getType(x, y, z);

            // Check if the block below is solid and not water
            if (blockBelow.isSolid() && blockBelow != Material.WATER) {
                // Check if the block at the selected location is not water and not leaves
                if (blockAt != Material.WATER && !isLeaves(blockAt) && !hasNearbyLeaves(limitedRegion, x, y, z)) {
                    if(blockBelow == Material.GRASS){
                        limitedRegion.setType(x, y - 1, z, Material.AIR);
                    }
                    // Generate the tree
                    TreeType selectedTreeType = trees.get(random.nextInt(trees.size()));
                    limitedRegion.generateTree(location, random, selectedTreeType);

                    // Add the location along with the tree type to the set of generated tree locations
                    generatedTreeLocations.add(getLocationKey(location));
                }
            }
        }
    }

    private String getLocationKey(Location location) {
        // Create a unique string key for the location (x, z coordinates)
        return location.getWorld().getName() + "_" + location.getBlockX() + "_" + location.getBlockZ();
    }

    private boolean isLeaves(Material material) {
        return material == Material.OAK_LEAVES
                || material == Material.BIRCH_LEAVES
                || material == Material.SPRUCE_LEAVES
                || material == Material.JUNGLE_LEAVES
                || material == Material.ACACIA_LEAVES
                || material == Material.DARK_OAK_LEAVES;
    }
    private boolean hasNearbyLeaves(LimitedRegion limitedRegion, int x, int y, int z) {
        // Check if there are leaves nearby the specified location
        // Radius of 2 blocks TODO make this configurable or based on tree type in enum
        for (int offsetX = -2; offsetX <= 2; offsetX++) {
            for (int offsetY = -2; offsetY <= 2; offsetY++) {
                for (int offsetZ = -2; offsetZ <= 2; offsetZ++) {
                    if (isLeaves(limitedRegion.getType(x + offsetX, y + offsetY, z + offsetZ))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
