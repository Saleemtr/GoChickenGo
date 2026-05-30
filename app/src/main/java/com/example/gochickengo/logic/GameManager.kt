package com.example.gochickengo.logic

import com.example.gochickengo.model.Obstacle
import com.example.gochickengo.utilities.Constants
import kotlin.random.Random

class GameManager {

    var lives: Int = Constants.Game.LIFE_COUNT
        private set

    var playerLane: Int = 1
        private set

    val obstacles: MutableList<Obstacle> = mutableListOf()

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

    fun moveObstaclesDown() {
        for (obstacle in obstacles) {
            obstacle.row++
        }

        obstacles.removeAll { obstacle ->
            obstacle.row >= Constants.Game.ROW_COUNT
        }
    }

    fun addObstacle() {
        val randomLane = Random.nextInt(Constants.Game.LANE_COUNT)
        obstacles.add(
            Obstacle(
                row = 0,
                lane = randomLane
            )
        )
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

}