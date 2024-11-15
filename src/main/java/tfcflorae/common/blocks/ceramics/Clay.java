package tfcflorae.common.blocks.ceramics;

import java.awt.Color;
import java.util.Locale;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;

import net.minecraft.world.level.material.MapColor;

import tfcflorae.util.registry.RegistryClay;

public enum Clay implements RegistryClay
{
    EARTHENWARE(new Color(143, 70, 47).getRGB(), MapColor.TERRACOTTA_RED, false),
    KAOLINITE(new Color(168, 155, 139).getRGB(), MapColor.TERRACOTTA_LIGHT_GRAY, false),
    STONEWARE(new Color(103, 99, 98).getRGB(), MapColor.TERRACOTTA_GRAY, false);

    public static final Clay[] VALUES = values();

    public static Clay valueOf(int i)
    {
        return i >= 0 && i < VALUES.length ? VALUES[i] : EARTHENWARE;
    }

    public final String serializedName;
    public final int dustColor;
    public final MapColor materialColor;
    private final boolean hasRock;

    Clay(int dustColor, MapColor materialColor, boolean hasRock)
    {
        this.serializedName = name().toLowerCase(Locale.ROOT);
        this.dustColor = dustColor;
        this.materialColor = materialColor;
        this.hasRock = hasRock;
    }

    @Override
    public String getSerializedName()
    {
        return serializedName;
    }

    public MapColor getMaterialColor()
    {
        return materialColor;
    }

    public boolean hasRock()
    {
        return hasRock;
    }

    public Block create()
    {
        return new Block(Block.Properties.copy(Blocks.STONE).sound(SoundType.STONE).strength(5.5f, 9).noCollission().requiresCorrectToolForDrops());
    }
}
