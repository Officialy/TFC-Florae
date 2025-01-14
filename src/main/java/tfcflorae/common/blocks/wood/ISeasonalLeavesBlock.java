package tfcflorae.common.blocks.wood;



import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;

/**
 * Marker interface for common bush-type blocks.
 * These do random tick updates that are on average 1/day, and use time tracking to implement fast forwarding.
 */
public interface ISeasonalLeavesBlock
{
    /**
     * Target average delay between random ticks = one day.
     * Ticks are done at a rate of randomTickSpeed ticks / chunk section / world tick.
     *
     * Only implement if ticking is not desired every tick, ie that we're OK with staggered updating.
     */
    static void randomTick(ISeasonalLeavesBlock bush, BlockState state, ServerLevel level, BlockPos pos, RandomSource random)
    {
        final int rarity = Math.max(1, (int) (ICalendar.TICKS_IN_DAY * (((level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING)) * (1 / 4096f)) * ((Calendars.SERVER.getCalendarDaysInMonth() / 15) + 1))));
        //final int rarity = Math.max(1, (int) (ICalendar.TICKS_IN_DAY * level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING) * (1 / 4096f)));
        if (random.nextInt(rarity) == 0)
        { 
            bush.onUpdate(level, pos, state);
        }
    }

    void onUpdate(Level level, BlockPos pos, BlockState state);
}
