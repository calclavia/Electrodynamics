if (te instanceof TilePipe)
		{
			TilePipe tile = (TilePipe) te;

			if (material == FluidContainerMaterial.WOOD || material == FluidContainerMaterial.STONE)
			{
				FluidStack liquid = tile.getInternalTank().getFluid();
				int cap = tile.getInternalTank().getCapacity();

				// FluidStack liquid = new FluidStack(FluidRegistry.WATER, cap);
				if (liquid != null && liquid.amount > 100)
				{
					float per = Math.max(1, (float) liquid.amount / (float) (cap));
					int[] displayList = RenderFluidHelper.getFluidDisplayLists(liquid, te.worldObj, false);
					bindTexture(RenderFluidHelper.getFluidSheet(liquid));

					GL11.glPushMatrix();
					GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
					GL11.glEnable(GL11.GL_CULL_FACE);
					GL11.glDisable(GL11.GL_LIGHTING);
					GL11.glEnable(GL11.GL_BLEND);
					GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

					GL11.glTranslatef((float) d + 0.3F, (float) d1 + 0.1F, (float) d2 + 0.3F);
					GL11.glScalef(0.4F, 0.4F, 0.4F);

					GL11.glCallList(displayList[(int) (per * (RenderFluidHelper.DISPLAY_STAGES - 1))]);

					GL11.glPopAttrib();
					GL11.glPopMatrix();

					for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
					{
						if (tile.canRenderSide(direction) && direction != ForgeDirection.UP && direction != ForgeDirection.DOWN)
						{
							GL11.glPushMatrix();
							GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
							GL11.glEnable(GL11.GL_CULL_FACE);
							GL11.glDisable(GL11.GL_LIGHTING);
							GL11.glEnable(GL11.GL_BLEND);
							GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

							switch (direction.ordinal())
							{
								case 4:
									GL11.glTranslatef((float) d + 0F, (float) d1 + 0.1F, (float) d2 + 0.3F);
									break;
								case 5:
									GL11.glTranslatef((float) d + 0.7F, (float) d1 + 0.1F, (float) d2 + 0.3F);
									break;
								case 2:
									GL11.glTranslatef((float) d + 0.3F, (float) d1 + 0.1F, (float) d2 + 0F);
									break;
								case 3:
									GL11.glTranslatef((float) d + 0.3F, (float) d1 + 0.1F, (float) d2 + 0.7F);
									break;
							}
							GL11.glScalef(0.3F, 0.4F, 0.4F);

							GL11.glCallList(displayList[(int) (per * (RenderFluidHelper.DISPLAY_STAGES - 1))]);

							GL11.glPopAttrib();
							GL11.glPopMatrix();
						}
					}
				}
			}

			GL11.glPushMatrix();
			GL11.glTranslatef((float) d + 0.5F, (float) d1 + 1.5F, (float) d2 + 0.5F);
			GL11.glScalef(1.0F, -1F, -1F);
			bindTexture(RenderPipe.getTexture(material, 0));
			render(material, tile.getSubID(), tile.renderSides);
			GL11.glPopMatrix();
		}
		else
		{
			GL11.glPushMatrix();
			GL11.glTranslatef((float) d + 0.5F, (float) d1 + 1.5F, (float) d2 + 0.5F);
			GL11.glScalef(1.0F, -1F, -1F);
			render(material, 0, (byte) 0b0);
			GL11.glPopMatrix();
		}