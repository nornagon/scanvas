package scanvas

import org.bytedeco.javacpp.Skia._

class Surface private[scanvas] (surface: sk_surface_t, info: sk_imageinfo_t) {
  val canvas = new Canvas(sk_surface_get_canvas(surface), info)

  def snapshot(): Image = {
    val img = new sk_image_t(sk_surface_new_image_snapshot(surface)) {
      deallocator(() => sk_image_unref(this))
    }
    new Image(img)
  }
}

object Surface {
  def newRaster(width: Int, height: Int): Surface = {
    val info = new sk_imageinfo_t()
      .width(width)
      .height(height)
      .colorType(sk_colortype_get_default_8888())
      .alphaType(PREMUL_SK_ALPHATYPE)
    new Surface(new sk_surface_t(sk_surface_new_raster(info, null)) {
      deallocator(() => sk_surface_unref(this))
    }, info)
  }
}
