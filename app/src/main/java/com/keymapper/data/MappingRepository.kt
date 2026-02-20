package com.keymapper.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MappingRepository(context: Context) {

    private val prefs = context.getSharedPreferences("key_mappings", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getAll(): List<KeyMapping> {
        val json = prefs.getString("list", "[]") ?: "[]"
        val type = object : TypeToken<List<KeyMapping>>() {}.type
        return gson.fromJson(json, type)
    }

    fun save(mappings: List<KeyMapping>) {
        prefs.edit().putString("list", gson.toJson(mappings)).apply()
    }

    fun add(mapping: KeyMapping) {
        save(getAll() + mapping)
    }

    fun toggle(id: String) {
        save(getAll().map { if (it.id == id) it.copy(isEnabled = !it.isEnabled) else it })
    }

    fun delete(id: String) {
        save(getAll().filter { it.id != id })
    }
}
