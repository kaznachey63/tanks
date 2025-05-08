package com.example.zxc.utils

// интерфейс, для отображения прогресса
interface ProgressIndicator {
    fun showProgress() // отображение
    fun dismissProgress() // скрытие
}
