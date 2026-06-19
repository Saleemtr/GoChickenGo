package com.example.gochickengo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.gochickengo.model.ScoreRecord
import com.example.gochickengo.utilities.ScoreManager

class HighScoresFragment : Fragment() {

    interface OnScoreSelectedListener {
        fun onScoreSelected(scoreRecord: ScoreRecord)
    }

    private lateinit var highScoresListLayout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val scrollView = ScrollView(requireContext())

        highScoresListLayout = LinearLayout(requireContext())
        highScoresListLayout.orientation = LinearLayout.VERTICAL
        highScoresListLayout.setPadding(12, 12, 12, 12)

        scrollView.addView(highScoresListLayout)

        return scrollView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showScores()
    }

    private fun showScores() {
        val scores = ScoreManager.getScores(requireContext())

        highScoresListLayout.removeAllViews()

        if (scores.isEmpty()) {
            val emptyTextView = createScoreTextView("No scores yet")
            highScoresListLayout.addView(emptyTextView)
            return
        }

        for (i in scores.indices) {
            val scoreRecord = scores[i]
            val scoreText = buildScoreText(i + 1, scoreRecord)

            val scoreTextView = createScoreTextView(scoreText)

            scoreTextView.setOnClickListener {
                val listener = activity as? OnScoreSelectedListener
                listener?.onScoreSelected(scoreRecord)
            }

            highScoresListLayout.addView(scoreTextView)
        }
    }

    private fun buildScoreText(place: Int, scoreRecord: ScoreRecord): String {
        return "$place. Score: ${scoreRecord.score} | Distance: ${scoreRecord.distance} | Coins: ${scoreRecord.coins}"
    }

    private fun createScoreTextView(text: String): TextView {
        val textView = TextView(requireContext())

        textView.text = text
        textView.textSize = 16f
        textView.setTextColor(resources.getColor(android.R.color.white, null))
        textView.setPadding(12, 12, 12, 12)

        return textView
    }
}