package scanvas

import java.io.FileNotFoundException

import org.bytedeco.javacpp.Skia
import org.bytedeco.javacpp.Skia._

class Image private[scanvas] (private[scanvas] val img: sk_image_t) {
  val width: Int = sk_image_get_width(img)
  val height: Int = sk_image_get_height(img)

  /**
    * Usage example:
    *
    * <pre><code>
    * val s = Surface.newRaster(100, 100)
    * s.canvas.drawRect(20, 20, 30, 30, Paint.blank.setColor(0xff0000ff))
    * s.snapshot.writeAsPngTo(new FileOutputStream("out.png"))
    * </code></pre>
    */
  def writeAsPngTo(writer: java.io.OutputStream): Unit = {
    val data = sk_image_encode(img)
    try {
      val size = sk_data_get_size(data)
      val bytes = sk_data_get_bytes(data).limit(size)
      writer.write(bytes.getStringBytes)
    } finally {
      sk_data_unref(data)
    }
  }
}

object Image {
  def fromFile(path: java.nio.file.Path): Image = {
    val data = sk_data_new_from_file(path.toString)
    if (data == null || data.isNull)
      throw new FileNotFoundException(s"Couldn't open file at '$path'")
    val img = try {
      val img = sk_image_new_from_encoded(data, null)
      if (img == null || img.isNull)
        throw new RuntimeException(s"Couldn't decode image at '$path'")
      img
    } finally {
      sk_data_unref(data)
    }
    new Image(new sk_image_t(img) { deallocator(() => sk_image_unref(this)) })
  }
}
