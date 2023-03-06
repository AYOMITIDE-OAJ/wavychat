package com.oajstudios.wavychat.groupVideoCall;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.oajstudios.wavychat.R;
import com.oajstudios.wavychat.group.GroupChatActivity;
import com.oajstudios.wavychat.groupVideoCall.openvcall.model.ConstantApp;
import com.oajstudios.wavychat.groupVideoCall.openvcall.ui.BaseActivity;
import com.oajstudios.wavychat.groupVideoCall.openvcall.ui.VideoGroupCallActivity;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


@SuppressWarnings("ALL")
public class CallingGroupVideoActivity extends BaseActivity {


    String groupId,room;
    boolean isAnswered = false;
    MediaPlayer mp;

    private void setDay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            Drawable background = getResources().getDrawable(R.drawable.gradient_bg);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(android.R.color.transparent));
            window.setBackgroundDrawable(background);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setDay();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling_group_voice);

        mp = MediaPlayer.create(this, R.raw.calling);
        mp.start();

        groupId = getIntent().getStringExtra("group");
        room = getIntent().getStringExtra("room");

        //Query
        FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Video").child(room).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("ans").exists()){
                    mp.stop();
                    isAnswered = true;
                    vSettings().mChannelName = room;
                    String encryption = "AES-128-XTS";
                    vSettings().mEncryptionKey = encryption;
                    Intent i = new Intent(CallingGroupVideoActivity.this, VideoGroupCallActivity.class);
                    i.putExtra(ConstantApp.ACTION_KEY_CHANNEL_NAME, room);
                    i.putExtra(ConstantApp.ACTION_KEY_ENCRYPTION_KEY, encryption);
                    i.putExtra(ConstantApp.ACTION_KEY_ENCRYPTION_MODE, getResources().getStringArray(R.array.encryption_mode_values)[vSettings().mEncryptionModeIndex]);
                    i.putExtra("group", groupId);
                    startActivity(i);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //GroupInfo
        FirebaseFirestore.getInstance().collection("groups").document(groupId).addSnapshotListener((value, error) -> {
            TextView name = findViewById(R.id.name);
            name.setText(Objects.requireNonNull(value.get("name")).toString());
            CircleImageView dp = findViewById(R.id.dp);
            if (!Objects.requireNonNull(value.get("photo")).toString().isEmpty())  Picasso.get().load(Objects.requireNonNull(value.get("photo")).toString()).into(dp);
        });

        findViewById(R.id.end).setOnClickListener(v -> {
            mp.stop();
            Toast.makeText(CallingGroupVideoActivity.this, "Canceled", Toast.LENGTH_SHORT).show();
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("type", "cancel");
            FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Video").child(room)
                    .updateChildren(hashMap);
            Intent intent = new Intent(CallingGroupVideoActivity.this, GroupChatActivity.class);
            intent.putExtra("group", groupId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

    }

    @Override
    protected void initUIandEvent() {

    }

    @Override
    protected void deInitUIandEvent() {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!isAnswered){
            mp.stop();
            Toast.makeText(CallingGroupVideoActivity.this, "Canceled", Toast.LENGTH_SHORT).show();
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("type", "cancel");
            FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Video").child(room)
                    .updateChildren(hashMap);
            Intent intent = new Intent(CallingGroupVideoActivity.this, GroupChatActivity.class);
            intent.putExtra("group", groupId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }
}