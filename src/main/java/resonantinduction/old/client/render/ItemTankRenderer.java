package resonantinduction.old.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import resonantinduction.mechanical.fluid.tank.BlockTank;
import resonantinduction.old.client.model.ModelTankSide;
import resonantinduction.old.core.recipe.RecipeLoader;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ItemTankRenderer implements IItemRenderer
{
	private ModelTankSide tank = new ModelTankSide();

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
		if (item.itemID == RecipeLoader.blockTank.blockID)
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
				GL11.glTranslatef(0.7F, .4F, 0.7F);
			}

			FMLClientHandler.instance().getClient().renderEngine.bindTexture(new ResourceLocation(Reference.DOMAIN, item.getItemDamage() == 1 ? "textures/blocks/obsidian.png" : "textures/blocks/iron_block.png"));
			GL11.glTranslatef(0.0F, -0.9F, 0.0F);
			tank.render(0.0625F, false, false, false, false);
			GL11.glRotatef(90f, 0f, 1f, 0f);
			tank.render(0.0625F, false, false, false, false);
			GL11.glRotatef(90f, 0f, 1f, 0f);
			tank.render(0.0625F, false, false, false, false);
			GL11.glRotatef(90f, 0f, 1f, 0f);
			tank.render(0.0625F, false, false, false, false);
			GL11.glPopMatrix();

		}

		if (item.getTagCompound() != null && item.getTagCompound().hasKey("fluid"))
		{
			FluidStack liquid = FluidStack.loadFluidStackFromNBT(item.getTagCompound().getCompoundTag("fluid"));

			if (liquid != null && liquid.amount > 100)
			{

				int[] displayList = RenderFluidHelper.getFluidDisplayLists(liquid, Minecraft.getMinecraft().theWorld, false);

				GL11.glPushMatrix();
				GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
				GL11.glEnable(GL11.GL_CULL_FACE);
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				// GL11.glScalef(0.80F, 0.9F, 0.80F);
				if (type == ItemRenderType.ENTITY)
				{
					GL11.glTranslatef(-.5F, -0.2F, -.5F);
				}
				else if (type == ItemRenderType.EQUIPPED_FIRST_PERSON)
				{
					GL11.glTranslatef(-0.1F, 0.2F, -.3F);
				}
				else if (type == ItemRenderType.EQUIPPED)
				{
					GL11.glScalef(0.9F, 0.9F, 0.9F);
					GL11.glTranslatef(-0.4F, 0.1F, 0.9F);
				}
				else
				{
					GL11.glScalef(0.80F, 0.9F, 0.80F);
					GL11.glTranslatef(0.5F, .2F, 0.5F);
				}

				FMLClientHandler.instance().getClient().renderEngine.bindTexture((RenderFluidHelper.getFluidSheet(liquid)));

				int cap = BlockTank.tankVolume * FluidContainerRegistry.BUCKET_VOLUME;
				if (liquid.getFluid().isGaseous())
				{
					cap = liquid.amount;
				}
				GL11.glCallList(displayList[(int) Math.min(((float) liquid.amount / (float) (cap) * (RenderFluidHelper.DISPLAY_STAGES - 1)), displayList.length - 1)]);

				GL11.glPopAttrib();
				GL11.glPopMatrix();
			}
		}
	}
}
