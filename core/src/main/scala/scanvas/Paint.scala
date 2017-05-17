package scanvas

import org.bytedeco.javacpp.Skia._

class Paint private[scanvas] (private[scanvas] val p: sk_paint_t) {
  def setAntiAlias(b: Boolean): Paint = { sk_paint_set_antialias(p, b); this }
  def setTextAlign(align: Paint.TextAlign.TextAlign): Paint = { sk_paint_set_text_align(p, align.id); this }
  def setSubpixelText(b: Boolean): Paint = { sk_paint_set_subpixel_text(p, b); this }
  def setColor(color: Int): Paint = { sk_paint_set_color(p, color); this }
  def setStyle(style: Paint.Style.Style): Paint = { sk_paint_set_style(p, style.id); this }
  def setStrokeWidth(width: Float): Paint = { sk_paint_set_stroke_width(p, width); this }

  def setTypeface(tf: Typeface): Paint = { sk_paint_set_typeface(p, tf.tf); this }
  def setMaskFilter(mf: MaskFilter): Paint = { sk_paint_set_maskfilter(p, mf.mf); this }

  override def clone(): Paint = new Paint(sk_paint_clone(p))
}

object Paint {
  def blank: Paint = new Paint(sk_paint_new())

  object TextAlign extends Enumeration {
    type TextAlign = Value
    val Left = Value(LEFT_SK_TEXT_ALIGN)
    val Center = Value(CENTER_SK_TEXT_ALIGN)
    val Right = Value(RIGHT_SK_TEXT_ALIGN)
  }

  object Style extends Enumeration {
    type Style = Value
    val Fill = Value(FILL_SK_PAINT_STYLE)
    val Stroke = Value(STROKE_SK_PAINT_STYLE)
    val StrokeAndFill = Value(STROKE_AND_FILL_SK_PAINT_STYLE)
  }
}
