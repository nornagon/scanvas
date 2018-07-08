package scanvas

import org.bytedeco.javacpp.Skia._
import scanvas.Path._
import scanvas.Shader.Mat33

class Path private[scanvas] (private[scanvas] val path: sk_path_t) {
  private val matTmp = new sk_matrix_t()

  def moveTo(x: Float, y: Float): Path = { sk_path_move_to(path, x, y); this }
  def lineTo(x: Float, y: Float): Path = { sk_path_line_to(path, x, y); this }
  def quadTo(x0: Float, y0: Float, x1: Float, y1: Float): Path =
    { sk_path_quad_to(path, x0, y0, x1, y1); this }
  def conicTo(x0: Float, y0: Float, x1: Float, y1: Float, w: Float): Path =
    { sk_path_conic_to(path, x0, y0, x1, y1, w); this }
  def cubicTo(x0: Float, y0: Float, x1: Float, y1: Float, x2: Float, y2: Float): Path =
    { sk_path_cubic_to(path, x0, y0, x1, y1, x2, y2); this }
  def close(): Path = { sk_path_close(path); this }
  def circle(cx: Float, cy: Float, r: Float, dir: Path.PathDirection.PathDirection = Path.PathDirection.Clockwise): Path =
    { sk_path_add_circle(path, cx, cy, r, dir.id); this }
  def transform(mat: Mat33): Path = {
    val (a, b, c, d, e, f, g, h, i) = mat
    matTmp.mat().put(a, b, c, d, e, f, g, h, i)
    sk_path_transform(path, matTmp)
    this
  }

  def contains(x: Float, y: Float): Boolean = sk_path_contains(path, x, y)

  def setFillType(fillType: Path.FillType.FillType): Path =
    { sk_path_set_filltype(path, fillType.id); this }

  def rect(x: Float, y: Float, w: Float, h: Float, direction: Path.PathDirection.PathDirection = Path.PathDirection.Clockwise): Path = {
    Path.tmpRect.left(x).top(y).right(x + w).bottom(y + h)
    sk_path_add_rect(path, Path.tmpRect, direction.id)
    this
  }

  def reset(): Unit = sk_path_reset(path)

  def bounds: (Float, Float, Float, Float) = {
    sk_path_get_bounds(path, Path.tmpRect)
    (Path.tmpRect.left, Path.tmpRect.top, Path.tmpRect.right - Path.tmpRect.left, Path.tmpRect.bottom - Path.tmpRect.top)
  }

  def tightBounds: (Float, Float, Float, Float) = {
    sk_path_compute_tight_bounds(path, Path.tmpRect)
    (Path.tmpRect.left, Path.tmpRect.top, Path.tmpRect.right - Path.tmpRect.left, Path.tmpRect.bottom - Path.tmpRect.top)
  }

  def foreach(f: (PathElement) => Unit): Unit = {
    val iter = sk_path_create_iter(path, 0)
    val pointBuf = new sk_point_t(4)
    var verb: Verb.Verb = Verb.Move
    while (verb != Verb.Done) {
      pointBuf.position(0)
      verb = Verb(sk_path_iter_next(iter, pointBuf, 0, 0))
      verb match {
        case Verb.Done =>
        case Verb.Close =>
          f(CloseElement())
        case Verb.Move =>
          f(MoveElement((pointBuf.x(), pointBuf.y())))
        case Verb.Line =>
          val p1 = (pointBuf.x(), pointBuf.y())
          pointBuf.position(1)
          val p2 = (pointBuf.x(), pointBuf.y())
          f(LineElement(p1, p2))
        case Verb.Quad =>
          val p1 = (pointBuf.x(), pointBuf.y())
          pointBuf.position(1)
          val p2 = (pointBuf.x(), pointBuf.y())
          pointBuf.position(2)
          val p3 = (pointBuf.x(), pointBuf.y())
          f(QuadElement(p1, p2, p3))
        case Verb.Conic =>
          val p1 = (pointBuf.x(), pointBuf.y())
          pointBuf.position(1)
          val p2 = (pointBuf.x(), pointBuf.y())
          pointBuf.position(2)
          val p3 = (pointBuf.x(), pointBuf.y())
          f(ConicElement(p1, p2, p3, sk_path_iter_conic_weight(iter)))
        case Verb.Cubic =>
          val p1 = (pointBuf.x(), pointBuf.y())
          pointBuf.position(1)
          val p2 = (pointBuf.x(), pointBuf.y())
          pointBuf.position(2)
          val p3 = (pointBuf.x(), pointBuf.y())
          pointBuf.position(3)
          val p4 = (pointBuf.x(), pointBuf.y())
          f(CubicElement(p1, p2, p3, p4))
      }
    }
    sk_path_iter_destroy(iter)
  }
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

  object Verb extends Enumeration {
    type Verb = Value
    val Move = Value(MOVE_SK_PATH_VERB)
    val Line = Value(LINE_SK_PATH_VERB)
    val Quad = Value(QUAD_SK_PATH_VERB)
    val Conic = Value(CONIC_SK_PATH_VERB)
    val Cubic = Value(CUBIC_SK_PATH_VERB)
    val Close = Value(CLOSE_SK_PATH_VERB)
    val Done = Value(DONE_SK_PATH_VERB)
  }

  sealed trait PathElement
  case class MoveElement(p: (Float, Float)) extends PathElement
  case class LineElement(p1: (Float, Float), p2: (Float, Float)) extends PathElement
  case class QuadElement(p1: (Float, Float), p2: (Float, Float), p3: (Float, Float)) extends PathElement
  case class ConicElement(p1: (Float, Float), p2: (Float, Float), p3: (Float, Float), weight: Float) extends PathElement
  case class CubicElement(p1: (Float, Float), p2: (Float, Float), p3: (Float, Float), p4: (Float, Float)) extends PathElement
  case class CloseElement() extends PathElement
}
