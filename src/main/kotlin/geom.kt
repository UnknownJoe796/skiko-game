import org.jetbrains.skia.Point
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

data class Circle(val center: Vector, val radius: Float)

data class Vector(var x: Float = 0f, var y: Float = 0f) {

    companion object {
        val ZERO = Vector(0f, 0f)
    }

    operator fun plusAssign(point: Point) {
        x += point.x
        y += point.y
    }

    operator fun plusAssign(point: Vector) {
        x += point.x
        y += point.y
    }

    operator fun plus(point: Point): Vector = Vector(x + point.x, y + point.y)
    operator fun plus(point: Vector): Vector = Vector(x + point.x, y + point.y)

    operator fun minusAssign(point: Point) {
        x -= point.x
        y -= point.y
    }

    operator fun minusAssign(point: Vector) {
        x -= point.x
        y -= point.y
    }

    operator fun minus(point: Point): Vector = Vector(x - point.x, y - point.y)
    operator fun minus(point: Vector): Vector = Vector(x - point.x, y - point.y)

    operator fun timesAssign(value: Float) {
        x *= value
        y *= value
    }

    operator fun timesAssign(point: Point) {
        x *= point.x
        y *= point.y
    }

    operator fun timesAssign(point: Vector) {
        x *= point.x
        y *= point.y
    }

    operator fun times(value: Float): Vector = Vector(x * value, y * value)
    operator fun times(point: Point): Vector = Vector(x * point.x, y * point.y)
    operator fun times(point: Vector): Vector = Vector(x * point.x, y * point.y)

    operator fun divAssign(value: Float) {
        x /= value
        y /= value
    }

    operator fun divAssign(point: Point) {
        x /= point.x
        y /= point.y
    }

    operator fun divAssign(point: Vector) {
        x /= point.x
        y /= point.y
    }

    operator fun div(value: Float): Vector = Vector(x / value, y / value)
    operator fun div(point: Point): Vector = Vector(x / point.x, y / point.y)
    operator fun div(point: Vector): Vector = Vector(x / point.x, y / point.y)

    infix fun distanceTo(other: Point): Float = sqrt((this.x - other.x).squared() + (this.y - other.y).squared())
    infix fun distanceTo(other: Vector): Float = sqrt((this.x - other.x).squared() + (this.y - other.y).squared())
    infix fun dot(other: Vector): Float = x * other.x + y * other.y
    val length: Float get() = sqrt((this.x).squared() + (this.y).squared())
    val angle: Float get() = atan2(y, x)

    val orthogonal: Vector get() = Vector(y, -x)
    fun orthogonalAssign() {
        val temp = -x
        x = y
        y = temp
    }

    fun normalize() {
        this /= length
    }

    fun set(other: Vector) {
        this.x = other.x
        this.y = other.y
    }

    fun toPoint(): Point = Point(x, y)
}

data class FloatRange(override var start: Float = 0f, override var endInclusive: Float = 0f) :
    ClosedFloatingPointRange<Float> {
    override fun lessThanOrEquals(a: Float, b: Float): Boolean = a <= b
    operator fun plusAssign(value: Float) {
        start += value
        endInclusive += value
    }
}

infix fun FloatRange.overlaps(other: FloatRange): Boolean =
    this.endInclusive <= other.start || this.start >= other.endInclusive

infix fun FloatRange.ejection(other: FloatRange): Float {
    val a = other.start - this.endInclusive
    val b = other.endInclusive - this.start
    // START          |---|
    // OTHER   |----|
    if (this.start > other.endInclusive) return Float.NaN
    if (this.endInclusive < other.start) return Float.NaN
    return if (-a < b) a else b
}

fun Float.squared(): Float = this * this

typealias Polygon = List<Vector>

fun Polygon.edgeVectors(using: Vector): Sequence<Vector> {
    return indices.asSequence().map {
        val first = this[it]
        val second = this[(it + 1) % size]
        using.x = second.x - first.x
        using.y = second.y - first.y
        using
    }
}

fun Polygon.project(axis: Vector, reuse: FloatRange = FloatRange()): FloatRange {
    reuse.endInclusive = Float.NEGATIVE_INFINITY
    reuse.start = Float.POSITIVE_INFINITY
    asSequence().map { it dot axis }.forEach {
        if (it > reuse.endInclusive) reuse.endInclusive = it
        if (it < reuse.start) reuse.start = it
    }
    return reuse
}

fun Polygon.separatingAxisTheorem(offset: Vector, other: Polygon, otherOffset: Vector): Vector? {
    val reuse = Vector()
    var bestLen = Float.POSITIVE_INFINITY
    val best = Vector(0f, 0f)
    val range1 = FloatRange()
    val range2 = FloatRange()
    edgeVectors(reuse).plus(other.edgeVectors(reuse))
        .map { it.orthogonalAssign(); it.normalize(); it }
        .forEach {
            project(it, range1)
            range1 += offset dot it
            other.project(it, range2)
            range2 += otherOffset dot it
            val overlap = range1 ejection range2
            val absOverlap = abs(overlap)
            if (overlap.isNaN()) return null
            if (absOverlap < bestLen) {
                bestLen = abs(overlap)
                best.set(it)
                best *= overlap
            }
        }
    return best
}

infix fun Polygon.separatingAxisTheorem(other: Polygon): Vector? =
    separatingAxisTheorem(Vector.ZERO, other, Vector.ZERO)

fun Polygon.separatingAxisTheorem(offset: Vector, other: Vector): Vector? {
    val reuse = Vector()
    var bestLen = Float.POSITIVE_INFINITY
    val best = Vector(0f, 0f)
    val range1 = FloatRange()
    val range2 = FloatRange()
    edgeVectors(reuse)
        .map { it.orthogonalAssign(); it.normalize(); it }
        .forEach {
            project(it, range1)
            range1 += offset dot it
            range2.start = other dot it
            range2.endInclusive = range2.start
            val overlap = range1 ejection range2
            val absOverlap = abs(overlap)
            if (overlap.isNaN()) return null
            if (absOverlap < bestLen) {
                bestLen = abs(overlap)
                best.set(it)
                best *= overlap
            }
        }
    return best
}

infix fun Polygon.separatingAxisTheorem(other: Vector): Vector? = separatingAxisTheorem(Vector.ZERO, other)

fun Vector.separatingAxisTheorem(other: Polygon, otherOffset: Vector): Vector? =
    other.separatingAxisTheorem(otherOffset, this)?.also { it.timesAssign(-1f) }

infix fun Vector.separatingAxisTheorem(other: Polygon): Vector? = separatingAxisTheorem(other, Vector.ZERO)


fun Polygon.separatingAxisTheorem(offset: Vector, other: Circle, otherOffset: Vector): Vector? {
    val reuse = Vector()
    var bestLen = Float.POSITIVE_INFINITY
    val best = Vector(0f, 0f)
    val range1 = FloatRange()
    val range2 = FloatRange()
    edgeVectors(reuse).map { it.orthogonalAssign(); it }.plus(this.asSequence().map {
        reuse.x = (it.x + offset.x) - (other.center.x + otherOffset.x)
        reuse.y = (it.y + offset.y) - (other.center.y + otherOffset.y)
        reuse
    })
        .map { it.normalize(); it }
        .forEach {
            project(it, range1)
            range1 += offset dot it
            val center = other.center dot it
            range2.start = center - other.radius
            range2.endInclusive = center + other.radius
            range2 += otherOffset dot it
            val overlap = range1 ejection range2
            val absOverlap = abs(overlap)
            if (overlap.isNaN()) return null
            if (absOverlap < bestLen) {
                bestLen = abs(overlap)
                best.set(it)
                best *= overlap
            }
        }
    return best
}

infix fun Polygon.separatingAxisTheorem(other: Circle): Vector? = separatingAxisTheorem(Vector.ZERO, other, Vector.ZERO)

fun Circle.separatingAxisTheorem(offset: Vector, other: Polygon, otherOffset: Vector): Vector? =
    other.separatingAxisTheorem(otherOffset, this, offset)?.also { it.timesAssign(-1f) }

infix fun Circle.separatingAxisTheorem(other: Polygon): Vector? = separatingAxisTheorem(Vector.ZERO, other, Vector.ZERO)