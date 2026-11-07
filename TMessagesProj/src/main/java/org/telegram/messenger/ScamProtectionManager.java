package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * Local keyword hints only; it never sends message contents to another service.
 * GhostGram: the upstream remote keyword feed (developer-hosted list) has been
 * removed. Only the bundled defaults and the user's own custom keywords apply.
 */
public final class ScamProtectionManager {
    public static final String DEFAULT_KEYWORDS = "刷单返利\n先交保证金\n解冻费\n安全账户\n助记词\n私钥\n保证收益\n稳赚不赔\n充值解锁提现\nseed phrase\nverify your wallet\nguaranteed profit";
    private static final LinkedHashSet<String> shown = new LinkedHashSet<>();
    private static final ArrayList<Runnable> listeners = new ArrayList<>();
    public static final long REFRESH_INTERVAL_MS = 2 * 60 * 60 * 1000L;

    public static void addListener(Runnable listener) { synchronized (listeners) { if (!listeners.contains(listener)) listeners.add(listener); } }
    public static void removeListener(Runnable listener) { synchronized (listeners) { listeners.remove(listener); } }
    private static void notifyChanged() {
        AndroidUtilities.runOnUIThread(() -> {
            ArrayList<Runnable> snapshot;
            synchronized (listeners) { snapshot = new ArrayList<>(listeners); }
            for (Runnable listener : snapshot) listener.run();
        });
    }

    private static SharedPreferences preferences(int account) {
        long owner = UserConfig.getInstance(account).getClientUserId();
        return ApplicationLoader.applicationContext.getSharedPreferences("scam_protection_" + account + "_" + owner, Context.MODE_PRIVATE);
    }

    public static boolean isEnabled(int account) {
        return preferences(account).getBoolean("enabled", true);
    }

    public static void setEnabled(int account, boolean value) {
        preferences(account).edit().putBoolean("enabled", value).apply();
    }

    public static ArrayList<String> getKeywords(int account) {
        LinkedHashSet<String> combined = new LinkedHashSet<>(MessageInsightsRules.parseKeywords(DEFAULT_KEYWORDS));
        combined.addAll(getCustomKeywords(account));
        return new ArrayList<>(combined);
    }

    public static ArrayList<String> getCustomKeywords(int account) {
        String saved = preferences(account).getString("keywords", "");
        // Earlier releases persisted the bundled list when restoring defaults.
        ArrayList<String> words = MessageInsightsRules.parseKeywords(saved);
        return words.equals(MessageInsightsRules.parseKeywords(DEFAULT_KEYWORDS)) ? new ArrayList<>() : words;
    }

    public static void setKeywords(int account, String value) {
        preferences(account).edit().putString("keywords", android.text.TextUtils.join("\n", MessageInsightsRules.parseKeywords(value))).apply();
        synchronized (shown) { shown.clear(); }
    }

    public static String match(int account, MessageObject message) {
        if (!isEnabled(account) || message == null || message.messageOwner == null || message.isOut()
                || message.messageOwner.action != null && !(message.messageOwner.action instanceof org.telegram.tgnet.TLRPC.TL_messageActionEmpty)) return null;
        return MessageInsightsRules.matchingKeyword(message.messageOwner.message, getKeywords(account));
    }

    /** Once per content revision per account, bounded to avoid keeping old chat contents. */
    public static boolean markShown(int account, MessageObject message, String keyword) {
        String key = account + ":" + UserConfig.getInstance(account).getClientUserId() + ":" + message.getDialogId() + ":" + message.getId()
                + ":" + message.messageOwner.edit_date + ":" + keyword;
        synchronized (shown) {
            if (!shown.add(key)) return false;
            while (shown.size() > 2000) shown.remove(shown.iterator().next());
            return true;
        }
    }
}
