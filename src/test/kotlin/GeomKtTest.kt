import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GeomKtTest {
    @Test fun test() {
        assertEquals(
            null,
            listOf(
                Vector(0f, 0f),
                Vector(1f, 0f),
                Vector(1f, 1f),
                Vector(0f, 1f),
            ) separatingAxisTheorem listOf(
                Vector(0f + 2f, 0f),
                Vector(1f + 2f, 0f),
                Vector(1f + 2f, 1f),
                Vector(0f + 2f, 1f),
            )
        )
        assertEquals(
            Vector(-.25f, 0f),
            listOf(
                Vector(0f, 0f),
                Vector(1f, 0f),
                Vector(1f, 1f),
                Vector(0f, 1f),
            ) separatingAxisTheorem listOf(
                Vector(0.75f, 0.5f),
                Vector(1.75f, 0.5f),
                Vector(1.75f, 1.5f),
                Vector(0.75f, 1.5f),
            )
        )

        val square = listOf(
            Vector(0f, 0f),
            Vector(1f, 0f),
            Vector(1f, 1f),
            Vector(0f, 1f),
        )
        assertEquals(null, square.separatingAxisTheorem(Vector(0f, 0f), square, Vector(2f, 0f)))
        assertEquals(Vector(-.25f, 0f), square.separatingAxisTheorem(Vector(0f, 0f), square, Vector(.75f, 0f)))
    }
}