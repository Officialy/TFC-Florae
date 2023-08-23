package tfcflorae.world.layer;

import net.dries007.tfc.world.layer.framework.AreaContext;
import net.dries007.tfc.world.layer.framework.CenterTransformLayer;
import net.dries007.tfc.world.layer.TFCLayers;

import tfcflorae.interfaces.TFCLayersMixinInterface;

import static net.dries007.tfc.world.layer.TFCLayers.*;

public enum AddGuyotsLayer implements CenterTransformLayer
{
    //SMALL(40),
    //LARGE(160);
    SMALL(1),
    LARGE(6);

    private final int chance;

    public static TFCLayers staticBiomes = new TFCLayers();

    static final int GUYOTS_MARKER = ((TFCLayersMixinInterface) (Object) staticBiomes).getStaticGuyotsMarker();
    static final int PELAGIC_ZONE = ((TFCLayersMixinInterface) (Object) staticBiomes).getStaticPelagicZone();
    static final int NEAR_SHORE = ((TFCLayersMixinInterface) (Object) staticBiomes).getStaticNearShore();

    AddGuyotsLayer(int chance)
    {
        this.chance = chance;
    }

    @Override
    public int apply(AreaContext context, int value)
    {
        //if ((isOceanOrMarker(value) || value != INLAND_MARKER) && !(value == OCEAN || value == OCEAN_REEF || value == NEAR_SHORE || value == SHORE) && context.random().nextInt(chance) == 0)
        /*if (isOceanOrMarker(value) && !(value == OCEAN || value == OCEAN_REEF || value == NEAR_SHORE || value == SHORE) && context.random().nextInt(chance) == 0)
        {
            return GUYOTS_MARKER;
        }
        return value;*/

        if (value == OCEAN_OCEAN_DIVERGING_MARKER)
        {
            final int r = context.random().nextInt(10);
            if (r <= chance)
            {
                return GUYOTS_MARKER;
            }
        }
        else if (value == DEEP_OCEAN)
        {
            final int r = context.random().nextInt(20);
            if (r <= chance)
            {
                return GUYOTS_MARKER;
            }
        }
        return value;
    }
}