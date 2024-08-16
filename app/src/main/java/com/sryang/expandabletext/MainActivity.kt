package com.sryang.expandabletext

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.sryang.expandabletext.ui.theme.ExpandableTextTheme
import com.sryang.library.BasicTextPractice

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExpandableTextTheme {
                BasicTextPractice()
            }
        }
    }
}