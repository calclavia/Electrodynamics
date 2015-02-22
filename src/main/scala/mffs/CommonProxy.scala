package mffs

class CommonProxy extends AbstractProxy with IGuiHandler
{
  override def getServerGuiElement(id: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): AnyRef =
  {
    id match
    {
      case 0 =>
      {
        val tileEntity = world.getTileEntity(x, y, z)
        tileEntity match
        {
          case tile: TileFortronCapacitor => return new ContainerFortronCapacitor(player, tile)
          case tile: TileElectromagneticProjector => return new ContainerElectromagneticProjector(player, tile)
          case tile: TileCoercionDeriver => return new ContainerCoercionDeriver(player, tile)
          case tile: TileBiometricIdentifier => return new ContainerBiometricIdentifier(player, tile)
          case tile: TileForceMobilizer => return new ContainerMatrix(player, tile)
        }
      }
      case 1 => return new ContainerFrequency(player, player.getCurrentEquippedItem)
      case 2 => return new ContainerItem(player, player.getCurrentEquippedItem)
    }

    return null
  }

  def getClientWorld: World =
  {
    return null
  }

  /**
   * Checks if the player is an operator.
   */
  def isOp(profile: GameProfile): Boolean =
  {
    val theServer: MinecraftServer = FMLCommonHandler.instance.getMinecraftServerInstance()

    if (theServer != null)
    {
      return theServer.getConfigurationManager().func_152596_g(profile)
    }
    return false
  }

	def renderBeam(world: World, position: Vector3d, target: Vector3d, color: (Float, Float, Float), age: Int)
  {
  }

	def renderHologram(world: World, position: Vector3d, color: (Float, Float, Float), age: Int, targetPosition: Vector3d)
  {
  }

	def renderHologramMoving(world: World, position: Vector3d, color: (Float, Float, Float), age: Int)
  {
  }

	def renderHologramOrbit(world: World, orbitCenter: Vector3d, color: (Float, Float, Float), age: Int, maxSpeed: Float)
  {
  }

	def renderHologramOrbit(owner: IEffectController, world: World, orbitCenter: Vector3d, position: Vector3d, color: (Float, Float, Float), age: Int, maxSpeed: Float)
  {
  }
}