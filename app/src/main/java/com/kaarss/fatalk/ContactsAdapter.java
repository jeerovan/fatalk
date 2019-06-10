package com.kaarss.fatalk;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ListViewHolder> {

    private static String TAG = ContactsAdapter.class.getSimpleName();
    private List<ChatProfile> users;
    private Context mContext;

    public ContactsAdapter(Context context) {
        mContext = context;
    }

    public void setUsers(List<ChatProfile> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                             int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_item, parent, false);

        CircleImageView _dp = view.findViewById(R.id.profile_image);
        LinearLayout _userDetail = view.findViewById(R.id.user_detail);

        _dp.setOnClickListener(ContactsActivity.listItemClickListener);
        _userDetail.setOnClickListener(ContactsActivity.listItemClickListener);

        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        ChatProfile user = users.get(position);
        String userId = user.userId;
        String userName = user.name;
        holder._profileImage.setImageBitmap(App.getUserImage(userId,user.gender));
        holder._username.setText(userName);
        holder._age.setText(user.age+"");
        holder._country.setText(user.country);
        if(user.gender == 1){
            holder._female.setVisibility(View.GONE);
            holder._male.setVisibility(View.VISIBLE);
        } else {
            holder._male.setVisibility(View.GONE);
            holder._female.setVisibility(View.VISIBLE);
        }
        int unread = user.unread;
        if(unread > 0){
            holder._unread.setVisibility(View.VISIBLE);
            holder._unread.setText(unread+"");
            if(user.messageType == Keys.chatMessageTypeText){
                holder._bio.setText(user.message);
            } else if(user.messageType == Keys.chatMessageTypePicture){
                holder._bio.setText("Has Sent You An Image");
            } else if(user.messageType == Keys.chatMessageTypeGif){
                holder._bio.setText("Has Sent You A GIF");
            } else if(user.messageType == Keys.chatMessageTypeAudio){
                holder._bio.setText("Has Sent You An Audio");
            } else if(user.messageType == Keys.chatMessageTypeVideo){
                holder._bio.setText("Has Sent You A Video");
            }
        } else {
            holder._unread.setVisibility(View.INVISIBLE);
            holder._bio.setText(user.bio);
        }
        String contentDescription = userId+ "|" +userName;
        holder._profileImage.setContentDescription(contentDescription);
        holder._userDetail.setContentDescription(contentDescription);
    }

    @Override
    public int getItemCount() {
        if (users != null) {
            return users.size();
        } else {
            return 0;
        }
    }

    public void updateImage(String userId){
        for(ChatProfile user : users){
            if(user.userId.equals(userId)){
                notifyItemChanged(users.indexOf(user));
            }
        }
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {

        CircleImageView _profileImage;
        LinearLayout _userDetail;
        TextView _username;
        TextView _age;
        TextView _country;
        TextView _bio;
        ImageView _female;
        ImageView _male;
        TextView _unread;

        public ListViewHolder(View itemView) {
            super(itemView);
            this._profileImage = itemView.findViewById(R.id.profile_image);
            this._userDetail = itemView.findViewById(R.id.user_detail);
            this._username = itemView.findViewById(R.id.username);
            this._age = itemView.findViewById(R.id.age);
            this._country = itemView.findViewById(R.id.country);
            this._bio = itemView.findViewById(R.id.bio);
            this._female = itemView.findViewById(R.id.f);
            this._male = itemView.findViewById(R.id.m);
            this._unread = itemView.findViewById(R.id.unread_count);
        }
    }
}
