import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Path

class Player(val controller: Controller2): GameComponent {

    val myShape = listOf(
        Vector(20f, 0f),
        Vector(0f, 20f),
        Vector(-20f, 0f),
        Vector(0f, -20f),
    )
    val myShapeRendering = Path().apply {
        this.moveTo(myShape.first().x, myShape.first().y)
        myShape.forEach { lineTo(it.x, it.y) }
        closePath()
    }
    val circleShape = Circle(Vector.ZERO, 50f)
    val position = Vector(0f, 0f)

    val blockedOut = listOf(
        Vector(100f, 100f),
        Vector(200f, 100f),
        Vector(250f, 150f),
        Vector(200f, 200f),
        Vector(100f, 200f),
    )
    val blockedOutRendering = Path().apply {
        this.moveTo(blockedOut.first().x, blockedOut.first().y)
        blockedOut.forEach { lineTo(it.x, it.y) }
        closePath()
    }

    override fun step(deltaSeconds: Float) {
        position += controller.primary * 250f * deltaSeconds
        myShape.separatingAxisTheorem(position, blockedOut, Vector.ZERO)?.let { position += it }
//        circleShape.separatingAxisTheorem(position, blockedOut, Vector.ZERO)?.let { position += it }
    }

    val paint = Paint().apply {
        color = Color.makeARGB(64, 255, 0, 0)
    }

    override fun render(canvas: Canvas, width: Int, height: Int, deltaSeconds: Float) {
        canvas.save()
        canvas.translate(position.x, position.y)
        canvas.drawPath(myShapeRendering, paint)
//        canvas.drawCircle(circleShape.center.x, circleShape.center.y, circleShape.radius, paint)
        canvas.restore()

        canvas.drawPath(blockedOutRendering, paint)
    }
}