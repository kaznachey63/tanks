package com.zxc.sounds

import android.media.AudioManager
import android.media.SoundPool
import android.os.Build

// фабричный класс для создания SoundPool — объекта
class SoundPoolFactory {

    // макс. кол-во одновременно проигрываемых звуков
    private val maxStreamsAmount = 6

    // метод создаёт и возвращает объект SoundPool в зависимости от версии Android
    fun createSoundPool(): SoundPool {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)

            // для Android 5.0+ - новый апи
            // создаёт объект строителя для SoundPool
            // это не сам SoundPool, а объект,
            // который позволяет настроить параметры перед созданием
            SoundPool.Builder()
                .setMaxStreams(maxStreamsAmount)
                .build() //  возвращает ноый готовый объект

        // для старых устройств используем устаревший конструктор
        else SoundPool(maxStreamsAmount, AudioManager.STREAM_MUSIC, 0)
    }
}
