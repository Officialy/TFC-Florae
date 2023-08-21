package tfcflorae.common.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

import net.dries007.tfc.common.entities.aquatic.TFCTropicalFish;

public class TadpoleBucketItem extends BucketItem
{
    private final Supplier<? extends EntityType<?>> entityType;
    private final Supplier<? extends SoundEvent> emptySound;

    public TadpoleBucketItem(Supplier<? extends EntityType<?>> entityType, Fluid fluid, Supplier<? extends SoundEvent> emptySound, Item.Properties properties)
    {
        super(fluid, properties);
        this.entityType = entityType;
        this.emptySound = emptySound;
    }

    public void checkExtraContent(@Nullable Player player, Level level, ItemStack stack, BlockPos pos)
    {
        if (level instanceof ServerLevel server)
        {
            this.spawn(server, stack, pos);
            level.gameEvent(player, GameEvent.ENTITY_PLACE, pos);
        }
    }

    protected void playEmptySound(@Nullable Player player, LevelAccessor access, BlockPos pos)
    {
        access.playSound(player, pos, this.emptySound.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
    }

    private void spawn(ServerLevel level, ItemStack stack, BlockPos pos)
    {
        Entity entity = this.entityType.get().spawn(level, stack, null, pos, MobSpawnType.BUCKET, true, false);
        if (entity instanceof Bucketable bucketable)
        {
            bucketable.loadFromBucketTag(stack.getOrCreateTag());
            bucketable.setFromBucket(true);
        }
    }

    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag tooltip)
    {
        if (this.entityType.get() == EntityType.TROPICAL_FISH)
        {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("BucketVariantTag", 3))
            {
                int variant = tag.getInt("BucketVariantTag");
                ChatFormatting[] format = new ChatFormatting[]{ChatFormatting.ITALIC, ChatFormatting.GRAY};
                String base = "color.minecraft." + TFCTropicalFish.getBaseColor(variant);
                String pattern = "color.minecraft." + TFCTropicalFish.getPatternColor(variant);

                for(int i = 0; i < TFCTropicalFish.COMMON_VARIANTS.length; ++i)
                {
                    if (variant == TFCTropicalFish.COMMON_VARIANTS[i])
                    {
                        components.add((new TranslatableComponent(TFCTropicalFish.getPredefinedName(i))).withStyle(format));
                        return;
                    }
                }

                components.add(new TranslatableComponent(TFCTropicalFish.getFishTypeName(variant)).withStyle(format));
                MutableComponent component = new TranslatableComponent(base);
                if (!base.equals(pattern)) component.append(", ").append(new TranslatableComponent(pattern));

                component.withStyle(format);
                components.add(component);
            }
        }
    }
}