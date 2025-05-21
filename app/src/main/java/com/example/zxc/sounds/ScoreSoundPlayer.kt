package com.example.zxc.sounds

import android.content.Context
import android.content.pm.PackageManager
import com.zxc.R
import com.zxc.sounds.GameSound
import com.zxc.sounds.SoundPoolFactory

// класс для управления воспроизведением звука при показе очков
class ScoreSoundPlayer(
    private val context: Context,

    // лямбда-функция, вызываемая после загрузки звука
    private val soundReadyListener: () -> Unit
) {
    // хранние звукового эффекта (будет инициализировано позже)
    private lateinit var scoreSound: GameSound
    private val soundPool = SoundPoolFactory().createSoundPool()

    // при создании объекта загружаем звуки
    init {
        loadSounds()
    }

    // метод для загрузки звука в SoundPool и создания GameSound
    private fun loadSounds() {
        scoreSound = GameSound(
            resourceInPool = soundPool.load(context, R.raw.score_count, 1),

            // передача SoundPool в объект GameSound для дальнейшего управления звуком
            pool = soundPool
        )
    }

    // метод для запуска звука счета
    fun playScoreSound() {
        // слушатель сработает, когда звук будет загружен полностью
        soundPool.setOnLoadCompleteListener { _, _, _ ->

            // вызов коллбэк - звук готов
            soundReadyListener()

            // запуск или возобновение звука с зацикливанием
            scoreSound.startOrResume(isLooping = true)
        }
    }

    // метод для остановки звука счета
    fun pauseScoreSound() {
        scoreSound.pause()
    }
}
