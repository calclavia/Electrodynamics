package resonantinduction.electrical.encoder.gui;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import resonantinduction.electrical.encoder.TileEncoder;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiEncoderInventory extends GuiEncoderBase
{
	public static final ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.GUI_DIRECTORY + "gui_encoder_slot.png");

	public GuiEncoderInventory(InventoryPlayer inventoryPlayer, TileEncoder tileEntity)
	{
		super(inventoryPlayer, tileEntity, new ContainerEncoder(inventoryPlayer, tileEntity));
	}
}
