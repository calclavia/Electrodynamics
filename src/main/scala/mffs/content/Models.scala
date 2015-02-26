package mffs.content

import com.resonant.core.prefab.modcontent.ContentLoader
import mffs.Reference
import nova.core.render.model.TechneModel

/**
 * Textures
 * @author Calclavia
 */
object Models extends ContentLoader {
	val fortronCapacitor = new TechneModel(Reference.domain, "fortronCapacitor")
	val projector = new TechneModel(Reference.domain, "electromagneticProjector")
	val biometric = new TechneModel(Reference.domain, "biometricIdentifier")
}
