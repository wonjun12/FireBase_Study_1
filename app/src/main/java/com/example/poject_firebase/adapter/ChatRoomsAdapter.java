package com.example.poject_firebase.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.poject_firebase.MainActivity;
import com.example.poject_firebase.R;
import com.example.poject_firebase.model.ChatRoom;
import com.example.poject_firebase.model.Message;
import com.example.poject_firebase.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChatRoomsAdapter extends RecyclerView.Adapter<ChatRoomsAdapter.ViewHolder>{

    Context context;
    ArrayList<ChatRoom> chatRoomArrayList;
    ArrayList<String> chatKeyArrayList;
    String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    public ChatRoomsAdapter(Context context, ArrayList<ChatRoom> chatRoomArrayList, ArrayList<String> chatKeyArrayList) {
        this.context = context;
        this.chatRoomArrayList = chatRoomArrayList;
        this.chatKeyArrayList = chatKeyArrayList;
    }

    @NonNull
    @Override
    public ChatRoomsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_room_row, parent, false);

        return new ChatRoomsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatRoom chatRoom = chatRoomArrayList.get(position);

        // TODO: 2023-08-04 채팅방의 유저의 키 값을 for로 돌려서 
        for(String key : chatRoom.users.keySet()){
            // TODO: 2023-08-04 접속한 자신의 uid와 일치 하지 않다면 상대방의 uid이니까 정보를 찾아서 이름과, 키 값을 넣자. 
            if(!key.equals(myUid)){
                FirebaseDatabase.getInstance()
                        .getReference("User").child("users")
                                .orderByChild("uid").equalTo(key)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for( DataSnapshot data : snapshot.getChildren()){
                                    holder.txtName.setText(data.getValue(User.class).getName());
                                    holder.txtOpponent.setText(key); // TODO: 2023-08-04 숨겨져 있는 TextView임, GONE으로 숨겨져 있음 
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }
        }

        int limit = chatRoom.messages.values().size();// TODO: 2023-08-04 해당 채팅방의 메시지의 크기가 1이상이면 
        if(limit > 0){
            // TODO: 2023-08-04 메시지의 마지막의 값을 찾습니다. 
            List<Message> messageList = chatRoom.messages.values().stream()
                    .sorted(Comparator.comparing(Message::getDate)).skip(limit-1).collect(Collectors.toList());
            Message message = messageList.get(0);

            if(message.getConfirmed()){ // TODO: 2023-08-04 true일 경우 이미지로 출력하기로 하자. 
                holder.imageView.setVisibility(View.VISIBLE);
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference(message.getContent());
                Glide.with(context).load(storageRef).placeholder(R.drawable.baseline_broken_image_24)
                        .override(300, 300)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .into(holder.imageView);
                holder.txtMessage.setText("");
            }else{ // TODO: 2023-08-04 flase일 경우 텍스트로 출력하기로 하자 
                holder.imageView.setVisibility(View.GONE);
                holder.txtMessage.setText(message.getContent());
            }


        }else {
            holder.txtMessage.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return chatRoomArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView txtName;
        TextView txtMessage;
        TextView txtOpponent;
        CardView cardView;
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txtName);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            cardView = itemView.findViewById(R.id.cardView);
            imageView = itemView.findViewById(R.id.imageView);
            txtOpponent = itemView.findViewById(R.id.txtOpponent);
            txtOpponent.setVisibility(View.GONE);

            // TODO: 2023-08-04 채팅방 리스트에서 클릭시에 채팅방 고유 키값도 보낼 수 있다. 
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int index = getAdapterPosition();
                    String opponent = txtOpponent.getText().toString();

                    ChatRoom chatRoom = chatRoomArrayList.get(index);
                    String chatKey = chatKeyArrayList.get(index);

                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra("chatRoom", chatRoom);
                    intent.putExtra("chatKey", chatKey);

                    intent.putExtra("opponent", opponent);

                    context.startActivity(intent);
                }
            });
        }
    }
}
