package scanvas

import org.bytedeco.javacpp.Skia._

class Path private[scanvas] (private[scanvas] val path: sk_path_t) {
  override def finalize(): Unit = { sk_path_delete(path) }

  def moveTo(x: Float, y: Float): Unit = sk_path_move_to(path, x, y)
  def lineTo(x: Float, y: Float): Unit = sk_path_line_to(path, x, y)
  def quadTo(x0: Float, y0: Float, x1: Float, y1: Float): Unit =
    sk_path_quad_to(path, x0, y0, x1, y1)
  def conicTo(x0: Float, y0: Float, x1: Float, y1: Float, w: Float): Unit =
    sk_path_conic_to(path, x0, y0, x1, y1, w)
  def cubicTo(x0: Float, y0: Float, x1: Float, y1: Float, x2: Float, y2: Float): Unit =
    sk_path_cubic_to(path, x0, y0, x1, y1, x2, y2)
  def close(): Unit = sk_path_close(path)

  def contains(x: Float, y: Float): Boolean = sk_path_contains(path, x, y)

  def reset(): Unit = sk_path_reset(path)
}

object Path {
  def empty: Path = new Path(sk_path_new())
}
