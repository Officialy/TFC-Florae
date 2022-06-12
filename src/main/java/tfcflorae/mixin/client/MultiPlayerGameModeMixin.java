package tfcflorae.mixin.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import tfcflorae.util.TFCFInteractionManager;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin
{
    @Inject(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;"), require = 2, cancellable = true)
    private void inject$useItemOn(LocalPlayer player, ClientLevel level, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir)
    {
        final ItemStack stack = player.getItemInHand(hand);
        final int startCount = stack.getCount();
        final UseOnContext itemContext = new UseOnContext(player, hand, hitResult);
        TFCFInteractionManager.onItemUse(stack, itemContext, false).ifPresent(result -> {
            if (player.isCreative())
            {
                stack.setCount(startCount);
            }
            cir.setReturnValue(result);
        });
    }
}
