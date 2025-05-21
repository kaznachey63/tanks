package com.zxc.sounds

import android.media.SoundPool // класс SoundPool для работы со звуками

// класс для управления воспроизведением звуков
class GameSound(
    var resourceInPool: Int, // ID звукового ресурса в SoundPool
    var isStarted: Boolean = false, // запущен ли звук
    val pool: SoundPool // сылка на SoundPool
) {
    // метод для воспроизведения звука
    fun soundPlay() {
        // громкость, приоритет, повтор, скорость
        pool.play(resourceInPool, 1f, 1f, 1, 0, 1f)
    }

    // метод для запуска или возобновления воспр. звука
    fun startOrResume(isLooping: Boolean) {
        // запущен - продолжаем
        if (isStarted)
            pool.resume(resourceInPool)

        // не запущен - запускаем
        else {
            // Иначе, запускаем воспроизведение с начала и устанавливаем флаг
            isStarted = true

            // сохранение нового ресурса
            resourceInPool = pool.play(resourceInPool, 1f, 1f, 1, isLooping.toInt(), 1f)
        }
    }

    // метод для преобразования бул. знач. в цифр.
    private fun Boolean.toInt() =
        if (this) -1 // повторять бесконечно
        else 0 // не повторять

    // метод для паузы звука
    fun pause() {
        pool.pause(resourceInPool)
    }
}
