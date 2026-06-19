package com.example.gochickengo

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.gochickengo.fragments.HighScoresFragment
import com.example.gochickengo.fragments.LocationFragment
import com.example.gochickengo.model.ScoreRecord

class HighScoresActivity : AppCompatActivity(), HighScoresFragment.OnScoreSelectedListener {

    private lateinit var high_scores_BTN_back: Button

    private lateinit var locationFragment: LocationFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_high_scores)

        findViews()
        initViews()
        loadFragments()
    }

    private fun findViews() {
        high_scores_BTN_back = findViewById(R.id.high_scores_BTN_back)
    }

    private fun initViews() {
        high_scores_BTN_back.setOnClickListener {
            finish()
        }
    }

    private fun loadFragments() {
        locationFragment = LocationFragment()

        supportFragmentManager.beginTransaction()
            .replace(R.id.high_scores_FRAME_list, HighScoresFragment())
            .replace(R.id.high_scores_FRAME_map, locationFragment)
            .commit()
    }

    override fun onScoreSelected(scoreRecord: ScoreRecord) {
        locationFragment.showLocation(
            latitude = scoreRecord.latitude,
            longitude = scoreRecord.longitude,
            title = "Score: ${scoreRecord.score}"
        )
    }
}