package resonantinduction.mechanical.fluid.tank;

import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ItemTankRenderer implements IItemRenderer
{
	private final TileTank tileTank = new TileTank();

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type)
	{
		return true;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
	{
		return true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data)
	{
		GL11.glPushMatrix();
		GL11.glScalef(1.0F, 1.0F, 1.0F);

		if (type == ItemRenderType.ENTITY)
		{
			GL11.glTranslatef(0F, 0.2F, 0F);
		}
		else if (type == ItemRenderType.EQUIPPED_FIRST_PERSON)
		{
			GL11.glTranslatef(0.4F, 0.6F, 0.2F);
		}
		else if (type == ItemRenderType.EQUIPPED)
		{
			GL11.glTranslatef(0.1F, 0.4F, 1.2F);
		}
		else
		{
			GL11.glTranslatef(0.7F, .45F, 0.7F);
		}

		FluidStack fluid = null;
		
		if (item.getTagCompound() != null && item.getTagCompound().hasKey("fluid"))
		{
			fluid = FluidStack.loadFluidStackFromNBT(item.getTagCompound().getCompoundTag("fluid"));
		}

		RenderTank.INSTANCE.renderTank(tileTank, 0, 0, 0, fluid);
		GL11.glPopMatrix();
	}
}
