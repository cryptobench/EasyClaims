package com.landclaims.map;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.worldmap.IWorldMap;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapLoadException;
import com.hypixel.hytale.server.core.universe.world.worldmap.provider.IWorldMapProvider;

/**
 * World map provider that renders claim overlays on the map.
 * Based on SimpleClaims' approach of implementing IWorldMapProvider.
 */
public class LandClaimsWorldMapProvider implements IWorldMapProvider {
    public static final String ID = "LandClaims";
    public static final BuilderCodec<LandClaimsWorldMapProvider> CODEC = BuilderCodec.builder(
            LandClaimsWorldMapProvider.class, LandClaimsWorldMapProvider::new).build();

    @Override
    public IWorldMap getGenerator(World world) throws WorldMapLoadException {
        return LandClaimsChunkWorldMap.INSTANCE;
    }
}
