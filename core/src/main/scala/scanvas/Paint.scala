package scanvas

import java.text.BreakIterator

import org.bytedeco.javacpp.BytePointer
import org.bytedeco.javacpp.Skia._
import scanvas.Paint.FontMetrics

import scala.collection.mutable

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
  def setBlendMode(blendMode: Paint.BlendMode.BlendMode): Paint = { sk_paint_set_blendmode(p, blendMode.id); this }

  def setTypeface(tf: Typeface): Paint = { sk_paint_set_typeface(p, tf.tf); this }
  def setMaskFilter(mf: MaskFilter): Paint = { sk_paint_set_maskfilter(p, mf.mf); this }

  // TODO(jeremy): other functions use top/left/width/height. standardize.
  def getTextBounds(text: String): (Float, Float, Float, Float) = {
    val textBytes = new BytePointer(text, "UTF-8")
    sk_paint_measure_text(p, textBytes, text.getBytes.length, Tmp.rect1)
    (Tmp.rect1.top(), Tmp.rect1.left(), Tmp.rect1.bottom(), Tmp.rect1.right())
  }

  def breakText(text: String, maxWidth: Float): (Long, Float) = {
    val textBytes = new BytePointer(text, "UTF-8")
    val measuredWidth = new Array[Float](1)
    val measuredBytes = sk_paint_break_text(p, textBytes, text.getBytes.length, maxWidth, measuredWidth)
    (measuredBytes, measuredWidth(0))
  }

  def wrapText(text: String, maxWidth: Float): (Seq[String], Float) = {
    val textBytes = new BytePointer(text, "UTF-8")
    val measuredWidth = new Array[Float](1)
    var pos = 0l
    val lines = mutable.ArrayBuffer.empty[String]
    var maxMeasuredWidth = 0f

    while (pos < textBytes.limit()) {
      val measuredBytes = sk_paint_break_text(p, textBytes, textBytes.limit() - pos, maxWidth, measuredWidth)
      lines += new BytePointer(textBytes).limit(pos + measuredBytes).getString("UTF-8")
      pos += measuredBytes
      textBytes.position(pos)
      if (measuredWidth(0) > maxMeasuredWidth) {
        maxMeasuredWidth = measuredWidth(0)
      }
    }
    (lines, maxMeasuredWidth)
  }

  // TODO: this is starting to get a bit ridiculous
  def wrapText2(text: String, maxWidth: Float): (Seq[String], Float) = {
    var maxLineWidth = 0f
    val lines = mutable.ArrayBuffer.empty[String]
    val wordIterator = BreakIterator.getLineInstance
    wordIterator.setText(text)
    var start = wordIterator.first
    var end = wordIterator.next
    val line = StringBuilder.newBuilder
    var lineWidth = 0f
    while (end != BreakIterator.DONE) {
      val word = text.substring(start, end)
      val textBytes = new BytePointer(word, "UTF-8")
      val width = sk_paint_measure_text(p, textBytes, textBytes.limit(), null)
      if (lineWidth + width > maxWidth) {
        // break the line
        lines.append(line.toString())
        line.clear()
        lineWidth = width
      } else {
        lineWidth += width
      }
      line.append(word)
      if (lineWidth > maxLineWidth)
        maxLineWidth = lineWidth

      start = end
      end = wordIterator.next
    }
    if (line.nonEmpty) {
      lines.append(line.toString())
    }
    (lines, maxLineWidth)
  }

  def fontMetrics(): FontMetrics = {
    sk_paint_get_fontmetrics(p, Paint.tmpFontMetrics, 0)
    FontMetrics.fromSkFontMetrics(Paint.tmpFontMetrics)
  }

  override def clone(): Paint = new Paint(new sk_paint_t(sk_paint_clone(p)) { deallocator(() => sk_paint_delete(this)) })
}

object Paint {
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
  ) {
    lazy val lineSpacing: Float = descent - ascent + leading
  }

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

  object BlendMode extends Enumeration {
    type BlendMode = Value
    val Clear = Value(CLEAR_SK_BLENDMODE)
    val Src = Value(SRC_SK_BLENDMODE)
    val Dst = Value(DST_SK_BLENDMODE)
    val SrcOver = Value(SRCOVER_SK_BLENDMODE)
    val DstOver = Value(DSTOVER_SK_BLENDMODE)
    val SrcIn = Value(SRCIN_SK_BLENDMODE)
    val DstIn = Value(DSTIN_SK_BLENDMODE)
    val SrcOut = Value(SRCOUT_SK_BLENDMODE)
    val DstOut = Value(DSTOUT_SK_BLENDMODE)
    val SrcAtop = Value(SRCATOP_SK_BLENDMODE)
    val DstAtop = Value(DSTATOP_SK_BLENDMODE)
    val Xor = Value(XOR_SK_BLENDMODE)
    val Plus = Value(PLUS_SK_BLENDMODE)
    val Modulate = Value(MODULATE_SK_BLENDMODE)
    val Screen = Value(SCREEN_SK_BLENDMODE)
    val Overlay = Value(OVERLAY_SK_BLENDMODE)
    val Darken = Value(DARKEN_SK_BLENDMODE)
    val Lighten = Value(LIGHTEN_SK_BLENDMODE)
    val ColorDodge = Value(COLORDODGE_SK_BLENDMODE)
    val ColorBurn = Value(COLORBURN_SK_BLENDMODE)
    val HardLight = Value(HARDLIGHT_SK_BLENDMODE)
    val SoftLight = Value(SOFTLIGHT_SK_BLENDMODE)
    val Difference = Value(DIFFERENCE_SK_BLENDMODE)
    val Exclusion = Value(EXCLUSION_SK_BLENDMODE)
    val Multiply = Value(MULTIPLY_SK_BLENDMODE)
    val Hue = Value(HUE_SK_BLENDMODE)
    val Saturation = Value(SATURATION_SK_BLENDMODE)
    val Color = Value(COLOR_SK_BLENDMODE)
    val Luminosity = Value(LUMINOSITY_SK_BLENDMODE)
  }
}
