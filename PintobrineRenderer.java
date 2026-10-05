package com.pintobrine.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pintobrine.entity.PintobrineEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class PintobrineRenderer extends MobRenderer<PintobrineEntity, HumanoidModel<PintobrineEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("pintobrine", "textures/entity/pintobrine.png");
    public PintobrineRenderer(EntityRendererProvider.Context ctx) { super(ctx, new HumanoidModel<>(ctx.bakeLayer(ModelLayers.PLAYER)), 0.5f); }
    @Override public ResourceLocation getTextureLocation(PintobrineEntity e) { return TEXTURE; }
    @Override protected void scale(PintobrineEntity e, PoseStack pose, float partial) { float s = 1.02f; pose.scale(s, s, s); }
}
