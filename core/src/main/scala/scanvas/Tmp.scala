package scanvas
import org.bytedeco.javacpp.Skia.{sk_matrix_t, sk_point_t, sk_rect_t}

private[scanvas] object Tmp {
  val rect1 = new sk_rect_t
  val rect2 = new sk_rect_t
  val mat1 = new sk_matrix_t
  val point1 = new sk_point_t
}
