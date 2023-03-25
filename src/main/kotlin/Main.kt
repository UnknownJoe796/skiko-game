import org.jetbrains.skia.*
import org.jetbrains.skiko.*
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.WindowConstants

// Kotlin starts here at the root main functions
fun main(args: Array<String>) {

    // Skia is our rendering library
    val skiaLayer = SkiaLayer()

    // Here we set out own GameComponent to be rendered
    skiaLayer.setRootComponent(StarterTest())

    // Swing creates the window.
    SwingUtilities.invokeLater {
        val window = JFrame("Skiko example").apply {
            defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            preferredSize = Dimension(800, 600)
        }
        skiaLayer.attachTo(window.contentPane)
        skiaLayer.needRedraw()
        window.pack()
        window.isVisible = true
    }
}


// Do you stuff here.
class StarterTest : GameComponent {
    override fun step(deltaSeconds: Float) {

    }

    override fun render(canvas: Canvas, width: Int, height: Int, deltaSeconds: Float) {
        val paint = Paint().apply {
            color = Color.BLACK
        }

        canvas.clear(Color.WHITE)
        canvas.drawCircle(width / 2f, height / 2f, 25f, paint)

    }
}
