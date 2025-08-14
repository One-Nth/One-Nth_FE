package com.example.onenthapp

import android.content.Context
import android.text.InputType
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView

private fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

/** 엔터/완료/보내기 → confirm() 호출 후 입력창 비우고 키보드 내리기 */
fun EditText.clearOnEnter(confirm: (() -> Unit)? = null) {
    // 멀티라인 플래그 제거 + 완료 버튼 지정 + 한 줄
    inputType = inputType and InputType.TYPE_TEXT_FLAG_MULTI_LINE.inv()
    imeOptions = EditorInfo.IME_ACTION_DONE
    setSingleLine(true)

    setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE ||
            actionId == EditorInfo.IME_ACTION_GO   ||
            actionId == EditorInfo.IME_ACTION_SEND) {
            confirm?.invoke()
            text?.clear()
            v.hideKeyboard()
            return@OnEditorActionListener true
        }
        false
    })

    setOnKeyListener { v, keyCode, event ->
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
            confirm?.invoke()
            text?.clear()
            v.hideKeyboard()
            true
        } else false
    }

    // 포커스가 빠질 때도 남은 입력을 확정하고 비우고 싶다면 주석 해제
    // setOnFocusChangeListener { v, hasFocus ->
    //     if (!hasFocus && !text.isNullOrBlank()) {
    //         confirm?.invoke()
    //         text?.clear()
    //     }
    // }
}
