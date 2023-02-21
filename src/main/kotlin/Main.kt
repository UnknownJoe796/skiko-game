import org.jetbrains.skia.*
import org.jetbrains.skiko.*
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.WindowConstants

fun main(args: Array<String>) {
    val skiaLayer = SkiaLayer()
//    skiaLayer.addView(GenericSkikoView(skiaLayer, object : SkikoView {
//        val font = Font(Typeface.makeDefault(), 24f)
//
//        var gamepad: Controller2? = null
//
//        val paint = Paint().apply {
//            color = Color.RED
//        }
//        val paint2 = Paint().apply {
//            this.setStroke(true)
//            color = Color.RED
//        }
//
//        override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
//            canvas.clear(Color.CYAN)
//            val ts = nanoTime / 5_000_000
//            canvas.drawCircle((ts % width).toFloat(), (ts % height).toFloat(), 20f, paint)
////            canvas.drawTextBlob(TextBlobBuilder().appendRun(font, "Test Text that's too long", 0f, 0f, Rect(0f, 0f, 100f, 100f)).build()!!, 100f, 100f, paint)
//            val paragraph = ParagraphBuilder(
//                ParagraphStyle(),
//                FontCollection()
//                    .setDefaultFontManager(FontMgr.default)
//            )
//                .pushStyle(TextStyle().setColor(0xFF000000.toInt()))
//                .addText("Some Text\n with multiple \n lines")
//                .popStyle()
//                .build()
//            paragraph.layout(100f)
//            paragraph.paint(canvas, 100f, 100f)
////            canvas.drawPicture()
//            canvas.drawRect(Rect(100f, 100f, 200f, 100f + paragraph.height), paint2)
////            canvas.drawText
////            canvas.drawTextLine(TextLine.make("testnl", font), 100f, 100f, paint)
//        }
//    }))
    skiaLayer.setRootComponent(GameControllerTest())
    SwingUtilities.invokeLater {
        val window = JFrame("Skiko example").apply {
            defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            preferredSize = Dimension(800, 600)
        }
        skiaLayer.attachTo(window.contentPane)
        skiaLayer.needRedraw()
//        skiaLayer.fullscreen = true
        window.pack()
        window.isVisible = true
    }
}

class GameControllerTest : GameComponent {
    var player: Player? = null
    var controller: Controller2? = null

    val font = Font(Typeface.makeDefault(), 24f)
    val textPaint = Paint().apply {
        color = Color.BLACK
    }
    val paint = Paint().apply {
        color = Color.BLUE
    }
    val okPaint = Paint().apply {
        color = Color.makeRGB(0, 200, 0)
    }
    val negativePaint = Paint().apply {
        color = Color.RED
    }
    val stroke = Paint().apply {
        color = Color.BLUE
        strokeWidth = 2f
        setStroke(true)
    }
    val strokeDeadZoned = Paint().apply {
        color = Color.makeARGB(128, 0, 0, 255)
        strokeWidth = 2f
        setStroke(true)
    }

    override fun step(deltaSeconds: Float) {
        if (controller == null) {
            controller = Controllers.gamepads.onEach { Controllers.interested(it) }.firstOrNull { it.active }?.also {
                player = Player(it)
            }
        }
        player?.step(deltaSeconds)
    }

    override fun render(canvas: Canvas, width: Int, height: Int, deltaSeconds: Float) {
        canvas.clear(Color.WHITE)
        player?.render(canvas, width, height, deltaSeconds)
        canvas.drawString(controller?.name ?: "Press a button", 50f, 50f, font, textPaint)
        controller?.components?.forEachIndexed { index, it ->
            val tl = Point((index % 8) * 100f, (index / 8) * 100f + 200)
            val center = tl.offset(50f, 50f)
            val br = tl.offset(100f, 100f)
            when (it.type) {
                ComponentType.Axis -> {
                    canvas.drawCircle(center.x, center.y, 25f * it.deadZone, strokeDeadZoned)
                    canvas.drawCircle(center.x, center.y, 25f * it.valueWithoutDeadzone, if (it.valueWithoutDeadzone < 0f) negativePaint else paint)
                    canvas.drawCircle(center.x, center.y, 25f, stroke)
                }

                ComponentType.POV -> {
                    canvas.drawCircle(center.x + it.pov.x * 12f, center.y + it.pov.y * 12f, 12f, paint)
                    canvas.drawCircle(center.x, center.y, 25f, stroke)
                }

                ComponentType.Button,
                ComponentType.Key -> {
                    canvas.drawCircle(center.x, center.y, 25f * it.valueWithoutDeadzone, paint)
                    canvas.drawCircle(center.x, center.y, 25f, stroke)
                }

                ComponentType.Other -> {}
            }
            canvas.drawString(it.name, center.x, br.y, font, textPaint)
        }

        var currentX = 50f
        val currentY = 150f
        run {
            val center = Point(currentX, currentY)
            currentX += 100f
            canvas.drawCircle(
                center.x + (controller?.primaryX?.valueWithoutDeadzone ?: 0f) * 12f,
                center.y + (controller?.primaryY?.valueWithoutDeadzone ?: 0f) * 12f,
                12f,
                if (controller?.primaryThumb?.down == true) negativePaint else paint
            )
            canvas.drawCircle(center.x, center.y, 25f, stroke)
        }
        run {
            val center = Point(currentX, currentY)
            currentX += 100f
            canvas.drawCircle(
                center.x + (controller?.secondaryX?.valueWithoutDeadzone ?: 0f) * 12f,
                center.y + (controller?.secondaryY?.valueWithoutDeadzone ?: 0f) * 12f,
                12f,
                if (controller?.secondaryThumb?.down == true) negativePaint else paint
            )
            canvas.drawCircle(center.x, center.y, 25f, stroke)
        }
        run {
            val center = Point(currentX, currentY)
            currentX += 100f
            canvas.drawCircle(
                center.x + (controller?.dPad?.pov?.x ?: 0f) * 12f,
                center.y + (controller?.dPad?.pov?.y ?: 0f) * 12f,
                12f,
                paint
            )
            canvas.drawCircle(center.x, center.y, 25f, stroke)
        }
        run {
            val center = Point(currentX, currentY)
            currentX += 100f
            run {
                val c2 = center.offset(-25f, 0f)
                canvas.drawCircle(c2.x, c2.y, 12f * (controller?.menuAlt?.valueWithoutDeadzone ?: 0f), paint)
                canvas.drawCircle(c2.x, c2.y, 12f, stroke)
            }
            run {
                val c2 = center.offset(25f, 0f)
                canvas.drawCircle(c2.x, c2.y, 12f * (controller?.menu?.valueWithoutDeadzone ?: 0f), paint)
                canvas.drawCircle(c2.x, c2.y, 12f, stroke)
            }
            run {
                val c2 = center.offset(-8f, 12f)
                canvas.drawCircle(c2.x, c2.y, 4f * (controller?.media?.valueWithoutDeadzone ?: 0f), paint)
                canvas.drawCircle(c2.x, c2.y, 4f, stroke)
            }
            run {
                val c2 = center.offset(8f, 12f)
                canvas.drawCircle(c2.x, c2.y, 4f * (controller?.system?.valueWithoutDeadzone ?: 0f), paint)
                canvas.drawCircle(c2.x, c2.y, 4f, stroke)
            }
        }
        run {
            val center = Point(currentX, currentY)
            currentX += 100f
            run {
                val c2 = center.offset(0f, 25f)
                canvas.drawCircle(c2.x, c2.y, 12f * (controller?.primaryAction?.valueWithoutDeadzone ?: 0f), paint)
                canvas.drawCircle(c2.x, c2.y, 12f, stroke)
            }
            run {
                val c2 = center.offset(25f, 0f)
                canvas.drawCircle(c2.x, c2.y, 12f * (controller?.primaryAlt?.valueWithoutDeadzone ?: 0f), paint)
                canvas.drawCircle(c2.x, c2.y, 12f, stroke)
            }
            run {
                val c2 = center.offset(0f, -25f)
                canvas.drawCircle(c2.x, c2.y, 12f * (controller?.secondaryAlt?.valueWithoutDeadzone ?: 0f), paint)
                canvas.drawCircle(c2.x, c2.y, 12f, stroke)
            }
            run {
                val c2 = center.offset(-25f, 0f)
                canvas.drawCircle(c2.x, c2.y, 12f * (controller?.secondaryAction?.valueWithoutDeadzone ?: 0f), paint)
                canvas.drawCircle(c2.x, c2.y, 12f, stroke)
            }
            run {
                val c2 = center.offset(0f, 0f)
                canvas.drawCircle(c2.x, c2.y, 8f * (controller?.ok?.valueWithoutDeadzone ?: 0f), okPaint)
                canvas.drawCircle(c2.x, c2.y, 8f * (controller?.cancel?.valueWithoutDeadzone ?: 0f), negativePaint)
            }
        }
        run {
            val center = Point(currentX, currentY)
            currentX += 100f
            run {
                val c2 = center.offset(-25f, 15f)
                canvas.drawRect(
                    Rect(
                        c2.x - 20f,
                        c2.y - 12f,
                        c2.x + 20f,
                        c2.y + -12f + (24f * (controller?.leftTrigger?.valueWithoutDeadzone ?: 0f))
                    ),
                    paint
                )
                canvas.drawRect(
                    Rect(
                        c2.x - 20f,
                        c2.y - 12f,
                        c2.x + 20f,
                        c2.y + -12f + 24f
                    ),
                    stroke
                )

            }
            run {
                val c2 = center.offset(25f, 15f)
                canvas.drawRect(
                    Rect(
                        c2.x - 20f,
                        c2.y - 12f,
                        c2.x + 20f,
                        c2.y + -12f + (24f * (controller?.rightTrigger?.valueWithoutDeadzone ?: 0f))
                    ),
                    paint
                )
                canvas.drawRect(
                    Rect(
                        c2.x - 20f,
                        c2.y - 12f,
                        c2.x + 20f,
                        c2.y + -12f + 24f
                    ),
                    stroke
                )

            }
            run {
                val c2 = center.offset(-25f, -25f)
                canvas.drawRect(
                    Rect(
                        c2.x - 20f,
                        c2.y - 8f,
                        c2.x + 20f,
                        c2.y + -8f + (16f * (controller?.leftBumper?.valueWithoutDeadzone ?: 0f))
                    ),
                    paint
                )
                canvas.drawRect(
                    Rect(
                        c2.x - 20f,
                        c2.y - 8f,
                        c2.x + 20f,
                        c2.y + -8f + 16f
                    ),
                    stroke
                )

            }
            run {
                val c2 = center.offset(25f, -25f)
                canvas.drawRect(
                    Rect(
                        c2.x - 20f,
                        c2.y - 8f,
                        c2.x + 20f,
                        c2.y + -8f + (16f * (controller?.rightBumper?.valueWithoutDeadzone ?: 0f))
                    ),
                    paint
                )
                canvas.drawRect(
                    Rect(
                        c2.x - 20f,
                        c2.y - 8f,
                        c2.x + 20f,
                        c2.y + -8f + 16f
                    ),
                    stroke
                )

            }
        }
    }

}