package scanvas

import org.bytedeco.javacpp.Skia._

class Typeface private[scanvas] (private[scanvas] val tf: sk_typeface_t) {

}

object Typeface {
  def fromName(name: String, style: Style.Style): Typeface = {
    val tf = sk_typeface_create_from_name(name, style.id)
    new Typeface(new sk_typeface_t(tf) { deallocator(() => sk_typeface_unref(this)) })
  }

  object Style extends Enumeration {
    type Style = Value
    val Normal = Value(0)
    val Bold = Value(1)
    val Italic = Value(2)
    val BoldItalic = Value(3)
  }
}
