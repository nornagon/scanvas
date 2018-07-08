package scanvas

import org.bytedeco.javacpp.Skia._

class Bitmap private[scanvas] (private[scanvas] val b: sk_bitmap_t, info: sk_imageinfo_t) {
  def setPixel(x: Int, y: Int, v: Int): Bitmap = { sk_bitmap_set_pixel_color(b, x, y, v); this }
  def setPixels(pixels: Array[Int]): Bitmap = { sk_bitmap_set_pixel_colors(b, pixels); this }
  def getPixel(x: Int, y: Int): Int = sk_bitmap_get_pixel_color(b, x, y)
}

object Bitmap {
  def make(w: Int, h: Int): Bitmap = {
    val info = new sk_imageinfo_t()
      .width(w)
      .height(h)
      .colorType(sk_colortype_get_default_8888())
      .alphaType(PREMUL_SK_ALPHATYPE)
    val b = new sk_bitmap_t(sk_bitmap_new()) { deallocator(() => sk_bitmap_destructor(this)) }
    val res = sk_bitmap_try_alloc_pixels(b, info, 0)
    assert(res, s"Failed to allocate bytes for bitmap")
    new Bitmap(b, info)
  }
}
