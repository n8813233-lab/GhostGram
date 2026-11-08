package desu.inugram.ui.settings

import android.view.View
import desu.inugram.SearchRegistry
import desu.inugram.helpers.InuUtils
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.ui.Components.UItem
import org.telegram.ui.Components.UniversalAdapter
import uz.unnarsx.cherrygram.core.configs.backup.BackupHelper
import uz.unnarsx.cherrygram.helpers.CreateQRSheet
import uz.unnarsx.cherrygram.preferences.CherrygramPreferencesNavigator

class InuSettingsActivity : SettingsPageActivity() {
    override fun getTitle(): CharSequence = LocaleController.getString(R.string.InuSettings)

    override fun fillItems(items: ArrayList<UItem>, adapter: UniversalAdapter) {
        items.add(UItem.asHeader(LocaleController.getString(R.string.InuAppearance)))
        items.add(
            UItem.asButton(
                BUTTON_GENERAL,
                R.drawable.msg_palette,
                LocaleController.getString(R.string.InuLookAndFeel)
            )
        )
        items.add(
            UItem.asButton(
                BUTTON_CHATS,
                R.drawable.msg_discussion,
                LocaleController.getString(R.string.MainTabsChats)
            )
        )
        items.add(
            UItem.asButton(
                BUTTON_MESSAGES,
                R.drawable.msg_discuss,
                LocaleController.getString(R.string.InuMessages)
            )
        )
        items.add(
            UItem.asButton(
                BUTTON_DIALOGS,
                R.drawable.msg_viewchats,
                LocaleController.getString(R.string.InuMainPage)
            )
        )
        items.add(
            UItem.asButton(
                BUTTON_USER_PROFILE,
                R.drawable.msg_openprofile,
                LocaleController.getString(R.string.InuUserProfile)
            )
        )
        items.add(
            UItem.asButton(
                BUTTON_ANNOYANCES,
                R.drawable.menu_hide_gift,
                LocaleController.getString(R.string.InuAnnoyances)
            )
        )
        items.add(UItem.asShadow(null))

        items.add(UItem.asHeader(LocaleController.getString(R.string.InuOther)))
        items.add(
            UItem.asButton(
                BUTTON_BEHAVIOR,
                R.drawable.avd_speed,
                LocaleController.getString(R.string.InuBehavior)
            )
        )
        items.add(
            UItem.asButton(
                BUTTON_TRANSLATOR,
                R.drawable.msg_translate,
                LocaleController.getString(R.string.InuTranslator)
            )
        )
        // #if PLUGINS
        items.add(
            UItem.asButton(
                BUTTON_PLUGINS,
                R.drawable.inu_tabler_code,
                addExperimentalSpan(LocaleController.getString(R.string.InuPlugins))
            )
        )
        // #endif
        items.add(
            UItem.asButton(
                BUTTON_PRIVACY,
                R.drawable.msg_permissions,
                LocaleController.getString(R.string.InuPrivacySecurity)
            )
        )
        items.add(UItem.asShadow(null))

        // GhostGram 2.0 phase 2: re-stitched Cherrygram feature pages and tools.
        items.add(UItem.asHeader(LocaleController.getString(R.string.InuCGFeatures)))
        items.add(
            UItem.asButton(
                BUTTON_CG_APPEARANCE,
                R.drawable.msg_customize,
                LocaleController.getString(R.string.AP_Header_Appearance)
            )
        )
        items.add(
            UItem.asButton(
                BUTTON_CG_CHATS,
                R.drawable.msg_discussion,
                LocaleController.getString(R.string.Chats)
            )
        )
        items.add(
            UItem.asButton(
                BUTTON_CG_MESSAGE_MENU,
                R.drawable.msg_customize,
                LocaleController.getString(R.string.CP_MessageMenu)
            )
        )
        items.add(
            UItem.asButton(
                BUTTON_CG_MESSAGE_FILTERS,
                R.drawable.msg_search,
                LocaleController.getString(R.string.CP_Message_Filtering)
            )
        )
        items.add(
            UItem.asButton(
                BUTTON_CG_GEMINI,
                R.drawable.msg_recent,
                LocaleController.getString(R.string.CP_GeminiAI_Header)
            )
        )
        items.add(
            UItem.asButton(
                BUTTON_CG_PRIVACY,
                R.drawable.msg_pin_code,
                LocaleController.getString(R.string.SettingsPrivacySecurity)
            )
        )
        items.add(
            UItem.asButton(
                BUTTON_CG_CREATE_QR,
                R.drawable.msg_qrcode,
                LocaleController.getString(R.string.CG_CreateQR)
            )
        )
        items.add(
            UItem.asButton(
                BUTTON_CG_BACKUP_EXPORT,
                R.drawable.msg_shareout,
                LocaleController.getString(R.string.CG_ExportSettings)
            )
        )
        items.add(
            UItem.asButton(
                BUTTON_CG_BACKUP_IMPORT,
                R.drawable.msg_photo_settings,
                LocaleController.getString(R.string.CG_ImportSettings)
            )
        )
        items.add(
            UItem.asButton(
                BUTTON_CG_BACKUP_RESET,
                R.drawable.msg_delete,
                LocaleController.getString(R.string.CG_ResetSettings)
            )
        )
        items.add(UItem.asShadow(null))

        items.add(
            UItem.asButton(
                BUTTON_BACKUP,
                R.drawable.inu_tabler_cloud,
                LocaleController.getString(R.string.InuBackupSettings)
            )
        )
        items.add(
            UItem.asButton(
                BUTTON_ABOUT,
                R.drawable.msg_info,
                LocaleController.getString(R.string.InuAbout)
            )
        )
        items.add(UItem.asShadow(null))
    }

    override fun onClick(item: UItem, view: View, position: Int, x: Float, y: Float) {
        when (item.id) {
            BUTTON_GENERAL -> presentFragment(AppearanceSettingsActivity())
            BUTTON_CHATS -> presentFragment(ChatsSettingsActivity())
            BUTTON_MESSAGES -> presentFragment(MessagesSettingsActivity())
            BUTTON_DIALOGS -> presentFragment(DialogsSettingsActivity())
            BUTTON_USER_PROFILE -> presentFragment(UserProfileSettingsActivity())
            BUTTON_ANNOYANCES -> presentFragment(AnnoyancesSettingsActivity())
            BUTTON_BEHAVIOR -> presentFragment(BehaviorSettingsActivity())
            BUTTON_TRANSLATOR -> presentFragment(TranslatorSettingsActivity())
            // #if PLUGINS
            BUTTON_PLUGINS -> presentFragment(PluginsActivity())
            // #endif
            BUTTON_PRIVACY -> presentFragment(PrivacySecurityActivity())
            BUTTON_ABOUT -> presentFragment(AboutActivity())
            BUTTON_BACKUP -> presentFragment(BackupSettingsActivity())
            BUTTON_CG_APPEARANCE -> CherrygramPreferencesNavigator.createAppearance(this)
            BUTTON_CG_CHATS -> CherrygramPreferencesNavigator.createChats(this)
            BUTTON_CG_MESSAGE_MENU -> CherrygramPreferencesNavigator.createMessageMenu(this)
            BUTTON_CG_MESSAGE_FILTERS -> CherrygramPreferencesNavigator.createMessageFilter(this)
            BUTTON_CG_GEMINI -> CherrygramPreferencesNavigator.createGemini(this)
            BUTTON_CG_PRIVACY -> CherrygramPreferencesNavigator.createPrivacy(this)
            BUTTON_CG_CREATE_QR -> CreateQRSheet(context, this, resourceProvider).show()
            BUTTON_CG_BACKUP_EXPORT -> BackupHelper.backupSettings(this)
            BUTTON_CG_BACKUP_IMPORT -> BackupHelper.importSettings(this)
            BUTTON_CG_BACKUP_RESET -> BackupHelper.resetSettings(this)
        }
    }

    companion object {
        private val BUTTON_GENERAL = InuUtils.generateId()
        private val BUTTON_CHATS = InuUtils.generateId()
        private val BUTTON_MESSAGES = InuUtils.generateId()
        private val BUTTON_DIALOGS = InuUtils.generateId()
        private val BUTTON_USER_PROFILE = InuUtils.generateId()
        private val BUTTON_ANNOYANCES = InuUtils.generateId()
        private val BUTTON_BEHAVIOR = InuUtils.generateId()
        private val BUTTON_TRANSLATOR = InuUtils.generateId()
        private val BUTTON_PLUGINS = InuUtils.generateId()
        private val BUTTON_PRIVACY = InuUtils.generateId()
        private val BUTTON_ABOUT = InuUtils.generateId()
        private val BUTTON_BACKUP = InuUtils.generateId()
        private val BUTTON_CG_APPEARANCE = InuUtils.generateId()
        private val BUTTON_CG_CHATS = InuUtils.generateId()
        private val BUTTON_CG_MESSAGE_MENU = InuUtils.generateId()
        private val BUTTON_CG_MESSAGE_FILTERS = InuUtils.generateId()
        private val BUTTON_CG_GEMINI = InuUtils.generateId()
        private val BUTTON_CG_PRIVACY = InuUtils.generateId()
        private val BUTTON_CG_CREATE_QR = InuUtils.generateId()
        private val BUTTON_CG_BACKUP_EXPORT = InuUtils.generateId()
        private val BUTTON_CG_BACKUP_IMPORT = InuUtils.generateId()
        private val BUTTON_CG_BACKUP_RESET = InuUtils.generateId()

        @JvmField
        val PAGE = SearchRegistry.Page(
            slug = "root",
            titleRes = R.string.InuSettings,
            iconRes = R.drawable.msg_settings,
            factory = ::InuSettingsActivity,
            entries = listOf(
                SearchRegistry.Entry("about", R.string.InuAbout, BUTTON_ABOUT),
            ),
        )
    }
}
