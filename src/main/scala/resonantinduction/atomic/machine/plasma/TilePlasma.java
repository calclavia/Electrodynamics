package resonantinduction.atomic.machine.plasma;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL11;
import resonant.api.event.PlasmaEvent.SpawnPlasmaEvent;
import resonant.content.prefab.java.TileAdvanced;
import resonant.engine.grid.thermal.ThermalGrid;
import resonant.lib.config.Config;
import universalelectricity.core.transform.vector.Vector3;
import universalelectricity.core.transform.vector.VectorWorld;

public class TilePlasma extends TileAdvanced
{
    @Config
    public static int plasmaMaxTemperature = 1000000;
    private float temperature = plasmaMaxTemperature;

    public TilePlasma()
    {
        super(Material.lava);
        textureName_$eq("plasma");
        isOpaqueCube(false);
    }

    @Override
    public int getLightValue(IBlockAccess access)
    {
        return 7;
    }

    @Override
    public boolean isSolid(IBlockAccess access, int side)
    {
        return false;
    }

    @Override
    public ArrayList<ItemStack> getDrops(int metadata, int fortune)
    {
        return new ArrayList<ItemStack>();
    }

    @Override
    public int getRenderBlockPass()
    {
        return 1;
    }

    @Override
    public void collide(Entity entity)
    {
        entity.attackEntityFrom(DamageSource.inFire, 100);
    }

    @Override
    public void update()
    {
        super.update();
        ThermalGrid.addTemperature(new VectorWorld(this), (temperature - ThermalGrid.getTemperature(new VectorWorld(this))) * 0.1f);

        if (ticks() % 20 == 0)
        {
            temperature /= 1.5;

            if (temperature <= plasmaMaxTemperature / 10)
            {
                worldObj.setBlock(xCoord, yCoord, zCoord, Blocks.fire, 0, 3);
                return;
            }

            for (int i = 0; i < 6; i++)
            {
                // Randomize spread direction.
                if (worldObj.rand.nextFloat() > 0.4)
                {
                    continue;
                }

                Vector3 diDian = new Vector3(this);
                diDian.add(ForgeDirection.getOrientation(i));

                TileEntity tileEntity = diDian.getTileEntity(worldObj);

                if (!(tileEntity instanceof TilePlasma))
                {
                    MinecraftForge.EVENT_BUS.post(new SpawnPlasmaEvent(worldObj, diDian.xi(), diDian.yi(), diDian.zi(), (int) temperature));
                }
            }
        }
    }

    public void setTemperature(int newTemperature)
    {
        temperature = newTemperature;
    }

}
