package com.sryang.library

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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

/**
 * @param minCollapsedLines 접혔을 때 라인 수
 */
@Composable
fun ExpandableText(modifier             : Modifier      = Modifier,
                   nickName             : String        = "",
                   text                 : String        = "",
                   onClickNickName      : () -> Unit    = {},
                   expandableTextColor  : Color         = ExpandableTextColor,
                   minCollapsedLines    : Int           = 1
) {
    // @formatter:off
    var isExpanded          : Boolean           by rememberSaveable { mutableStateOf(false) }
    var textLayoutResult    : TextLayoutResult? by remember { mutableStateOf(null) }
    var isClickable         : Boolean           by remember { mutableStateOf(false) }
    val seeMoreAndLessColor : Color             = SeeMoreAndLessColor

    //닉네임 + 내용을 초기에 설정한 text 생성
    var textWithMoreLess    : AnnotatedString   by remember { mutableStateOf(nickNameAndContent(nickName, text, expandableTextColor)) }

    LaunchedEffect(textLayoutResult) {
        textLayoutResult?.let {
            when {
                // 텍스트 확장 상태
                isExpanded -> {
                    textWithMoreLess = originString(nickName, text, seeMoreAndLessColor, expandableTextColor)
                }

                // 텍스트가 펼쳐지지 않은 상태이고 최대 줄 수를 초과하는 경우
                !isExpanded && it.hasVisualOverflow -> {
                    val lastCharIndex = it.getLineEnd(minCollapsedLines-1)
                    textWithMoreLess = summarizedString(nickName            = nickName,
                                                        text                = text,
                                                        lastCharIndex       = lastCharIndex,
                                                        seeMoreAndLessColor = seeMoreAndLessColor,
                                                        expandableTextColor = expandableTextColor)
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
                modifier        = modifier.animateContentSize(),
                text            = textWithMoreLess,
                style           = TextStyle(color = Color.DarkGray, fontSize = 15.sp),
                maxLines        = if (isExpanded) Int.MAX_VALUE else minCollapsedLines,
                onTextLayout    = { textLayoutResult = it },
                onClick         = { offset ->
                    textWithMoreLess.getStringAnnotations(
                        tag = "link_tag",
                        start = offset,
                        end = offset
                    ).firstOrNull()?.let { stringAnnotation ->
                        uriHandler.openUri(stringAnnotation.item)
                    }

                    if (offset < nickName.length) {
                        onClickNickName.invoke()
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
                }
            )
        }
    }
    // @formatter:on
}

fun nickNameAndContent(nickName : String = "",
                       content  : String = "",
                       color    : Color = Color.Black) : AnnotatedString{
    return buildAnnotatedString {
        nickName.let {
            withStyle(SpanStyle(color = color, fontWeight = FontWeight.Bold))
            {
                append(it)
            }
            append(" ")
        }
        withStyle(SpanStyle(color = color)) {
            append(content)
        }
    }
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
    nickName            : String    = "",
    text                : String    = "",
    lastCharIndex       : Int       = 0,
    showMoreString      : String    = "... more",
    seeMoreAndLessColor : Color     = Color.Unspecified,
    expandableTextColor : Color     = Color.Unspecified,
): AnnotatedString {
    return buildAnnotatedString {
        //닉네임이 있는 경우
        withStyle(
            SpanStyle(
                color = expandableTextColor,
                fontWeight = FontWeight.Bold
            )
        ) { append(nickName) }
        append(" ")
        withStyle(SpanStyle(color = expandableTextColor)) {
            // 내용 추가
            append(
                text.take(if(lastCharIndex > text.length) text.length else lastCharIndex)
                .dropLast(showMoreString.length + nickName.length + 1) // ... more 추가를 위에 문장 자르기
                .dropLastWhile { it == ' ' || it == '.' }) // 주의: 조정한 글자가 오버플로우되면 무한 루프 발생
        }

        append("... ")
        pushStringAnnotation(tag = "show_more_tag", annotation = "")
        withStyle(SpanStyle(color = seeMoreAndLessColor)) { append("more") }
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