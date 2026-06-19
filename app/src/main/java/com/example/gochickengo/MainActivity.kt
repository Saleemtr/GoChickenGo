package com.example.gochickengo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gochickengo.logic.GameManager
import com.example.gochickengo.model.ScoreRecord
import com.example.gochickengo.utilities.Constants
import com.example.gochickengo.utilities.ScoreManager

class MainActivity : AppCompatActivity(), SensorEventListener {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100

        private const val DEFAULT_CELL_PADDING_DP = 18
        private const val CHICKEN_PADDING_DP = 8
        private const val CAR_PADDING_DP = 8
        private const val BITCOIN_PADDING_DP = 18

        private const val SENSOR_MOVE_DELAY = 500L
        private const val SENSOR_SPEED_CHANGE_DELAY = 600L

        private const val MIN_GAME_SPEED = 250L
        private const val MAX_GAME_SPEED = 950L
        private const val SPEED_CHANGE_AMOUNT = 50L
    }

    private lateinit var main_BTN_left: ImageButton
    private lateinit var main_BTN_right: ImageButton
    private lateinit var main_BTN_restart: Button
    private lateinit var main_BTN_menu: Button

    private lateinit var main_LBL_gameOver: TextView
    private lateinit var main_LBL_hit: TextView
    private lateinit var main_LBL_distance: TextView
    private lateinit var main_LBL_coins: TextView
    private lateinit var main_LBL_score: TextView

    private lateinit var main_IMG_lives: Array<ImageView>
    private lateinit var main_IMG_cells: Array<Array<ImageView>>

    private lateinit var gameManager: GameManager

    private lateinit var soundPool: SoundPool
    private var crashSoundId: Int = 0

    private val handler = Handler(Looper.getMainLooper())

    private var tickCounter = 0
    private var gameSpeed: Long = Constants.Game.SLOW_SPEED
    private var gameMode: String = "SLOW"
    private var scoreSaved = false

    private var lastLatitude: Double = 0.0
    private var lastLongitude: Double = 0.0

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var lastSensorMoveTime: Long = 0
    private var lastSensorSpeedChangeTime: Long = 0

    private val gameRunnable = object : Runnable {
        override fun run() {
            gameStep()

            if (!gameManager.isGameOver) {
                handler.postDelayed(this, gameSpeed)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        applySystemInsets()
        readGameMode()
        findViews()

        gameManager = GameManager()

        initViews()
        initSound()
        initSensors()
        requestLocationPermissionIfNeeded()
        updateLastKnownLocation()
        startGame()
    }

    private fun applySystemInsets() {
        val mainLayout = findViewById<View>(R.id.main)

        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.setPadding(
                view.paddingLeft,
                systemBars.top,
                view.paddingRight,
                systemBars.bottom
            )

            insets
        }
    }

    private fun readGameMode() {
        gameMode = intent.getStringExtra("GAME_MODE") ?: "SLOW"

        gameSpeed = when (gameMode) {
            "FAST" -> Constants.Game.FAST_SPEED
            "SENSOR" -> Constants.Game.SENSOR_SPEED
            else -> Constants.Game.SLOW_SPEED
        }
    }

    private fun findViews() {
        main_BTN_left = findViewById(R.id.main_BTN_left)
        main_BTN_right = findViewById(R.id.main_BTN_right)
        main_BTN_restart = findViewById(R.id.main_BTN_restart)
        main_BTN_menu = findViewById(R.id.main_BTN_menu)

        main_LBL_gameOver = findViewById(R.id.main_LBL_gameOver)
        main_LBL_hit = findViewById(R.id.main_LBL_hit)
        main_LBL_distance = findViewById(R.id.main_LBL_distance)
        main_LBL_coins = findViewById(R.id.main_LBL_coins)
        main_LBL_score = findViewById(R.id.main_LBL_score)

        main_IMG_lives = arrayOf(
            findViewById(R.id.main_IMG_life0),
            findViewById(R.id.main_IMG_life1),
            findViewById(R.id.main_IMG_life2)
        )

        main_IMG_cells = arrayOf(
            arrayOf(
                findViewById(R.id.main_IMG_cell_0_0),
                findViewById(R.id.main_IMG_cell_0_1),
                findViewById(R.id.main_IMG_cell_0_2),
                findViewById(R.id.main_IMG_cell_0_3),
                findViewById(R.id.main_IMG_cell_0_4)
            ),
            arrayOf(
                findViewById(R.id.main_IMG_cell_1_0),
                findViewById(R.id.main_IMG_cell_1_1),
                findViewById(R.id.main_IMG_cell_1_2),
                findViewById(R.id.main_IMG_cell_1_3),
                findViewById(R.id.main_IMG_cell_1_4)
            ),
            arrayOf(
                findViewById(R.id.main_IMG_cell_2_0),
                findViewById(R.id.main_IMG_cell_2_1),
                findViewById(R.id.main_IMG_cell_2_2),
                findViewById(R.id.main_IMG_cell_2_3),
                findViewById(R.id.main_IMG_cell_2_4)
            ),
            arrayOf(
                findViewById(R.id.main_IMG_cell_3_0),
                findViewById(R.id.main_IMG_cell_3_1),
                findViewById(R.id.main_IMG_cell_3_2),
                findViewById(R.id.main_IMG_cell_3_3),
                findViewById(R.id.main_IMG_cell_3_4)
            ),
            arrayOf(
                findViewById(R.id.main_IMG_cell_4_0),
                findViewById(R.id.main_IMG_cell_4_1),
                findViewById(R.id.main_IMG_cell_4_2),
                findViewById(R.id.main_IMG_cell_4_3),
                findViewById(R.id.main_IMG_cell_4_4)
            ),
            arrayOf(
                findViewById(R.id.main_IMG_cell_5_0),
                findViewById(R.id.main_IMG_cell_5_1),
                findViewById(R.id.main_IMG_cell_5_2),
                findViewById(R.id.main_IMG_cell_5_3),
                findViewById(R.id.main_IMG_cell_5_4)
            ),
            arrayOf(
                findViewById(R.id.main_IMG_cell_6_0),
                findViewById(R.id.main_IMG_cell_6_1),
                findViewById(R.id.main_IMG_cell_6_2),
                findViewById(R.id.main_IMG_cell_6_3),
                findViewById(R.id.main_IMG_cell_6_4)
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

        main_BTN_menu.setOnClickListener {
            finish()
        }

        updateControlButtonsVisibility()
        refreshUI()
    }

    private fun updateControlButtonsVisibility() {
        if (gameMode == "SENSOR") {
            main_BTN_left.visibility = View.INVISIBLE
            main_BTN_right.visibility = View.INVISIBLE
        } else {
            main_BTN_left.visibility = View.VISIBLE
            main_BTN_right.visibility = View.VISIBLE
        }
    }

    private fun startGame() {
        handler.postDelayed(gameRunnable, gameSpeed)
    }

    private fun initSound() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()

        crashSoundId = soundPool.load(this, R.raw.crash_sound, 1)
    }

    private fun initSensors() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    private fun requestLocationPermissionIfNeeded() {
        val fineLocationGranted = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineLocationGranted && !coarseLocationGranted) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun updateLastKnownLocation() {
        val fineLocationGranted = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineLocationGranted && !coarseLocationGranted) {
            return
        }

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        val bestLocation = gpsLocation ?: networkLocation

        if (bestLocation != null) {
            lastLatitude = bestLocation.latitude
            lastLongitude = bestLocation.longitude
        }
    }

    private fun playCrashSound() {
        if (::soundPool.isInitialized) {
            soundPool.play(
                crashSoundId,
                1.0f,
                1.0f,
                1,
                0,
                1.0f
            )
        }
    }

    private fun gameStep() {
        tickCounter++

        gameManager.increaseDistance()
        gameManager.moveObstaclesDown()
        gameManager.moveBitcoinsDown()

        val hit = gameManager.checkCollision()
        if (hit) {
            showHitMessage()
            vibratePhone()
            playCrashSound()
        }

        gameManager.checkBitcoinCollection()

        if (tickCounter % 2 == 0) {
            gameManager.addObstacle()
        }

        if (tickCounter % 5 == 0) {
            gameManager.addBitcoin()
        }

        refreshUI()

        if (gameManager.isGameOver) {
            showGameOver()
        }
    }

    private fun refreshUI() {
        clearBoard()
        drawObstacles()
        drawBitcoins()
        drawPlayer()
        updateLives()
        updateDistance()
        updateCoins()
        updateScore()
    }

    private fun clearBoard() {
        for (row in main_IMG_cells.indices) {
            for (lane in main_IMG_cells[row].indices) {
                main_IMG_cells[row][lane].setImageDrawable(null)
                setCellPadding(main_IMG_cells[row][lane], DEFAULT_CELL_PADDING_DP)
            }
        }
    }

    private fun drawPlayer() {
        val playerRow = Constants.Game.ROW_COUNT - 1
        val playerLane = gameManager.playerLane

        val playerCell = main_IMG_cells[playerRow][playerLane]
        setCellPadding(playerCell, CHICKEN_PADDING_DP)

        playerCell.setImageResource(R.drawable.chicken)
    }

    private fun drawObstacles() {
        for (obstacle in gameManager.obstacles) {
            if (obstacle.row in 0 until Constants.Game.ROW_COUNT) {
                val carCell = main_IMG_cells[obstacle.row][obstacle.lane]
                setCellPadding(carCell, CAR_PADDING_DP)

                carCell.setImageResource(R.drawable.car)
            }
        }
    }

    private fun drawBitcoins() {
        for (bitcoin in gameManager.bitcoins) {
            if (bitcoin.row in 0 until Constants.Game.ROW_COUNT) {
                val bitcoinCell = main_IMG_cells[bitcoin.row][bitcoin.lane]
                setCellPadding(bitcoinCell, BITCOIN_PADDING_DP)

                bitcoinCell.setImageResource(R.drawable.bitcoin)
            }
        }
    }

    private fun setCellPadding(imageView: ImageView, paddingDp: Int) {
        val paddingPx = dpToPx(paddingDp)

        imageView.setPadding(
            paddingPx,
            paddingPx,
            paddingPx,
            paddingPx
        )
    }

    private fun dpToPx(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
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

    private fun updateDistance() {
        main_LBL_distance.text = "Distance: ${gameManager.distance}"
    }

    private fun updateCoins() {
        main_LBL_coins.text = "Coins: ${gameManager.coins}"
    }

    private fun updateScore() {
        main_LBL_score.text = "Score: ${gameManager.score}"
    }

    private fun showHitMessage() {
        main_LBL_hit.visibility = View.VISIBLE

        handler.postDelayed({
            main_LBL_hit.visibility = View.GONE
        }, 600)
    }

    private fun showGameOver() {
        saveScoreIfNeeded()

        handler.removeCallbacks(gameRunnable)

        main_LBL_hit.visibility = View.GONE
        main_LBL_gameOver.visibility = View.VISIBLE
        main_BTN_restart.visibility = View.VISIBLE
        main_BTN_menu.visibility = View.VISIBLE

        main_BTN_left.visibility = View.INVISIBLE
        main_BTN_right.visibility = View.INVISIBLE
    }

    private fun saveScoreIfNeeded() {
        if (scoreSaved) {
            return
        }

        updateLastKnownLocation()

        val scoreRecord = ScoreRecord(
            score = gameManager.score,
            distance = gameManager.distance,
            coins = gameManager.coins,
            latitude = lastLatitude,
            longitude = lastLongitude
        )

        ScoreManager.saveScore(this, scoreRecord)
        scoreSaved = true
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
        scoreSaved = false

        gameSpeed = when (gameMode) {
            "FAST" -> Constants.Game.FAST_SPEED
            "SENSOR" -> Constants.Game.SENSOR_SPEED
            else -> Constants.Game.SLOW_SPEED
        }

        main_LBL_gameOver.visibility = View.GONE
        main_LBL_hit.visibility = View.GONE
        main_BTN_restart.visibility = View.GONE
        main_BTN_menu.visibility = View.GONE

        updateControlButtonsVisibility()

        refreshUI()
        startGame()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (gameMode != "SENSOR") {
            return
        }

        if (event == null) {
            return
        }

        val x = event.values[0]
        val y = event.values[1]

        handleSensorMovement(x)
        handleSensorSpeed(y)
    }

    private fun handleSensorMovement(x: Float) {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastSensorMoveTime < SENSOR_MOVE_DELAY) {
            return
        }

        if (x > 3) {
            gameManager.movePlayerLeft()
            refreshUI()
            lastSensorMoveTime = currentTime
        } else if (x < -3) {
            gameManager.movePlayerRight()
            refreshUI()
            lastSensorMoveTime = currentTime
        }
    }

    private fun handleSensorSpeed(y: Float) {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastSensorSpeedChangeTime < SENSOR_SPEED_CHANGE_DELAY) {
            return
        }

        if (y < -3) {
            speedUpGame()
            lastSensorSpeedChangeTime = currentTime
        } else if (y > 3) {
            slowDownGame()
            lastSensorSpeedChangeTime = currentTime
        }
    }

    private fun speedUpGame() {
        gameSpeed -= SPEED_CHANGE_AMOUNT

        if (gameSpeed < MIN_GAME_SPEED) {
            gameSpeed = MIN_GAME_SPEED
        }
    }

    private fun slowDownGame() {
        gameSpeed += SPEED_CHANGE_AMOUNT

        if (gameSpeed > MAX_GAME_SPEED) {
            gameSpeed = MAX_GAME_SPEED
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onResume() {
        super.onResume()

        if (gameMode == "SENSOR" && accelerometer != null) {
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        updateLastKnownLocation()
    }

    override fun onPause() {
        super.onPause()

        if (gameMode == "SENSOR") {
            sensorManager.unregisterListener(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(gameRunnable)

        if (::soundPool.isInitialized) {
            soundPool.release()
        }
    }
}