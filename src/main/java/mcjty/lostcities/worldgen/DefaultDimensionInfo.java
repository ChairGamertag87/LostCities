package mcjty.lostcities.worldgen;

import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import mcjty.lostcities.worldgen.lost.cityassets.WorldStyle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkSource;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class DefaultDimensionInfo implements IDimensionInfo {

    private WorldGenLevel world;
    private final LostCityProfile profile;
    private final LostCityProfile profileOutside;
    private final WorldStyle style;

    private final Random random;

    private final Registry<Biome> biomeRegistry;
    private final LostCityTerrainFeature feature;

    public DefaultDimensionInfo(WorldGenLevel world, LostCityProfile profile, LostCityProfile profileOutside) {
        this.world = world;
        this.profile = profile;
        this.profileOutside = profileOutside;
        style = AssetRegistries.WORLDSTYLES.get(world, profile.getWorldStyle());
        random = new Random(world.getSeed());
        feature = new LostCityTerrainFeature(this, profile, random);
        feature.setupStates(profile);
        biomeRegistry = RegistryAccess.builtinCopy().registry(Registry.BIOME_REGISTRY).get();
    }

    @Override
    public void setWorld(WorldGenLevel world) {
        this.world = world;
    }

    @Override
    public long getSeed() {
        return world.getSeed();
    }

    @Override
    public WorldGenLevel getWorld() {
        return world;
    }

    @Override
    public ResourceKey<Level> getType() {
        return world.getLevel().dimension();
    }

    @Override
    public LostCityProfile getProfile() {
        return profile;
    }

    @Override
    public LostCityProfile getOutsideProfile() {
        return profileOutside;
    }

    @Override
    public WorldStyle getWorldStyle() {
        return style;
    }

    @Override
    public Random getRandom() {
        return world.getRandom();
    }

    @Override
    public LostCityTerrainFeature getFeature() {
        return feature;
    }

    @Override
    public ChunkHeightmap getHeightmap(int chunkX, int chunkZ) {
        ChunkCoord coord = new ChunkCoord(getType(), chunkX, chunkZ);
        return feature.getHeightmap(coord, getWorld());
    }

    @Override
    public ChunkHeightmap getHeightmap(ChunkCoord coord) {
        return feature.getHeightmap(coord, getWorld());
    }

    //    @Override
//    public Biome[] getBiomes(int chunkX, int chunkZ) {
//        AbstractChunkProvider chunkProvider = getWorld().getChunkProvider();
//        if (chunkProvider instanceof ServerChunkProvider) {
//            BiomeProvider biomeProvider = ((ServerChunkProvider) chunkProvider).getChunkGenerator().getBiomeProvider();
//            return biomeProvider.getBiomes((chunkX - 1) * 4 - 2, chunkZ * 4 - 2, 10, 10, false);
//        }
//    }
//
    @Override
    public Holder<Biome> getBiome(BlockPos pos) {
        ChunkSource chunkProvider = getWorld().getChunkSource();
        if (chunkProvider instanceof ServerChunkCache) {
            ChunkGenerator generator = ((ServerChunkCache) chunkProvider).getGenerator();
            BiomeSource biomeProvider = generator.getBiomeSource();
            return biomeProvider.getNoiseBiome(pos.getX() >> 2, pos.getY() >> 2, pos.getZ() >> 2, generator.climateSampler());
        }
        return biomeRegistry.getHolderOrThrow(Biomes.PLAINS);
    }

    @Nullable
    @Override
    public ResourceKey<Level> dimension() {
        return world.getLevel().dimension();
    }
}
