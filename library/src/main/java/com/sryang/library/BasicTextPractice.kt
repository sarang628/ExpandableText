package com.sryang.library

import android.util.Log
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay

@Preview
@Composable
fun BasicTextPractice() {

    var text by remember { mutableStateOf("") }

    LaunchedEffect(key1 = "") {
        while (true) {
            delay(100)
            text += "a"
        }
    }

    BasicText(text = text,
        maxLines = 2,
        onTextLayout = {
            Log.d("__sryang", "onTextLayout: ${it.hasVisualOverflow}")
        })
}