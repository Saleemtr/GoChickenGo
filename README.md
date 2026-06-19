# Go Chicken Go! 🐔🚗

Go Chicken Go! is an Android game developed in Kotlin as part of an Android development assignment.

The player controls a chicken on a 5-lane road, avoids cars, collects coins, and tries to achieve the highest score.

## Features

- 5-lane road game screen
- Chicken player movement between lanes
- Car obstacles moving down the road
- Bitcoin coins collection
- 3 lives system
- Distance counter
- Score calculation
- Slow button mode
- Fast button mode
- Sensor mode using phone tilt
- Bonus: forward/backward tilt changes the game speed
- Crash sound and vibration on collision
- Game Over screen with Restart and Back to Menu buttons
- High scores screen
- Google Maps integration for score locations

## Controls

### Button Mode

The player moves the chicken using left and right buttons.

### Sensor Mode

The game uses the phone accelerometer:

- Tilt left/right to move between lanes
- Tilt forward to increase speed
- Tilt backward to decrease speed

## Score

The score is calculated by:

```text
Score = Distance + Coins * 10
```

## High Scores and Map

The app saves the top scores locally using SharedPreferences.

Each saved score includes:

- Score
- Distance
- Coins
- Location

The High Scores screen displays the saved scores and a Google Map.  
When clicking a score, the map shows the location where that score was achieved.

## Technologies Used

- Kotlin
- Android Studio
- GridLayout
- SharedPreferences
- Accelerometer Sensor
- SoundPool
- Vibration API
- Google Maps API

## Notes

The game does not use Canvas or direct screen x/y positioning.  
Objects are placed using rows and lanes inside a GridLayout.

The Google Maps API key is stored in `local.properties`, which should not be uploaded to GitHub.