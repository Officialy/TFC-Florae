package tfcflorae.world.layer;

import net.dries007.tfc.world.layer.framework.AreaContext;
import net.dries007.tfc.world.layer.framework.CenterTransformLayer;
import tfcflorae.interfaces.TFCLayersMixinInterface;

import static net.dries007.tfc.world.layer.TFCLayers.*;

import net.dries007.tfc.world.layer.TFCLayers;

public enum SpecialOceanLayer implements CenterTransformLayer
{
    INSTANCE;

    public static TFCLayers staticBiomes = new TFCLayers();

    static final int PELAGIC_ZONES_MARKER = ((TFCLayersMixinInterface) (Object) staticBiomes).getStaticPelagicZoneMarker();
    static final int PELAGIC_ZONE = ((TFCLayersMixinInterface) (Object) staticBiomes).getStaticPelagicZone();
    static final int GUYOTS_MARKER = ((TFCLayersMixinInterface) (Object) staticBiomes).getStaticGuyotsMarker();
    static final int GUYOTS = ((TFCLayersMixinInterface) (Object) staticBiomes).getStaticGuyots();
    static final int SEAMOUNTS_MARKER = ((TFCLayersMixinInterface) (Object) staticBiomes).getStaticSeamountsMarker();
    static final int SEAMOUNTS = ((TFCLayersMixinInterface) (Object) staticBiomes).getStaticSeamounts();
    static final int BARRIER_REEF_MARKER = ((TFCLayersMixinInterface) (Object) staticBiomes).getStaticBarrierReefMarker();
    static final int BARRIER_REEF = ((TFCLayersMixinInterface) (Object) staticBiomes).getStaticBarrierReef();
    static final int LAGOON = ((TFCLayersMixinInterface) (Object) staticBiomes).getStaticLagoon();
    static final int LAGOON_MARKER = ((TFCLayersMixinInterface) (Object) staticBiomes).getStaticLagoonMarker();
    static final int ATOLL = ((TFCLayersMixinInterface) (Object) staticBiomes).getStaticAtoll();
    static final int ATOLL_MARKER = ((TFCLayersMixinInterface) (Object) staticBiomes).getStaticAtollMarker();
    static final int NEAR_SHORE = ((TFCLayersMixinInterface) (Object) staticBiomes).getStaticNearShore();

    @Override
    public int apply(AreaContext context, int value)
    {
        /*if (value == OCEAN_OCEAN_CONVERGING_MARKER)
        {
            // Ocean - Ocean Converging creates volcanic island chains on this marker
            final int r = context.random().nextInt(20);
            if (r <= 1)
            {
                return VOLCANIC_OCEANIC_MOUNTAINS;
            }
            else if (r == 2)
            {
                return OCEAN_REEF;
            }
            return OCEAN;
        }*/
        /*else if (value == OCEAN_OCEAN_DIVERGING_MARKER)
        {
            // Ocean - Ocean Diverging creates mid-ocean ridges, which become ocean biomes (shallow areas)
            // RandomSource chance for small non-volcanic islands (plains)
            final int r = context.random().nextInt(30);
            if (r == 0)
            {
                return PLAINS;
            }
            return OCEAN;
        }
        else*/ if (value == DEEP_OCEAN)
        {
            // Deep Oceans have a chance for a volcanic hotspot
            final int r = context.random().nextInt(250);
            if (r == 0)
            {
                return VOLCANIC_OCEANIC_MOUNTAINS;
            }
            else if (r > 0 && r <= 21)
            {
                return PELAGIC_ZONE;
            }
        }
        else if (value == PELAGIC_ZONE)
        {
            final int r = context.random().nextInt(250);
            if (r >= 0 && r <= 10)
            {
                return GUYOTS;
            }
            else if (r >= 11 && r <= 21)
            {
                return SEAMOUNTS;
            }
        }
       /* else if (value == OCEAN)
        {
            // All oceans are initially marked as reefs, as many other oceans will be added in this phase
            // We then go back and prune this via the marker, to not appear too close to shores
            return OCEAN_REEF_MARKER;
        }*/
        return value;
    }
}
