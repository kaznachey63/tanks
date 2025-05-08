package com.zxc

import android.app.Activity
import android.content.Context
import com.zxc.models.Element
import com.google.gson.Gson

// константа-ключ для хранения уровня в SharedPreferences (JSON-строки с элементами уровня)
const val KEY_LEVEL = "key_level" 

class LevelStorage(val context: Context) {
    // получение SharedPreferences 
    private val prefs = (context as Activity).getPreferences(Context.MODE_PRIVATE)
    private val gson = Gson()

    // метод для сохранения уровня
    fun saveLevel(elementsOnContainer: List<Element>) {
        // сохранение JSON в SharedPreferences с ключом
        prefs.edit().putString(KEY_LEVEL, gson.toJson(elementsOnContainer)).apply()
    }

    // метод для загрузки уровня
    fun loadLevel(): List<Element>? { // ? - нулл, если загрузка не удалась
        // ?: - оператор Элвис, елси из метода получается нулл, то и ретюрн сразу с нуллом
        val levelFromPrefs = prefs.getString(KEY_LEVEL, null)?: return null

        // создание типа списка элементов - нужно для корректной десериализации из JSON строки обратно в список объектов через Gson
        val type = object : com.google.gson.reflect.TypeToken<List<Element>>() {}.type

        // десериализация JSON-строки из levelFromPrefs в список объектов элементов
        val elementsFromStorage: List<Element> = gson.fromJson(levelFromPrefs, type)

        // новый список, куда будут добавлены новые копии объектов списка
        val elementsWithNewIds = mutableListOf<Element>()
        
        // перебор элементов
        elementsFromStorage.forEach {
            elementsWithNewIds.add( // создание новых экземпляров элемент
                Element(
                    material = it.material,
                    coordinate = it.coordinate
                )
            )
        }
        return elementsFromStorage
    }
}