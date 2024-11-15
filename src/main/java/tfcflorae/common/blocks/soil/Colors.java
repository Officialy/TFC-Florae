package tfcflorae.common.blocks.soil;

import java.awt.*;
import java.util.Locale;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

import net.dries007.tfc.common.blocks.soil.SandBlockType;
import net.dries007.tfc.common.blocks.soil.TFCSandBlock;

import static net.minecraft.world.level.material.MapColor.*;

public enum Colors implements StringRepresentable
{
    BLACK(new Color(56, 56, 56).getRGB(), MapColor.TERRACOTTA_BLACK, true, false, true),
    BLUE(new Color(50, 68, 255).getRGB(), MapColor.TERRACOTTA_BLUE, false, true, true),
    BROWN(new Color(112, 113, 89).getRGB(), MapColor.TERRACOTTA_BROWN, true, false, true),
    CYAN(new Color(73, 198, 198).getRGB(), MapColor.TERRACOTTA_CYAN, false, true, false),
    GRAY(new Color(112, 112, 112).getRGB(), MapColor.TERRACOTTA_GRAY, false, true, true),
    GREEN(new Color(106, 116, 81).getRGB(), MapColor.TERRACOTTA_GREEN, true, false, true),
    LIGHT_BLUE(new Color(85, 162, 214).getRGB(), MapColor.TERRACOTTA_LIGHT_BLUE, false, true, false),
    LIGHT_GRAY(new Color(175, 175, 175).getRGB(), MapColor.TERRACOTTA_LIGHT_GRAY, false, true, false),
    LIGHT_GREEN(new Color(136, 132, 61).getRGB(), MapColor.TERRACOTTA_LIGHT_GREEN, false, true, true),
    MAGENTA(new Color(165, 0, 165).getRGB(), MapColor.TERRACOTTA_MAGENTA, false, true, false),
    ORANGE(new Color(190, 109, 56).getRGB(), MapColor.TERRACOTTA_ORANGE, false, true, true),
    PINK(new Color(150, 101, 97).getRGB(), MapColor.TERRACOTTA_PINK, true, false, true),
    PURPLE(new Color(116, 73, 94).getRGB(), MapColor.TERRACOTTA_PURPLE, false, true, true),
    RED(new Color(125, 99, 84).getRGB(), MapColor.TERRACOTTA_RED, true, false, true),
    WHITE(new Color(202, 202, 201).getRGB(), MapColor.TERRACOTTA_WHITE, true, false, true),
    YELLOW(new Color(215, 196, 140).getRGB(), MapColor.TERRACOTTA_YELLOW, true, false, true);

    public static final Colors[] VALUES = values();

    public static Colors valueOf(int i)
    {
        return i >= 0 && i < VALUES.length ? VALUES[i] : BLACK;
    }

    public final int dustColor;
    public final MapColor mapColor;
    public final boolean hasSand;
    public final boolean hasSandNew;
    public final boolean hasLayered;
    private final String serializedName;

    Colors(int dustColor, MapColor MapColor, boolean hasSand, boolean hasSandNew, boolean hasLayered)
    {
        this.dustColor = dustColor;
        this.mapColor = MapColor;
        this.hasSand = hasSand;
        this.hasSandNew = hasSandNew;
        this.hasLayered = hasLayered;
        this.serializedName = name().toLowerCase(Locale.ROOT);
    }

    @Override
    public String toString()
    {
        return serializedName;
    }

    @Override
    public String getSerializedName()
    {
        return serializedName;
    }

    public int getDustColor()
    {
        return dustColor;
    }

    public MapColor getMapColor()
    {
        return mapColor;
    }

    public Block create()
    {
        return new TFCSandBlock(getDustColor(), BlockBehaviour.Properties.copy(Blocks.SAND).mapColor(getMapColor()).strength(1F).sound(SoundType.ANCIENT_DEBRIS));
    }

    public boolean hasSandTFC()
    {
        return hasSand;
    }

    public boolean hasSandNew()
    {
        return hasSandNew;
    }

    public boolean hasLayered()
    {
        return hasLayered;
    }

    /*public static Colors fromTFCF(TFCFSandBlockType sandColor)
    {
        switch (sandColor)
        {
            case BLACK:
                return Colors.BLACK;
            case BLUE:
                return Colors.BLUE;
            case BROWN:
                return Colors.BROWN;
            case GRAY:
                return Colors.GRAY;
            case GREEN:
                return Colors.GREEN;
            case LIGHT_GREEN:
                return Colors.LIGHT_GREEN;
            case ORANGE:
                return Colors.ORANGE;
            case PINK:
                return Colors.PINK;
            case PURPLE:
                return Colors.PURPLE;
            case RED:
                return Colors.RED;
            case WHITE:
                return Colors.WHITE;
            case YELLOW:
                return Colors.YELLOW;
            default:
                return Colors.YELLOW;
        }
    }*/

    public static Colors fromMaterialColour(MapColor MapColor)
    {
        if (MapColor == GRASS || MapColor == PLANT || MapColor == COLOR_GREEN || MapColor == EMERALD || MapColor == TERRACOTTA_GREEN)
        {
            return Colors.GREEN;
        }
        else if (MapColor == SAND || MapColor == COLOR_YELLOW || MapColor == GOLD || MapColor == TERRACOTTA_YELLOW || MapColor == RAW_IRON)
        {
            return Colors.YELLOW;
        }
        else if (MapColor == NONE || MapColor == WOOL || MapColor == SNOW || MapColor == QUARTZ || MapColor == TERRACOTTA_WHITE)
        {
            return Colors.WHITE;
        }
        else if (MapColor == FIRE || MapColor == COLOR_ORANGE || MapColor == TERRACOTTA_ORANGE)
        {
            return Colors.ORANGE;
        }
        else if (MapColor == ICE || MapColor == CLAY || MapColor == COLOR_LIGHT_BLUE || MapColor == TERRACOTTA_LIGHT_BLUE)
        {
            return Colors.LIGHT_BLUE;
        }
        else if (MapColor == METAL || MapColor == COLOR_LIGHT_GRAY || MapColor == TERRACOTTA_LIGHT_GRAY)
        {
            return Colors.LIGHT_GRAY;
        }
        else if (MapColor == DIRT || MapColor == WOOD || MapColor == COLOR_BROWN || MapColor == TERRACOTTA_BROWN || MapColor == PODZOL)
        {
            return Colors.BROWN;
        }
        else if (MapColor == STONE || MapColor == COLOR_GRAY || MapColor == TERRACOTTA_GRAY || MapColor == GLOW_LICHEN)
        {
            return Colors.GRAY;
        }
        else if (MapColor == WATER || MapColor == COLOR_BLUE || MapColor == TERRACOTTA_BLUE || MapColor == LAPIS)
        {
            return Colors.BLUE;
        }
        else if (MapColor == COLOR_MAGENTA || MapColor == TERRACOTTA_MAGENTA || MapColor == WARPED_STEM || MapColor == WARPED_HYPHAE)
        {
            return Colors.MAGENTA;
        }
        else if (MapColor == COLOR_LIGHT_GREEN || MapColor == TERRACOTTA_LIGHT_GREEN)
        {
            return Colors.LIGHT_GREEN;
        }
        else if (MapColor == COLOR_PINK || MapColor == TERRACOTTA_PINK)
        {
            return Colors.PINK;
        }
        else if (MapColor == COLOR_CYAN || MapColor == TERRACOTTA_CYAN || MapColor == DIAMOND || MapColor == WARPED_NYLIUM || MapColor == CRIMSON_STEM || MapColor == CRIMSON_HYPHAE)
        {
            return Colors.CYAN;
        }
        else if (MapColor == COLOR_PURPLE || MapColor == TERRACOTTA_PURPLE)
        {
            return Colors.PURPLE;
        }
        else if (MapColor == COLOR_RED || MapColor == TERRACOTTA_RED || MapColor == NETHER || MapColor == CRIMSON_NYLIUM || MapColor == WARPED_WART_BLOCK)
        {
            return Colors.RED;
        }
        else if (MapColor == COLOR_BLACK || MapColor == TERRACOTTA_BLACK || MapColor == DEEPSLATE)
        {
            return Colors.BLACK;
        }
        else
        {
            return Colors.ORANGE;
        }
    }

    public static Colors fromTFC(SandBlockType sandColor)
    {
        return switch (sandColor) {
            case BROWN -> Colors.BROWN;
            case WHITE -> Colors.WHITE;
            case BLACK -> Colors.BLACK;
            case RED -> Colors.RED;
            case YELLOW -> Colors.YELLOW;
            case GREEN -> Colors.GREEN;
            case PINK -> Colors.PINK;
            default -> Colors.ORANGE;
        };
    }

    public static Colors nonTFC(Colors sandColor)
    {
        return switch (sandColor) {
            case BROWN, WHITE, BLACK, RED, YELLOW, GREEN, PINK -> Colors.ORANGE;
            default -> sandColor;
        };
    }

    public Colors nonTFC()
    {
        return switch (this) {
            case BROWN, WHITE, BLACK, RED, YELLOW, GREEN, PINK -> Colors.ORANGE;
            default -> this;
        };
    }

    public static SandBlockType toSandTFC(Colors sandColor, boolean withDefault)
    {
        return switch (sandColor) {
            case BROWN -> SandBlockType.BROWN;
            case WHITE -> SandBlockType.WHITE;
            case BLACK -> SandBlockType.BLACK;
            case RED -> SandBlockType.RED;
            case YELLOW -> SandBlockType.YELLOW;
            case GREEN -> SandBlockType.GREEN;
            case PINK -> SandBlockType.PINK;
            default -> withDefault ? SandBlockType.YELLOW : null;
        };
    }

    public SandBlockType toSandTFC(boolean defaults)
    {
        return switch (this) {
            case BROWN -> SandBlockType.BROWN;
            case WHITE -> SandBlockType.WHITE;
            case BLACK -> SandBlockType.BLACK;
            case RED -> SandBlockType.RED;
            case YELLOW -> SandBlockType.YELLOW;
            case GREEN -> SandBlockType.GREEN;
            case PINK -> SandBlockType.PINK;
            default -> defaults ? SandBlockType.YELLOW : null;
        };
    }

    public static Colors fromDyeColor(DyeColor dyeColor, boolean withDefault)
    {
        return switch (dyeColor) {
            case WHITE -> Colors.WHITE;
            case ORANGE -> Colors.ORANGE;
            case MAGENTA -> Colors.MAGENTA;
            case LIGHT_BLUE -> Colors.LIGHT_BLUE;
            case YELLOW -> Colors.YELLOW;
            case LIME -> Colors.LIGHT_GREEN;
            case PINK -> Colors.PINK;
            case GRAY -> Colors.GRAY;
            case LIGHT_GRAY -> Colors.LIGHT_GRAY;
            case CYAN -> Colors.CYAN;
            case PURPLE -> Colors.PURPLE;
            case BLUE -> Colors.BLUE;
            case BROWN -> Colors.BROWN;
            case GREEN -> Colors.GREEN;
            case RED -> Colors.RED;
            case BLACK -> Colors.BLACK;
            default -> withDefault ? Colors.YELLOW : null;
        };
    }
}