import org.junit.Test

class WhenTest {
    @Test
    fun testWhen(){
        methodWhen(2)
    }

    fun methodWhen(count: Int){
        when(count){
            1 -> println("one")
            2,3 -> println("2 or 3")
        }
    }
}