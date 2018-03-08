package scanvas

import org.bytedeco.javacpp.Skia._

class MaskFilter private[scanvas] (private[scanvas] val mf: sk_maskfilter_t) {
  override def finalize(): Unit = {
    sk_maskfilter_unref(mf)
  }
}

object MaskFilter {
  def blur(radius: Float): MaskFilter =
    blurRaw(if (radius == 0) 0 else 0.57735f * radius + 0.5f)

  def blurRaw(sigma: Float, blurStyle: BlurStyle.BlurStyle = BlurStyle.Normal): MaskFilter = {
    new MaskFilter(new sk_maskfilter_t(sk_maskfilter_new_blur(blurStyle.id, sigma)) {
      deallocator(() => sk_maskfilter_unref(this))
    })
  }

  object BlurStyle extends Enumeration {
    type BlurStyle = Value
    val Normal = Value(NORMAL_SK_BLUR_STYLE)
    val Inner = Value(INNER_SK_BLUR_STYLE)
    val Outer = Value(OUTER_SK_BLUR_STYLE)
    val Solid = Value(SOLID_SK_BLUR_STYLE)
  }
}
