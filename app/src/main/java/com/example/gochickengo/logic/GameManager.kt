package com.example.gochickengo.logic

import com.example.gochickengo.model.Bitcoin
import com.example.gochickengo.model.Obstacle
import com.example.gochickengo.utilities.Constants
import kotlin.random.Random

class GameManager {

    var lives: Int = Constants.Game.LIFE_COUNT
        private set

    var playerLane: Int = 1
        private set

    var distance: Int = 0
        private set

    var coins: Int = 0
        private set

    val score: Int
        get() = distance + coins * 10

    val obstacles: MutableList<Obstacle> = mutableListOf()
    val bitcoins: MutableList<Bitcoin> = mutableListOf()

    val isGameOver: Boolean
        get() = lives == 0

    fun movePlayerLeft() {
        if (playerLane > 0) {
            playerLane--
        }
    }

    fun movePlayerRight() {
        if (playerLane < Constants.Game.LANE_COUNT - 1) {
            playerLane++
        }
    }

    fun increaseDistance() {
        distance++
    }

    fun moveObstaclesDown() {
        for (obstacle in obstacles) {
            obstacle.row++
        }

        obstacles.removeAll { obstacle ->
            obstacle.row >= Constants.Game.ROW_COUNT
        }
    }

    fun moveBitcoinsDown() {
        for (bitcoin in bitcoins) {
            bitcoin.row++
        }

        bitcoins.removeAll { bitcoin ->
            bitcoin.row >= Constants.Game.ROW_COUNT
        }
    }

    fun addObstacle() {
        val row = 0
        val availableLanes = getAvailableLanes(row)

        if (availableLanes.isEmpty()) {
            return
        }

        val randomLane = availableLanes.random()

        obstacles.add(
            Obstacle(
                row = row,
                lane = randomLane
            )
        )
    }

    fun addBitcoin() {
        val row = 0
        val availableLanes = getAvailableLanes(row)

        if (availableLanes.isEmpty()) {
            return
        }

        val randomLane = availableLanes.random()

        bitcoins.add(
            Bitcoin(
                row = row,
                lane = randomLane
            )
        )
    }

    private fun getAvailableLanes(row: Int): List<Int> {
        val availableLanes = mutableListOf<Int>()

        for (lane in 0 until Constants.Game.LANE_COUNT) {
            if (isCellAvailable(row, lane)) {
                availableLanes.add(lane)
            }
        }

        return availableLanes
    }

    private fun isCellAvailable(row: Int, lane: Int): Boolean {
        val hasObstacle = obstacles.any { obstacle ->
            obstacle.row == row && obstacle.lane == lane
        }

        val hasBitcoin = bitcoins.any { bitcoin ->
            bitcoin.row == row && bitcoin.lane == lane
        }

        return !hasObstacle && !hasBitcoin
    }

    fun checkCollision(): Boolean {
        val playerRow = Constants.Game.ROW_COUNT - 1

        val hitObstacle = obstacles.firstOrNull { obstacle ->
            obstacle.row == playerRow && obstacle.lane == playerLane
        }

        if (hitObstacle != null) {
            if (lives > 0) {
                lives--
            }

            obstacles.remove(hitObstacle)
            return true
        }

        return false
    }

    fun checkBitcoinCollection(): Boolean {
        val playerRow = Constants.Game.ROW_COUNT - 1

        val collectedBitcoin = bitcoins.firstOrNull { bitcoin ->
            bitcoin.row == playerRow && bitcoin.lane == playerLane
        }

        if (collectedBitcoin != null) {
            coins++
            bitcoins.remove(collectedBitcoin)
            return true
        }

        return false
    }
}