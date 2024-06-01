package com.eagletech.mypaint.dataApp

import android.content.Context
import android.content.SharedPreferences


class MyData constructor(context: Context) {
    private val sharedPreferences: SharedPreferences

    init {
        sharedPreferences = context.getSharedPreferences("MyData", Context.MODE_PRIVATE)
    }

    companion object {
        @Volatile
        private var instance: MyData? = null

        fun getInstance(context: Context): MyData {
            return instance ?: synchronized(this) {
                instance ?: MyData(context).also { instance = it }
            }
        }
    }

    // Lấy ra thông tin mua theo lượt
    fun getSaves(): Int {
        return sharedPreferences.getInt("saves", 0)
    }

    fun setSaves(lives: Int) {
        sharedPreferences.edit().putInt("saves", lives).apply()
    }

    fun addSaves(amount: Int) {
        val currentSaves = getSaves()
        setSaves(currentSaves + amount)
    }

    fun removeSaves() {
        val currentSaves = getSaves()
        if (currentSaves > 0) {
            setSaves(currentSaves - 1)
        }
    }

    // Lấy thông tin mua premium
    var isPremiumSaves: Boolean?
        get() {
            val userId = sharedPreferences.getString("UserId", "")
            return sharedPreferences.getBoolean("PremiumPlan_\$userId$userId", false)
        }
        set(state) {
            val userId = sharedPreferences.getString("UserId", "")
            sharedPreferences.edit().putBoolean("PremiumPlan_\$userId$userId", state!!).apply()
//            sharedPreferences.edit().apply()
        }

    // Lưu thông tin người dùng
    fun currentUserId(userid: String?) {
        sharedPreferences.edit().putString("UserId", userid).apply()
//        sharedPreferences.edit().apply()
    }

}