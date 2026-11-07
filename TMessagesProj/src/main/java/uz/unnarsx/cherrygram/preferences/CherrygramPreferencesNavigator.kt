/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 *
 * GhostGram C2 (PLAN.md 12.2): REDUCED port. The donor navigator routes
 * to every Cherrygram settings page, including the donations page and
 * the AdsGram ads screen (PLAN 8.1 C3/C9 - never ported). Only the three
 * pages this batch actually ports are routed here; later batches add
 * their pages as they land.
 */

package uz.unnarsx.cherrygram.preferences

import org.telegram.ui.ActionBar.BaseFragment
import uz.unnarsx.cherrygram.preferences.folders.FoldersPreferencesEntry
import uz.unnarsx.cherrygram.preferences.tabs.MainTabsPreferencesEntry

object CherrygramPreferencesNavigator {

    fun createGemini(fragment: BaseFragment): BaseFragment =
        GeminiPreferencesEntry().also(fragment::presentFragment)

    fun createMessageFilter(fragment: BaseFragment): BaseFragment =
        MessageFiltersPreferencesEntry().also(fragment::presentFragment)

    fun createMessageMenu(fragment: BaseFragment): BaseFragment =
        MessageMenuPreferencesEntry().also(fragment::presentFragment)

    // GhostGram C3 (PLAN.md 13.4): privacy / biometric-lock preferences
    fun createPrivacy(fragment: BaseFragment): BaseFragment =
        PrivacyPreferencesEntry().also(fragment::presentFragment)

    // GhostGram C3 (PLAN.md 13.7): chat-behavior preferences and its messages hub
    fun createChats(fragment: BaseFragment): BaseFragment =
        ChatsPreferencesEntry().also(fragment::presentFragment)

    fun createMessages(fragment: BaseFragment): BaseFragment =
        MessagesPreferencesEntry().also(fragment::presentFragment)

    // GhostGram C4 (PLAN.md 14.3): main-tabs preferences (order / enabled / titles)
    fun createMainTabs(fragment: BaseFragment): BaseFragment =
        MainTabsPreferencesEntry().also(fragment::presentFragment)

    // GhostGram C4 (PLAN.md 14.5): folder-tab preferences (mode / counters / stroke)
    fun createFolders(fragment: BaseFragment): BaseFragment =
        FoldersPreferencesEntry().also(fragment::presentFragment)

    // GhostGram C6 (PLAN.md 16.3): camera preferences (type / dual / enhancements / exposure)
    fun createCamera(fragment: BaseFragment): BaseFragment =
        CameraPreferencesEntry().also(fragment::presentFragment)

}
