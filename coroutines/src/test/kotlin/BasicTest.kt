import org.junit.jupiter.api.Test

class BasicTest {
    @Test
    fun testBreak() {
        run loop@{
            listOf(1, 2, 3, 4, 5).forEach {
                if (it >= 3) return@loop // non-local return from the lambda passed to run
                print(it)
            }
        }
        print(" done with nested loop")
    }

    @Test
    fun testSort() {
        println(listOf("Aa d", "Ac g", "aA", "aR", "Ab")
            .sortedBy { it })

    }

    data class TestClass(
        val s: String
    )

    data class MutableListInside(
        val list: MutableList<TestClass>
    )

    // After copy MutableList will change to ArrayList
    @Test
    fun testMutableListCopy() {
        val testData = MutableListInside(mutableListOf(TestClass("abc")))

        val copy = testData.copy()

        println(copy)
    }
}