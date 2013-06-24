package mffs;

import mffs.gui.GuiBiometricIdentifier;
import mffs.gui.GuiCoercionDeriver;
import mffs.gui.GuiForceFieldProjector;
import mffs.gui.GuiFortronCapacitor;
import mffs.gui.GuiInterdictionMatrix;
import mffs.render.FXBeam;
import mffs.render.FXHologramMoving;
import mffs.render.RenderBlockHandler;
import mffs.render.RenderCoercionDeriver;
import mffs.render.RenderForceField;
import mffs.render.RenderForceFieldProjector;
import mffs.render.RenderFortronCapacitor;
import mffs.render.RenderIDCard;
import mffs.tileentity.TileEntityBiometricIdentifier;
import mffs.tileentity.TileEntityCoercionDeriver;
import mffs.tileentity.TileEntityForceFieldProjector;
import mffs.tileentity.TileEntityFortronCapacitor;
import mffs.tileentity.TileEntityInterdictionMatrix;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import universalelectricity.core.vector.Vector3;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy
{
	@Override
	public void preInit()
	{
		super.preInit();
		MinecraftForge.EVENT_BUS.register(SoundHandler.INSTANCE);
	}

	@Override
	public void init()
	{
		super.init();
		RenderingRegistry.registerBlockHandler(new RenderBlockHandler());
		RenderingRegistry.registerBlockHandler(new RenderForceField());
		MinecraftForgeClient.registerItemRenderer(ModularForceFieldSystem.itemCardID.itemID, new RenderIDCard());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFortronCapacitor.class, new RenderFortronCapacitor());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCoercionDeriver.class, new RenderCoercionDeriver());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityForceFieldProjector.class, new RenderForceFieldProjector());
	}

	@Override
	public World getClientWorld()
	{
		return FMLClientHandler.instance().getClient().theWorld;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity != null)
		{
			if (tileEntity.getClass() == TileEntityFortronCapacitor.class)
			{
				return new GuiFortronCapacitor(player, (TileEntityFortronCapacitor) tileEntity);
			}
			else if (tileEntity.getClass() == TileEntityForceFieldProjector.class)
			{
				return new GuiForceFieldProjector(player, (TileEntityForceFieldProjector) tileEntity);
			}
			else if (tileEntity.getClass() == TileEntityCoercionDeriver.class)
			{
				return new GuiCoercionDeriver(player, (TileEntityCoercionDeriver) tileEntity);
			}
			else if (tileEntity.getClass() == TileEntityBiometricIdentifier.class)
			{
				return new GuiBiometricIdentifier(player, (TileEntityBiometricIdentifier) tileEntity);
			}
			else if (tileEntity.getClass() == TileEntityInterdictionMatrix.class)
			{
				return new GuiInterdictionMatrix(player, (TileEntityInterdictionMatrix) tileEntity);
			}
		}

		return null;
	}

	@Override
	public boolean isOp(String username)
	{
		return false;
	}

	@Override
	public void renderBeam(World world, Vector3 position, Vector3 target, float red, float green, float blue, int age)
	{
		FMLClientHandler.instance().getClient().effectRenderer.addEffect(new FXBeam(world, position, target, red, green, blue, age));
	}

	@Override
	public void renderHologram(World world, Vector3 position, float red, float green, float blue, int age)
	{
		FMLClientHandler.instance().getClient().effectRenderer.addEffect(new FXHologramMoving(world, position, red, green, blue, age));
	}
}