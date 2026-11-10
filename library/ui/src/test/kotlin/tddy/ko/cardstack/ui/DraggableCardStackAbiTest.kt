package tddy.ko.cardstack.ui

import androidx.compose.runtime.Composer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Guard for the 1.0.1 JVM entry point of DraggableCardStack.
 */
class DraggableCardStackAbiTest {

    @Test
    fun legacyJvmEntry_isKept() {
        val facade = Class.forName("tddy.ko.cardstack.ui.CardStackKt")
        val method = facade.getDeclaredMethod(
            "DraggableCardStack-EUb7tLY",
            List::class.java,
            Float::class.javaPrimitiveType,
            Float::class.javaPrimitiveType,
            CardAlignment::class.java,
            DragAlignment::class.java,
            Function4::class.java,
            Composer::class.java,
            Int::class.javaPrimitiveType,
        )
        assertNotNull(method)
        assertEquals(Void.TYPE, method.returnType)
    }
}
