/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 *
 * GhostGram 骨架说明（地基批第二轮，详见 PLAN.md 第十节）：
 * 本文件 4 个键在供体侧的消费方是崩溃上报的 OOM 非致命记录（8.1 C5，
 * 不带入）与远程配置绑定表（8.1 C1，不带入）；键本身是纯本地存储，
 * 按原样落盘为惰性键，键名/默认值与供体逐项一致（脚本核对）。
 * 将来若做本地 OOM 提示可复用弹窗两键，上报通道永不接回。
 */

package uz.unnarsx.cherrygram.core.configs

import android.app.Activity
import android.content.SharedPreferences
import org.telegram.messenger.ApplicationLoader
import uz.unnarsx.cherrygram.preferences.boolean
import uz.unnarsx.cherrygram.preferences.float

object CherrygramExperimentalConfig {

    private val sharedPreferences: SharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)

    var recordOOMasNonFatal by sharedPreferences.boolean("CG_Record_OOM_AS_NF", true)
    var use_CG_OOMHandler by sharedPreferences.boolean("CG_Use_CG_OOMHandler", true)
    var oomHandlerPopup by sharedPreferences.boolean("CG_OOMHandlerPopup", false)
    var oomHandlerPopupThreshold by sharedPreferences.float("CG_OOMHandlerPopupThreshold", 95f)

}
