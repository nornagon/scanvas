package scanvas

import org.bytedeco.javacpp.Skia._

class Surface private[scanvas] (surface: sk_surface_t, info: sk_imageinfo_t) {
  val canvas = new Canvas(sk_surface_get_canvas(surface), info)
}

object Surface {
  def newRaster(width: Int, height: Int): Surface = {
    val info = new sk_imageinfo_t()
    info.width(width)
    info.height(height)
    info.colorType(sk_colortype_get_default_8888())
    info.alphaType(PREMUL_SK_ALPHATYPE)
    new Surface(new sk_surface_t(sk_surface_new_raster(info, null)) {
      deallocator(() => sk_surface_unref(this))
    }, info)
  }
}
