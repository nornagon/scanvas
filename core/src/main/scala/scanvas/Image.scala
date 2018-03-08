package scanvas

import java.io.FileNotFoundException

import org.bytedeco.javacpp.Skia
import org.bytedeco.javacpp.Skia._

class Image private[scanvas] (private[scanvas] val img: sk_image_t) {
  val width: Int = sk_image_get_width(img)
  val height: Int = sk_image_get_height(img)
}

object Image {
  def fromFile(path: java.nio.file.Path): Image = {
    val data = sk_data_new_from_file(path.toString)
    if (data == null || data.isNull)
      throw new FileNotFoundException(s"Couldn't open file at '$path'")
    val img = sk_image_new_from_encoded(data, null)
    if (img == null || img.isNull)
      throw new RuntimeException(s"Couldn't decode image at '$path'")
    Skia.sk_data_unref(data)
    new Image(new sk_image_t(img) {
      deallocator(() => sk_image_unref(this))
    })
  }
}
