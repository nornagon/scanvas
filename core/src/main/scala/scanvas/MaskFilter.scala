package scanvas

import org.bytedeco.javacpp.Skia._

class MaskFilter private[scanvas] (private[scanvas] val mf: sk_maskfilter_t) {
  override def finalize(): Unit = {
    sk_maskfilter_unref(mf)
  }
}

object MaskFilter {
  def blur(radius: Float): MaskFilter = {
    new MaskFilter(sk_maskfilter_new_blur(
      NORMAL_SK_BLUR_STYLE, if (radius == 0) 0 else 0.57735f * radius + 0.5f))
  }
}
