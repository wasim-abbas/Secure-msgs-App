package com.wasim.messegeingApp.activities;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wasim.messegeingApp.R;
import com.wasim.messegeingApp.adapters.SingleGroupAdapter;
import com.wasim.messegeingApp.constants.Constants;
import com.wasim.messegeingApp.constants.SmsContract;
import com.wasim.messegeingApp.receivers.DeliverReceiver;
import com.wasim.messegeingApp.receivers.SentReceiver;
import com.wasim.messegeingApp.services.UpdateSMSService;

public class SmsDetailedView extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    private String contact;
    private SingleGroupAdapter singleGroupAdapter;
    private RecyclerView recyclerView;
    private EditText etMessage;
    private ImageView btSend;
    private String message;
    private boolean from_reciever;
    private long _Id;
    private int color;
    private String read = "1";
    String encryptedMsg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_detailed_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();
    }


    private void init() {

        Intent intent = getIntent();


        contact = intent.getStringExtra(Constants.CONTACT_NAME);
        _Id = intent.getLongExtra(Constants.SMS_ID, -123);
        color = intent.getIntExtra(Constants.COLOR, 0);
        read = intent.getStringExtra(Constants.READ);

        from_reciever = intent.getBooleanExtra(Constants.FROM_SMS_RECIEVER, false);

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(contact);

        recyclerView = findViewById(R.id.recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        etMessage = findViewById(R.id.etMessage);
        btSend = findViewById(R.id.btSend);

        btSend.setOnClickListener(this);

        setRecyclerView(null);

        if (read != null && read.equals("0"))
            setReadSMS();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            if (from_reciever)
                startActivity(new Intent(this, MainActivity.class));

            finish();
        }

        return super.onOptionsItemSelected(item);
    }


    private void setRecyclerView(Cursor cursor) {
        singleGroupAdapter = new SingleGroupAdapter(this, cursor, color);
        recyclerView.setAdapter(singleGroupAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().initLoader(Constants.CONVERSATION_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] selectionArgs = {contact};

        return new CursorLoader(this,
                SmsContract.ALL_SMS_URI,
                null,
                SmsContract.SMS_SELECTION,
                selectionArgs,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {

        if (cursor != null && cursor.getCount() > 0) {
            singleGroupAdapter.swapCursor(cursor);
        }  //no sms

    }


    private void setReadSMS() {

        Intent intent = new Intent(this, UpdateSMSService.class);
        intent.putExtra("id", _Id);
        startService(intent);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        singleGroupAdapter.swapCursor(null);
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.btSend) {
            sendSMSMessage();
        }
    }

    protected void sendSMSMessage() {

        message = etMessage.getText().toString();
        try {
           /////////////////////////////// encryptedMsg = AESUtils.encrypt(message);
            System.out.println("Encrypted msg in Sms DEtail class"+encryptedMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (message.trim().length() > 0)
            requestPermissions();
        else
            etMessage.setError(getString(R.string.please_write_message));

    }

    private void requestPermissions() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        Constants.MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        } else {
            sendSMSNow();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Constants.MY_PERMISSIONS_REQUEST_SEND_SMS) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendSMSNow();
            } else {
                Toast.makeText(getApplicationContext(),
                        "SMS failed, please try again.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void sendSMSNow() {

        BroadcastReceiver sendBroadcastReceiver = new SentReceiver();
        BroadcastReceiver deliveryBroadcastReciever = new DeliverReceiver();

        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

        registerReceiver(sendBroadcastReceiver, new IntentFilter(SENT));
        registerReceiver(deliveryBroadcastReciever, new IntentFilter(DELIVERED));

        try {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(contact, null, encryptedMsg, sentPI, deliveredPI);
            etMessage.setText("");
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.cant_send), Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void onBackPressed() {

        if (from_reciever) {
            startActivity(new Intent(this, MainActivity.class));
        } else
            super.onBackPressed();
    }
}
