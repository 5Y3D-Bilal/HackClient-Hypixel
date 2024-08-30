package keystrokesmod.mixins.impl.entity;

import keystrokesmod.event.PrePlayerInputEvent;
import keystrokesmod.event.SafeWalkEvent;
import keystrokesmod.module.impl.other.RotationHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @Redirect(method = "moveEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isSneaking()Z"))
    public boolean onSafeWalk(@NotNull Entity instance) {
        SafeWalkEvent event = new SafeWalkEvent(instance.isSneaking());
        MinecraftForge.EVENT_BUS.post(event);
        return event.isSafeWalk();
    }

    /**
     * @author strangerrs
     * @reason moveFlying mixin
     */
    @Inject(method = "moveFlying", at = @At("HEAD"), cancellable = true)
    public void moveFlying(float p_moveFlying_1_, float p_moveFlying_2_, float p_moveFlying_3_, CallbackInfo ci) {
        float yaw = ((Entity)(Object) this).rotationYaw;
        if((Object) this instanceof EntityPlayerSP) {
            PrePlayerInputEvent prePlayerInput = new PrePlayerInputEvent(p_moveFlying_1_, p_moveFlying_2_, p_moveFlying_3_, RotationHandler.getMovementYaw((Entity) (Object) this));
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(prePlayerInput);
            if (prePlayerInput.isCanceled()) {
                return;
            }
            p_moveFlying_1_ = prePlayerInput.getStrafe();
            p_moveFlying_2_ = prePlayerInput.getForward();
            p_moveFlying_3_ = prePlayerInput.getFriction();
            yaw = prePlayerInput.getYaw();
        }

        float f = p_moveFlying_1_ * p_moveFlying_1_ + p_moveFlying_2_ * p_moveFlying_2_;
        if (f >= 1.0E-4F) {
            f = MathHelper.sqrt_float(f);
            if (f < 1.0F) {
                f = 1.0F;
            }

            f = p_moveFlying_3_ / f;
            p_moveFlying_1_ *= f;
            p_moveFlying_2_ *= f;
            float f1 = MathHelper.sin(yaw * 3.1415927F / 180.0F);
            float f2 = MathHelper.cos(yaw * 3.1415927F / 180.0F);
            ((Entity)(Object) this).motionX += p_moveFlying_1_ * f2 - p_moveFlying_2_ * f1;
            ((Entity)(Object) this).motionZ += p_moveFlying_2_ * f2 + p_moveFlying_1_ * f1;
        }
        ci.cancel();
    }

    @Redirect(method = "rayTrace", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getLook(F)Lnet/minecraft/util/Vec3;"))
    public Vec3 onGetLook(Entity instance, float partialTicks) {
        return RotationHandler.getLook(partialTicks);
    }
}