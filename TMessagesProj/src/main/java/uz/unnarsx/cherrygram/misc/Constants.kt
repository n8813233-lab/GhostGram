/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 *
 * GhostGram C2 (PLAN.md 12.1): REDUCED port. The donor Constants file is
 * mostly author links (official/APKs/beta channel URLs, update URL,
 * donations-and-terms URL, privacy page) - all PLAN 8.1 C6/C10 territory
 * and NOT ported. Only the two constants that ported code actually reads
 * are kept:
 * - CG_AUTHOR: donor value is an "Updates: @CherrygramAPKs" promotion
 *   string used solely by CGResourcesHelper.getAboutString(); replaced
 *   with an empty string (GhostGram's about page is its own).
 * - Cherrygram_Support: a chat-id literal CGChatMenuInjector uses to
 *   recognise the donor's support chat; kept verbatim (pure data check,
 *   no network, no promotion surface in GhostGram).
 */

package uz.unnarsx.cherrygram.misc

object Constants {

    @JvmField
    var CG_AUTHOR = ""

    /** CG Chats IDs start */
    const val Cherrygram_Owner = 282287840L // Cherrygram Owner (Arslan)
    const val Cherrygram_Support = 1554776538L // Cherrygram Support Group

    const val Alina = 553511970L
    /** CG Chats IDs finish */
}
