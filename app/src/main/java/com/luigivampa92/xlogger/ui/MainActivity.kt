package com.luigivampa92.xlogger.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.luigivampa92.xlogger.R
import com.luigivampa92.xlogger.data.DebugLastLogStorage

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val textView = findViewById<View>(R.id.text_debug) as TextView
        val storage = DebugLastLogStorage(this)
        textView.text = storage.getLastLog()
    }
}