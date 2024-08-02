package com.csg.codeit.model

import com.csg.codeit.config.objectMapper
import com.csg.codeit.service.CoordinatorService
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import java.lang.RuntimeException
import java.nio.file.Path
import java.nio.file.Paths
import java.util.LinkedList
import java.util.Scanner
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.streams.toList

@Service
class DodgeBulletService(val httpClient: OkHttpClient) {
    private val logger: Logger = LoggerFactory.getLogger(DodgeBulletService::class.java)
    private val levels = loadLevels()
    final fun loadLevels(): List<Level> {
        val levels = mutableListOf<Level>()
        var index = 0
        while (true) {
//            val uri =  this.javaClass.getResource("/").toURI()
//
//            println( "what ${Paths.get(uri).listDirectoryEntries()}")
            val fis = this.javaClass.getResourceAsStream("/$index.level")
            if (fis == null) {
                break
            } else {
                val scanner = Scanner(fis)
                val map = mutableListOf<List<Char>>()
                var impossible = false
                while (scanner.hasNext()) {
                    val line = scanner.nextLine()
                    if (line == "impossible") {
                        impossible = true
                    } else {
                        map.add(line.toCharArray().toList())
                    }
                }
                levels.add(Level(index, map, impossible))
                index++
            }
        }
        return levels
    }

    fun validate(actions: List<Char>?, level: Level): Boolean {
        if (level.impossible) {
            return actions == null
        } else if (actions==null){
            return false
        }

        val maxY = level.map.size - 1
        val maxX = level.map[0].size - 1
        var you: Item? = null
        val bullets = mutableListOf<Item>()
        level.map.forEachIndexed { y, row ->
            row.forEachIndexed { x, char ->
                when (char) {
                    'u', 'd', 'l', 'r' -> bullets.add(Item(x, y, char))
                    '*' -> you = Item(x, y, char)
                    '.' -> {}
                    else -> throw RuntimeException("invalidate char $char")
                }
            }
        }
        require(you != null) { "failed to load level" }

        val mutableAction = LinkedList(actions)
        while (bullets.isNotEmpty()) {
            if (mutableAction.isNotEmpty()) {
                val action = mutableAction.removeFirst()
                printMap(maxX, maxY, you!!, action, bullets)
                you!!.move(action)
                if (bullets.any { you!!.hit(it, action) }) {
                    return false
                }
            }
            bullets.forEach { it.move(it.direction) }

            if (!you!!.isInsideMap(maxX, maxY)) {
                return false
            }
            bullets.removeIf { !it.isInsideMap(maxX, maxY) }
            if (bullets.any { it.isSameCoordinate(you!!) }) {
                return false
            }
        }
        return true
    }

    private fun printMap(maxX: Int, maxY: Int, you: Item, action: Char?, bullets: MutableList<Item>) {
        val map = mutableListOf<MutableList<Char>>()
        repeat(maxY + 1) {
            val row = mutableListOf<Char>()
            repeat(maxX + 1) { row.add('.') }
            map.add(row)
        }
        map[you.getY()][you.getX()] = '*'
        bullets.forEach { map[it.getY()][it.getX()] = it.direction }
//        println("current action $action")
//        map.forEach {
//            println(it.joinToString(""))
//        }
    }


    fun postChallenge(teamUrl: String): List<Pair<Solution, Int>> {
        return levels.parallelStream().map { level ->
            val requestBody =
                level.map.joinToString("\n") { it.joinToString("") }.toRequestBody("text/plain".toMediaType())
            val request = Request.Builder().url(teamUrl).post(requestBody).build()
            httpClient.newCall(request).execute().use { resp ->
                try {
                    objectMapper.readValue<Solution>(resp.body!!.string()) to level.index
                } catch (exp: Exception) {
                    logger.error("failed to parse solution")
                    null
                }
            }
        }.filter { it != null }.toList() as List<Pair<Solution, Int>>
    }

    fun evaluateSolution(solutions: List<Pair<Solution, Int>>): ChallengeResult {
        val passed = mutableListOf<Int>()
        solutions.forEach {solution->
            if (validate(solution.first.instructions, levels[solution.second])){
                passed.add(solution.second)
            }
        }
        return  ChallengeResult(passed.size, "failed test ${(levels.map { it.index }-passed).joinToString(",")}")
    }
}

class Level(val index: Int, val map: List<List<Char>>, val impossible: Boolean)
class Item(private var x: Int, private var y: Int, val direction: Char) {
    fun move(action: Char) {
        when (action) {
            'r' -> x += 1
            'l' -> x -= 1
            'u' -> y -= 1
            'd' -> y += 1
            else -> throw RuntimeException("invalidate action $action")
        }
    }

    fun hit(item: Item, action: Char): Boolean {
        if (isSameCoordinate(item)) {
            if ((action == 'r' && item.direction == 'l') || (action == 'l' && item.direction == 'r') || (action == 'u' && item.direction == 'd') || (action == 'd' && item.direction == 'u')) {
                return true
            }
        }
        return false
    }

    fun getX() = x
    fun getY() = y
    fun isInsideMap(maxX: Int, maxY: Int) = !(x < 0 || x > maxX || y < 0 || y > maxY)
    fun isSameCoordinate(other: Item) = (x == other.getX() && y == other.getY())
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class Solution(val instructions: List<Char>?)
