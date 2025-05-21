package com.zxc.sounds

import android.content.Context
import com.zxc.R
import com.example.zxc.utils.ProgressIndicator

private const val INTRO_MUSIC_INDEX = 0
private const val BULLET_SHOT_INDEX = 1
private const val BULLET_BURST_INDEX = 2
private const val TANK_MOVE_INDEX = 3
private const val SUCCESS_UPLOAD = 0

// класс загрузки и воспроизвдения всех звуков
class MainSoundPlayer(val context: Context, val progressIndicator: ProgressIndicator) {
    private val sounds = mutableListOf<GameSound>()  // список всех звуков
    private val soundPool = SoundPoolFactory().createSoundPool()
    private var soundsReady = 0 // сколько загружено
    private var allSoundsLoaded = false // все ли загружены

    // метод для загрузки звуков
    fun loadSounds() {
        progressIndicator.showProgress() // прогресс

        // загрузка каждого звука и добавление в список
        sounds.add(INTRO_MUSIC_INDEX, GameSound(
            resourceInPool = soundPool.load(context, R.raw.tanks_pre_music, 1),
            pool = soundPool
        ))

        sounds.add(BULLET_SHOT_INDEX, GameSound(
            resourceInPool = soundPool.load(context, R.raw.bullet_shot, 1),
            pool = soundPool
        ))

        sounds.add(BULLET_BURST_INDEX, GameSound(
            resourceInPool = soundPool.load(context, R.raw.bullet_burst, 1),
            pool = soundPool
        ))

        sounds.add(TANK_MOVE_INDEX, GameSound(
            resourceInPool = soundPool.load(context, R.raw.tank_move_long, 1),
            pool = soundPool
        ))

        // слушатель окончания загрузки
        soundPool.setOnLoadCompleteListener { soundPool, sampleId, status ->
            // если это интро и оно загружено - то запуск
            if (sampleId == sounds[INTRO_MUSIC_INDEX].resourceInPool && status == SUCCESS_UPLOAD)
                playIntroMusic()

            soundsReady++
            if (soundsReady == sounds.size) { // если все загружены
                progressIndicator.dismissProgress() // скрытие прогресса
                allSoundsLoaded = true // все готово
            }
        }
    }

    // метод для обозначения, что все звуки загружены
    fun areSoundsReady() = allSoundsLoaded

    // метод для проигрывания интро
    fun playIntroMusic() {
        sounds[INTRO_MUSIC_INDEX].startOrResume(isLooping = false)
    }

    // метод для остановки всех звуков
    fun pauseSounds() {
        pauseSound(INTRO_MUSIC_INDEX)
        pauseSound(BULLET_SHOT_INDEX)
        pauseSound(BULLET_BURST_INDEX)
        pauseSound(TANK_MOVE_INDEX)
    }

    // метод для остановки звука по индексу
    private fun pauseSound(index: Int) {
        sounds[index].pause()
    }

    // метод для воспр. стрельбы
    fun bulletShot() {
        sounds[BULLET_SHOT_INDEX].soundPlay()
    }

    // метод для воспр. взрыва
    fun bulletBurst() {
        sounds[BULLET_BURST_INDEX].soundPlay()
    }

    // метод для воспр. перемещения
    fun tankMove() {
        sounds[TANK_MOVE_INDEX].startOrResume(isLooping = true)
    }

    // метод для воспр. остановки
    fun tankStop() {
        sounds[TANK_MOVE_INDEX].pause()
    }
}
