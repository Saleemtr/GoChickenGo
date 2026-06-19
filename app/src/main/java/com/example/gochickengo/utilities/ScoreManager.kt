package com.example.gochickengo.utilities

import android.content.Context
import com.example.gochickengo.model.ScoreRecord
import org.json.JSONArray
import org.json.JSONObject

object ScoreManager {

    private const val PREFS_NAME = "scores_prefs"
    private const val KEY_SCORES = "scores"

    fun saveScore(context: Context, scoreRecord: ScoreRecord) {
        val scores = getScores(context).toMutableList()

        scores.add(scoreRecord)

        val sortedScores = scores
            .sortedByDescending { it.score }
            .take(10)

        saveScoresList(context, sortedScores)
    }

    fun getScores(context: Context): List<ScoreRecord> {
        val sharedPreferences = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        val scoresJson = sharedPreferences.getString(KEY_SCORES, "[]")
        val jsonArray = JSONArray(scoresJson)

        val scores = mutableListOf<ScoreRecord>()

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)

            val scoreRecord = ScoreRecord(
                score = jsonObject.getInt("score"),
                distance = jsonObject.getInt("distance"),
                coins = jsonObject.getInt("coins"),
                latitude = jsonObject.optDouble("latitude", 0.0),
                longitude = jsonObject.optDouble("longitude", 0.0)
            )

            scores.add(scoreRecord)
        }

        return scores
    }

    private fun saveScoresList(context: Context, scores: List<ScoreRecord>) {
        val jsonArray = JSONArray()

        for (scoreRecord in scores) {
            val jsonObject = JSONObject()

            jsonObject.put("score", scoreRecord.score)
            jsonObject.put("distance", scoreRecord.distance)
            jsonObject.put("coins", scoreRecord.coins)
            jsonObject.put("latitude", scoreRecord.latitude)
            jsonObject.put("longitude", scoreRecord.longitude)

            jsonArray.put(jsonObject)
        }

        val sharedPreferences = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        sharedPreferences.edit()
            .putString(KEY_SCORES, jsonArray.toString())
            .apply()
    }
}