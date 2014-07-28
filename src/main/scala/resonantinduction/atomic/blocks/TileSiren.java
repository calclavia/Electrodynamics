package resonantinduction.atomic.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import resonant.content.spatial.block.SpatialBlock;
import resonantinduction.core.Reference;
import universalelectricity.core.transform.vector.Vector3;

/** Siren block */
public class TileSiren extends SpatialBlock
{
    public TileSiren()
    {
        super(Material.wood);
    }

    @Override
    public void update()
    {
        World world = worldObj;
        if (world != null)
        {
            int metadata = world.getBlockMetadata(x(), y(), z());

            if (world.getBlockPowerInput(x(), y(), z()) > 0)
            {
                float volume = 0.5f;
                for (int i = 0; i < 6; i++)
                {
                    Vector3 check = position().add(ForgeDirection.getOrientation(i));
                    if (check.getBlock(world) == getBlockType())
                    {
                        volume *= 1.5f;
                    }
                }

                world.playSoundEffect(x(), y(), z(), Reference.prefix() + "alarm", volume, 1f - 0.18f * (metadata / 15f));
            }
        }
    }

    @Override
    public boolean configure(EntityPlayer player, int side, Vector3 hit)
    {
        int metadata = world().getBlockMetadata(x(), y(), z());

        if (player.isSneaking())
        {
            metadata -= 1;
        }
        else
        {
            metadata += 1;
        }

        metadata = Math.max(metadata % 16, 0);

        world().setBlockMetadataWithNotify(x(), y(), z(), metadata, 2);
        return true;
    }
}
