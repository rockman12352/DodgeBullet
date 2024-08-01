package com.csg.codeit.model

import io.mockk.mockkClass
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DodgeBulletServiceTest {
    val subject = DodgeBulletService(mockkClass(OkHttpClient::class))
    val level = subject.loadLevels().first()

    @Test
    fun `throw exception when action invalidate`() {
        val exp = assertThrows<RuntimeException> { subject.validate(listOf('a'), level) }
        assertEquals("invalidate action a", exp.message)
    }

    @Test
    fun `can fail evaluation`(){
        val result = subject.validate(listOf('r'), level)
        assertFalse(result)
    }

    @Test
    fun `can pass evaluation`(){
        val result = subject.validate(listOf('d','l'), level)
        assertTrue(result)
    }

    @Test
    fun `you can't move towards bullet`(){
        val result = subject.validate(listOf('l'), level)
        assertFalse(result)
    }

    @Test
    fun `you can't move out of map`(){
        val result = subject.validate(listOf('d','d'), level)
        assertFalse(result)
    }
}