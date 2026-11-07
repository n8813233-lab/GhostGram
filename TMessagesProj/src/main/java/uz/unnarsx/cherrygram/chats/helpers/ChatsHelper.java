/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 *
 * GhostGram C1 (PLAN.md 11.3): REDUCED port - originally only the static
 * addFileToClipboard the QR sheets need.
 * GhostGram C2 (PLAN.md 12.2): EXPANDED to the donor's BaseController
 * singleton shape, because CGMessageMenuInjector calls
 * chatActivity.getChatsHelper().getMessageCaption(...) and donor code
 * uses ChatsHelper.getInstance(account). The three instance methods at
 * the bottom are donor-verbatim. The donor's remaining ~800 lines stay
 * unported until their feature batches (C3+) expand this file further;
 * several of them belong to payment deeplink features that are not
 * ported at all.
 */

package uz.unnarsx.cherrygram.chats.helpers;

import static org.telegram.messenger.LocaleController.getString;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import androidx.core.content.FileProvider;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.BaseController;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.TranscribeButton;

import java.io.File;
import java.io.FileOutputStream;

import uz.unnarsx.cherrygram.core.CherrygramLogger;
import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig;
import uz.unnarsx.cherrygram.helpers.ui.PopupHelper;

public class ChatsHelper extends BaseController {

    private static final ChatsHelper[] Instance = new ChatsHelper[UserConfig.MAX_ACCOUNT_COUNT];

    public ChatsHelper(int num) {
        super(num);
    }

    public static ChatsHelper getInstance(int num) {
        ChatsHelper localInstance = Instance[num];
        if (localInstance == null) {
            synchronized (ChatsHelper.class) {
                localInstance = Instance[num];
                if (localInstance == null) {
                    Instance[num] = localInstance = new ChatsHelper(num);
                }
            }
        }
        return localInstance;
    }

    public static void addFileToClipboard(File file, Runnable callback) {
        try {
            Context context = ApplicationLoader.applicationContext;
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            Uri uri = FileProvider.getUriForFile(context, ApplicationLoader.getApplicationId() + ".provider", file);
            ClipData clip = ClipData.newUri(context.getContentResolver(), "label", uri);
            clipboard.setPrimaryClip(clip);
            callback.run();
        } catch (Exception e) {
            CherrygramLogger.e(e);
        }
    }

    public String getPathToMessage(MessageObject messageObject) {
        String path = messageObject.messageOwner.attachPath;
        if (!TextUtils.isEmpty(path)) {
            File temp = new File(path);
            if (!temp.exists()) {
                path = null;
            }
        }
        if (TextUtils.isEmpty(path)) {
            path = FileLoader.getInstance(UserConfig.selectedAccount).getPathToMessage(messageObject.messageOwner).toString();
            File temp = new File(path);
            if (!temp.exists()) {
                path = null;
            }
        }
        if (TextUtils.isEmpty(path)) {
            path = FileLoader.getInstance(UserConfig.selectedAccount).getPathToAttach(messageObject.getDocument(), true).toString();
            File temp = new File(path);
            if (!temp.exists()) {
                return null;
            }
        }
        return path;
    }

    public CharSequence getMessageCaption(MessageObject messageObject, MessageObject.GroupedMessages group) {
        String restrictionReason = getMessagesController().getRestrictionReason(messageObject.messageOwner.restriction_reason);
        if (!TextUtils.isEmpty(restrictionReason)) {
            return restrictionReason;
        }
        if (messageObject.isVoiceTranscriptionOpen() && !TranscribeButton.isTranscribing(messageObject)) {
            return messageObject.getVoiceTranscription();
        }
        if (messageObject.caption != null) {
            return messageObject.caption;
        }
        if (group == null) {
            return null;
        }
        CharSequence caption = null;
        for (int a = 0, N = group.messages.size(); a < N; a++) {
            MessageObject message = group.messages.get(a);
            if (message.caption != null) {
                if (caption != null) {
                    return null;
                }
                caption = message.caption;
            }
        }
        return caption;
    }

    public CharSequence getMessageContent(MessageObject messageObject) {
        android.text.SpannableStringBuilder str = new android.text.SpannableStringBuilder();
        String restrictionReason = getMessagesController().getRestrictionReason(messageObject.messageOwner.restriction_reason);
        if (!TextUtils.isEmpty(restrictionReason)) {
            str.append(restrictionReason);
        } else if (messageObject.caption != null) {
            str.append(messageObject.caption);
        } else {
            str.append(messageObject.messageText);
        }
        return str;
    }

    public boolean isTopic(MessageObject messageObject) {
        TLRPC.TL_forumTopic topic = MessagesController.getInstance(currentAccount).getTopicsController().findTopic(
                -messageObject.getDialogId(), MessageObject.getTopicId(currentAccount, messageObject.messageOwner, true)
        );
        return topic != null;
    }


    public void addMessageToClipboard(MessageObject selectedObject, Runnable callback) {
        String path = getPathToMessage(selectedObject);
        if (!TextUtils.isEmpty(path)) {
            addFileToClipboard(new File(path), callback);
        }
    }

    public void saveStickerToGallery(Activity activity, TLRPC.Document document, Utilities.Callback<Uri> callback) {
        String path = FileLoader.getInstance(UserConfig.selectedAccount).getPathToAttach(document, true).toString();
        File temp = new File(path);
        if (!temp.exists()) {
            return;
        }
        saveStickerToGallery(activity, path, MessageObject.isVideoSticker(document), callback);
    }

    private void saveStickerToGallery(Activity activity, String path, boolean video, Utilities.Callback<Uri> callback) {
        Utilities.globalQueue.postRunnable(() -> {
            try {
                if (video) {
                    MediaController.saveFile(path, activity, 1, null, null, callback);
                } else {
                    Bitmap image = BitmapFactory.decodeFile(path);
                    if (image != null) {
                        File file = new File(path.endsWith(".webp") ? path.replace(".webp", ".png") : path + ".png");
                        FileOutputStream stream = new FileOutputStream(file);
                        image.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        stream.close();
                        MediaController.saveFile(file.toString(), activity, 0, null, null, callback);
                    }
                }
            } catch (Exception e) {
                CherrygramLogger.e(e);
            }
        });
    }

    public void forwardMessages(ChatActivity chatActivity, ArrayList<MessageObject> arrayList, boolean fromMyName, boolean notify, int scheduleDate, long did) {
        if (arrayList == null || arrayList.isEmpty()) {
            return;
        }
        if ((scheduleDate != 0) == (chatActivity.getChatMode() == ChatActivity.MODE_SCHEDULED)) {
            chatActivity.waitingForSendingMessageLoad = true;
        }
        AlertsCreator.showSendMediaAlert(getSendMessagesHelper().sendMessage(arrayList, did == 0 ? chatActivity.getDialogId(): did, fromMyName, false, notify, scheduleDate, 0), chatActivity, chatActivity.getResourceProvider());
    }

    public void showSearchMessageFilterSelector(ChatActivity chatActivity) {
        ArrayList<String> configStringKeys = new ArrayList<>();
        ArrayList<Integer> configValues = new ArrayList<>();

        configStringKeys.add(getString(R.string.CG_SearchFilter_None));
        configValues.add(CherrygramChatsConfig.FILTER_NONE);

        configStringKeys.add(getString(R.string.CG_SearchFilter_Photos));
        configValues.add(CherrygramChatsConfig.FILTER_PHOTOS);

        configStringKeys.add(getString(R.string.CG_SearchFilter_Videos));
        configValues.add(CherrygramChatsConfig.FILTER_VIDEOS);

        configStringKeys.add(getString(R.string.CG_SearchFilter_VoiceMessages));
        configValues.add(CherrygramChatsConfig.FILTER_VOICE_MESSAGES);

        configStringKeys.add(getString(R.string.CG_SearchFilter_VideoMessages));
        configValues.add(CherrygramChatsConfig.FILTER_VIDEO_MESSAGES);

        configStringKeys.add(getString(R.string.CG_SearchFilter_Files));
        configValues.add(CherrygramChatsConfig.FILTER_FILES);

        configStringKeys.add(getString(R.string.CG_SearchFilter_Music));
        configValues.add(CherrygramChatsConfig.FILTER_MUSIC);

        configStringKeys.add(getString(R.string.CG_SearchFilter_GIFs));
        configValues.add(CherrygramChatsConfig.FILTER_GIFS);

        configStringKeys.add(getString(R.string.CG_SearchFilter_Geolocation));
        configValues.add(CherrygramChatsConfig.FILTER_GEO);

        configStringKeys.add(getString(R.string.CG_SearchFilter_Contacts));
        configValues.add(CherrygramChatsConfig.FILTER_CONTACTS);

        configStringKeys.add(getString(R.string.CG_SearchFilter_MyMentions));
        configValues.add(CherrygramChatsConfig.FILTER_MENTIONS);

        PopupHelper.show(configStringKeys, getString(R.string.CG_SearchFilter), configValues.indexOf(CherrygramChatsConfig.INSTANCE.getMessagesSearchFilter()), chatActivity.getContext(), i -> {
            CherrygramChatsConfig.INSTANCE.setMessagesSearchFilter(configValues.get(i));

            chatActivity.openSearchWithText(null);
            chatActivity.showMessagesSearchListView(true);
        }, chatActivity.getResourceProvider());
    }

    public CharSequence getMessageText(MessageObject selectedObject, MessageObject.GroupedMessages selectedObjectGroup) {
        CharSequence messageTextToTranslate = null;
        if (selectedObject != null && selectedObject.type != MessageObject.TYPE_EMOJIS && selectedObject.type != MessageObject.TYPE_ANIMATED_STICKER && selectedObject.type != MessageObject.TYPE_STICKER) {
            messageTextToTranslate = getMessageCaption(selectedObject, selectedObjectGroup);
            if (messageTextToTranslate == null && selectedObject.isPoll()) {
                try {
                    TLRPC.Poll poll = ((TLRPC.TL_messageMediaPoll) selectedObject.messageOwner.media).poll;
                    StringBuilder pollText = new StringBuilder(poll.question.text).append("\n");
                    for (TLRPC.PollAnswer answer : poll.answers)
                        pollText.append("\n\uD83D\uDD18 ").append(answer.text == null ? "" : answer.text.text);
                    messageTextToTranslate = pollText.toString();
                } catch (Exception e) {
                    CherrygramLogger.e(e);
                }
            }
            if (messageTextToTranslate == null && MessageObject.isMediaEmpty(selectedObject.messageOwner)) {
                messageTextToTranslate = getMessageContent(selectedObject);
            }
            if (messageTextToTranslate != null && Emoji.fullyConsistsOfEmojis(messageTextToTranslate)) {
                messageTextToTranslate = null;
            }
        }
        return messageTextToTranslate == null ? " " : messageTextToTranslate;
    }

}
