package mffs.security.access

import resonant.lib.access.Permissions

object MFFSPermissions
{
  val mffs = Permissions.root.addChild("mffs")
  /** Force Field Wrap - Allows a player to go through force fields. */
  val forceFieldWrap = mffs.addChild("warp")

  /** Defense - Allows the bypassing of interdiction matrix defenses. */
  val defense = mffs.addChild("defense")
  /** Place Access - Allows to open GUIs and activate blocks. */
  val blockAlter = defense.addChild("blockPlaceAccess")
  /** Block Access - Allows block access and opening GUIs. */
  val blockAccess = defense.addChild("blockAccess")
  /** Bypass Confiscation - Allows the bypassing of interdiction matrix confiscation. */
  val bypassConfiscation = defense.addChild("bypassConfiscation")

  /** Configure - Allows to configure biometric identifiers. */
  val configure = mffs.addChild("configure")
  /** Remote Control - Allows player to remotely control blocks with the remote. */
  val remoteControl = mffs.addChild("remoteControl")
}