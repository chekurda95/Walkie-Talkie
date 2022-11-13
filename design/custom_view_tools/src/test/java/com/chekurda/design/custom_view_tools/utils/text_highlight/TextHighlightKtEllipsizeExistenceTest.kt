package com.chekurda.design.custom_view_tools.utils.text_highlight

import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import ru.tensor.sbis.common.testing.params
import com.chekurda.design.custom_view_tools.utils.hasSimpleEllipsize
import com.chekurda.design.custom_view_tools.utils.hasSymbolEllipsize
import com.chekurda.design.custom_view_tools.utils.lastTextIndex

/**
 * Тесты методов [hasSimpleEllipsize], [hasSymbolEllipsize], [lastTextIndex].
 *
 * @author vv.chekurda
 */
@RunWith(JUnitParamsRunner::class)
class TextHighlightKtEllipsizeExistenceTest {

    @Test
    @Parameters(value = [
        "TestString...",
        "Test TestString ...",
        "Test.......",
        "... ......."
    ])
    fun `Simple ellipsize is exists`(text: CharSequence) {
        assertTrue(text.hasSimpleEllipsize)
    }

    @Test
    @Parameters(value = [
        "Test.t.",
        "Test...t",
        "Test..",
        "Test ..",
        "Test…",
        "...Test",
        "..",
        ""
    ])
    fun `Simple ellipsize is not exists`(text: CharSequence) {
        assertFalse(text.hasSimpleEllipsize)
    }

    /**
     * Набор параметров тримит строки, поэтому нужна отдельная проверка.
     */
    @Test
    fun `Simple ellipsize is not exists, when last symbol is space`() {
        val stringWithLastSpace = "Test... "

        assertFalse(stringWithLastSpace.hasSimpleEllipsize)
    }

    @Test
    @Parameters(value = [
        "Test…",
        "Test……",
        "Test …",
        "Test… ",
        "… …"
    ])
    fun `Symbol ellipsize is exists`(text: CharSequence) {
        assertTrue(text.hasSymbolEllipsize)
    }

    @Test
    @Parameters(value = [
        "…Test",
        "Test….",
        "Tes…t",
        ""
    ])
    fun `Symbol ellipsize is not exists`(text: CharSequence) {
        assertFalse(text.hasSymbolEllipsize)
    }

    /**
     * Набор параметров тримит строки, поэтому нужна отдельная проверка.
     */
    @Test
    fun `Symbol ellipsize is not exists, when last symbol is space`() {
        val stringWithLastSpace = "Test… "

        assertFalse(stringWithLastSpace.hasSimpleEllipsize)
    }

    @Test
    @Parameters(method = "paramsForExpectedLastTextPosition")
    fun `Last text position before ellipsize equals expected`(text: CharSequence, expected: Int) {
        assertEquals(expected, text.lastTextIndex)
    }

    @Suppress("unused")
    private fun paramsForExpectedLastTextPosition() = params {
        add("0123...", 3)
        add("0123....", 4)
        add("0123..", 5)
        add("0123... ", 7)
        add("012 456 ...", 7)
        add("012… ", 4)
        add("0123…", 3)
        add("0123……", 4)
        add("012…4", 4)
        add("01…34…", 4)
    }
}