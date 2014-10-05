package resonantinduction.atomic.blocks

import java.util.Random

import net.minecraft.block.material.Material
import net.minecraft.entity.{Entity, EntityLivingBase}
import net.minecraft.util.DamageSource
import net.minecraft.world.World
import net.minecraftforge.fluids.BlockFluidClassic
import resonant.lib.prefab.poison.PoisonRadiation
import resonantinduction.atomic.AtomicContent
import universalelectricity.core.transform.vector.Vector3

class BlockToxicWaste extends BlockFluidClassic(AtomicContent.getFluidToxicWaste, Material.water)
{
    //Constructor
    setTickRate(20)

    override def randomDisplayTick(par1World: World, x: Int, y: Int, z: Int, par5Random: Random)
    {
        super.randomDisplayTick(par1World, x, y, z, par5Random)
        if (par5Random.nextInt(100) == 0)
        {
            val d5: Double = x + par5Random.nextFloat
            val d7: Double = y + this.maxY
            val d6: Double = z + par5Random.nextFloat
            par1World.spawnParticle("suspended", d5, d7, d6, 0.0D, 0.0D, 0.0D)
        }
        if (par5Random.nextInt(200) == 0)
        {
            par1World.playSound(x, y, z, "liquid.lava", 0.2F + par5Random.nextFloat * 0.2F, 0.9F + par5Random.nextFloat * 0.15F, false)
        }
    }

    override def onEntityCollidedWithBlock(par1World: World, x: Int, y: Int, z: Int, entity: Entity)
    {
        if (entity.isInstanceOf[EntityLivingBase])
        {
            entity.attackEntityFrom(DamageSource.wither, 3)
            PoisonRadiation.INSTANCE.poisonEntity(new Vector3(x, y, z), entity.asInstanceOf[EntityLivingBase], 4)
        }
    }
}