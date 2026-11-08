/**
 * GhostGram C3 (PLAN.md 13.6): LOCAL implementation.
 *
 * The donor's ProfileActivity references uz.unnarsx.cherrygram.Extra
 * (getProfileDC / getRegistrationDate / addBirthdayToCalendar), but that
 * class is absent from the donor's published source tree - the same
 * situation as the classes locally implemented in C2 (PLAN.md 12.3).
 * Only the surface the ported code actually calls is implemented here,
 * from local data: the DC id comes from the peer's photo object, the
 * registration-date line from the ported UserHelper (bundled id_date.json
 * estimate), and the calendar action delegates to UserHelper as well.
 * No network, no remote data.
 */

package uz.unnarsx.cherrygram;

import android.app.Activity;

import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.tgnet.TLRPC;

import uz.unnarsx.cherrygram.helpers.UserHelper;

public final class Extra {

    public static final Extra INSTANCE = new Extra();

    private Extra() {
    }

    public StringBuilder getProfileDC(BaseFragment fragment, TLRPC.User user, TLRPC.Chat chat) {
        StringBuilder stringBuilder = new StringBuilder();

        int dcId = 0;
        if (user != null && user.photo != null) {
            dcId = user.photo.dc_id;
        } else if (chat != null && chat.photo != null) {
            dcId = chat.photo.dc_id;
        }
        if (dcId > 0) {
            stringBuilder.append("DC: ").append(dcId);
        }

        if (user != null) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(" • ");
            }
            stringBuilder.append(UserHelper.getUserTime(user.id, null));
        }

        return stringBuilder;
    }

    public CharSequence getRegistrationDate(BaseFragment fragment, long userId, long chatId) {
        if (userId != 0) {
            return UserHelper.getUserTime(userId, null);
        }
        return null;
    }

    public void addBirthdayToCalendar(Activity parentActivity, long userId) {
        UserHelper.addBirthdayEvent(parentActivity, userId);
    }

}
