package com.pintobrine.entity;

import com.pintobrine.core.PintobrineMod;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class PintobrineEntity extends PathfinderMob {
    public enum State { OBSERVING, FOLLOWING, INVADING, CHASING, TROLLING, HUNTING }
    private float consciousness = 0f;
    private State state = State.OBSERVING;
    private UUID targetId;
    private int seenTicks = 0;
    private int stationaryTicks = 0;
    private BlockPos rememberedBase;
    private Vec3 lastTargetPos;

    public PintobrineEntity(EntityType<? extends PathfinderMob> type, Level level) { super(type, level); setPersistenceRequired(); }

    public static PintobrineEntity spawnAt(Level level, double x, double y, double z, ServerPlayer target) {
        PintobrineEntity e = new PintobrineEntity(PintobrineMod.PINTOBRINE.get(), level);
        e.moveTo(x, y, z, level.random.nextFloat() * 360f, 0);
        e.targetId = target.getUUID();
        e.getPersistentData().putUUID("Target", target.getUUID());
        return e;
    }

    @Override protected void registerGoals() {
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25, false));
        goalSelector.addGoal(2, new BreakDoorGoal(this, d -> d != net.minecraft.world.Difficulty.PEACEFUL));
        goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 40f));
        goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    @Override public void tick() {
        super.tick();
        if (level().isClientSide) return;
        ServerPlayer target = findTarget();
        if (target == null) return;
        double d = distanceTo(target);
        boolean looking = isPlayerLookingAt(target);
        if (d < 40 && hasLineOfSight(target)) consciousness = Math.min(100f, consciousness + 0.018f);
        if (looking && d < 45) {
            seenTicks++;
            if (consciousness < 55 && seenTicks > 8 && random.nextFloat() < 0.035f) disappear();
        } else seenTicks = Math.max(0, seenTicks - 1);
        boolean moved = lastTargetPos == null || target.position().distanceToSqr(lastTargetPos) > 0.0004D;
        lastTargetPos = target.position();
        if (target.distanceToSqr(this) < 18 * 18 && !moved) stationaryTicks++; else stationaryTicks = 0;
        if (stationaryTicks > 120) rememberedBase = target.blockPosition();
        state = chooseState(d);
        act(target, d, looking);
        if (consciousness > 70 && random.nextFloat() < 0.004f) leaveMark(target);
    }

    private State chooseState(double d) {
        if (consciousness >= 80) return State.HUNTING;
        if (consciousness >= 60 && rememberedBase != null && d > 10) return State.INVADING;
        if (consciousness >= 40 && d < 22) return State.CHASING;
        if (consciousness >= 20) return State.FOLLOWING;
        return State.OBSERVING;
    }

    private void act(ServerPlayer p, double d, boolean looking) {
        if (state == State.OBSERVING) {
            getNavigation().stop();
            getLookControl().setLookAt(p, 20, 20);
            if (d < 16 && looking && random.nextFloat() < 0.01f) disappear();
        } else if (state == State.FOLLOWING) {
            if (d > 13) getNavigation().moveTo(p, 0.78); else getNavigation().stop();
        } else if (state == State.CHASING || state == State.HUNTING) {
            getNavigation().moveTo(p, state == State.HUNTING ? 1.35 : 1.15);
            if (d < 2.3) doHurtTarget(p);
        } else if (state == State.INVADING) {
            if (rememberedBase != null) getNavigation().moveTo(rememberedBase.getX()+0.5, rememberedBase.getY(), rememberedBase.getZ()+0.5, 1.0);
            interactWithBlocks();
            if (d < 14 && !looking && random.nextFloat() < 0.04f) getNavigation().moveTo(p, 1.05);
        } else {
            getNavigation().moveTo(p, 0.55);
        }
    }

    private void interactWithBlocks() {
        BlockPos c = blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(c.offset(-2,-1,-2), c.offset(2,2,2))) {
            BlockState s = level().getBlockState(pos);
            if (s.getBlock() instanceof DoorBlock && s.getValue(DoorBlock.OPEN) == false) {
                level().setBlock(pos, s.setValue(DoorBlock.OPEN, true), 3);
                level().playSound(null, pos, SoundEvents.WOODEN_DOOR_OPEN, getSoundSource(), 1f, 0.7f);
                return;
            }
            if (s.is(Blocks.GLASS) || s.is(Blocks.GLASS_PANE)) {
                if (random.nextFloat() < 0.12f) {
                    level().destroyBlock(pos, false, this);
                    level().playSound(null, pos, SoundEvents.GLASS_BREAK, getSoundSource(), 1f, 0.6f);
                    return;
                }
            }
        }
    }

    private void leaveMark(ServerPlayer p) {
        BlockPos pos = p.blockPosition().below();
        if (level().getBlockState(pos).isAir()) return;
        if (random.nextFloat() < 0.5f) level().playSound(null, pos, SoundEvents.NOTE_BLOCK_BASS.value(), getSoundSource(), 0.35f, 0.45f);
    }

    private ServerPlayer findTarget() {
        if (targetId != null && level().getPlayerByUUID(targetId) instanceof ServerPlayer sp && sp.isAlive()) return sp;
        Player np = level().getNearestPlayer(this, 64);
        if (np instanceof ServerPlayer p) {
            targetId = p.getUUID();
            return p;
        }
        return null;
    }

    private boolean isPlayerLookingAt(Player p) {
        Vec3 look = p.getViewVector(1f).normalize();
        Vec3 toMe = position().add(0, getEyeHeight()*0.8, 0).subtract(p.getEyePosition()).normalize();
        return look.dot(toMe) > 0.965 && p.hasLineOfSight(this);
    }

    private void disappear() {
        playSound(SoundEvents.ENDERMAN_TELEPORT, 0.5f, 0.65f + random.nextFloat()*0.2f);
        discard();
    }

    public float getConsciousness() { return consciousness; }
    public State getState() { return state; }

    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag); tag.putFloat("Consciousness", consciousness); if (targetId != null) tag.putUUID("Target", targetId);
        if (rememberedBase != null) { tag.putInt("BaseX", rememberedBase.getX()); tag.putInt("BaseY", rememberedBase.getY()); tag.putInt("BaseZ", rememberedBase.getZ()); }
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag); consciousness = tag.getFloat("Consciousness"); if (tag.hasUUID("Target")) targetId = tag.getUUID("Target");
        if (tag.contains("BaseX")) rememberedBase = new BlockPos(tag.getInt("BaseX"), tag.getInt("BaseY"), tag.getInt("BaseZ"));
    }
    @Override protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) { }
}
