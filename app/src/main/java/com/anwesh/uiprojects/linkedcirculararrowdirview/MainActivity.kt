package com.anwesh.uiprojects.linkedcirculararrowdirview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.circulararrowdirview.CircularArrowDirView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CircularArrowDirView.create(this)
    }
}
