package com.wasim.messegeingApp.adapters;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.wasim.messegeingApp.R;
import com.wasim.messegeingApp.receivers.SmsReceiver;
import com.wasim.messegeingApp.utils.AESUtils;
import com.wasim.messegeingApp.utils.ColorGeneratorModified;
import com.wasim.messegeingApp.utils.Helpers;

public class SingleGroupAdapter extends RecyclerView.Adapter<SingleGroupAdapter.MyViewHolder> {

    private ColorGeneratorModified generator;
    private Context context;
    private Cursor dataCursor;
    private int color;
    public String decMsg;
    private String firstFiveChars = "";
    private String myMsg = null;
    private String myMsg2 = null;

    public SingleGroupAdapter(Context context, Cursor dataCursor, int color) {

        this.context = context;
        this.dataCursor = dataCursor;
        this.color = color;

        if (color == 0)
            generator = ColorGeneratorModified.MATERIAL;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.single_sms_detailed, parent, false);
        return new MyViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {


        dataCursor.moveToPosition(position);
        String myMessage = dataCursor.getString(dataCursor.getColumnIndexOrThrow("body"));
        try {
            decMsg = AESUtils.decodeFile(SmsReceiver.returnKey(myMessage),SmsReceiver.returnMessege(myMessage));

        } catch (Exception e) {
            e.printStackTrace();
            decMsg = myMessage;
            Log.e("ok", "the msg Error is :" + e.getMessage());
        }
        holder.message.setText(decMsg);

        long time = dataCursor.getLong(dataCursor.getColumnIndexOrThrow("date"));
        holder.time.setText(Helpers.getDate(time));

        String name = dataCursor.getString(dataCursor.getColumnIndexOrThrow("address"));
        String firstChar = String.valueOf(name.charAt(0));

        if (color == 0) {
            if (generator != null) {
                try {
                    color = generator.getColor(name);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        TextDrawable drawable = TextDrawable.builder().buildRound(firstChar, color);
        holder.image.setImageDrawable(drawable);


    }

    public void swapCursor(Cursor cursor) {
        if (dataCursor == cursor) {
            return;
        }
        this.dataCursor = cursor;
        if (cursor != null) {
            this.notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return (dataCursor == null) ? 0 : dataCursor.getCount();
    }


    class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView message;
        private ImageView image;
        private TextView time;

        MyViewHolder(View itemView) {
            super(itemView);

            message = itemView.findViewById(R.id.message);
            image = itemView.findViewById(R.id.smsImage);
            time = itemView.findViewById(R.id.time);

        }

    }
}
