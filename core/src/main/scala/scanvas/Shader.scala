package scanvas

import org.bytedeco.javacpp.Skia._

class Shader private[scanvas](private[scanvas] val s: sk_shader_t) {

}

object Shader {
  object TileMode extends Enumeration {
    type TileMode = Value
    val Clamp = Value(CLAMP_SK_SHADER_TILEMODE)
    val Repeat = Value(REPEAT_SK_SHADER_TILEMODE)
    val Mirror = Value(MIRROR_SK_SHADER_TILEMODE)
  }

  def fromImage(image: Image, tileX: TileMode.TileMode, tileY: TileMode.TileMode, matrix: Mat33): Shader = {
    val (a, b, c, d, e, f, g, h, i) = matrix
    Tmp.mat1.mat().put(a, b, c, d, e, f, g, h, i)
    val s = new sk_shader_t(sk_image_make_shader(image.img, tileX.id, tileY.id, Tmp.mat1)) {
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

  def radialGradient(cx: Float, cy: Float, radius: Float, colors: Seq[(Int, Float)], tileMode: TileMode.TileMode): Shader = {
    val center = Tmp.point1.x(cx).y(cy)
    val colorsArr = colors.map(_._1).toArray
    val colorPosArr = colors.map(_._2).toArray
    val s = new sk_shader_t(sk_shader_new_radial_gradient(center, radius, colorsArr, colorPosArr, colors.size, tileMode.id, null)) {
      deallocator(() => sk_shader_unref(this))
    }
    new Shader(s)
  }
}
