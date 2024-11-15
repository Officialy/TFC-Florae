package tfcflorae.world.feature.tree;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TrunkVineDecorator;
import tfcflorae.world.feature.TFCFFeatures;

import java.util.List;

public class BambooLeavesDecorator extends TrunkVineDecorator {
    public static final Codec<BambooLeavesDecorator> CODEC = BlockStateProvider.CODEC.fieldOf("block_provider").xmap(BambooLeavesDecorator::new, decorator -> decorator.inputState).codec();

    private final BlockStateProvider inputState;

    public BambooLeavesDecorator(BlockStateProvider inputState) {
        this.inputState = inputState;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return TFCFFeatures.BAMBOO_LEAVES.get();
    }

    @Override
    public void place(Context context) {

        LevelSimulatedReader levelReader = context.level();
        RandomSource random = context.random();
        List<BlockPos> listBlockPos = context.logs();
        List<BlockPos> listBlockPos2 = context.leaves();

        BlockPos newpos = listBlockPos.get(listBlockPos.size() - 1);
        BlockPos up1 = new BlockPos(newpos.getX(), newpos.getY() + 5, newpos.getZ());
        BlockPos up2 = new BlockPos(newpos.getX(), newpos.getY() + 6, newpos.getZ());
        BlockPos px1 = new BlockPos(newpos.getX() + 2, newpos.getY(), newpos.getZ());
        BlockPos px2 = new BlockPos(newpos.getX() + 2, newpos.getY(), newpos.getZ() + 1);
        BlockPos px3 = new BlockPos(newpos.getX() + 2, newpos.getY(), newpos.getZ() - 1);
        BlockPos nx1 = new BlockPos(newpos.getX() - 2, newpos.getY(), newpos.getZ());
        BlockPos nx2 = new BlockPos(newpos.getX() - 2, newpos.getY(), newpos.getZ() + 1);
        BlockPos nx3 = new BlockPos(newpos.getX() - 2, newpos.getY(), newpos.getZ() - 1);
        BlockPos pz1 = new BlockPos(newpos.getX(), newpos.getY(), newpos.getZ() + 2);
        BlockPos pz2 = new BlockPos(newpos.getX() + 1, newpos.getY(), newpos.getZ() + 2);
        BlockPos pz3 = new BlockPos(newpos.getX() - 1, newpos.getY(), newpos.getZ() + 2);
        BlockPos nz1 = new BlockPos(newpos.getX(), newpos.getY(), newpos.getZ() - 2);
        BlockPos nz2 = new BlockPos(newpos.getX() + 1, newpos.getY(), newpos.getZ() - 2);
        BlockPos nz3 = new BlockPos(newpos.getX() - 1, newpos.getY(), newpos.getZ() - 2);
        BlockPos px = new BlockPos(newpos.getX() + 1, newpos.getY(), newpos.getZ());
        BlockPos nx = new BlockPos(newpos.getX() - 1, newpos.getY(), newpos.getZ());
        BlockPos pz = new BlockPos(newpos.getX(), newpos.getY(), newpos.getZ() + 1);
        BlockPos nz = new BlockPos(newpos.getX(), newpos.getY(), newpos.getZ() - 1);
        BlockPos pxt = px.above();
        BlockPos nxt = nx.above();
        BlockPos pzt = pz.above();
        BlockPos nzt = nz.above();
        BlockPos pxq = pxt.above();
        BlockPos nxq = nxt.above();
        BlockPos pzq = pzt.above();
        BlockPos nzq = nzt.above();
        BlockPos pxc = pxq.above();
        BlockPos nxc = nxq.above();
        BlockPos pzc = pzq.above();
        BlockPos nzc = nzq.above();
        BlockPos pxz = pxc.above();
        BlockPos nxz = nxc.above();
        BlockPos pzz = pzc.above();
        BlockPos nzz = nzc.above();
        BlockPos pxo = px.below();
        BlockPos nxo = nx.below();
        BlockPos pzo = pz.below();
        BlockPos nzo = nz.below();
        BlockPos pxm = pxo.below();
        BlockPos nxm = nxo.below();
        BlockPos pzm = pzo.below();
        BlockPos nzm = nzo.below();
        BlockPos pxp = pxm.below();
        BlockPos nxp = nxm.below();
        BlockPos pzp = pzm.below();
        BlockPos nzp = nzm.below();

        if (context.isAir(px1))
            context.setBlock(px1, inputState.getState(random, px1));
        if (context.isAir(px2))
            context.setBlock(px2, inputState.getState(random, px2));
        if (context.isAir(px3))
            context.setBlock(px3, inputState.getState(random, px3));
        if (context.isAir(nx1))
            context.setBlock(nx1, inputState.getState(random, nx1));
        if (context.isAir(nx2))
            context.setBlock(nx2, inputState.getState(random, nx2));
        if (context.isAir(nx3))
            context.setBlock(nx3, inputState.getState(random, nx3));
        if (context.isAir(pz1))
            context.setBlock(pz1, inputState.getState(random, pz1));
        if (context.isAir(pz2))
            context.setBlock(pz2, inputState.getState(random, pz2));
        if (context.isAir(pz3))
            context.setBlock(pz3, inputState.getState(random, pz3));
        if (context.isAir(nz1))
            context.setBlock(nz1, inputState.getState(random, nz1));
        if (context.isAir(nz2))
            context.setBlock(nz2, inputState.getState(random, nz2));
        if (context.isAir(nz3))
            context.setBlock(nz3, inputState.getState(random, nz3));
        if (context.isAir(px))
            context.setBlock(px, inputState.getState(random, px));
        if (context.isAir(nx))
            context.setBlock(nx, inputState.getState(random, nx));
        if (context.isAir(pz))
            context.setBlock(pz, inputState.getState(random, pz));
        if (context.isAir(nz))
            context.setBlock(nz, inputState.getState(random, nz));
        if (context.isAir(pxt))
            context.setBlock(pxt, inputState.getState(random, pxt));
        if (context.isAir(nxt))
            context.setBlock(nxt, inputState.getState(random, nxt));
        if (context.isAir(pzt))
            context.setBlock(pzt, inputState.getState(random, pzt));
        if (context.isAir(nzt))
            context.setBlock(nzt, inputState.getState(random, nzt));
        if (context.isAir(pxq))
            context.setBlock(pxq, inputState.getState(random, pxq));
        if (context.isAir(nxq))
            context.setBlock(nxq, inputState.getState(random, nxq));
        if (context.isAir(pzq))
            context.setBlock(pzq, inputState.getState(random, pzq));
        if (context.isAir(nzq))
            context.setBlock(nzq, inputState.getState(random, nzq));
        if (context.isAir(pxc))
            context.setBlock(pxc, inputState.getState(random, pxc));
        if (context.isAir(nxc))
            context.setBlock(nxc, inputState.getState(random, nxc));
        if (context.isAir(pzc))
            context.setBlock(pzc, inputState.getState(random, pzc));
        if (context.isAir(nzc))
            context.setBlock(nzc, inputState.getState(random, nzc));
        if (context.isAir(pxz))
            context.setBlock(pxz, inputState.getState(random, pxz));
        if (context.isAir(nxz))
            context.setBlock(nxz, inputState.getState(random, nxz));
        if (context.isAir(pzz))
            context.setBlock(pzz, inputState.getState(random, pzz));
        if (context.isAir(nzz))
            context.setBlock(nzz, inputState.getState(random, nzz));
        if (context.isAir(up1))
            context.setBlock(up1, inputState.getState(random, up1));
        if (context.isAir(up2))
            context.setBlock(up2, inputState.getState(random, up2));
        if (context.isAir(pxo))
            context.setBlock(pxo, inputState.getState(random, pxo));
        if (context.isAir(nxo))
            context.setBlock(nxo, inputState.getState(random, nxo));
        if (context.isAir(pzo))
            context.setBlock(pzo, inputState.getState(random, pzo));
        if (context.isAir(nzo))
            context.setBlock(nzo, inputState.getState(random, nzo));
        if (context.isAir(pxm))
            context.setBlock(pxm, inputState.getState(random, pxm));
        if (context.isAir(nxm))
            context.setBlock(nxm, inputState.getState(random, nxm));
        if (context.isAir(pzm))
            context.setBlock(pzm, inputState.getState(random, pzm));
        if (context.isAir(nzm))
            context.setBlock(nzm, inputState.getState(random, nzm));
        if (context.isAir(pxp))
            context.setBlock(pxp, inputState.getState(random, pxp));
        if (context.isAir(nxp))
            context.setBlock(nxp, inputState.getState(random, nxp));
        if (context.isAir(pzp))
            context.setBlock(pzp, inputState.getState(random, pzp));
        if (context.isAir(nzp))
            context.setBlock(nzp, inputState.getState(random, nzp));
    }
}
