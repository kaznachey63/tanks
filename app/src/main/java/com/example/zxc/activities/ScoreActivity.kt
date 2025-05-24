package com.zxc.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.zxc.databinding.ActivityScoreBinding
import com.example.zxc.sounds.ScoreSoundPlayer

// код запроса
const val SCORE_REQUEST_CODE = 100

class ScoreActivity : AppCompatActivity() {

    // компаньон-объект - позволяет создать статические переменные и функции,
    // доступные без создания экземпляра класса
    companion object {
        // константа-ключ для передачи счёта через Intent,
        // используется при передаче данных из одной Activity в другую.
        const val EXTRA_SCORE = "extra_score"

        // метод создающий Intent для запуска ScoreActivity.
        fun createIntent(context: Context, score: Int): Intent {
            // создание объекта Intent, указывая, что нужно открыть ScoreActivity.
            return Intent(context, ScoreActivity::class.java)
                .apply {
                    // добавление в Intent - счёта игрока.
                    putExtra(EXTRA_SCORE, score)
                }
        }
    }


    // отложенное создание объекта
    private val scoreSoundPlayer by lazy {
        //  лямбда-функция, которая запускается, когда звук готов
        ScoreSoundPlayer(this, soundReadyListener =  {
            startScoreCounting()
        })
    }

    // метод для начала подсчета очков
    private fun startScoreCounting() {
        Thread(Runnable {
            var currentScore = 0
            while (currentScore <= score) {
                // обновление UI нельзя делать из фонового потока,
                // потому запуска в главном потоке
                runOnUiThread {
                    binding.scoreTextView.text = currentScore.toString()
                    // currentScore += 100
                    currentScore += 1
                }
                Thread.sleep(150)
            }
            // остановка звука счета
            scoreSoundPlayer.pauseScoreSound()
        }).start()
    }

    var score = 0
    lateinit var binding: ActivityScoreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // получаем число, переданное через Intent, иначе - 0.
        score = intent.getIntExtra(EXTRA_SCORE, 0)
        scoreSoundPlayer.playScoreSound()
    }

    // метод остановки звука при выходе
    override fun onPause() {
        super.onPause()
        scoreSoundPlayer.pauseScoreSound()
    }

    // метод нажатия системной кнопки назад
    override fun onBackPressed() {
        super.onBackPressed()
        setResult(Activity.RESULT_OK) // - это может использовать вызывающая активность
        finish()
    }
}