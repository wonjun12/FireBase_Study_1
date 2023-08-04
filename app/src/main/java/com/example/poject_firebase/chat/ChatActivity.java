package com.example.poject_firebase.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.example.poject_firebase.MainActivity;
import com.example.poject_firebase.R;
import com.example.poject_firebase.adapter.ChatAdapter;
import com.example.poject_firebase.adapter.ChatRoomsAdapter;
import com.example.poject_firebase.config.Config;
import com.example.poject_firebase.model.ChatRoom;
import com.example.poject_firebase.user.LoginActivity;
import com.example.poject_firebase.user.RegisterActivity;
import com.example.poject_firebase.user.UserSearchActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;

// TODO: 2023-08-04 채팅 방 리스트 
public class ChatActivity extends AppCompatActivity {


    String googleToken;

    ProgressBar progressBar;
    RecyclerView recyclerView;
    ChatRoomsAdapter adapter;
    ArrayList<ChatRoom> chatRoomArrayList = new ArrayList<>();
    ArrayList<String> chatKeyArrayList = new ArrayList<>();

    private FirebaseAuth mAuth;
    StorageReference storageRef;
    DatabaseReference myRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() == null){
            Intent intent = new Intent(ChatActivity.this, LoginActivity.class);
            startActivity(intent);

            finish();
            return;
        }

        getSupportActionBar().setTitle("채팅 방");
        
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(ChatActivity.this));

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("ChatRoom").child("chatRooms");

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference().child("images");

        adapter = new ChatRoomsAdapter(ChatActivity.this, chatRoomArrayList, chatKeyArrayList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logout_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        // 로그아웃 버튼을 눌렀을때
        if(itemId == R.id.btnLogout){
            mAuth.getInstance().signOut();

            Intent intent = new Intent(ChatActivity.this, LoginActivity.class);
            startActivity(intent);

            finish();
        }else if(itemId == R.id.btnSearch){
            Intent intent = new Intent(ChatActivity.this, UserSearchActivity.class);
            startActivity(intent);
        }

        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();

        chatRoomAdd();
    }

    protected void chatRoomAdd(){
        chatRoomArrayList.clear();

        // TODO: 2023-08-04 ChatRoom/chatRooms의 경로안에 users의 경로중 접속한 자신의 uid와 일치하는 부분을 전부 찾는다. 
        myRef.orderByChild("users/" + mAuth.getCurrentUser().getUid()).equalTo(true)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot data : snapshot.getChildren()){
                            // TODO: 2023-08-04 찾은 정보들을 전부 ChatRoom 클래스에 담자. 
                            // TODO: 2023-08-04 찾은 정보들의 키값들이 존재하는데, 고유 키값이니 같이 저장하자. 

                            ChatRoom chatRoom = data.getValue(ChatRoom.class);

                            chatKeyArrayList.add(data.getKey());
                            chatRoomArrayList.add(chatRoom);

                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}