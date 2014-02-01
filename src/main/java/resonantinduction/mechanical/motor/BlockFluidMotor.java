package resonantinduction.mechanical.motor;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import resonantinduction.core.Reference;
import resonantinduction.core.prefab.block.BlockRIRotatable;
import resonantinduction.core.render.RIBlockRenderingHandler;
import resonantinduction.electrical.generator.TileGenerator;

public class BlockFluidMotor extends BlockRIRotatable
{
    public BlockFluidMotor()
    {
        super("FluidMotor");
        setTextureName(Reference.PREFIX + "material_steel");
        rotationMask = Byte.parseByte("111111", 2);
    }

    @Override
    public boolean onSneakMachineActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
    {
        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

        if (tileEntity instanceof TileGenerator)
        {
            if (!world.isRemote)
            {
                ((TileGenerator) tileEntity).isInversed = !((TileGenerator) tileEntity).isInversed;
                entityPlayer.addChatMessage("Generator now producing " + (((TileGenerator) tileEntity).isInversed ? "mechanical" : "electrical") + " energy.");
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean onSneakUseWrench(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
    {
        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

        if (tileEntity instanceof TileGenerator)
        {
            if (!world.isRemote)
            {
                entityPlayer.addChatMessage("Generator torque ratio: " + ((TileGenerator) tileEntity).toggleRatio());
            }

            return true;
        }
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(World world)
    {
        return new TileGenerator();
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock()
    {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getRenderType()
    {
        return RIBlockRenderingHandler.ID;
    }

}
