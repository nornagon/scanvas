package scanvas

import org.bytedeco.javacpp.Skia._

class Path private[scanvas] (private[scanvas] val path: sk_path_t) {
  override def finalize(): Unit = { sk_path_delete(path) }

  def moveTo(x: Float, y: Float): Path = { sk_path_move_to(path, x, y); this }
  def lineTo(x: Float, y: Float): Path = { sk_path_line_to(path, x, y); this }
  def quadTo(x0: Float, y0: Float, x1: Float, y1: Float): Path =
    { sk_path_quad_to(path, x0, y0, x1, y1); this }
  def conicTo(x0: Float, y0: Float, x1: Float, y1: Float, w: Float): Path =
    { sk_path_conic_to(path, x0, y0, x1, y1, w); this }
  def cubicTo(x0: Float, y0: Float, x1: Float, y1: Float, x2: Float, y2: Float): Path =
    { sk_path_cubic_to(path, x0, y0, x1, y1, x2, y2); this }
  def close(): Path = { sk_path_close(path); this }

  def contains(x: Float, y: Float): Boolean = sk_path_contains(path, x, y)

  def setFillType(fillType: Path.FillType.FillType): Path =
    { sk_path_set_filltype(path, fillType.id); this }

  def rect(x: Float, y: Float, w: Float, h: Float, direction: Path.PathDirection.PathDirection = Path.PathDirection.Clockwise): Path = {
    Path.tmpRect.left(x).top(y).right(x + w).bottom(y + h)
    sk_path_add_rect(path, Path.tmpRect, direction.id)
    this
  }

  def reset(): Unit = sk_path_reset(path)
}

object Path {
  private[scanvas] val tmpRect = new sk_rect_t()
  def empty: Path = new Path(new sk_path_t(sk_path_new()) {
    deallocator(() => sk_path_delete(this))
  })

  object FillType extends Enumeration {
    type FillType = Value
    val Winding = Value(WINDING_SK_PATH_FILLTYPE)
    val EvenOdd = Value(EVENODD_SK_PATH_FILLTYPE)
    val InverseWinding = Value(INVERSE_WINDING_SK_PATH_FILLTYPE)
    val InverseEvenOdd = Value(INVERSE_EVENODD_SK_PATH_FILLTYPE)
  }

  object PathDirection extends Enumeration {
    type PathDirection = Value
    val Clockwise = Value(CW_SK_PATH_DIRECTION)
    val CounterClockwise = Value(CCW_SK_PATH_DIRECTION)
  }
}
