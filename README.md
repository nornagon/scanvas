# scanvas

scanvas is a lightning-fast vector graphics library for Scala, backed by the
same engine that powers the Android UI and the `<canvas>` tag in Chrome:
[Skia](https://skia.org).

scanvas uses [javacpp](https://github.com/bytedeco/javacpp) to bind [mono's
fork](https://github.com/mono/skia) of Skia, bringing you the full power of
Skia with a friendly, idiomatic Scala API.

As well as using scanvas to draw and save flat `png` files, you can also make
use of `scanvas-gpu` and `lwjgl` to render real-time graphics, for instance in
a game.

## Example

```scala
import scanvas._
import java.io.FileOutputStream

def main = {
  val surface = Surface.newRaster(640, 480)
  val canvas = surface.canvas
  canvas.clear(Color.RGB(1f, 0f, 0f))
  val paint = Paint.blank.setColor(Color.RGB(0f, 1f, 0f))
  canvas.drawCircle(cx = 320, cy = 240, radius = 100, paint = paint)
  surface.snapshot.writeAsPngTo(new FileOutputStream("out.png"))
}
```
