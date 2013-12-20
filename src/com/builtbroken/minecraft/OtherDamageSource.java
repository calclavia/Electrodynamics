package com.builtbroken.minecraft;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import universalelectricity.prefab.CustomDamageSource;

public class OtherDamageSource extends CustomDamageSource
{
    protected Object damageSource;

    public OtherDamageSource(String damageName, Object attacker)
    {
        super(damageName);
        this.damageSource = attacker;
    }

    @Override
    public Entity getEntity()
    {
        return damageSource instanceof Entity ? ((Entity) damageSource) : null;
    }

    public TileEntity getTileEntity()
    {
        return damageSource instanceof TileEntity ? ((TileEntity) damageSource) : null;
    }

    @Override
    public boolean isDifficultyScaled()
    {
        return this.damageSource != null && this.damageSource instanceof EntityLiving && !(this.damageSource instanceof EntityPlayer);
    }

    public static OtherDamageSource doBulletDamage(Object object)
    {
        return (OtherDamageSource) ((CustomDamageSource) new OtherDamageSource("Bullets", object).setProjectile()).setDeathMessage("%1$s was filled with holes!");
    }

    public static OtherDamageSource doLaserDamage(Object object)
    {
        return (OtherDamageSource) ((CustomDamageSource) new OtherDamageSource("Laser", object).setProjectile()).setDeathMessage("%1$s was vaporized!");
    }
}
