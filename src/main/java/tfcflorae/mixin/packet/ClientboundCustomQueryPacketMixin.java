package tfcflorae.mixin.packet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;

@Mixin(ClientboundCustomQueryPacket.class)
public class ClientboundCustomQueryPacketMixin
{
    @ModifyConstant(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", constant = @Constant(intValue = 1048576))
    private int injected(int value)
    {
        return value*10;
    }
}