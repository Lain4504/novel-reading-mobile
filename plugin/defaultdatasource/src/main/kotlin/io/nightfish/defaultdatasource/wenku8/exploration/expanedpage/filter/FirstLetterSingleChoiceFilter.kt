package io.nightfish.defaultdatasource.wenku8.exploration.expanedpage.filter

import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.filter.SingleChoiceFilter

class FirstLetterSingleChoiceFilter(onChange: (String) -> Unit): SingleChoiceFilter(
    title = "首字母",
    dialogTitleId = R.string.key_letter_filter_title,
    descriptionId = R.string.key_letter_filter_desc,
    choices = listOf("任意", "0~9", " A ", " B ", " C ", " D ", " E ", " F ", " G ", " H ", " I ", " J ", " K ", " L ", " M ", " N ", " O ", " P ", " Q ", " R ", " S ", " T ", " U ", " V ", " W ", " X ", " Y ", " Z "),
    defaultChoice = "任意",
    onChange = onChange
)