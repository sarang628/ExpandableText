package com.sryang.library

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val ExpandableTextColor: Color @Composable get() = if (isSystemInDarkTheme()) Color.White else Color.Black
val SeeMoreAndLessColor: Color @Composable get() = if (isSystemInDarkTheme()) Color.LightGray else Color.Gray

/** 접혀있을 때 라인 수*/
private const val collaspLine = 3

@Composable
fun ExpandableText(
    modifier: Modifier = Modifier,
    nickName: String? = null,
    text: String,
    onClickNickName: () -> Unit,
    expandableTextColor: Color = ExpandableTextColor
) {
    // @formatter:off
    var isExpanded by remember { mutableStateOf(false) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var isClickable by remember { mutableStateOf(false) }
    val seeMoreandLessColor = SeeMoreAndLessColor

    //닉네임 + 내용을 초기에 설정한 text 생성
    var textWithMoreLess by remember { mutableStateOf(buildAnnotatedString {
        nickName?.let {
            withStyle(SpanStyle(color = expandableTextColor, fontWeight = FontWeight.Bold))
            {
                append(it)
            }
            append(" ")
        }
        withStyle(SpanStyle(color = expandableTextColor)) {
            append(text)
        }
    }) }

    LaunchedEffect(textLayoutResult) {
        textLayoutResult?.let {
            when {
                // 텍스트 확장 상태
                isExpanded -> {
                    textWithMoreLess = originString(nickName, text, seeMoreandLessColor, expandableTextColor)
                }

                // 텍스트가 펼쳐지지 않은 상태이고 최대 줄 수를 초과하는 경우
                !isExpanded && it.hasVisualOverflow -> {
                    val lastCharIndex = it.getLineEnd(collaspLine-1)
                    textWithMoreLess = summarizedString(nickName, text, lastCharIndex, seeMoreandLessColor = seeMoreandLessColor, expandableTextColor = expandableTextColor)
                    isClickable = true
                }
            }
        }
    }

    // UriHandler parse and opens URI inside AnnotatedString Item in Browse
    val uriHandler = LocalUriHandler.current

    //Composable container
    Box(modifier = modifier)
    {
        SelectionContainer {
            ClickableText(
                text = textWithMoreLess,
                style = TextStyle(color = Color.DarkGray, fontSize = 15.sp),
                onClick = { offset ->
                    Log.d("__ExpandableText", "offset : ${offset}")
                    textWithMoreLess.getStringAnnotations(
                        tag = "link_tag",
                        start = offset,
                        end = offset
                    ).firstOrNull()?.let { stringAnnotation ->
                        uriHandler.openUri(stringAnnotation.item)
                    }

                    if (offset < (nickName?.length ?: 0)) {
                        onClickNickName.invoke()
                        Log.d("ExpandableText", "onClickNickName")
                    }

                    if (isClickable) {
                        textWithMoreLess.getStringAnnotations(
                            tag = "show_more_tag",
                            start = offset,
                            end = offset
                        ).firstOrNull()?.let {
                            isExpanded = !isExpanded
                        }
                    }
                },
                maxLines = if (isExpanded) Int.MAX_VALUE else collaspLine,
                onTextLayout = { textLayoutResult = it },
                modifier = modifier.animateContentSize()
            )
        }
    }
    // @formatter:on
}

fun originString(
    nickName: String?,
    text: String,
    seeMoreandLessColor: Color,
    expandableTextColor: Color,
): AnnotatedString {
    return buildAnnotatedString {
        //닉네임 추가
        nickName?.let {
            withStyle(
                SpanStyle(
                    color = expandableTextColor,
                    fontWeight = FontWeight.Bold
                )
            ) { append(it) }
            append(" ")
        }
        // 내용 추가
        withStyle(SpanStyle(color = expandableTextColor)) { append(text) }
        pushStringAnnotation(tag = "show_more_tag", annotation = "") // 어노테이션 추가
        withStyle(SpanStyle(color = seeMoreandLessColor)) { append(" See less") } // see less 추가
        pop() // 어노테이션 제거
    }
}

fun summarizedString(
    nickName: String?,
    text: String,
    lastCharIndex: Int,
    showMoreString: String = "... more",
    seeMoreandLessColor: Color = Color.Unspecified,
    expandableTextColor: Color = Color.Unspecified,
): AnnotatedString {
    return buildAnnotatedString {
        //닉네임이 있는 경우
        if (nickName != null) {
            withStyle(
                SpanStyle(
                    color = expandableTextColor,
                    fontWeight = FontWeight.Bold
                )
            ) { append(nickName) }
            append(" ")
            withStyle(SpanStyle(color = expandableTextColor)) {
                // 내용 추가
                append(text.substring(0, lastCharIndex)
                    .dropLast(showMoreString.length + nickName.length + 1) // ... more 추가를 위에 문장 자르기
                    .dropLastWhile { it == ' ' || it == '.' }) // 주의: 조정한 글자가 오버플로우되면 무한 루프 발생
            }
        }
        //닉네임이 없는 경우
        else {
            withStyle(SpanStyle(color = expandableTextColor)) {
                // 내용 추가
                append(text.substring(0, lastCharIndex)
                    .dropLast(showMoreString.length + 1)  // ... more 추가를 위에 문장 자르기
                    .dropLastWhile { it == ' ' || it == '.' }) // 주의: 조정한 글자가 오버플로우되면 무한 루프 발생
            }
        }

        append("... ")
        pushStringAnnotation(tag = "show_more_tag", annotation = "")
        withStyle(SpanStyle(color = seeMoreandLessColor)) { append("more") }
        pop()
    }
}

@Preview
@Composable
fun PreviewExpandableText() {
    ExpandableText(
        nickName = "nickName",
        text = "a a a a",
        onClickNickName = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewExpandableText1() {
    Box(Modifier.height(120.dp))
    {
        ExpandableText(
            modifier = Modifier.align(alignment = Alignment.BottomCenter),
            nickName = "nickName",
            text = "aaaaaaaaaaaaaaaaaaaaaaaaaa " +
                    "bb bb bb bb bbb bb bb bb bb bb bb" +
                    "ccc ccc cccc cccc ccc ccc cc" +
                    "ddddddddd ddd ddd dddd d ccc ccc cccc cccc ccc ccc cc ccc ccc cccc cccc ccc ccc cc ccc ccc cccc cccc ccc ccc cc ccc ccc cccc cccc ccc ccc cc ccc ccc cccc cccc ccc ccc cc ccc ccc cccc cccc ccc ccc cc ccc ccc cccc cccc ccc ccc cc",
            onClickNickName = {}
        )
    }
}