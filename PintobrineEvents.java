package com.pintobrine.core;

import com.pintobrine.entity.PintobrineEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PintobrineEvents {
    @SubscribeEvent
    public void serverTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        for (ServerLevel level : event.getServer().getAllLevels()) tickLevel(level);
    }

    private void tickLevel(ServerLevel level) {
        long time = level.getGameTime();
        if (time % 100 != 0) return;
        if (level.isDay()) return;
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) continue;
            if (level.random.nextFloat() > 0.055f) continue;
            AABB area = player.getBoundingBox().inflate(48);
            if (!level.getEntitiesOfClass(PintobrineEntity.class, area).isEmpty()) continue;
            double angle = level.random.nextDouble() * Math.PI * 2;
            double dist = 26 + level.random.nextDouble() * 18;
            int x = (int)Math.floor(player.getX() + Math.cos(angle) * dist);
            int z = (int)Math.floor(player.getZ() + Math.sin(angle) * dist);
            int y = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new net.minecraft.core.BlockPos(x, 0, z)).getY();
            if (y <= level.getMinBuildHeight()) continue;
            PintobrineEntity p = PintobrineEntity.spawnAt(level, x + 0.5, y, z + 0.5, player);
            if (p != null) level.addFreshEntity(p);
        }
    }
}
