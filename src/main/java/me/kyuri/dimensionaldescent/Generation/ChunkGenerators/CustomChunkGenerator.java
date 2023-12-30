package me.kyuri.dimensionaldescent.Generation.ChunkGenerators;


import me.kyuri.dimensionaldescent.DimensionalDescent;
import me.kyuri.dimensionaldescent.Generation.Noise.FastNoiseLite;
import me.kyuri.dimensionaldescent.Utils.TerrainUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/*
* TODO:
* Fix the water level + Heightmap scale
* Make the terrain more interesting (add caves, ravines, etc.)
* Make noise functions work together better
* */

public class CustomChunkGenerator extends ChunkGenerator {
    private final FastNoiseLite Continentalness = new FastNoiseLite();
    private final FastNoiseLite Peaks = new FastNoiseLite();
    private final FastNoiseLite Erosion = new FastNoiseLite();
    private final TerrainUtil ContinentalnessChart;
    private final TerrainUtil ErosionChart;
    private final TerrainUtil PeaksChart;

    public CustomChunkGenerator() {
        Continentalness.SetNoiseType(FastNoiseLite.NoiseType.Perlin);
        Continentalness.SetFractalType(FastNoiseLite.FractalType.FBm);
        Continentalness.SetFractalOctaves(4);
        Continentalness.SetFrequency(0.008f); // Increase for larger patterns


        Peaks.SetNoiseType(FastNoiseLite.NoiseType.Perlin);
        Peaks.SetFractalOctaves(2);
        Peaks.SetFrequency(0.005f); // Increase for sharper peaks

        Erosion.SetNoiseType(FastNoiseLite.NoiseType.Perlin); // Change noise type to OpenSimplex2 for more irregular patterns
        Erosion.SetFractalOctaves(2); // Increase octaves for more detail
        Erosion.SetFrequency(0.003f); // Increase frequency for more frequent changes


        ContinentalnessChart = new TerrainUtil();
        double[] ContinentalValues = new double[]{-1.0,0.15,0.50,1.0};
        double[] ContinentalHeight = new double[]{50,100,150,150};
        ContinentalnessChart.setControlPoints(ContinentalValues, ContinentalHeight, true);


        ErosionChart = new TerrainUtil();
        double[] ErosionValues = new double[]{-1.0, -0.9, -0.6, -0.3, 0.1, 0.2, 0.5, 0.7, 0.75, 0.8, 1.0};
        double[] ErosionHeight = new double[]{150.0, 130.0, 61.0, 60.0, 60.0, 131.0, 130.0, 80.0, 70, 65, 65};
        ErosionChart.setControlPoints(ErosionValues, ErosionHeight, true);

        PeaksChart = new TerrainUtil();
        double[] PeaksValues = new double[]{-0.85,-0.30,0.10,0.50,0.75,1.00};
        double[] PeaksHeight = new double[]{0,70,70,160,170,170};
        PeaksChart.setControlPoints(PeaksValues, PeaksHeight, false);
    }
    private TerrainUtil currentTerrainUtil;

    public void generateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
        for (int y = chunkData.getMinHeight(); y < 319 && y < chunkData.getMaxHeight(); y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {

                    double continentalNoiseCurrent = Continentalness.GetNoise(x + (chunkX * 16), z + (chunkZ * 16));
                    double continentalNoiseNext = Continentalness.GetNoise(x + (chunkX * 16) + 1, z + (chunkZ * 16) + 1);

                    double erosionNoiseCurrent = Erosion.GetNoise(x + (chunkX * 16), z + (chunkZ * 16));
                    double erosionNoiseNext = Erosion.GetNoise(x + (chunkX * 16) + 1, z + (chunkZ * 16) + 1);

                    double peaksNoiseCurrent = Peaks.GetNoise(x + (chunkX * 16), z + (chunkZ * 16));
                    double peaksNoiseNext = Peaks.GetNoise(x + (chunkX * 16) + 1, z + (chunkZ * 16) + 1);


                    double continentalWeight = 0.6;
                    double erosionWeight = 0.2;
                    double peaksWeight = 0.2;

                    // Ensure the weights sum to 1
                    double totalWeight = continentalWeight + erosionWeight + peaksWeight;
                    continentalWeight /= totalWeight;
                    erosionWeight /= totalWeight;
                    peaksWeight /= totalWeight;

                    // Compute the weighted average of the noises
                    double noiseCurrent = continentalWeight * continentalNoiseCurrent + erosionWeight * erosionNoiseCurrent + peaksWeight * peaksNoiseCurrent;
                    double noiseNext = continentalWeight * continentalNoiseNext + erosionWeight * erosionNoiseNext + peaksWeight * peaksNoiseNext;


                    // Adjust the thresholds for terrain selection
                    double continentalThreshold = 0.5;   // Adjust this threshold for continental terrain
                    double erosionThreshold = -0.6;   // Adjust this threshold for lower erosion terrain
                    double PeaksThreshold = 0.2;  // Adjust this threshold for higher erosion terrain




                    if (continentalNoiseCurrent > continentalThreshold) {
                        currentTerrainUtil = ErosionChart;
                    } else if (erosionNoiseCurrent > erosionThreshold) {
                        currentTerrainUtil = PeaksChart;
                    } else {
                        currentTerrainUtil = ContinentalnessChart;
                    }


                    // Map noise to height with smoothing
                    double targetHeight = currentTerrainUtil.getSmoothTerrainHeight(noiseCurrent, noiseNext, 0.5);


                    if (y < targetHeight) {
                        if (y + 1 >= targetHeight) {
                            chunkData.setBlock(x, y, z, Material.GRASS_BLOCK);
                        } else {
                            chunkData.setBlock(x, y, z, Material.STONE);
                        }
                    } else if (y < 62){
                        chunkData.setBlock(x,y,z, Material.WATER);
                    }
                }
            }
        }
    }


    /*
    *
    * UTILS
    *
    * */






    private BossBar continentalBar;
    private BossBar erosionBar;
    private BossBar peaksBar;

    public void logNoiseValues(Player player) {
        double continentalThreshold = 0.5;   // Adjust this threshold for continental terrain
        double erosionThreshold = -0.6;   // Adjust this threshold for lower erosion terrain
        double PeaksThreshold = 0.2;  // Adjust this threshold for higher erosion terrain

        // Create boss bars for each noise value if they don't exist
        if (continentalBar == null) {
            continentalBar = Bukkit.createBossBar("", BarColor.BLUE, BarStyle.SOLID);
            continentalBar.addPlayer(player);
        }
        if (erosionBar == null) {
            erosionBar = Bukkit.createBossBar("", BarColor.GREEN, BarStyle.SOLID);
            erosionBar.addPlayer(player);
        }
        if (peaksBar == null) {
            peaksBar = Bukkit.createBossBar("", BarColor.RED, BarStyle.SOLID);
            peaksBar.addPlayer(player);
        }

        // Create a new Bukkit Runnable to update the boss bars
        new BukkitRunnable() {
            @Override
            public void run() {
                int x = player.getLocation().getBlockX();
                int z = player.getLocation().getBlockZ();

                double continentalNoise = Continentalness.GetNoise(x, z);
                double erosionNoise = Erosion.GetNoise(x, z);
                double peaksNoise = Peaks.GetNoise(x, z);

                // Update the title of each boss bar to include the noise value
                continentalBar.setTitle(String.format("Continentalness Noise: %.2f", continentalNoise));
                erosionBar.setTitle(String.format("Erosion Noise: %.2f", erosionNoise));
                peaksBar.setTitle(String.format("Peaks Noise: %.2f", peaksNoise));

                // Update the progress of each boss bar
                continentalBar.setProgress(Math.abs(continentalNoise));
                erosionBar.setProgress(Math.abs(erosionNoise));
                peaksBar.setProgress(Math.abs(peaksNoise));

                // Send a chat message indicating the current chart being used
                if (continentalNoise > continentalThreshold) {
                    currentTerrainUtil = ErosionChart;
                    player.sendMessage("Using Erosion Chart for terrain generation.");
                } else if (erosionNoise > erosionThreshold) {
                    currentTerrainUtil = PeaksChart;
                    player.sendMessage("Using Peaks Chart for terrain generation.");
                } else {
                    currentTerrainUtil = ContinentalnessChart;
                    player.sendMessage("Using Continentalness Chart for terrain generation.");
                }
            }
        }.runTaskTimer(DimensionalDescent.getInstance(), 0L, 25L);
    }



}
