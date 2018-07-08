package scanvas

import org.bytedeco.javacpp.Skia._

class Shader private[scanvas] (private[scanvas] val s: sk_shader_t) {

}

object Shader {
  object TileMode extends Enumeration {
    type TileMode = Value
    val Clamp = Value(CLAMP_SK_SHADER_TILEMODE)
    val Repeat = Value(REPEAT_SK_SHADER_TILEMODE)
    val Mirror = Value(MIRROR_SK_SHADER_TILEMODE)
  }

  type Mat33 = (
    Float, Float, Float,
    Float, Float, Float,
    Float, Float, Float
  )

  def fromImage(image: Image, tileX: TileMode.TileMode, tileY: TileMode.TileMode, matrix: Mat33): Shader = {
    val mat = new sk_matrix_t
    val (a, b, c, d, e, f, g, h, i) = matrix
    mat.mat().put(a, b, c, d, e, f, g, h, i)
    val s = new sk_shader_t(sk_image_make_shader(image.img, tileX.id, tileY.id, mat)) {
      deallocator(() => sk_shader_unref(this))
    }

    new Shader(s)
  }

  def fromImage(image: Image, tileX: TileMode.TileMode, tileY: TileMode.TileMode, scale: Float = 1): Shader = {
    val mat = (
      scale, 0f, 0f,
      0f, scale, 0f,
      0f, 0f, 1f
    )
    fromImage(image, tileX, tileY, mat)
  }
}
