package scanvas

import org.bytedeco.javacpp.Pointer
import org.bytedeco.javacpp.Skia._

class Canvas private[scanvas] (c: sk_canvas_t, info: sk_imageinfo_t) {
  private val rectTmp = new sk_rect_t()
  private val matTmp = new sk_matrix_t()

  def save(): Unit = sk_canvas_save(c)
  def restore(): Unit = sk_canvas_restore(c)

  def flush(): Unit = sk_canvas_flush(c)

  def scale(x: Float, y: Float): Unit = sk_canvas_scale(c, x, y)
  def translate(x: Float, y: Float): Unit = sk_canvas_translate(c, x, y)
  def rotate(angle: Float): Unit = sk_canvas_rotate_radians(c, angle)
  def rotateDegrees(angle: Float): Unit = sk_canvas_rotate_degrees(c, angle)
  def skew(sx: Float, sy: Float): Unit = sk_canvas_skew(c, sx, sy)
  def setMatrix(mat: sk_matrix_t): Unit = sk_canvas_set_matrix(c, mat)
  def setMatrix(a: Float, b: Float, c: Float, d: Float, e: Float, f: Float): Unit = {
    matTmp.mat(0, a)
    matTmp.mat(1, b)
    matTmp.mat(2, c)
    matTmp.mat(3, d)
    matTmp.mat(4, e)
    matTmp.mat(5, f)
    matTmp.mat(6, 0)
    matTmp.mat(7, 0)
    matTmp.mat(8, 1)
    sk_canvas_set_matrix(this.c, matTmp)
  }
  def concat(mat: sk_matrix_t): Unit = sk_canvas_concat(c, mat)
  def concat(a: Float, b: Float, c: Float, d: Float, e: Float, f: Float): Unit = {
    matTmp.mat(0, a)
    matTmp.mat(1, b)
    matTmp.mat(2, c)
    matTmp.mat(3, d)
    matTmp.mat(4, e)
    matTmp.mat(5, f)
    matTmp.mat(6, 0)
    matTmp.mat(7, 0)
    matTmp.mat(8, 1)
    sk_canvas_concat(this.c, matTmp)
  }

  def clear(color: Int): Unit = sk_canvas_clear(c, color)
  def paint(paint: Paint): Unit = sk_canvas_draw_paint(c, paint.p)
  def drawText(text: String, x: Float, y: Float, paint: Paint): Unit =
    sk_canvas_draw_text(c, text, text.getBytes().length, x, y, paint.p)
  def drawCircle(cx: Float, cy: Float, radius: Float, paint: Paint): Unit =
    sk_canvas_draw_circle(c, cx, cy, radius, paint.p)
  def drawLine(x1: Float, y1: Float, x2: Float, y2: Float, paint: Paint): Unit =
    sk_canvas_draw_line(c, x1, y1, x2, y2, paint.p)
  def drawRect(x: Float, y: Float, w: Float, h: Float, paint: Paint): Unit =
    sk_canvas_draw_rect(c,
      rectTmp.left(x).top(y).right(x + w).bottom(y + h), paint.p)
  def drawRoundRect(x: Float, y: Float, w: Float, h: Float, rx: Float, ry: Float, paint: Paint): Unit =
    sk_canvas_draw_round_rect(c, rectTmp.left(x).top(y).right(x + w).bottom(y + h), rx, ry, paint.p)

  def drawPoints(mode: Canvas.PointMode.PointMode, points: Seq[(Float, Float)], paint: Paint): Unit = {
    val buf = java.nio.FloatBuffer.allocate(points.size * 2)
    for (i <- points.indices) {
      buf.put(i*2, points(i)._1)
      buf.put(i*2+1, points(i)._2)
    }
    val sk_points = new sk_point_t(new Pointer(buf))
    sk_canvas_draw_points(c, mode.id, points.size, sk_points, paint.p)
  }

  def drawPoint(x: Float, y: Float, paint: Paint): Unit = {
    sk_canvas_draw_point(c, x, y, paint.p)
  }

  def drawPath(path: Path, paint: Paint): Unit =
    sk_canvas_draw_path(c, path.path, paint.p)

  // TODO: bind SkCanvas::imageInfo
  def width: Int = info.width()
  def height: Int = info.height()
}

object Canvas {
  object PointMode extends Enumeration {
    type PointMode = Value
    val Points = Value(POINTS_SK_POINT_MODE)
    val Lines = Value(LINES_SK_POINT_MODE)
    val Polygon = Value(POLYGON_SK_POINT_MODE)
  }
}
