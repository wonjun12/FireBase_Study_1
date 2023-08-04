package com.example.poject_firebase.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.poject_firebase.MainActivity;
import com.example.poject_firebase.R;
import com.example.poject_firebase.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RegisterActivity extends AppCompatActivity {

    EditText editName;
    EditText editEmail;
    EditText editPassword;
    EditText editPassword2;
    TextView txtToLogin;
    ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        getSupportActionBar().setTitle("회원가입");

        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editPassword2 = findViewById(R.id.editPassword2);
        txtToLogin = findViewById(R.id.txtToLogin);
        progressBar = findViewById(R.id.progressBar);

        progressBar.setVisibility(View.GONE);
        mAuth = FirebaseAuth.getInstance();

        // 로그인 엑티비티 이동
        txtToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
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
            userRegister();
        }
        return true;
    }

    String name;
    String email;
    private void userRegister(){
        name = editName.getText().toString().trim();
        email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString();
        String password2 = editPassword2.getText().toString();

        if(!password.equals(password2)){
            return;
        }

        if(password2.length() < 6 || password.length() < 6){
            return;
        }

        if (name.isEmpty()){
            return;
        }

        Pattern pattern = Patterns.EMAIL_ADDRESS;
        if(!pattern.matcher(email).matches()){
            return;
        }


        progressBar.setVisibility(View.VISIBLE);

        // TODO: 2023-08-04 이메일과 비밀번호 받아서 회원가입 시도 
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this
                        , new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if(task.isSuccessful()){
                            // TODO: 2023-08-04 회원가입 성공시, 유저의 정보를 검색, 활용하기 위해 실시간 데이터 베이스 저장 
                            FirebaseUser user = mAuth.getCurrentUser();
                            String uid = user.getUid().toString();

                            FirebaseDatabase.getInstance()
                                    .getReference("User").child("users") // TODO: 2023-08-04 경로는 User/users에 저장한다. 
                                    .child(uid).setValue(new User(uid, name, email));

                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);

                            finish();
                        }else{

                            Log.i("확인", task.getException().getMessage());
                        }
                    }
                });


    }
}