package com.kaarss.fatalk;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import static android.view.View.GONE;

public class ChatAdapter extends RecyclerView.Adapter {

    private static String TAG = ChatAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_DATE = 100;

    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 1;
    private static final int VIEW_TYPE_IMAGE_RECEIVED = 2;
    private static final int VIEW_TYPE_GIF_RECEIVED = 3;
    private static final int VIEW_TYPE_AUDIO_RECEIVED = 4;
    private static final int VIEW_TYPE_VIDEO_RECEIVED = 5;
    private static final int VIEW_TYPE_DOCUMENT_RECEIVED = 6;
    private static final int VIEW_TYPE_CONTACT_RECEIVED = 7;

    private static final int VIEW_TYPE_MESSAGE_SENT = 51;
    private static final int VIEW_TYPE_IMAGE_SENT = 52;
    private static final int VIEW_TYPE_GIF_SENT = 53;
    private static final int VIEW_TYPE_AUDIO_SENT = 54;
    private static final int VIEW_TYPE_VIDEO_SENT = 55;
    private static final int VIEW_TYPE_DOCUMENT_SENT = 56;
    private static final int VIEW_TYPE_CONTACT_SENT = 57;




    private Context mContext;
    private List<ChatMessage> mMessageList;

    public ChatAdapter(Context context) {
        mContext = context;
    }

    void setMessages(List<ChatMessage> messages){
        mMessageList = messages;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if(mMessageList != null) {
            return mMessageList.size();
        } else {
            return 0;
        }
    }

    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        ChatMessage message = mMessageList.get(position);
        int type = VIEW_TYPE_DATE;
        int messageType = message.getType();
        if(message.isMine()){
            if(messageType == Keys.chatMessageTypeText){
                type = VIEW_TYPE_MESSAGE_SENT;
            } else if (messageType == Keys.chatMessageTypePicture){
                type = VIEW_TYPE_IMAGE_SENT;
            } else if (messageType == Keys.chatMessageTypeGif){
                type = VIEW_TYPE_GIF_SENT;
            } else if (messageType == Keys.chatMessageTypeAudio){
                type = VIEW_TYPE_AUDIO_SENT;
            } else if (messageType == Keys.chatMessageTypeVideo){
                type = VIEW_TYPE_VIDEO_SENT;
            } else if (messageType == Keys.chatMessageTypeDocument){
                type = VIEW_TYPE_DOCUMENT_SENT;
            }
        } else {
            if(messageType == Keys.chatMessageTypeText){
                type = VIEW_TYPE_MESSAGE_RECEIVED;
            } else if (messageType == Keys.chatMessageTypePicture){
                type = VIEW_TYPE_IMAGE_RECEIVED;
            } else if (messageType == Keys.chatMessageTypeGif){
                type = VIEW_TYPE_GIF_RECEIVED;
            } else if (messageType == Keys.chatMessageTypeAudio){
                type = VIEW_TYPE_AUDIO_RECEIVED;
            } else if (messageType == Keys.chatMessageTypeVideo){
                type = VIEW_TYPE_VIDEO_RECEIVED;
            } else if (messageType == Keys.chatMessageTypeDocument){
                type = VIEW_TYPE_DOCUMENT_RECEIVED;
            }
        }
        return type;
    }

    // Inflates the appropriate layout according to the ViewType.
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = getLayoutType(viewType);
        View view = LayoutInflater.from(parent.getContext()).inflate(layout,parent,false);
        // ---- Attach To Listener -------
        RecyclerView.ViewHolder viewHolder = null;
        switch (viewType){
            case VIEW_TYPE_MESSAGE_RECEIVED:
                viewHolder = new ReceivingViewHolderForMessages(view);
                break;
            case VIEW_TYPE_IMAGE_RECEIVED:
            case VIEW_TYPE_GIF_RECEIVED:
            case VIEW_TYPE_AUDIO_RECEIVED:
            case VIEW_TYPE_VIDEO_RECEIVED:
            case VIEW_TYPE_DOCUMENT_RECEIVED:
                ImageView receivingPreview = view.findViewById(R.id.preview);
                receivingPreview.setOnClickListener(ChatActivity.chatItemClickListener);
                receivingPreview.setOnLongClickListener(ChatActivity.chatItemLongClickListener);
                FrameLayout receivingTransferProgress = view.findViewById(R.id.transfer_progress);
                receivingTransferProgress.setOnClickListener(ChatActivity.chatItemClickListener);
                viewHolder = new ReceivingViewHolderForDownloads(view);
                break;
            case VIEW_TYPE_MESSAGE_SENT:
                viewHolder = new SendingViewHolderForMessages(view);
                break;
            case VIEW_TYPE_IMAGE_SENT:
            case VIEW_TYPE_GIF_SENT:
            case VIEW_TYPE_AUDIO_SENT:
            case VIEW_TYPE_VIDEO_SENT:
            case VIEW_TYPE_DOCUMENT_SENT:
                ImageView sendingPreview = view.findViewById(R.id.preview);
                sendingPreview.setOnClickListener(ChatActivity.chatItemClickListener);
                sendingPreview.setOnLongClickListener(ChatActivity.chatItemLongClickListener);
                FrameLayout sendingTransferProgress = view.findViewById(R.id.transfer_progress);
                sendingTransferProgress.setOnClickListener(ChatActivity.chatItemClickListener);
                viewHolder = new SendingViewHolderForUploads(view);
                break;
        }
        if(viewHolder == null){
            viewHolder = new ViewHolderForDates(view);
        }
        return viewHolder;
    }

    private int getLayoutType(int viewType){
        int layout = R.layout.date_message;
        switch (viewType){
            case VIEW_TYPE_MESSAGE_RECEIVED:
                layout = R.layout.received_message;
                break;
            case VIEW_TYPE_IMAGE_RECEIVED:
                layout = R.layout.received_image_message;
                break;
            case VIEW_TYPE_GIF_RECEIVED:
                layout = R.layout.received_gif_message;
                break;
            case VIEW_TYPE_AUDIO_RECEIVED:
                layout = R.layout.received_audio_message;
                break;
            case VIEW_TYPE_VIDEO_RECEIVED:
                layout = R.layout.received_video_message;
                break;
            case VIEW_TYPE_DOCUMENT_RECEIVED:
                layout = R.layout.received_document_message;
                break;
            case VIEW_TYPE_MESSAGE_SENT:
                layout = R.layout.sent_message;
                break;
            case VIEW_TYPE_IMAGE_SENT:
                layout = R.layout.sent_image_message;
                break;
            case VIEW_TYPE_GIF_SENT:
                layout = R.layout.sent_gif_message;
                break;
            case VIEW_TYPE_AUDIO_SENT:
                layout = R.layout.sent_audio_message;
                break;
            case VIEW_TYPE_VIDEO_SENT:
                layout = R.layout.sent_video_message;
                break;
            case VIEW_TYPE_DOCUMENT_SENT:
                layout = R.layout.sent_document_message;
                break;
        }
        return layout;
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = mMessageList.get(position);
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_DATE:
                ((ViewHolderForDates)holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivingViewHolderForMessages)holder).bind(message);
                break;
            case VIEW_TYPE_IMAGE_RECEIVED:
            case VIEW_TYPE_GIF_RECEIVED:
            case VIEW_TYPE_AUDIO_RECEIVED:
            case VIEW_TYPE_VIDEO_RECEIVED:
            case VIEW_TYPE_DOCUMENT_RECEIVED:
                ((ReceivingViewHolderForDownloads)holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_SENT:
                ((SendingViewHolderForMessages)holder).bind(message);
                break;
            case VIEW_TYPE_IMAGE_SENT:
            case VIEW_TYPE_GIF_SENT:
            case VIEW_TYPE_AUDIO_SENT:
            case VIEW_TYPE_VIDEO_SENT:
            case VIEW_TYPE_DOCUMENT_SENT:
                ((SendingViewHolderForUploads)holder).bind(message);
                break;
        }
    }

    private void setSendingAttributes(ChatMessage message, TextView sentAt, ImageView _sending, ImageView _sent, ImageView _delivered){
        String timeStamp = TimeUtils.toAMPM((message.getSentAt()*1000));
        if(message.isDelivered()){
            _delivered.setVisibility(View.VISIBLE);
            _sent.setVisibility(GONE);
            _sending.setVisibility(GONE);
            sentAt.setVisibility(View.VISIBLE);
            sentAt.setText(timeStamp);
        } else if(message.isSent()){
            _delivered.setVisibility(GONE);
            _sent.setVisibility(View.VISIBLE);
            _sending.setVisibility(GONE);
            sentAt.setVisibility(View.VISIBLE);
            sentAt.setText(timeStamp);
        } else {
            _delivered.setVisibility(GONE);
            _sent.setVisibility(GONE);
            _sending.setVisibility(View.VISIBLE);
            sentAt.setVisibility(GONE);
        }
    }
    private class SendingViewHolderForMessages extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView sentAt;
        ImageView _sending;
        ImageView _sent;
        ImageView _delivered;
        SendingViewHolderForMessages(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message_body);
            sentAt = itemView.findViewById(R.id.sent_at);
            _sending = itemView.findViewById(R.id.sending);
            _sent = itemView.findViewById(R.id.sent);
            _delivered = itemView.findViewById(R.id.delivered);
        }
        void bind(ChatMessage message) {
            messageText.setText(message.getMessage().trim().replaceAll(" +"," "));
            setSendingAttributes(message,sentAt,_sending,_sent,_delivered);
        }
    }
    // ---- For Image/Gif/Audio/Video/Documents ------
    private class SendingViewHolderForUploads extends RecyclerView.ViewHolder {
        ImageView preview;
        FrameLayout transferProgress;
        FloatingActionButton transferIcon;
        ProgressBar progressBar;
        TextView fileSize;
        TextView fileName;
        TextView sentAt;
        ImageView _sending;
        ImageView _sent;
        ImageView _delivered;
        SendingViewHolderForUploads(View view) {
            super(view);
            preview = view.findViewById(R.id.preview);
            transferProgress = view.findViewById(R.id.transfer_progress);
            transferIcon = view.findViewById(R.id.transfer_icon);
            progressBar = view.findViewById(R.id.progress_circle);
            fileSize = view.findViewById(R.id.file_size);
            fileName = view.findViewById(R.id.name);
            sentAt = view.findViewById(R.id.sent_at);
            _sending = view.findViewById(R.id.sending);
            _sent = view.findViewById(R.id.sent);
            _delivered = view.findViewById(R.id.delivered);
            transferIcon.setImageResource(R.drawable.ic_arrow_upward);
        }
        void bind(ChatMessage message) {
            String messageId = message.getMessageId();
            if(App.chatMediaWithPreview.contains(message.getType())) {
                Bitmap previewImage = App.getMediaPreview(messageId);
                if (previewImage != null) {
                    preview.setImageBitmap(previewImage);
                } else {
                    setStringBitmap(preview, message.getMediaData());
                }
                fileName.setVisibility(GONE);
            } else {
                fileName.setVisibility(View.VISIBLE);
                fileName.setText(message.getMediaName());
            }
            int uploadStatus = message.getMediaUploadStatus();
            if(uploadStatus == Keys.mediaUploaded){
                transferProgress.setVisibility(GONE);
            } else if (uploadStatus == Keys.mediaUploading){
                transferProgress.setVisibility(View.VISIBLE);
                transferIcon.hide();
                progressBar.setVisibility(View.VISIBLE);
            } else if (uploadStatus < Keys.mediaUploaded){
                transferProgress.setVisibility(View.VISIBLE);
                transferIcon.show();
                progressBar.setVisibility(GONE);
            }
            fileSize.setText(FileUtils.getFileSize(message.getMediaSize()));
            setSendingAttributes(message,sentAt,_sending,_sent,_delivered);
            transferProgress.setContentDescription(messageId);
            preview.setContentDescription(messageId);
        }
    }

    // --- View For Image/Gif/Audio/Video/Documents ---
    private class ReceivingViewHolderForDownloads extends RecyclerView.ViewHolder{
        ImageView preview;
        FrameLayout transferProgress;
        FloatingActionButton transferIcon;
        ProgressBar progressBar;
        TextView fileSize;
        TextView fileName;
        TextView receivedAt;
        ReceivingViewHolderForDownloads(View view){
            super(view);
            preview = view.findViewById(R.id.preview);
            transferProgress = view.findViewById(R.id.transfer_progress);
            transferIcon = view.findViewById(R.id.transfer_icon);
            progressBar = view.findViewById(R.id.progress_circle);
            fileSize = view.findViewById(R.id.file_size);
            fileName = view.findViewById(R.id.name);
            receivedAt = view.findViewById(R.id.received_at);

            transferIcon.setImageResource(R.drawable.ic_arrow_downward);
        }
        void bind(ChatMessage message){
            String messageId = message.getMessageId();
            if(App.chatMediaWithPreview.contains(message.getType())) {
                Bitmap previewImage = App.getMediaPreview(messageId);
                if (previewImage != null) {
                    preview.setImageBitmap(previewImage);
                } else {
                    setStringBitmap(preview, message.getMediaData());
                }
                fileName.setVisibility(GONE);
            } else {
                fileName.setVisibility(View.VISIBLE);
                fileName.setText(message.getMediaName());
            }
            int downloadStatus = message.getMediaDownloadStatus();
            if(downloadStatus == Keys.mediaDownloaded){
                transferProgress.setVisibility(GONE);
            } else if (downloadStatus == Keys.mediaDownloading){
                transferProgress.setVisibility(View.VISIBLE);
                transferIcon.hide();
                progressBar.setVisibility(View.VISIBLE);
            } else if (downloadStatus < Keys.mediaDownloaded){
                transferProgress.setVisibility(View.VISIBLE);
                transferIcon.show();
                progressBar.setVisibility(GONE);
            }
            fileSize.setText(FileUtils.getFileSize(message.getMediaSize()));
            receivedAt.setText(TimeUtils.toAMPM(message.getAddedAt()*1000));
            transferProgress.setContentDescription(messageId);
            preview.setContentDescription(messageId);
        }
    }
    private class ReceivingViewHolderForMessages extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView receivedAt;
        ReceivingViewHolderForMessages(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message_body);
            receivedAt = itemView.findViewById(R.id.received_at);
        }
        void bind(ChatMessage message) {
            messageText.setText(message.getMessage().trim().replaceAll(" +"," "));
            receivedAt.setText(TimeUtils.toAMPM(message.getAddedAt()*1000));
        }
    }
    private class ViewHolderForDates extends  RecyclerView.ViewHolder{
        TextView date;
        ViewHolderForDates(View itemView){
            super(itemView);
            date = itemView.findViewById(R.id.date);
        }
        void bind(ChatMessage message){
            date.setText(message.getMessage());
        }
    }

    // -------- Convert Base64 String Image To Bitmap -----
    private void setStringBitmap(ImageView preview,String bitmapString){
        byte[] decodedBytes = Base64.decode(
                bitmapString.substring(bitmapString.indexOf(",")  + 1),
                Base64.DEFAULT
        );
        Bitmap previewImage = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        preview.setImageBitmap(previewImage);
    }
}
