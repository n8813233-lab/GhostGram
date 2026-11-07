/**
 * GhostGram C2 (PLAN.md 12.0/12.2): LOCAL implementation.
 *
 * The donor's MessageMenuCompactView class is referenced by the ported
 * CGMessageMenuInjector but is NOT present in the public Cherrygram tree
 * (PLAN 8.1 C8 class of incompleteness). The only surface used is
 * allowCompactStyle(), whose donor semantics are driven by the
 * msgMenuItemsCompactView config key (CP_MsgMenuItemsCompactView) that
 * the foundation batch already ported into CherrygramMessagesConfig;
 * this implementation reads that key directly.
 */

package uz.unnarsx.cherrygram.chats.ui

import uz.unnarsx.cherrygram.core.configs.CherrygramMessagesConfig

object MessageMenuCompactView {

    @JvmStatic
    fun allowCompactStyle(): Boolean = CherrygramMessagesConfig.msgMenuItemsCompactView
}
