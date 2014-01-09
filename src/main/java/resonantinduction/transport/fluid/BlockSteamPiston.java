package resonantinduction.transport.fluid;

import java.util.Random;
import java.util.Set;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import resonantinduction.core.Settings;
import resonantinduction.lib.prefab.BlockMachine;
import resonantinduction.mechanics.generator.steam.TileEntitySteamPiston;

import com.builtbroken.common.Pair;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockSteamPiston extends BlockMachine
{

    public BlockSteamPiston()
    {
        super(Settings.CONFIGURATION, "SteamEngine", Material.iron);

    }

    @Override
    public boolean onUseWrench(World par1World, int x, int y, int z, EntityPlayer par5EntityPlayer, int side, float hitX, float hitY, float hitZ)
    {
        int angle = MathHelper.floor_double((par5EntityPlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        int metadata = par1World.getBlockMetadata(x, y, z);
        if (metadata < 3)
        {
            par1World.setBlock(x, y, z, blockID, metadata + angle, 3);
        }
        else
        {
            par1World.setBlock(x, y, z, blockID, 0, 3);
        }
        return true;
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean renderAsNormalBlock()
    {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderType()
    {
        return -1;
    }

    @Override
    public TileEntity createNewTileEntity(World world)
    {
        return new TileEntitySteamPiston();

    }

    @Override
    public int idDropped(int par1, Random par2Random, int par3)
    {
        return this.blockID;
    }

    @Override
    public boolean canPlaceBlockAt(World par1World, int par2, int par3, int par4)
    {
        int var5 = par1World.getBlockId(par2, par3, par4);
        int var6 = par1World.getBlockId(par2, par3 + 1, par4);
        return (var5 == 0 || blocksList[var5].blockMaterial.isReplaceable()) && (var6 == 0 || blocksList[var6].blockMaterial.isReplaceable());
    }

    @Override
    public void getTileEntities(int blockID, Set<Pair<String, Class<? extends TileEntity>>> list)
    {
        list.add(new Pair<String, Class<? extends TileEntity>>("FMSteamPiston", TileEntitySteamPiston.class));

    }
}
