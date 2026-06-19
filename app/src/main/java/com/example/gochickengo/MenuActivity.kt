package com.example.gochickengo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {

    private lateinit var menu_BTN_slow: Button
    private lateinit var menu_BTN_fast: Button
    private lateinit var menu_BTN_sensor: Button
    private lateinit var menu_BTN_highScores: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        findViews()
        initViews()
    }

    private fun findViews() {
        menu_BTN_slow = findViewById(R.id.menu_BTN_slow)
        menu_BTN_fast = findViewById(R.id.menu_BTN_fast)
        menu_BTN_sensor = findViewById(R.id.menu_BTN_sensor)
        menu_BTN_highScores = findViewById(R.id.menu_BTN_highScores)
    }

    private fun initViews() {
        menu_BTN_slow.setOnClickListener {
            openGame("SLOW")
        }

        menu_BTN_fast.setOnClickListener {
            openGame("FAST")
        }

        menu_BTN_sensor.setOnClickListener {
            openGame("SENSOR")
        }

        menu_BTN_highScores.setOnClickListener {
            openHighScores()
        }
    }

    private fun openGame(gameMode: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("GAME_MODE", gameMode)
        startActivity(intent)
    }

    private fun openHighScores() {
        val intent = Intent(this, HighScoresActivity::class.java)
        startActivity(intent)
    }
}