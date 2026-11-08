/**
 * GhostGram appearance batch (PLAN.md 22.1): LOCAL implementation.
 *
 * The donor's TelegramSettingsHelper.kt (517 lines) was not ported whole;
 * the appearance page only needs Helper.getProfileButtonColor(). This is a
 * faithful re-implementation of the donor logic restricted to the APIs this
 * Mango base has: the donor's TL_peerColorCollectible / peerColors branches
 * are TG13-generation and are skipped (they only refine the gradient of one
 * settings-row icon); the common path - the user's avatar name color, with
 * the donor's white/near-white fallback to the default blue - is preserved.
 */

package uz.unnarsx.cherrygram.preferences.helpers;

import android.graphics.Color;

import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.IconBackgroundColors;

import kotlin.Pair;

public class TelegramSettingsHelper {

    public static class Helper {
        public static final Helper INSTANCE = new Helper();

        private Helper() {}

        public Pair<Integer, Integer> getProfileButtonColor(TLRPC.User user, boolean force) {
            int colorTop = IconBackgroundColors.BLUE.top;
            int colorBottom = IconBackgroundColors.BLUE_LIGHT.bottom;

            if (user != null) {
                int colorId = UserObject.getColorId(user);
                if (colorId >= 0 && colorId < 7) {
                    int color = Theme.getColor(Theme.keys_avatar_nameInMessage[colorId]);
                    if (!isWhiteOrNearWhite(color)) {
                        colorTop = color;
                        colorBottom = color;
                    }
                }
            }
            return new Pair<>(colorTop, colorBottom);
        }

        // Local equivalent of the donor's CherrygramExtras.isWhiteOrNearWhite
        // (that helper class is not part of the ported tree).
        private static boolean isWhiteOrNearWhite(int color) {
            return Color.red(color) >= 0xE8 && Color.green(color) >= 0xE8 && Color.blue(color) >= 0xE8;
        }
    }
}
