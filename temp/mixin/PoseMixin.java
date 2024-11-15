package tfcflorae.mixin;

import net.minecraft.world.entity.Pose;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tfcflorae.common.entities.access.api.Poses;

@Mixin(Pose.class)
public class PoseMixin
{
    @Shadow @Mutable @Final private static Pose[] $VALUES;

    @Invoker("<init>")
    public static Pose create(String name, int id)
    {
        throw new AssertionError();
    }

    @Inject(method = "<clinit>", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/Pose;$VALUES:[Lnet/minecraft/world/entity/Pose;", shift = At.Shift.AFTER))
    private static void wb$addPose(CallbackInfo ci)
    {
        List<Pose> poses = new ArrayList<>(Arrays.asList($VALUES));
        Pose last = poses.get(poses.size() - 1);
        int i = 1;
        for (Poses pose : Poses.values())
        {
            poses.add(create(pose.name(), last.ordinal() + i));
            i++;
        }

        $VALUES = poses.toArray(new Pose[0]);
    }
}