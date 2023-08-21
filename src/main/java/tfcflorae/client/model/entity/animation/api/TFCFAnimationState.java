package tfcflorae.client.model.entity.animation.api;

import java.util.function.Consumer;

import net.minecraft.util.Mth;

public class TFCFAnimationState
{
    private long startedAt = Long.MAX_VALUE;
    private long runningTime;

    public void start(int ticks)
    {
        this.startedAt = (long)ticks * 1000L / 20L;
        this.runningTime = 0L;
    }

    public void startIfNotRunning(int ticks)
    {
        if (!this.isRunning()) this.start(ticks);
    }

    public void stop()
    {
        this.startedAt = Long.MAX_VALUE;
    }

    public void run(Consumer<TFCFAnimationState> consumer)
    {
        if (this.isRunning()) consumer.accept(this);
    }

    public void run(float animationProgress, float speedMultiplier)
    {
        if (this.isRunning())
        {
            long runningAt = Mth.lfloor(animationProgress * 1000.0F / 20.0F);
            this.runningTime += (long)((float)(runningAt - this.startedAt) * speedMultiplier);
            this.startedAt = runningAt;
        }
    }

    public long runningTime()
    {
        return this.runningTime;
    }

    public boolean isRunning()
    {
        return this.startedAt != Long.MAX_VALUE;
    }
}
