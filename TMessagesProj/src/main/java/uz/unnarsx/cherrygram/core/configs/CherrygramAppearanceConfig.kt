/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 *
 * GhostGram foundation skeleton (2026-11-04, PLAN.md 9.4):
 * - All preference keys/defaults are Cherrygram originals (default look = Cherry).
 * - CUT per PLAN 8.1 C13: ola()/aV()/vI() donation-verification tail
 *   (remote donor lists + a hidden unpublished constant) is NOT ported.
 * - CUT per C13/C9: marketPlaceDrawerButton (remote Marketplace list) is NOT ported.
 * - getCurrentIconPack() restored in the C5 icon-pack batch (PLAN.md 15.2),
 *   verbatim from the donor; iconReplacement key/default were kept.
 */

package uz.unnarsx.cherrygram.core.configs

import android.app.Activity
import android.content.SharedPreferences
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.SharedConfig
import uz.unnarsx.cherrygram.core.icons.icon_replaces.BaseIconReplace
import uz.unnarsx.cherrygram.core.icons.icon_replaces.NoIconReplace
import uz.unnarsx.cherrygram.core.icons.icon_replaces.SolarIconReplace
import uz.unnarsx.cherrygram.preferences.boolean
import uz.unnarsx.cherrygram.preferences.int
import uz.unnarsx.cherrygram.preferences.string

object CherrygramAppearanceConfig {

    private val sharedPreferences: SharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)

    /** Header start */
    var centerTitle by sharedPreferences.boolean("AP_CenterTitle", true)
    var hideSearchFiled by sharedPreferences.boolean("AP_HideSearchField", true)
    var drawSnowInActionBar by sharedPreferences.boolean("AP_DrawSnowInActionBar", false && SharedConfig.getDevicePerformanceClass() >= SharedConfig.PERFORMANCE_CLASS_AVERAGE)
    /** Header finish */

    /** Appearance start */
    var iconBasedSplash by sharedPreferences.boolean("AP_IconBasedSplash", false)

    const val ICON_REPLACE_NONE = 0
    const val ICON_REPLACE_SOLAR = 1

    var iconReplacement by sharedPreferences.int("AP_Icon_Replacements1", ICON_REPLACE_SOLAR)
    fun getCurrentIconPack(): BaseIconReplace {
        return when (iconReplacement) {
            ICON_REPLACE_SOLAR -> SolarIconReplace()
            else -> NoIconReplace()
        }
    }

    var oneUI_SwitchStyle by sharedPreferences.boolean("AP_OneUI_SwitchStyle", true)
    var disableDividers by sharedPreferences.boolean("AP_DisableDividers", true)

    const val GLARE_EFFECTS_DISABLE = 0
    const val GLARE_EFFECTS_DEFAULT = 1
    const val GLARE_EFFECTS_STROKE = 2
    var glareEffectsType by sharedPreferences.int("AP_GlareEffect", GLARE_EFFECTS_DEFAULT)
    /** Appearance finish */

    /** Folders start */
    var tabsHideAllChats by sharedPreferences.boolean("CP_NewTabs_RemoveAllChats", false)
    var tabsNoUnread by sharedPreferences.boolean("CP_NewTabs_NoCounter", false)
    var tabsIncludeMutedInCounter by sharedPreferences.boolean("CP_TabsIncludeMutedInCounter", true)

    const val TAB_TYPE_MIX = 0
    const val TAB_TYPE_TEXT = 1
    const val TAB_TYPE_ICON = 2
    var tabMode by sharedPreferences.int("AP_TabMode", TAB_TYPE_MIX)

    var tabStyleStroke by sharedPreferences.boolean("AP_TabStyleAddStroke", false)
    var folderNameInHeader by sharedPreferences.boolean("AP_FolderNameInHeader", false)
    var foldersAtBottom by sharedPreferences.boolean("AP_FoldersAtBottom", false)
    /** Folders finish */

    /** Main tabs start */
    var showMainTabs by sharedPreferences.boolean("AP_ShowMainTabs", true)
    var openSettingsBySwipe by sharedPreferences.boolean("AP_OpenSettingsBySwipe", false)
    var mainTabsOrder by sharedPreferences.string("AP_MainTabsPosition_New", "SETTINGS,CHATS,!PROFILE,!CONTACTS,!CALLS,SEARCH")
    var showSearchInTabs by sharedPreferences.boolean("AP_ShowSearchInTabs_New", true)
    var showMainTabsTitle by sharedPreferences.boolean("AP_ShowMainTabsTitle", true)
    var mainTabsForceOpenChats by sharedPreferences.boolean("AP_MainTabsForceOpenChats", false)
    /** Main tabs finish */

    /** Messages and profiles start */
    var showSeconds by sharedPreferences.boolean("CP_ShowSeconds", false)
    var disablePremiumStatuses by sharedPreferences.boolean("CP_DisablePremiumStatuses", SharedConfig.getDevicePerformanceClass() == SharedConfig.PERFORMANCE_CLASS_LOW)
    var replyBackground by sharedPreferences.boolean("CP_ReplyBackground", SharedConfig.getDevicePerformanceClass() >= SharedConfig.PERFORMANCE_CLASS_AVERAGE)
    var replyCustomColors by sharedPreferences.boolean("CP_ReplyCustomColors", SharedConfig.getDevicePerformanceClass() >= SharedConfig.PERFORMANCE_CLASS_AVERAGE)
    var replyBackgroundEmoji by sharedPreferences.boolean("CP_ReplyBackgroundEmoji", SharedConfig.getDevicePerformanceClass() >= SharedConfig.PERFORMANCE_CLASS_AVERAGE)
    var profileHidePhoneNumber by sharedPreferences.boolean("CP_ProfileHidePhoneNumber", true)
    var profileChannelPreview by sharedPreferences.boolean("CP_ProfileChannelPreview", true)

    const val ID_DC_NONE = 0
    const val ID_DC = 1
    var showIDDC_old by sharedPreferences.int("AP_ShowID_DC", ID_DC_NONE) // Not used anymore, use only for migration
    var showIDDC by sharedPreferences.boolean("AP_ShowID_DC_new", false)

    var profileBirthDatePreview by sharedPreferences.boolean("CP_ProfileBirthDatePreview", true)
    var profileBusinessPreview by sharedPreferences.boolean("CP_ProfileBusinessPreview", true)
    var profileBackgroundColor by sharedPreferences.boolean("CP_ProfileBackgroundColor", SharedConfig.getDevicePerformanceClass() >= SharedConfig.PERFORMANCE_CLASS_AVERAGE)
    var profileBackgroundEmoji by sharedPreferences.boolean("CP_ProfileBackgroundEmoji", SharedConfig.getDevicePerformanceClass() >= SharedConfig.PERFORMANCE_CLASS_AVERAGE)
    /** Messages and profiles finish */

    /** Drawer items start */
    var showAccounts by sharedPreferences.boolean("AP_ShowAccounts", true)
    /** Drawer items finish */

}
