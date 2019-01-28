package com.study.blebroadcast

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.study.blebroadcast.ui.BroadcastActivity
import com.study.blebroadcast.ui.MonitorActivity
import com.study.blebroadcast.utils.BroadcastUtil

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun broadcast(view:View){
        if (BroadcastUtil.getInstance(this).initBLE()) {
            startActivity(Intent(this@MainActivity, BroadcastActivity::class.java))
        }
    }

    fun monitor(view:View){
        if (BroadcastUtil.getInstance(this).initBLE()){
            startActivity(Intent(this@MainActivity,MonitorActivity::class.java))
        }
    }

    override fun onDestroy() {
        System.exit(0)
        super.onDestroy()
    }
}
