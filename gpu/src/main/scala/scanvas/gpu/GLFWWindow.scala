package scanvas.gpu

import org.lwjgl.BufferUtils
import org.lwjgl.glfw.GLFW._
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_BINDING
import org.lwjgl.opengl._
import org.lwjgl.system.MemoryUtil.NULL
import scanvas.{Canvas, Surface}


object GLFWWindow {
  if (!glfwInit()) {
    throw new RuntimeException("Failed to init GLFW.")
  }
  GLFWErrorCallback.createPrint().set()
}

class GLFWWindow(val width: Int, val height: Int, title: String) {
  GLFWWindow // reference the object to trigger initialization code

  private val handle = glfwCreateWindow(width, height, title, NULL, NULL)
  private var grContext: Context = _
  private var renderSurface: Surface = _
  private var mouseX: Double = 0
  private var mouseY: Double = 0

  var onMouseMove: (Double, Double) => Unit = _
  var onMouseDown: (Double, Double, Int) => Unit = _
  var onMouseUp: (Double, Double, Int) => Unit = _
  var onKeyDown: (Int, Int, Int) => Unit = _
  var onKeyUp: (Int, Int, Int) => Unit = _
  var onCharacter: (Int) => Unit = _
  var onClose: () => Unit = _

  {
    glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE)
    glfwWindowHint(GLFW_RED_BITS, 8)
    glfwWindowHint(GLFW_BLUE_BITS, 8)
    glfwWindowHint(GLFW_GREEN_BITS, 8)
    glfwWindowHint(GLFW_DOUBLEBUFFER, 1)
    glfwWindowHint(GLFW_DEPTH_BITS, 0)
    glfwWindowHint(GLFW_STENCIL_BITS, 8)
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 0)

    glfwMakeContextCurrent(handle)
    GL.createCapabilities(/* forwardCompatible = */true)
    glfwSwapInterval(1)
    val (fbWidth, fbHeight) = framebufferSize
    glViewport(0, 0, fbWidth, fbHeight)
    glClearColor(1, 1, 1, 1)
    glClearStencil(0)
    glClear(GL_COLOR_BUFFER_BIT | GL_STENCIL_BUFFER_BIT)

    grContext = Context.openGlBackendForCurrentContext
    renderSurface = grContext.makeSurfaceForFramebuffer(
      glGetInteger(GL_FRAMEBUFFER_BINDING), fbWidth, fbHeight)
    // To handle high-dpi displays
    renderSurface.canvas.scale(fbWidth.toFloat / width, fbHeight.toFloat / height)

    val bufX = BufferUtils.createDoubleBuffer(1)
    val bufY = BufferUtils.createDoubleBuffer(1)
    glfwGetCursorPos(handle, bufX, bufY)
    mouseX = bufX.get(0)
    mouseY = bufY.get(0)
    glfwSetCursorPosCallback(handle, (_: Long, x: Double, y: Double) => {
      mouseX = x
      mouseY = y
      if (onMouseMove != null)
        onMouseMove(x, y)
    })
    glfwSetMouseButtonCallback(handle, (_: Long, button: Int, action: Int, mods: Int) => {
      action match {
        case GLFW_PRESS =>
          if (onMouseDown != null)
            onMouseDown(mouseX, mouseY, button)
        case GLFW_RELEASE =>
          if (onMouseUp != null)
            onMouseUp(mouseX, mouseY, button)
      }
    })
    glfwSetKeyCallback(handle, (_: Long, key: Int, scancode: Int, action: Int, mods: Int) => {
      action match {
        case GLFW_PRESS =>
          if (onKeyDown != null)
            onKeyDown(key, scancode, mods)
        case GLFW_RELEASE =>
          if (onKeyUp != null)
            onKeyUp(key, scancode, mods)
        case GLFW_REPEAT =>
      }
    })
    glfwSetCharCallback(handle, (_, codepoint: Int) => {
      if (onCharacter != null)
        onCharacter(codepoint)
    })
    glfwSetWindowCloseCallback(handle, (_: Long) => {
      if (onClose != null)
        onClose()
    })
  }

  def show(): Unit = glfwShowWindow(handle)

  def framebufferSize: (Int, Int) = {
    val fbW = BufferUtils.createIntBuffer(1)
    val fbH = BufferUtils.createIntBuffer(1)
    glfwGetFramebufferSize(handle, fbW, fbH)
    (fbW.get(0), fbH.get(0))
  }

  def swapBuffers(): Unit = {
    glfwSwapBuffers(handle)
    glfwPollEvents()
  }

  def shouldClose: Boolean = glfwWindowShouldClose(handle)
  def canvas: Canvas = renderSurface.canvas
}
