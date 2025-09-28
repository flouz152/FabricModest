package com.slayvisual.mixin;

import com.slayvisual.config.SlayvisualConfig;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {
        @Inject(method = "renderFirstPersonItem", at = @At("HEAD"))
        private void slayvisual$applyViewModel(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand,
                        float swingProgress, ItemStack stack, float equipProgress, MatrixStack matrices,
                        VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
                if (hand != Hand.MAIN_HAND) {
                        return;
                }

                applyViewModelOffset(matrices);
                applySwordAnimation(matrices, stack, swingProgress);
        }

        private void applyViewModelOffset(MatrixStack matrices) {
                if (!SlayvisualConfig.VISUAL.isViewModelEnabled()) {
                        return;
                }

                matrices.translate(SlayvisualConfig.VISUAL.getViewModelOffsetX(),
                                SlayvisualConfig.VISUAL.getViewModelOffsetY(),
                                SlayvisualConfig.VISUAL.getViewModelOffsetZ());
                float scale = SlayvisualConfig.VISUAL.getViewModelScale();
                matrices.scale(scale, scale, scale);
        }

        private void applySwordAnimation(MatrixStack matrices, ItemStack stack, float swingProgress) {
                if (!(stack.getItem() instanceof SwordItem) && !(stack.getItem() instanceof AxeItem)) {
                        return;
                }

                SlayvisualConfig.SwordAnimation animation = SlayvisualConfig.VISUAL.getSwordAnimation();
                float sine = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);

                switch (animation) {
                        case CELESTIAL:
                                matrices.translate(0.0f, -0.05f * sine, 0.0f);
                                matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(8.0f * sine));
                                break;
                        case CASCADE:
                                matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(-18.0f * sine));
                                matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(12.0f * sine));
                                break;
                        case NOVA:
                                matrices.translate(0.02f * sine, 0.0f, 0.02f * sine);
                                matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-16.0f * sine));
                                break;
                        case VORTEX:
                                matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(24.0f * sine));
                                matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(6.0f * sine));
                                matrices.translate(0.0f, 0.0f, -0.04f * sine);
                                break;
                        case HORIZON:
                                matrices.translate(0.0f, 0.03f * sine, -0.06f * sine);
                                matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(-18.0f * sine));
                                break;
                        default:
                                break;
                }
        }
}
