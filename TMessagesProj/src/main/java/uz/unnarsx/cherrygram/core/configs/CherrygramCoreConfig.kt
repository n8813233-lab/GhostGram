/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 *
 * GhostGram 骨架说明（地基批第二轮，详见 PLAN.md 第十节）：
 * 本文件按 PLAN.md 8.1 的 C14 先行处理后落盘——签名/篡改自检字段群、
 * 应用内更新开关组、远程名单刷新与隐藏常量驱动的初始化/消息加载整段切除，
 * 只保留本地开关与本地迁移。其余键名/默认值与供体逐项一致（脚本核对）。
 */

package uz.unnarsx.cherrygram.core.configs

import android.app.Activity
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.Keep
import androidx.core.content.edit
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.MessagesController
import org.telegram.messenger.UserConfig
import uz.unnarsx.cherrygram.preferences.boolean
import uz.unnarsx.cherrygram.preferences.int

@Keep
object CherrygramCoreConfig {

    private val sharedPreferences: SharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)

    fun putBoolean(key: String, value: Boolean) {
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        preferences.edit {
            putBoolean(key, value)
        }
    }

    fun putStringForUserPrefs(key: String, value: String) {
        val preferences = MessagesController.getMainSettings(UserConfig.selectedAccount)
        preferences.edit {
            putString(key, value)
        }
    }

    /** General start */
    /** Animations start */
    const val ANIMATION_CLASSIC = 0
    const val ANIMATION_SPRING = 1
    const val ANIMATION_PIXEL = 2
    var navigationAnimation by sharedPreferences.int("CG_NavigationAnimation", ANIMATION_SPRING)

    var actionbarCrossfade by sharedPreferences.boolean("CG_ActionbarCrossfade", false)
    var predictiveBack by sharedPreferences.boolean("CG_PredictiveBack", true)
    /** Animations finish */

    /** Notifications start */
    var silenceNonContacts by sharedPreferences.boolean("CP_SilenceNonContacts", false)
    var ignoreMentions by sharedPreferences.boolean("CG_IgnoreMentions", false)
    var ignoreMentionsMarkAsRead by sharedPreferences.boolean("CG_IgnoreMentionsMarkAsRead", false)
    var oldNotificationIcon by sharedPreferences.boolean("AP_Old_Notification_Icon", false)
    var residentNotification by sharedPreferences.boolean("CG_ResidentNotification", Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM && !ApplicationLoader.checkPlayServices())
    /** Notifications finish */

    /** Stories start */
    var hideStories by sharedPreferences.boolean("CP_HideStories", false)
    var archiveStoriesFromUsers by sharedPreferences.boolean("CP_ArchiveStoriesFromUsers", false)
    var archiveStoriesFromChannels by sharedPreferences.boolean("CP_ArchiveStoriesFromChannels", false)
    /** Stories finish */

    /** Miscellaneous start */
    var noRounding by sharedPreferences.boolean("CP_NoRounding1", true)
    var systemEmoji by sharedPreferences.boolean("AP_SystemEmoji", false)
    var systemFonts by sharedPreferences.boolean("AP_SystemFonts", true)

    const val EDGE_MODE_ENABLE = 0
    const val EDGE_MODE_DISABLE = 1
    const val EDGE_MODE_AUTO = 2
    var edgeToEdgeMode by sharedPreferences.int("CP_EdgeToEdge", EDGE_MODE_AUTO)

    const val TABLET_MODE_ENABLE = 0
    const val TABLET_MODE_DISABLE = 1
    const val TABLET_MODE_AUTO = 2
    var tabletMode by sharedPreferences.int("AP_Tablet_Mode", TABLET_MODE_AUTO)
    /** Miscellaneous finish */

    /** Network start */
    const val BOOST_NONE = 0
    const val BOOST_AVERAGE = 1
    const val BOOST_EXTREME = 2
    var downloadSpeedBoost by sharedPreferences.int("EP_DownloadSpeedBoost", BOOST_NONE)

    var uploadSpeedBoost by sharedPreferences.boolean("EP_UploadSpeedBoost", false)
    var slowNetworkMode by sharedPreferences.boolean("EP_SlowNetworkMode", false)
    /** Network finish */

    /** GhostGram 切除（C6/C14）：应用内更新开关组整段不移植，更新靠手动装包 */

    /** Misc start */
    var cgBrandedScreenshots by sharedPreferences.boolean("DP_BrandedScreenshots", false)
    var sleepTimer by sharedPreferences.boolean("CG_Sleep_Timer", false)

    /** AppSec start */
    var showNotifications by sharedPreferences.boolean("CG_ShowNotifications", true)

    /** GhostGram 切除（C14）：签名/篡改自检字段群（含证书扫描状态与全部
     *  setter）整段不移植，自检与上报逻辑一律不带入 */
    /** AppSec finish */

    /** Misc finish */

    /** Cherrygram build types start */
    @JvmStatic
    fun isStandaloneStableBuild(): Boolean {
        return ApplicationLoader.isStandaloneBuild() && !isDevBuild() && !isStandalonePremiumBuild() && !isStandaloneBetaBuild()
    }

    @JvmStatic
    fun isStandaloneBetaBuild(): Boolean {
        return false
    }

    @JvmStatic
    fun isDevBuild(): Boolean {
        return false
    }

    @JvmStatic
    fun isStandalonePremiumBuild(): Boolean {
        return false
    }

    @JvmStatic
    fun isPlayStoreBuild(): Boolean {
        return !ApplicationLoader.isStandaloneBuild()
    }
    /** Cherrygram build types finish */

    /** GhostGram 切除（C9）：远程名单刷新时间戳不移植 */

    /** Migration start */
    private fun migratePreferences() {
        if (CherrygramAppearanceConfig.showIDDC_old >= CherrygramAppearanceConfig.ID_DC) {
            CherrygramAppearanceConfig.showIDDC_old = CherrygramAppearanceConfig.ID_DC_NONE
            CherrygramAppearanceConfig.showIDDC = true
        }
    }
    /** Migration finish */

    /**
     * GhostGram 瘦身版 init（C8/C9/C10）：供体 init 里的远程名单自动刷新、
     * 官方频道字符串校验、按隐藏常量链接加载消息三段整段切除，
     * 只保留本地偏好迁移；协程作用域随切除一并去掉。
     */
    fun init() {
        migratePreferences()
    }

}
