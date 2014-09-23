package resonantinduction.core.render

import codechicken.lib.vec.{Vector3, Matrix4, Transformation, VariableTransformation}

class InvertX extends VariableTransformation(new Matrix4(1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1))
{
  override def inverse: Transformation = {
    return this
  }

  override def apply(vec: Vector3) {
    vec.x = -vec.x
  }
}