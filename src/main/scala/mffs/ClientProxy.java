package mffs;

import mffs.gui.GuiBiometricIdentifier;
import mffs.gui.GuiCoercionDeriver;
import mffs.gui.GuiForceFieldProjector;
import mffs.gui.GuiForceManipulator;
import mffs.gui.GuiFortronCapacitor;
import mffs.gui.GuiInterdictionMatrix;
import mffs.render.FXHologram;
import mffs.render.FXHologramMoving;
import mffs.render.FXHologramOrbit;
import mffs.render.FxFortronBeam;
import mffs.render.FxMFFS;
import mffs.render.IEffectController;
import mffs.render.RenderBlockHandler;
import mffs.render.RenderCoercionDeriver;
import mffs.render.RenderForceField;
import mffs.render.RenderForceFieldProjector;
import mffs.render.RenderForceManipulator;
import mffs.render.RenderFortronCapacitor;
import mffs.render.RenderIDCard;
import mffs.tile.TileBiometricIdentifier;
import mffs.tile.TileCoercionDeriver;
import mffs.tile.TileForceFieldProjector;
import mffs.tile.TileForceManipulator;
import mffs.tile.TileFortronCapacitor;
import mffs.tile.TileInterdictionMatrix;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import universalelectricity.api.vector.Vector3;
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
		ClientRegistry.bindTileEntitySpecialRenderer(TileFortronCapacitor.class, new RenderFortronCapacitor());
		ClientRegistry.bindTileEntitySpecialRenderer(TileCoercionDeriver.class, new RenderCoercionDeriver());
		ClientRegistry.bindTileEntitySpecialRenderer(TileForceManipulator.class, new RenderForceManipulator());
		ClientRegistry.bindTileEntitySpecialRenderer(TileForceFieldProjector.class, new RenderForceFieldProjector());
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
			if (tileEntity.getClass() == TileFortronCapacitor.class)
			{
				return new GuiFortronCapacitor(player, (TileFortronCapacitor) tileEntity);
			}
			else if (tileEntity.getClass() == TileForceFieldProjector.class)
			{
				return new GuiForceFieldProjector(player, (TileForceFieldProjector) tileEntity);
			}
			else if (tileEntity.getClass() == TileCoercionDeriver.class)
			{
				return new GuiCoercionDeriver(player, (TileCoercionDeriver) tileEntity);
			}
			else if (tileEntity.getClass() == TileBiometricIdentifier.class)
			{
				return new GuiBiometricIdentifier(player, (TileBiometricIdentifier) tileEntity);
			}
			else if (tileEntity.getClass() == TileInterdictionMatrix.class)
			{
				return new GuiInterdictionMatrix(player, (TileInterdictionMatrix) tileEntity);
			}
			else if (tileEntity.getClass() == TileForceManipulator.class)
			{
				return new GuiForceManipulator(player, (TileForceManipulator) tileEntity);
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
		FMLClientHandler.instance().getClient().effectRenderer.addEffect(new FxFortronBeam(world, position, target, red, green, blue, age));
	}

	@Override
	public void renderHologram(World world, Vector3 position, float red, float green, float blue, int age, Vector3 targetPosition)
	{
		if (targetPosition != null)
		{
			FMLClientHandler.instance().getClient().effectRenderer.addEffect(new FXHologram(world, position, red, green, blue, age).setTarget(targetPosition));
		}
		else
		{
			FMLClientHandler.instance().getClient().effectRenderer.addEffect(new FXHologram(world, position, red, green, blue, age));
		}
	}

	@Override
	public void renderHologramOrbit(World world, Vector3 orbitCenter, Vector3 position, float red, float green, float blue, int age, float maxSpeed)
	{
		FMLClientHandler.instance().getClient().effectRenderer.addEffect(new FXHologramOrbit(world, orbitCenter, position, red, green, blue, age, maxSpeed));
	}

	@Override
	public void renderHologramOrbit(IEffectController controller, World world, Vector3 orbitCenter, Vector3 position, float red, float green, float blue, int age, float maxSpeed)
	{
		FxMFFS fx = new FXHologramOrbit(world, orbitCenter, position, red, green, blue, age, maxSpeed);
		fx.setController(controller);
		FMLClientHandler.instance().getClient().effectRenderer.addEffect(fx);
	}

	@Override
	public void renderHologramMoving(World world, Vector3 position, float red, float green, float blue, int age)
	{
		FMLClientHandler.instance().getClient().effectRenderer.addEffect(new FXHologramMoving(world, position, red, green, blue, age));
	}
}