package com.wasim.messegeingApp.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.wasim.messegeingApp.R;
import com.wasim.messegeingApp.receivers.SmsReceiver;
import com.wasim.messegeingApp.utils.AESUtils;
import com.wasim.messegeingApp.utils.ColorGeneratorModified;
import com.wasim.messegeingApp.utils.Helpers;
import com.wasim.messegeingApp.utils.SMS;

import java.util.List;


public class AllConversationAdapter extends RecyclerView.Adapter<AllConversationAdapter.MyHolder> {

    private Context context;
    private List<SMS> data;
    private ItemCLickListener itemClickListener;
    private ColorGeneratorModified generator = ColorGeneratorModified.MATERIAL;
    String decMsg = "";


    public AllConversationAdapter(Context context, List<SMS> data) {
        this.context = context;
        this.data = data;

    }

    @NonNull
    @Override
    public AllConversationAdapter.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.single_sms_small_layout, parent, false);
        return new MyHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onBindViewHolder(@NonNull AllConversationAdapter.MyHolder holder, int position) {

        SMS sms = data.get(position);

        holder.senderContact.setText(sms.getAddress());
        String myMessage = sms.getMsg();
        Log.e("ok", "Message in All converssation adapter: " + myMessage);

        // decrypt here plz


        try {
            decMsg = AESUtils.decodeFile(SmsReceiver.returnKey(myMessage),SmsReceiver.returnMessege(myMessage));
        } catch (Exception e) {
            e.printStackTrace();
            decMsg = myMessage;
            Log.e("ok", "Erro msg all conversation adapter ;" + e.getMessage());
        }
        holder.message.setText(decMsg);

        int color = 0;
        try {
            color = generator.getColor(sms.getAddress());

            String firstChar = String.valueOf(sms.getAddress().charAt(0));

            TextDrawable drawable = TextDrawable.builder().buildRound(firstChar, color);
            holder.senderImage.setImageDrawable(drawable);

            sms.setColor(color);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ok", "error in reat sate" + e.getMessage());
        }

        if (sms.getReadState().equals("0")) {
            holder.senderContact.setTypeface(holder.senderContact.getTypeface(), Typeface.BOLD);
            holder.message.setTypeface(holder.message.getTypeface(), Typeface.BOLD);
            holder.message.setTextColor(ContextCompat.getColor(context, R.color.black));
            holder.time.setTypeface(holder.time.getTypeface(), Typeface.BOLD);
            holder.time.setTextColor(ContextCompat.getColor(context, R.color.black));
        } else {
            holder.senderContact.setTypeface(null, Typeface.NORMAL);
            holder.message.setTypeface(null, Typeface.NORMAL);
            holder.time.setTypeface(null, Typeface.NORMAL);

        }

        holder.time.setText(Helpers.getDate(sms.getTime()));

    }


    @Override
    public int getItemCount() {
        return (data == null) ? 0 : data.size();
    }


    public void setItemClickListener(ItemCLickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private ImageView senderImage;
        private TextView senderContact;
        private TextView message;
        private TextView time;
        private RelativeLayout mainLayout;

        MyHolder(View itemView) {
            super(itemView);
            senderImage = itemView.findViewById(R.id.smsImage);
            senderContact = itemView.findViewById(R.id.smsSender);
            message = itemView.findViewById(R.id.smsContent);
            time = itemView.findViewById(R.id.time);
            mainLayout = itemView.findViewById(R.id.small_layout_main);

            mainLayout.setOnClickListener(this);
            mainLayout.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) {


                data.get(getAdapterPosition()).setReadState("1");
                notifyItemChanged(getAdapterPosition());


                itemClickListener.itemClicked(data.get(getAdapterPosition()).getColor(),
                        senderContact.getText().toString(),
                        data.get(getAdapterPosition()).getId(),
                        data.get(getAdapterPosition()).getReadState());
            }

        }

        @Override
        public boolean onLongClick(View view) {

            String[] items = {"Delete"};

            ArrayAdapter<String> adapter = new ArrayAdapter<>(context
                    , android.R.layout.simple_list_item_1, android.R.id.text1, items);

            new MaterialAlertDialogBuilder(context)
                    .setAdapter(adapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            deleteDialog();
                        }
                    })
                    .show();

            return true;
        }

        private void deleteDialog() {

            MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(context);
            alert.setMessage("Are you sure you want to delete this message?");
            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    deleteSMS(data.get(getAdapterPosition()).getId(), getAdapterPosition());
                }

            });
            alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                }
            });
            alert.create();
            alert.show();
        }
    }

    private void deleteSMS(long messageId, int position) {

        long affected = context.getContentResolver().delete(
                Uri.parse("content://sms/" + messageId), null, null);

        if (affected != 0) {
            data.remove(position);
            notifyItemRemoved(position);
        }


    }
}
