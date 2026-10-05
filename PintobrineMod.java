package com.pintobrine.core;

import com.pintobrine.client.PintobrineRenderer;
import com.pintobrine.entity.PintobrineEntity;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(PintobrineMod.MODID)
public class PintobrineMod {
    public static final String MODID = "pintobrine";
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final RegistryObject<EntityType<PintobrineEntity>> PINTOBRINE = ENTITIES.register("pintobrine", () ->
            EntityType.Builder.of(PintobrineEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.95f).clientTrackingRange(64).updateInterval(2).build("pintobrine"));

    public PintobrineMod() {
        IEventBus bus = net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus();
        ENTITIES.register(bus);
        bus.addListener(this::attributes);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> bus.addListener(PintobrineMod::registerRenderers));
        MinecraftForge.EVENT_BUS.register(new PintobrineEvents());
    }

    private void attributes(EntityAttributeCreationEvent e) {
        AttributeSupplier.Builder b = net.minecraft.world.entity.Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 36.0).add(Attributes.MOVEMENT_SPEED, 0.29)
                .add(Attributes.ATTACK_DAMAGE, 7.0).add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.25);
        e.put(PINTOBRINE.get(), b.build());
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(PINTOBRINE.get(), PintobrineRenderer::new);
    }
}
