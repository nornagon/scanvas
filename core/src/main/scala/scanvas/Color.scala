package scanvas

class Color {

}

object Color {
  def RGB(r: Float, g: Float, b: Float): Int = RGBA(r, g, b, 1f)
  def RGB(r: Int, g: Int, b: Int): Int = RGBA(r, g, b, 0xff)
  def RGBA(r: Float, g: Float, b: Float, a: Float): Int =
    RGBA((r * 255).round, (g * 255).round, (b * 255).round, (a * 255).round)
  def RGBA(r: Int, g: Int, b: Int, a: Int): Int = (a << 24) | (r << 16) | (g << 8) | b
  val Black = RGB(0, 0, 0)
  val White = RGB(0xff, 0xff, 0xff)
}
