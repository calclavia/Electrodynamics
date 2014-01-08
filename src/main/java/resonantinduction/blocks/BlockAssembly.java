package resonantinduction.blocks;

import resonantinduction.AssemblyLine;
import resonantinduction.ResonantInductionTabs;
import resonantinduction.machine.TileEntityAssembly;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.builtbroken.minecraft.prefab.BlockMachine;

public class BlockAssembly extends BlockMachine
{
    public BlockAssembly(String blockName, Material material)
    {
        super(ResonantInductionTransport.CONFIGURATION, blockName, material);
        this.setCreativeTab(ResonantInductionTabs.tabAutomation());
    }

    @Override
    public boolean onMachineActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
    {
        if (entityPlayer != null && entityPlayer.getHeldItem() != null && entityPlayer.getHeldItem().itemID == Item.stick.itemID)
        {
            TileEntity entity = world.getBlockTileEntity(x, y, z);
            if (entity instanceof TileEntityAssembly)
            {
                System.out.println(((TileEntityAssembly) entity).getTileNetwork().toString());
            }

        }
        return false;
    }

}