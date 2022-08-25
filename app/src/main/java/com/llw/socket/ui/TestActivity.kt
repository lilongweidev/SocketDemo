package com.llw.socket.ui

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.llw.socket.R


class TestActivity : AppCompatActivity() {

    private var isExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        // get the bottom sheet view
        val llBottomSheet = findViewById<View>(R.id.bottom_sheet) as LinearLayout
        val tvTest = findViewById<TextView>(R.id.tv_test)

        // init the bottom sheet behavior
        val bottomSheetBehavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(llBottomSheet)

        // change the state of the bottom sheet
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        // set the peek height
//        bottomSheetBehavior.peekHeight = 240

        // set hideable or not
        bottomSheetBehavior.isHideable = false
        bottomSheetBehavior.isDraggable = false

        tvTest.setOnClickListener {
            if (isExpanded) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                isExpanded = false
            } else {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                isExpanded = true
            }
        }

        // set callback for changes
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
    }
}