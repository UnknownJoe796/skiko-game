import org.jetbrains.skia.Canvas
import org.jetbrains.skiko.GenericSkikoView
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkikoView

interface GameComponent {
    fun step(deltaSeconds: Float)
    fun render(canvas: Canvas, width: Int, height: Int, deltaSeconds: Float)
}

fun SkiaLayer.setRootComponent(gameComponent: GameComponent) {
    addView(GenericSkikoView(this, object : SkikoView {
        var lastTime = 0L
        override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
            Controllers.step()
            if(lastTime == 0L) {
                gameComponent.render(canvas, width, height, 0f)
                lastTime = nanoTime
            } else {
                val deltaSeconds = ((nanoTime - lastTime).toDouble() / 1_000_000_000).toFloat()
                gameComponent.step(deltaSeconds)
                gameComponent.render(canvas, width, height, deltaSeconds)
                lastTime = nanoTime
            }
        }
    }))
}