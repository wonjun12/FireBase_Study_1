package com.example.poject_firebase.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.poject_firebase.MainActivity;
import com.example.poject_firebase.R;
import com.example.poject_firebase.chat.ChatActivity;
import com.example.poject_firebase.config.Config;
import com.example.poject_firebase.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

// TODO: 2023-08-04 로그인 액티비티 
public class LoginActivity extends AppCompatActivity {

    EditText editEmail;
    EditText editPassword;
    ProgressBar progressBar;
    TextView txtToRegister;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setTitle("로그인");

        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        progressBar = findViewById(R.id.progressBar);
        txtToRegister = findViewById(R.id.txtToRegister);

        progressBar.setVisibility(View.GONE);
        mAuth = FirebaseAuth.getInstance();

        txtToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);

                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        // 완료 버튼을 눌렀을때
        if(itemId == R.id.btnComplete){
            userLogin();
        }

        return true;
    }

    private void userLogin(){
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString();

        if(password.length() < 4){
            return;
        }

        Pattern pattern = Patterns.EMAIL_ADDRESS;
        if(!pattern.matcher(email).matches()){
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // TODO: 2023-08-04 이메일, 비밀번호를 받아서 로그인을 시도 
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if(task.isSuccessful()){
                            // TODO: 2023-08-04 로그인 정상적으로 성공시, ChatActivity(채팅방 리스트)로 이동
                            Intent intent = new Intent(LoginActivity.this, ChatActivity.class);
                            startActivity(intent);

                            finish();
                        }else{
                            Toast.makeText(
                                    LoginActivity.this,
                                    "이메일, 비밀번호가 다릅니다.",
                                    Toast.LENGTH_SHORT
                            );
                        }
                    }
                });



    }
}