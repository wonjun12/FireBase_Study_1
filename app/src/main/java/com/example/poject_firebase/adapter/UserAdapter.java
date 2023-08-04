package com.example.poject_firebase.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.poject_firebase.MainActivity;
import com.example.poject_firebase.R;
import com.example.poject_firebase.chat.ChatActivity;
import com.example.poject_firebase.model.ChatRoom;
import com.example.poject_firebase.model.User;
import com.example.poject_firebase.user.UserSearchActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>{

    Context context;
    ArrayList<User> userArrayList;

    public UserAdapter(Context context, ArrayList<User> userArrayList) {
        this.context = context;
        this.userArrayList = userArrayList;
    }

    @NonNull
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_list_row, parent, false);

        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userArrayList.get(position);

        holder.txtName.setText(user.getName());
        holder.txtEmail.setText(user.getEmail());

    }

    @Override
    public int getItemCount() {
        return userArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView txtName;
        TextView txtEmail;
        CardView cardView;

        DatabaseReference myRef;

        ChatRoom chatRoom;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txtName);
            txtEmail = itemView.findViewById(R.id.txtEmail);
            cardView = itemView.findViewById(R.id.cardView);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int index = getAdapterPosition();

                    User user = userArrayList.get(index);

                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    String myUid = mAuth.getCurrentUser().getUid();

                    // TODO: 2023-08-04 대화방을 Chatroom/chatrooms 경로로 설정한다.
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    myRef = database.getReference("ChatRoom").child("chatRooms");

                    // TODO: 2023-08-04 chatroom객체 안에 로그인한 유저와 선택된 유저가 대화 중이라는 값을 넣는다.
                    chatRoom = new ChatRoom();
                    HashMap<String, Boolean> hashUser = new HashMap<>();
                    hashUser.put(myUid, true);
                    hashUser.put(user.getUid(), true);
                    chatRoom.users = hashUser;

                    // TODO: 2023-08-04 ChatRoom/chatRooms경로안의 users라는 경로가 존재 할시, 상대방의 uid의 정보가 포함된 정보들을 가져온다. 
                    myRef.orderByChild("users/" + user.getUid()).equalTo(true)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.getValue() != null) {
                                        for (DataSnapshot data : snapshot.getChildren()){
                                            ChatRoom chatRoom1 = data.getValue(ChatRoom.class);

                                            if (chatRoom1.users.get(myUid) != null) {
                                                // TODO: 2023-08-04 접속한 자신과 선택된 유저가 대화한 데이터가 있다면, 채팅방 생성하지 않고 이동만 한다. 
                                                goToChatRoom(chatRoom, user.getUid());

                                                return;
                                            }
                                        }
                                    }
                                    // TODO: 2023-08-04 만약 한번도 대화한적이 없는 유저이거나, 접속한 유저와 대화한적이 없다면 채팅방을 생성하고 이동한다. 
                                    myRef.push().setValue(chatRoom);
                                    goToChatRoom(chatRoom, user.getUid());
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) { }
                            });

                }

                // TODO: 2023-08-04 채팅룸의 사용자, 메시지 정보인 chatRomm, 상대방의 uid를 담은 opponent, 채팅방의 고유 키값 chatKey를 담아 보낸다. 
                // TODO: 2023-08-04 유저 검색에서는 채팅방 고유 키값을 찾을 수 없기에 값은 없다.
                private void goToChatRoom(ChatRoom chatRoom, String opponent){
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra("chatRoom", chatRoom);
                    intent.putExtra("opponent", opponent);
                    intent.putExtra("chatKey", "");

                    context.startActivity(intent);

                    ((UserSearchActivity)context).finish();
                }
            });
        }
    }
}
