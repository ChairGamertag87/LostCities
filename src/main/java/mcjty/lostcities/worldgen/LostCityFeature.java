package mcjty.lostcities.worldgen;

import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.setup.Config;
import mcjty.lostcities.setup.Registration;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class LostCityFeature extends Feature<NoneFeatureConfiguration> {

    /**
     * On dedicated servers the dimensionInfo cache is no problem. The server starts only once
     * and will have the correct dimension info and for the clients it doesn't matter.
     * However, to make sure that on a single player world this cache is cleared when the player
     * exits the world and creates a new one we keep a static flag which is incremented whenever
     * the player exits the world. That is then used to help clear this cache
     */
    private final Map<ResourceKey<Level>, IDimensionInfo> dimensionInfo = new HashMap<>();
    public static int globalDimensionInfoDirtyCounter = 0;
    private int dimensionInfoDirtyCounter = -1;

    public static PlacedFeature LOSTCITY_CONFIGURED_FEATURE;

    public static void registerConfiguredFeatures() {
        LOSTCITY_CONFIGURED_FEATURE = registerPlacedFeature("configured_feature", Registration.LOSTCITY_FEATURE.configured(NoneFeatureConfiguration.INSTANCE),
                CountPlacement.of(1));
    }

    public LostCityFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    private static <C extends FeatureConfiguration, F extends Feature<C>> PlacedFeature registerPlacedFeature(String registryName, ConfiguredFeature<C, F> feature, PlacementModifier... placementModifiers) {
        PlacedFeature placed = BuiltinRegistries.register(BuiltinRegistries.CONFIGURED_FEATURE, new ResourceLocation(registryName), feature).placed(placementModifiers);
        return PlacementUtils.register(registryName, placed);
    }


    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        if (level instanceof WorldGenRegion) {
            IDimensionInfo diminfo = getDimensionInfo(level);
            if (diminfo != null) {
                WorldGenRegion region = (WorldGenRegion) level;
                ChunkPos center = region.getCenter();
                int chunkX = center.x;
                int chunkZ = center.z;
                diminfo.setWorld(level);
//                generator.getBiomeProvider() ->OverworldBiomeProvider
//                diminfo.getFeature().generateDummy(region, region.getChunk(chunkX, chunkZ));
                diminfo.getFeature().generate(region, region.getChunk(chunkX, chunkZ));
                return true;
            }
        }
        return false;
    }

    @Nullable
    public IDimensionInfo getDimensionInfo(WorldGenLevel world) {
        if (globalDimensionInfoDirtyCounter != dimensionInfoDirtyCounter) {
            // Force clear of cache
            dimensionInfo.clear();
            dimensionInfoDirtyCounter = globalDimensionInfoDirtyCounter;
        }
        ResourceKey<Level> type = world.getLevel().dimension();
        String profileName = Config.getProfileForDimension(type);
        if (profileName != null) {
            if (!dimensionInfo.containsKey(type)) {
                LostCityProfile profile = LostCityConfiguration.standardProfiles.get(profileName);
                IDimensionInfo diminfo = new DefaultDimensionInfo(world, profile);
                dimensionInfo.put(type, diminfo);
            }
            return dimensionInfo.get(type);
        }
        return null;
    }
}
