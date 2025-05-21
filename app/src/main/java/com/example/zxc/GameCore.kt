package com.zxc

import android.app.Activity
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.zxc.activities.SCORE_REQUEST_CODE
import com.zxc.activities.ScoreActivity
import com.zxc.activities.binding

class GameCore(private val activity: Activity) {
    @Volatile // (аннотация) - гарантирует видимость изменений между потоками
    private var isPlay = false
    private var isPlayerOrBaseDestroyed = false
    private  var isPlayerWin = false

    // метод переключения состояния игры
    fun startOrPauseTheGame() {
        isPlay  = !isPlay
    }

    // игра играется
    fun isPlaying() = isPlay && !isPlayerOrBaseDestroyed && !isPlayerWin

    // метод паузы игры
    fun pauseTheGame() {
        isPlay = false
    }

    // метод проверки на победу над игроком
    fun destroyPlayerOrBase(score: Int) {
        isPlayerOrBaseDestroyed = true
        pauseTheGame()
        animateEndGame(score)
    }

    // метод возобновления игры
    fun resumeTheGame() {
        isPlay = true
    }

    // метод победы игрока
    fun playerWon(score: Int) {
        isPlayerWin = true

        // запуск активити с результатом
        activity.startActivityForResult(ScoreActivity.createIntent(activity,score), SCORE_REQUEST_CODE)
    }

    // метод для анимации окончания игры 
    private fun animateEndGame(score: Int) {
        activity.runOnUiThread { // запуск блока кода в главном потоке (ui)
            binding.gameOverText.visibility = View.VISIBLE
            val slideUp = AnimationUtils.loadAnimation(activity, R.anim.slide_up)
            binding.gameOverText.startAnimation(slideUp)

            // слушатель событий анимации
            slideUp.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) { }

                override fun onAnimationRepeat(animation: Animation?) { }

                // метод срабатывает когда анимация заканчивается 
                override fun onAnimationEnd(animation: Animation?) {
                    activity.startActivityForResult( // запуск активити с результатом
                        ScoreActivity.createIntent(activity, score),
                        SCORE_REQUEST_CODE
                    )
                }
            })
        }
    }
}