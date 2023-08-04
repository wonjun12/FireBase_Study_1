package com.example.poject_firebase;

import static android.Manifest.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.poject_firebase.adapter.ChatAdapter;
import com.example.poject_firebase.config.Config;
import com.example.poject_firebase.model.ChatRoom;
import com.example.poject_firebase.model.Message;
import com.example.poject_firebase.model.User;
import com.example.poject_firebase.user.LoginActivity;
import com.example.poject_firebase.user.RegisterActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.UploadTask;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    ProgressBar progressBar;

    private FirebaseAuth mAuth;
    DatabaseReference myRef;

    public RecyclerView recyclerView;
    ChatAdapter adapter;
//    ArrayList<ChatRoom> chatRoomArrayList = new ArrayList<>();
//    ArrayList<String> chatRoomKeys = new ArrayList<>();
    ArrayList<Message> messageArrayList = new ArrayList<>();
    Button btnSend;
    EditText editContent;

    ImageView btnImageSend;

    File photoFile;
    StorageReference storageRef;

    ChatRoom chatRoom;
    String opponent;
    String chatKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatRoom = (ChatRoom) getIntent().getSerializableExtra("chatRoom");
        opponent = getIntent().getStringExtra("opponent");
        chatKey = getIntent().getStringExtra("chatKey");

        mAuth = FirebaseAuth.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference("ChatRoom").child("chatRooms");

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        editContent = findViewById(R.id.editContent);
        btnSend = findViewById(R.id.btnSend);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = editContent.getText().toString();
                if(content.isEmpty()){
                    return;
                }

                editContent.setText("");
                sendMessage(content);
            }
        });

        btnImageSend = findViewById(R.id.btnImageSend);
        btnImageSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });

        adapter = new ChatAdapter(MainActivity.this, messageArrayList, mAuth);
        recyclerView.setAdapter(adapter);

        // TODO: 2023-08-04 User/users안에 있는 uid를 검색하면서 Intent로 받은 상대방의 uid를 비교하여 일치하는 정보를 찾는다. 
        FirebaseDatabase.getInstance().getReference("User").child("users")
                .orderByChild("uid").equalTo(opponent)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot data : snapshot.getChildren()){// TODO: 2023-08-04 찾은 정보를 타이틀로 작성 
                            getSupportActionBar().setTitle(data.getValue(User.class).getName());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        if(chatKey.isEmpty()){
            chatReset(); // TODO: 2023-08-04 유저 검색 후 채팅방의 고유 키 값이 없으니, 키값을 먼저 찾자. 
        }else{
            chatRoom(); // TODO: 2023-08-04 채팅방 리스크에서는 고유 키값이 있으니 바로 채팅을 시작하자. 
        }
    }

    void chatReset(){
        myRef.orderByChild("users/" + opponent).equalTo(true) // TODO: 2023-08-04 ChatRoom/chatRooms 경로 중 Intent로 받은 상대방 uid와 일치하는 모든 채팅방을 찾는다. 
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            ChatRoom chatRoom1 = child.getValue(ChatRoom.class);
                            if(chatRoom1.users.get(mAuth.getCurrentUser().getUid()) != null){ // TODO: 2023-08-04 모든 채팅방 중 접속한 자신의 uid와 일치하는 채팅방을 찾음 
                                chatKey = child.getKey(); // TODO: 2023-08-04 찾은 채팅방의 고유 키 값을 넣는다. 
                                chatRoom(); // TODO: 2023-08-04 고유 키 값을 넣은 후 채팅을 시작할 수 있다. 
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

   private void chatRoom(){
       // TODO: 2023-08-04 채팅방의 고유 키 값의 경로 안에서 messages의 경로에 채팅이 추가 될 경우 
        myRef.child(chatKey).child("messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // TODO: 2023-08-04 만든다. 
                Message message = snapshot.getValue(Message.class);

                messageArrayList.add(message);
                adapter.notifyDataSetChanged();

                recyclerView.scrollToPosition(messageArrayList.size() - 1);

            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
   }

    // TODO: 2023-08-04 채팅 보내기 
   private void sendMessage(String content){
        Message message = new Message(mAuth.getCurrentUser().getUid(), content,getDateTimeString(), false);

       myRef.child(chatKey).child("messages")
               .push().setValue(message);
   }

    // TODO: 2023-08-04 날짜를 변환하는 함수 
   private String getDateTimeString(){
       LocalDateTime localDateTime = LocalDateTime.now();
       localDateTime.atZone(TimeZone.getDefault().toZoneId());
       DateTimeFormatter  dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
       return localDateTime.format(dateTimeFormatter).toString();
   }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("선택하세요.");

        // 다이아로그에 여러개 항목을 보여주게 가능한 함수
        builder.setItems(R.array.alert_photo, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) { // 각각을 클릭했을때 이벤트를 작성
                // i : 누른게 무엇인지 알려줌
                if(i == 0){ // 첫번째 항목을 눌렀다면,
                    camera();
                }else if(i == 1){
                    album();
                }
            }
        });

        builder.show();
    }

    private void camera(){
        // 퍼미션 체크 (권한 여부 체크)
        int permissionCheck = ContextCompat.checkSelfPermission(
                MainActivity.this, permission.CAMERA);

        // 허용이 안되어있으면,
        if(permissionCheck != PackageManager.PERMISSION_GRANTED){
            // 다시한번 물어 봐라
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{permission.CAMERA} ,
                    1000);
            Toast.makeText(MainActivity.this, "카메라 권한 필요합니다.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        // 권한을 이미 허용했다면
        else {
            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //카메라가  있다면 실행
            if(i.resolveActivity(MainActivity.this.getPackageManager())  != null  ){

                // 사진의 파일명을 만들기
                String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                photoFile = getPhotoFile(fileName);

                //android:authorities="com.wonjun.cameraapp.fileprovider"
                // 동일해야함
                Uri fileProvider = FileProvider.getUriForFile(MainActivity.this,
                        "com.example.poject_firebase.fileprovider", photoFile);
                i.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
                startActivityForResult(i, 100);

            } else{
                Toast.makeText(MainActivity.this, "이폰에는 카메라 앱이 없습니다.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File getPhotoFile(String fileName) {
        File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try{
            return File.createTempFile(fileName, ".jpg", storageDirectory);
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    private void album(){
        if(checkPermission()){ // 권한 여부 확인
            displayFileChoose();
        }else{
            requestPermission();
        }
    }

    // 앨범 권한 부여 체크
    private boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(MainActivity.this,
                permission.WRITE_EXTERNAL_STORAGE);
        return true;

//        if(result == PackageManager.PERMISSION_DENIED){
//            return false;
//        }else{
//
//        }
    }

    private void requestPermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                permission.WRITE_EXTERNAL_STORAGE)){
            Log.i("DEBUGGING5", "true");
            Toast.makeText(MainActivity.this, "권한 수락이 필요합니다.",
                    Toast.LENGTH_SHORT).show();
        }else{
            Log.i("DEBUGGING6", "false");
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{permission.WRITE_EXTERNAL_STORAGE}, 500);
        }
    }

    private void displayFileChoose() { //앨범이 열림
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "SELECT IMAGE"), 300);
        // 앨범 파일을 선택하면, Main으로 돌아옴 (이전 액티비티)
        // -> onActivityResult 300번으로 돌아옴
    }

    //앨범에서 선택한 사진이름 가져오기
    public String getFileName( Uri uri ) {
        Cursor cursor = getContentResolver( ).query( uri, null, null, null, null );
        try {
            if ( cursor == null ) return null;
            cursor.moveToFirst( );
            @SuppressLint("Range") String fileName = cursor.getString( cursor.getColumnIndex( OpenableColumns.DISPLAY_NAME ) );
            cursor.close( );
            return fileName;

        } catch ( Exception e ) {
            e.printStackTrace( );
            cursor.close( );
            return null;
        }
    }

    // 권한 관련해서 통신할때  사용되는 메서드
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1000: { //아까 카메라 권한이  설정안되어 있을때 1000번으로 보냈었음
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "권한 허가 되었음",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "아직 승인하지 않았음",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case 500: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "권한 허가 되었음",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "아직 승인하지 않았음",
                            Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    // 앱을 실행했을때, 사진에 대해 유저에게 보여주기 위해 실행되는 메서드
    // 카메라로 찍었을때, 사진을 보여준다.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        storageRef = FirebaseStorage.getInstance().getReference(chatKey).child("images");
        if(requestCode == 100 && resultCode == RESULT_OK){ //카메라

            Bitmap photo = BitmapFactory.decodeFile(photoFile.getAbsolutePath());

            ExifInterface exif = null;
            try {
                exif = new ExifInterface(photoFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            photo = rotateBitmap(photo, orientation); //가로, 세로 찍은 방향에 대해 정확하게 다시 돌려준다.

            // 압축시킨다. 해상도 낮춰서
            OutputStream os;
            try {
                os = new FileOutputStream(photoFile);
                photo.compress(Bitmap.CompressFormat.JPEG, 50, os);
                os.flush();
                os.close();
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
            }

            photo = BitmapFactory.decodeFile(photoFile.getAbsolutePath());

            // 네트워크로 데이터 보낸다.
            // 카메라 찍은 동시에 네트워크로 데이터를 보내야 한다면, 코드를 추가한다.

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageData = baos.toByteArray();

            UploadTask uploadTask = storageRef.child("/" + photoFile.getName()).putBytes(imageData);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Message message = new Message(mAuth.getCurrentUser().getUid() + "", "images/" + photoFile.getName(), getDateTimeString(), true);

                    myRef.push().setValue(message);
                }
            });


        }else if(requestCode == 300 && resultCode == RESULT_OK && data != null &&
                data.getData() != null){
            // 앨범에서 사진을 가져왔을때

            Uri albumUri = data.getData( );
            String fileName = getFileName( albumUri );
            try {

                ParcelFileDescriptor parcelFileDescriptor = getContentResolver( ).openFileDescriptor( albumUri, "r" );
                if ( parcelFileDescriptor == null ) return;
                FileInputStream inputStream = new FileInputStream( parcelFileDescriptor.getFileDescriptor( ) );
                photoFile = new File( this.getCacheDir( ), fileName );
                FileOutputStream outputStream = new FileOutputStream( photoFile );
                IOUtils.copy( inputStream, outputStream ); // 라이브러리 설치
                //implementation 'commons-io:commons-io:2.4'
                // 임시용 폴더에 저장해서 사용하자.
//                //임시파일 생성
//                File file = createImgCacheFile( );
//                String cacheFilePath = file.getAbsolutePath( );

                // 압축시킨다. 해상도 낮춰서
                Bitmap photo = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                OutputStream os;
                try {
                    os = new FileOutputStream(photoFile);
                    photo.compress(Bitmap.CompressFormat.JPEG, 60, os);
                    os.flush();
                    os.close();
                } catch (Exception e) {
                    Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
                }

                // TODO: 2023-08-04 ImageView에 이미지를 적용하기 전에 적용되어 있는 File을 쪼개서 파이어베이스 저장소에 채팅방의 고유 키 값의 경로로 저장시킨다. 
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageData = baos.toByteArray();

                UploadTask uploadTask = storageRef.child("/" + photoFile.getName()).putBytes(imageData);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // TODO: 2023-08-04 그리고 올린게 성공하면, 메시지에 해당 사진의 경로를 담아서 보내자. 
                        Message message = new Message(mAuth.getCurrentUser().getUid(),
                                chatKey + "/images/" + photoFile.getName(), getDateTimeString(), true);

                        myRef.child(chatKey).child("messages").push().setValue(message);
                    }
                });

//                imageView.setImageBitmap( getBitmapAlbum( imageView, albumUri ) );

            } catch ( Exception e ) {
                e.printStackTrace( );
            }

            // 네트워크로 보낸다.


        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    // 화면 가로, 세로 크기를 측정해 원래의 형태로 되돌리는 메서드
    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        }
        catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }
}