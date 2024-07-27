package com.sryang.expandabletext

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.sryang.expandabletext.ui.theme.ExpandableTextTheme
import com.sryang.library.PreviewExpandableText
import com.sryang.library.PreviewExpandableText1

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExpandableTextTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PreviewExpandableText1()
                }
            }
        }
    }
}