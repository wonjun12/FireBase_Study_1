package com.example.poject_firebase.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.example.poject_firebase.R;
import com.example.poject_firebase.adapter.UserAdapter;
import com.example.poject_firebase.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class UserSearchActivity extends AppCompatActivity {

    EditText editSearch;
    ProgressBar progressBar;

    RecyclerView recyclerView;
    UserAdapter adapter;
    ArrayList<User> userArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_search);
        
        getSupportActionBar().setTitle("사용자 검색");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editSearch = findViewById(R.id.editSearch);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(UserSearchActivity.this));

        // TODO: 2023-08-04 텍스트가 실시간으로 입려될 경우 발동되는 메서드 
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String search = editSearch.getText().toString();
                userArrayList.clear();

                if(search.isEmpty()){
                    adapter.notifyDataSetChanged();

                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                // TODO: 2023-08-04 실시간 데이터베이스의 User/users의 경로에 접근 
                FirebaseDatabase.getInstance()
                        .getReference("User").child("users")
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                progressBar.setVisibility(View.GONE);
                                for(DataSnapshot data : snapshot.getChildren()){
                                    // TODO: 2023-08-04 모든 유저를 반복문 돌리면서, User객체로 변환
                                    User user = data.getValue(User.class);

                                    // TODO: 2023-08-04 이름과 이메일에 포함되는 문자열이 없다면 다음 객체로 넘어간다.
                                    if(!user.getName().contains(search) && !user.getEmail().contains(search)){
                                        continue;
                                    }

                                    // TODO: 2023-08-04 검색된 id 중 접속된 자신의 id와 일치하지 않는 경우에만 출력한다.
                                    if(!user.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                        userArrayList.add(user);
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }
        });

        adapter = new UserAdapter(UserSearchActivity.this, userArrayList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}