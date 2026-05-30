package com.example.gochickengo

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.gochickengo.logic.GameManager
import com.example.gochickengo.utilities.Constants


class MainActivity : AppCompatActivity() {

    private lateinit var main_BTN_left: ImageButton
    private lateinit var main_BTN_right: ImageButton
    private lateinit var main_BTN_restart: Button
    private lateinit var main_LBL_gameOver: TextView
    private lateinit var main_LBL_hit: TextView

    private lateinit var main_IMG_lives: Array<ImageView>
    private lateinit var main_IMG_cells: Array<Array<ImageView>>

    private lateinit var gameManager: GameManager

    private val handler = Handler(Looper.getMainLooper())

    private var tickCounter = 0

    private val gameRunnable = object : Runnable {
        override fun run() {
            gameStep()

            if (!gameManager.isGameOver) {
                handler.postDelayed(this, Constants.Game.GAME_SPEED)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViews()
        gameManager = GameManager()
        initViews()
        startGame()
    }

    private fun findViews() {
        main_BTN_left = findViewById(R.id.main_BTN_left)
        main_BTN_right = findViewById(R.id.main_BTN_right)
        main_BTN_restart = findViewById(R.id.main_BTN_restart)
        main_LBL_gameOver = findViewById(R.id.main_LBL_gameOver)
        main_LBL_hit = findViewById(R.id.main_LBL_hit)

        main_IMG_lives = arrayOf(
            findViewById(R.id.main_IMG_life0),
            findViewById(R.id.main_IMG_life1),
            findViewById(R.id.main_IMG_life2)
        )

        main_IMG_cells = arrayOf(
            arrayOf(
                findViewById(R.id.main_IMG_cell_0_0),
                findViewById(R.id.main_IMG_cell_0_1),
                findViewById(R.id.main_IMG_cell_0_2)
            ),
            arrayOf(
                findViewById(R.id.main_IMG_cell_1_0),
                findViewById(R.id.main_IMG_cell_1_1),
                findViewById(R.id.main_IMG_cell_1_2)
            ),
            arrayOf(
                findViewById(R.id.main_IMG_cell_2_0),
                findViewById(R.id.main_IMG_cell_2_1),
                findViewById(R.id.main_IMG_cell_2_2)
            ),
            arrayOf(
                findViewById(R.id.main_IMG_cell_3_0),
                findViewById(R.id.main_IMG_cell_3_1),
                findViewById(R.id.main_IMG_cell_3_2)
            ),
            arrayOf(
                findViewById(R.id.main_IMG_cell_4_0),
                findViewById(R.id.main_IMG_cell_4_1),
                findViewById(R.id.main_IMG_cell_4_2)
            )
        )
    }

    private fun initViews() {
        main_BTN_left.setOnClickListener {
            gameManager.movePlayerLeft()
            refreshUI()
        }

        main_BTN_right.setOnClickListener {
            gameManager.movePlayerRight()
            refreshUI()
        }

        main_BTN_restart.setOnClickListener {
            restartGame()
        }

        refreshUI()
    }

    private fun startGame() {
        handler.postDelayed(gameRunnable, Constants.Game.GAME_SPEED)
    }

    private fun gameStep() {
        tickCounter++

        gameManager.moveObstaclesDown()

        val hit = gameManager.checkCollision()
        if (hit) {
            showHitMessage()
            vibratePhone()
        }

        if (tickCounter % 2 == 0) {
            gameManager.addObstacle()
        }

        refreshUI()

        if (gameManager.isGameOver) {
            showGameOver()
        }
    }

    private fun refreshUI() {
        clearBoard()
        drawObstacles()
        drawPlayer()
        updateLives()
    }

    private fun clearBoard() {
        for (row in main_IMG_cells.indices) {
            for (lane in main_IMG_cells[row].indices) {
                main_IMG_cells[row][lane].setImageDrawable(null)
            }
        }
    }

    private fun drawPlayer() {
        val playerRow = Constants.Game.ROW_COUNT - 1
        val playerLane = gameManager.playerLane

        main_IMG_cells[playerRow][playerLane].setImageResource(
            R.drawable.chicken
        )
    }

    private fun drawObstacles() {
        for (obstacle in gameManager.obstacles) {
            if (obstacle.row in 0 until Constants.Game.ROW_COUNT) {
                main_IMG_cells[obstacle.row][obstacle.lane].setImageResource(
                    R.drawable.car
                )
            }
        }
    }

    private fun updateLives() {
        for (i in main_IMG_lives.indices) {
            if (i < gameManager.lives) {
                main_IMG_lives[i].visibility = View.VISIBLE
            } else {
                main_IMG_lives[i].visibility = View.INVISIBLE
            }
        }
    }

    private fun showHitMessage() {
        main_LBL_hit.visibility = View.VISIBLE

        handler.postDelayed({
            main_LBL_hit.visibility = View.GONE
        }, 600)
    }

    private fun showGameOver() {
        handler.removeCallbacks(gameRunnable)

        main_LBL_hit.visibility = View.GONE
        main_LBL_gameOver.visibility = View.VISIBLE
        main_BTN_restart.visibility = View.VISIBLE

        main_BTN_left.visibility = View.INVISIBLE
        main_BTN_right.visibility = View.INVISIBLE
    }

    private fun vibratePhone() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator

            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    250,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )

        } else {
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        250,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                vibrator.vibrate(250)
            }
        }
    }

    private fun restartGame() {
        handler.removeCallbacks(gameRunnable)

        gameManager = GameManager()
        tickCounter = 0

        main_LBL_gameOver.visibility = View.GONE
        main_LBL_hit.visibility = View.GONE
        main_BTN_restart.visibility = View.GONE

        main_BTN_left.visibility = View.VISIBLE
        main_BTN_right.visibility = View.VISIBLE

        refreshUI()
        startGame()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(gameRunnable)
    }
}