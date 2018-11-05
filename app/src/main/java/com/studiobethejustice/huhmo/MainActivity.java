package com.studiobethejustice.huhmo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.studiobethejustice.huhmo.model.Memo;

import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private static FirebaseDatabase mFirebaseDatabase;
    private EditText etContents;
    private TextView txtEmail, txtName;
    private NavigationView mNavigationView;
    private String selectedMemoKey;

    static {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseDatabase.setPersistenceEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        etContents = findViewById(R.id.content);

        if (mFirebaseUser == null) {
            startActivity(new Intent(MainActivity.this, AuthActivity.class));
            finish();
            Toast.makeText(this, "다시 로그인하세요", Toast.LENGTH_LONG).show();
            return;
        }

        FloatingActionButton newMemo = findViewById(R.id.new_memo);
        newMemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initMemo();
            }
        });

        FloatingActionButton saveMemo = findViewById(R.id.save_memo);
        saveMemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( selectedMemoKey == null){
                    saveMemo();
                } else{
                    updateMemo();
                }

            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = mNavigationView.getHeaderView(0);
        txtEmail = headerView.findViewById(R.id.txtEmail);
        txtName = headerView.findViewById(R.id.txtName);
        mNavigationView.setNavigationItemSelectedListener(this);
        profileUpdate();
        displayMemos();
    }

    private void saveMemo() {

        Memo memo = new Memo();
        memo.setText(etContents.getText().toString());
        memo.setCreateDate(new Date().getTime());
        mFirebaseDatabase
                .getReference("memos/" + mFirebaseUser.getUid())
                .push()
                .setValue(memo)
                .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Snackbar.make(etContents, "메모가 저장되었습니다.", Snackbar.LENGTH_LONG).show();
                        initMemo();
                    }
                });
    }

    private void updateMemo(){
        String text = etContents.getText().toString();
        if(text.isEmpty()){
            return;
        }

        Memo memo = new Memo();
        memo.setText(etContents.getText().toString());
        memo.setCreateDate(new Date().getTime());
        mFirebaseDatabase
                .getReference("memos/"+mFirebaseUser.getUid()+"/"+selectedMemoKey)
                .setValue(memo)
        .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Snackbar.make(etContents, " 메모가 수정되었습니다", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void deletMemo(){
        if( selectedMemoKey == null) return;

        Snackbar.make(etContents, "메모를 삭제하시겠습니까?",Snackbar.LENGTH_LONG).setAction("삭제", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFirebaseDatabase
                        .getReference("memos/"+mFirebaseUser.getUid()+"/"+selectedMemoKey)
                        .removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                Snackbar.make(etContents, "삭제가완료되었습니다.", Snackbar.LENGTH_LONG).show();
                            }
                        });
            }
        });
    }

    private void initMemo() {
        selectedMemoKey = null;
        etContents.setText("");
    }

    private void profileUpdate() {
        txtEmail.setText(mFirebaseUser.getEmail());
        txtName.setText(mFirebaseUser.getDisplayName());
    }

    private void displayMemos() {
        mFirebaseDatabase.getReference("memos/" + mFirebaseUser.getUid())
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Memo memo = dataSnapshot.getValue(Memo.class);
                        memo.setKey(dataSnapshot.getKey());
                        displayMemoList(memo);
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Memo memo = dataSnapshot.getValue(Memo.class);
                        memo.setKey(dataSnapshot.getKey());

                        for (int i = 0; i < mNavigationView.getMenu().size(); i++) {
                            MenuItem menuItem = mNavigationView.getMenu().getItem(i);
                            if( memo.getKey().equals((Memo)menuItem.getActionView().getTag())){
                                menuItem.getActionView().setTag(memo);
                                menuItem.setTitle(memo.getTitle());
                                break;
                            }
                        }
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void displayMemoList(Memo memo) {
        Menu leftMenu = mNavigationView.getMenu();
        MenuItem item = leftMenu.add(memo.getTitle());
        View view = new View(getApplicationContext());
        view.setTag(memo);
        item.setActionView(view);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_delete) {
            deletMemo();
        }
        if (id == R.id.action_logout) {
            logout();
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        Snackbar.make(etContents, "로그아웃하시겠습니까?", Snackbar.LENGTH_LONG).setAction("로그아웃", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFirebaseAuth.signOut();
                startActivity(new Intent(MainActivity.this, AuthActivity.class));
                finish();
            }
        });
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Memo selectedMemo = (Memo) item.getActionView().getTag();
        selectedMemoKey = selectedMemo.getKey();
        etContents.setText(selectedMemo.getText());
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
