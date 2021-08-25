package com.charlie.asmdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        testAsmLog()

        testDeleteLog()
    }

    private fun testDeleteLog() {
        Log.d("testDeleteLog", "=================test log===================")
    }


    fun testAsmLog() {
        val start  = System.currentTimeMillis()
        val cost = System.currentTimeMillis() - start
        Log.d("test","testAsmLog cost = $cost ")
    }
}
