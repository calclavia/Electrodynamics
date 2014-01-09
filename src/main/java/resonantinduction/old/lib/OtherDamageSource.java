package resonantinduction.old.lib;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;

public class OtherDamageSource extends DamageSource
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

	@Override
	public OtherDamageSource setProjectile()
	{
		super.setProjectile();
		return this;
	}

	public static OtherDamageSource doBulletDamage(Object object)
	{
		return new OtherDamageSource("Bullets", object).setProjectile();
	}

	public static OtherDamageSource doLaserDamage(Object object)
	{
		return new OtherDamageSource("Laser", object).setProjectile();
	}
}
