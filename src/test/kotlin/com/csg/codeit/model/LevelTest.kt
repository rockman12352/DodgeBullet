package com.csg.codeit.model

import io.mockk.mockkClass
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class LevelTest {
    val subject = DodgeBulletService(mockkClass(OkHttpClient::class))
    val levels = subject.loadLevels()

    @Test
    fun `level1`() {
        assertTrue(subject.validate(null, levels[1]))
    }

    @Test
    fun `level2`() {
        assertTrue(subject.validate("lrlrlrlr".toCharArray().toList(), levels[2]))
    }

    @Test
    fun `level3`() {
        assertTrue(subject.validate(emptyList(), levels[3]))
    }

    @Test
    fun `level4`() {
        assertTrue(subject.validate(emptyList(), levels[4]))
    }
    @Test
    fun `level5`() {
        assertTrue(subject.validate("uuurrrrrrrrddddllllllll".toCharArray().toList(), levels[5]))
    }
    @Test
    fun `level6`() {
        assertTrue(subject.validate(null, levels[6]))
    }
    @Test
    fun `level7`() {
        assertTrue(subject.validate("rlrlrlrrrrr".toCharArray().toList(), levels[7]))
    }


}