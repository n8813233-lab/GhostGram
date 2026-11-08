/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 *
 * GhostGram 骨架说明（地基批第二轮，详见 PLAN.md 第十节）：
 * 供体 init() 整段切除——它包含统计事件上报（8.1 C4）与按隐藏常量链接
 * 加载消息后交给混淆自检模块的流程（8.1 C7/C8/C9），协程作用域随之去掉；
 * 统计开关键因其受控通道已整删而不移植（键名见 PLAN.md 第十节登记）。
 * 其余键名/默认值与供体逐项一致（脚本核对）。
 */

package uz.unnarsx.cherrygram.core.configs

import android.app.Activity
import android.content.SharedPreferences
import org.telegram.messenger.ApplicationLoader
import uz.unnarsx.cherrygram.preferences.boolean

object CherrygramPrivacyConfig {

    private val sharedPreferences: SharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)

    /** Privacy start */
    var hideProxySponsor by sharedPreferences.boolean("SP_NoProxySponsor", true)
    /** Privacy finish */

    /** Passcode lock start */
    var hideArchiveFromChatsList by sharedPreferences.boolean("SP_HideArchiveFromChatsList", false)
    var askBiometricsToOpenArchive by sharedPreferences.boolean("SP_AskBiometricsToOpenArchive", false)
    var askBiometricsToOpenEncrypted by sharedPreferences.boolean("SP_AskBiometricsToOpenEncrypted", false)
    var askBiometricsToOpenChat by sharedPreferences.boolean("SP_AskBiometricsToOpenChat", false)
    var askPasscodeBeforeDelete by sharedPreferences.boolean("SP_AskPinBeforeDelete", false)
    var allowSystemPasscode by sharedPreferences.boolean("SP_AllowSystemPasscode", false)
    /** Passcode lock finish */

    /** Misc **/
    var hideArchivedStories by sharedPreferences.boolean("CP_HideArchivedStories", false)
    /** Misc **/

    /** GhostGram 切除（C4/C7/C8/C9）：供体 init() 的统计上报与隐藏常量
     *  消息加载流程整段不移植，本 Config 无 init */

}
