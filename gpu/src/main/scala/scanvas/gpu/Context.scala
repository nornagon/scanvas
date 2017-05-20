package scanvas.gpu

import org.bytedeco.javacpp.Skia._
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL13._
import org.lwjgl.opengl.GL30._
import scanvas.Surface

class Context private[scanvas] (grContext: gr_context_t) {
  /** Make a GPU-backed surface which renders into the given FBO. */
  // TODO: I don't think passing windowWidth/width is quite right here? research how best to handle physical vs logical px
  def makeSurfaceForFramebuffer(fbo: Int, windowWidth: Int, windowHeight: Int, width: Int, height: Int): Surface = {
    val prevFBO = glGetInteger(GL_FRAMEBUFFER_BINDING)
    val samples = new Array[Int](1)
    val stencilBits = new Array[Int](1)
    try {
      glBindFramebuffer(GL_FRAMEBUFFER, fbo)
      glGetIntegerv(GL_SAMPLES, samples)
      glGetIntegerv(GL_STENCIL_BITS, stencilBits)
    } finally {
      glBindFramebuffer(GL_FRAMEBUFFER, prevFBO)
    }
    val rtDesc = new gr_backend_rendertarget_desc_t()
      .fWidth(width)
      .fHeight(height)
      .fConfig(RGBA_8888_GR_PIXEL_CONFIG)
      .fOrigin(BOTTOM_LEFT_GR_SURFACE_ORIGIN)
      .fSampleCnt(samples(0))
      .fStencilBits(stencilBits(0))
      .fRenderTargetHandle(fbo)

    // dummy, just to handle inability to sk_canvas_get_imageinfo()
    val info = new sk_imageinfo_t()
      .width(windowWidth)
      .height(windowHeight)

    new Surface(sk_surface_new_backend_render_target(grContext, rtDesc, null), info)
  }

  /** Make a new GPU-backed offscreen surface. */
  def makeSurface(width: Int, height: Int): Surface = {
    val info = new sk_imageinfo_t()
      .width(width)
      .height(height)
      .colorType(sk_colortype_get_default_8888())
      .alphaType(PREMUL_SK_ALPHATYPE)
    new Surface(sk_surface_new_render_target(grContext, false, info, 0, null), info)
  }
}

object Context {
  lazy private val nativeGlInterface = gr_glinterface_create_native_interface()
  def openGlBackendForCurrentContext: Context = {
    val grContext = gr_context_create_with_defaults(OPENGL_GR_BACKEND, 0)//nativeGlInterface.address())
    new Context(grContext)
  }
}
