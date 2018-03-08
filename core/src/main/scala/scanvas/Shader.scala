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

  def fromImage(image: Image, tileX: TileMode.TileMode, tileY: TileMode.TileMode, scale: Float = 1): Shader = {
    val mat = new sk_matrix_t
    sk_matrix_set_scale(mat, scale, scale)
    val s = new sk_shader_t(sk_image_make_shader(image.img, tileX.id, tileY.id, mat)) {
      deallocator(() => sk_shader_unref(this))
    }

    new Shader(s)
  }
}
