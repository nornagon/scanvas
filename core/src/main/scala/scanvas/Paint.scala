package scanvas

import java.nio.ByteBuffer

import org.bytedeco.javacpp.Pointer
import org.bytedeco.javacpp.Skia._
import scanvas.Paint.FontMetrics

class Paint private[scanvas] (private[scanvas] val p: sk_paint_t) {
  def setAntiAlias(b: Boolean): Paint = { sk_paint_set_antialias(p, b); this }
  def setSubpixelText(b: Boolean): Paint = { sk_paint_set_subpixel_text(p, b); this }
  def setTextAlign(align: Paint.TextAlign.TextAlign): Paint = { sk_paint_set_text_align(p, align.id); this }
  def setTextSize(size: Float): Paint = { sk_paint_set_textsize(p, size); this }
  def setLCDRenderText(b: Boolean): Paint = { sk_paint_set_lcd_render_text(p, b); this }
  def setAutohinted(b: Boolean): Paint = { sk_paint_set_autohinted(p, b); this }
  def setColor(color: Int): Paint = { sk_paint_set_color(p, color); this }
  def setStyle(style: Paint.Style.Style): Paint = { sk_paint_set_style(p, style.id); this }
  def setStrokeWidth(width: Float): Paint = { sk_paint_set_stroke_width(p, width); this }
  def setShader(shader: Shader): Paint = { sk_paint_set_shader(p, shader.s); this }

  def setTypeface(tf: Typeface): Paint = { sk_paint_set_typeface(p, tf.tf); this }
  def setMaskFilter(mf: MaskFilter): Paint = { sk_paint_set_maskfilter(p, mf.mf); this }

  def getTextBounds(text: String): (Float, Float, Float, Float) = {
    val textBytes = new Pointer(ByteBuffer.wrap(text.getBytes))
    sk_paint_measure_text(p, textBytes, text.getBytes.length, Paint.tmpRect)
    (Paint.tmpRect.top(), Paint.tmpRect.left(), Paint.tmpRect.bottom(), Paint.tmpRect.right())
  }

  def fontMetrics(): FontMetrics = {
    sk_paint_get_fontmetrics(p, Paint.tmpFontMetrics, 0)
    FontMetrics.fromSkFontMetrics(Paint.tmpFontMetrics)
  }

  override def clone(): Paint = new Paint(new sk_paint_t(sk_paint_clone(p)) { deallocator(() => sk_paint_delete(this)) })
}

object Paint {
  private val tmpRect = new sk_rect_t()
  private val tmpFontMetrics = new sk_fontmetrics_t()
  def blank: Paint = new Paint(new sk_paint_t(sk_paint_new()) {
    deallocator(() => sk_paint_delete(this))
  }).setAntiAlias(true)

  case class FontMetrics(
    top: Float,
    ascent: Float,
    descent: Float,
    bottom: Float,
    leading: Float,
    avgCharWidth: Float,
    maxCharWidth: Float,
    xMin: Float,
    xMax: Float,
    xHeight: Float,
    capHeight: Float,
    underlineThickness: Float,
    underlinePosition: Float
  )

  object FontMetrics {
    private[scanvas] def fromSkFontMetrics(fm: sk_fontmetrics_t): FontMetrics = {
      FontMetrics(
        top = fm.fTop(),
        ascent = fm.fAscent(),
        descent = fm.fDescent(),
        bottom = fm.fBottom(),
        leading = fm.fLeading(),
        avgCharWidth = fm.fAvgCharWidth(),
        maxCharWidth = fm.fMaxCharWidth(),
        xMin = fm.fXMin(),
        xMax = fm.fXMax(),
        xHeight = fm.fXHeight(),
        capHeight = fm.fCapHeight(),
        underlineThickness = fm.fUnderlineThickness(),
        underlinePosition = fm.fUnderlinePosition()
      )
    }
  }

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
