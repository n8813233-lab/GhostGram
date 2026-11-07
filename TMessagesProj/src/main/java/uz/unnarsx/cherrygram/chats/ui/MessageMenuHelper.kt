/**
 * GhostGram C2 (PLAN.md 12.0/12.2): LOCAL implementation.
 *
 * The donor's MessageMenuHelper class is referenced throughout the donor
 * sources but is NOT present in the public Cherrygram tree (same class of
 * incompleteness as the unpublished Extra.kt, PLAN 8.1 C8). This local
 * version covers exactly the surface the ported code calls, with
 * semantics matched to the base's classic popup message menu:
 * - allowNewMessageMenu() = false: the TG13 "new message menu" framework
 *   the donor builds on does not exist in the base; the classic
 *   ActionBarPopupWindow menu in ChatActivity.createMenu is the only
 *   menu, and the ported injector's classic branches are the live ones.
 * - showDivider() = true: the Gemini swipe-back section is separated
 *   from the menu items by a gap, as in the donor's classic layout.
 * - showCustomDivider() = false: the custom native-blur divider belongs
 *   to the new-menu framework; the classic GapView is used instead.
 * - getMessageMenuGapColor(): the gap is drawn over the submenu
 *   background, so the themed submenu background color is returned
 *   (approximation of the donor's dedicated gap color, registered in
 *   PLAN.md 12.0).
 */

package uz.unnarsx.cherrygram.chats.ui

import org.telegram.ui.ActionBar.Theme

class MessageMenuHelper {

    fun allowNewMessageMenu(): Boolean = false

    fun showDivider(): Boolean = true

    fun showCustomDivider(isMenu: Boolean): Boolean = false

    companion object {
        @JvmStatic
        fun getMessageMenuGapColor(resourcesProvider: Theme.ResourcesProvider?): Int =
            Theme.getColor(Theme.key_actionBarDefaultSubmenuBackground, resourcesProvider)
    }
}
