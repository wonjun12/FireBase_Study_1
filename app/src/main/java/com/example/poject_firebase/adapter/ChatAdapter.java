package com.example.poject_firebase.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.poject_firebase.R;
import com.example.poject_firebase.model.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder>{
    Context context;
//    ArrayList<ChatRoom> chatRoomArrayList;
//    ArrayList<String> chatRoomKeys;
//
//    public ChatRoomAdapter(Context context, ArrayList<ChatRoom> chatRoomArrayList, ArrayList<String> chatRoomKeys) {
//        this.context = context;
//        this.chatRoomArrayList = chatRoomArrayList;
//        this.chatRoomKeys = chatRoomKeys;
//    }

    ArrayList<Message> messageArrayList;
    FirebaseAuth mAuth;

    public ChatAdapter(Context context, ArrayList<Message> messageArrayList, FirebaseAuth mAuth) {
        this.context = context;
        this.messageArrayList = messageArrayList;
        this.mAuth = mAuth;
    }

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_row, parent, false);

        return new ChatAdapter.ViewHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        ChatRoom chatRoom = chatRoomArrayList.get(position);
//        Iterator<String> charRoomList = chatRoom.getUsers().keySet().iterator();
//        charRoomList.hasNext();
//        String opponent = charRoomList.next();
//
//        int arrSize = chatRoom.getMessages().values().size();

        Message message = messageArrayList.get(position);
        

        if(message.getConfirmed() == true){
            //파일 업로드 참조
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference(message.getContent());
            Glide.with(context).load(storageRef).placeholder(R.drawable.baseline_broken_image_24)
                    .override(300, 300)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .into(holder.imgMessage);

            holder.txtMessage.setText("");
        }else{
            holder.imgMessage.setImageResource(0);
            holder.txtMessage.setText(message.getContent());
        }



        if(mAuth.getCurrentUser().getUid().equals(message.getUid())){
            holder.chatRow.setGravity(Gravity.RIGHT);
            holder.cardView.setCardBackgroundColor(R.drawable.my_chat_background);
            //holder.cardView.setCardBackgroundColor(com.google.android.material.R.color.material_dynamic_neutral60);
        }else {
            holder.chatRow.setGravity(Gravity.LEFT);
            holder.cardView.setCardBackgroundColor(R.drawable.ouhter_chat_background);
            //holder.cardView.setCardBackgroundColor(com.google.android.material.R.color.material_dynamic_neutral80);
        }



    }

    @Override
    public int getItemCount() {
//        return chatRoomKeys.size();
        return messageArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView txtMessage;
        LinearLayout chatRow;
        CardView cardView;
        ImageView imgMessage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtMessage = itemView.findViewById(R.id.txtMessage);
            chatRow = itemView.findViewById(R.id.chatRow);
            cardView = itemView.findViewById(R.id.cardView);
            imgMessage = itemView.findViewById(R.id.imgMessage);
        }
    }
}
