package com.example.connectones.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.connectones.Activities.ChatActivity;
import com.example.connectones.Models.Messages;
import com.example.connectones.Models.User;
import com.example.connectones.R;
import com.example.connectones.databinding.DeleteDialogBinding;

import com.example.connectones.databinding.ItemReceiveGroupBinding;

import com.example.connectones.databinding.ItemSentGroupBinding;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.ArrayList;

public class GroupMessagesAdapter extends RecyclerView.Adapter{

    Context context;
    ArrayList<Messages> messages;
    final int ITEM_SENT = 1;
    final int ITEM_RECEIVE = 2;


    FirebaseRemoteConfig remoteConfig;

    public GroupMessagesAdapter(ArrayList<Messages> messages, Context context){
        remoteConfig = FirebaseRemoteConfig.getInstance();
        this.context=context;
        this.messages=messages;

    }

    public GroupMessagesAdapter(ChatActivity context, ArrayList<Message> messages) {
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SENT){
            View view = LayoutInflater.from(context).inflate(R.layout.item_sent_group,parent,false);
            return new SentViewHolder(view);
        }else{
            View view = LayoutInflater.from(context).inflate(R.layout.item_receive_group,parent,false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Messages messages = this.messages.get(position);
        if(FirebaseAuth.getInstance().getUid().equals(messages.getSenderId())){
//        if(messages.get(position).getSenderId().equals(FirebaseAuth.getInstance().getUid())){
            return ITEM_SENT;
        }else{
            return ITEM_RECEIVE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Messages messages = this.messages.get(position);

        int reactions[] = new int[]{
                R.drawable.ic_fb_like,
                R.drawable.ic_fb_love,
                R.drawable.ic_fb_laugh,
                R.drawable.ic_fb_wow,
                R.drawable.ic_fb_sad,
                R.drawable.ic_fb_angry
        };

        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactions)
                .build();

        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {

            if (pos < 0)
                return false;

            if (holder.getClass() == SentViewHolder.class){
                SentViewHolder viewHolder = (SentViewHolder)holder;
                viewHolder.binding.feeling.setImageResource(reactions[pos]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            }else{
                ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
                viewHolder.binding.feeling.setImageResource(reactions[pos]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            }

            messages.setFeeling(pos);
            FirebaseDatabase.getInstance().getReference()
                    .child("public")
                    .child(messages.getMessageId()).setValue(messages);

            return true; // true is closing popup, false is requesting a new selection
        });

        if (holder.getClass() == SentViewHolder.class){
            SentViewHolder viewHolder = (SentViewHolder)holder;

            if (messages.getMessage().equals("photo")){
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.messages.setVisibility(View.GONE);
                Glide.with(context)
                        .load(messages.getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(viewHolder.binding.image);
            }

//            FirebaseDatabase.getInstance()
//                    .getReference()
//                    .child("users")
//                    .child(messages.getSenderId())
//                    .addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            if (snapshot.exists()){
//                                User user= snapshot.getValue(User.class);
//                                viewHolder.binding.name.setText(user.getName());
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });

            viewHolder.binding.messages.setText(messages.getMessage());

            if (messages.getFeeling() >= 0){
                viewHolder.binding.feeling.setImageResource(reactions[messages.getFeeling()]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            }else{
                viewHolder.binding.feeling.setVisibility(View.GONE);
            }

            viewHolder.binding.messages.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    boolean isFeelingsEnabled = remoteConfig.getBoolean("isFeelingsEnabled");
                    if(isFeelingsEnabled)
                        popup.onTouch(v, event);
                    else
                        Toast.makeText(context, "This feature is disabled temporarily.", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });

            viewHolder.binding.image.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    boolean isFeelingsEnabled = remoteConfig.getBoolean("isFeelingsEnabled");
                    if(isFeelingsEnabled)
                        popup.onTouch(v, event);
                    else
                        Toast.makeText(context, "This feature is disabled temporarily.", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });

            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    View view = LayoutInflater.from(context).inflate(R.layout.delete_dialog, null);
                    DeleteDialogBinding binding = DeleteDialogBinding.bind(view);
                    AlertDialog dialog = new AlertDialog.Builder(context)
                            .setTitle("Delete Message")
                            .setView(binding.getRoot())
                            .create();

                    if(remoteConfig.getBoolean("isEveryoneDeletionEnabled")) {
                        binding.everyone.setVisibility(View.VISIBLE);
                    } else {
                        binding.everyone.setVisibility(View.GONE);
                    }
                    binding.everyone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            messages.setMessage("This message is removed.");
                            messages.setFeeling(-1);
                            FirebaseDatabase.getInstance().getReference()
                                    .child("public")
                                    .child(messages.getMessageId()).setValue(messages);

                            FirebaseDatabase.getInstance().getReference()
                                    .child("public")
                                    .child(messages.getMessageId()).setValue(messages);
                            dialog.dismiss();
                        }
                    });

                    binding.delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FirebaseDatabase.getInstance().getReference()
                                    .child("public")
                                    .child(messages.getMessageId()).setValue(null);
                            dialog.dismiss();
                        }
                    });

                    binding.cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();

                    return false;
                }
            });




        }else{
            ReceiverViewHolder viewHolder = (ReceiverViewHolder)holder;

            if (messages.getMessage().equals("photo")){
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.messages.setVisibility(View.GONE);
                Glide.with(context)
                        .load(messages.getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(viewHolder.binding.image);
            }

            FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(messages.getSenderId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                User user= snapshot.getValue(User.class);
                                viewHolder.binding.name.setText("@" + user.getName());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            viewHolder.binding.messages.setText(messages.getMessage());

            if (messages.getFeeling() >= 0){
                viewHolder.binding.feeling.setImageResource(reactions[messages.getFeeling()]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            }else{
                viewHolder.binding.feeling.setVisibility(View.GONE);
            }

            viewHolder.binding.messages.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    boolean isFeelingsEnabled = remoteConfig.getBoolean("isFeelingsEnabled");
                    if(isFeelingsEnabled)
                        popup.onTouch(v, event);
                    else
                        Toast.makeText(context, "This feature is disabled temporarily.", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });

            viewHolder.binding.image.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    boolean isFeelingsEnabled = remoteConfig.getBoolean("isFeelingsEnabled");
                    if(isFeelingsEnabled)
                        popup.onTouch(v, event);
                    else
                        Toast.makeText(context, "This feature is disabled temporarily.", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });

            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    View view = LayoutInflater.from(context).inflate(R.layout.delete_dialog, null);
                    DeleteDialogBinding binding = DeleteDialogBinding.bind(view);
                    AlertDialog dialog = new AlertDialog.Builder(context)
                            .setTitle("Delete Message")
                            .setView(binding.getRoot())
                            .create();

                    if(remoteConfig.getBoolean("isEveryoneDeletionEnabled")) {
                        binding.everyone.setVisibility(View.VISIBLE);
                    } else {
                        binding.everyone.setVisibility(View.GONE);
                    }
                    binding.everyone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            messages.setMessage("This message is removed.");
                            messages.setFeeling(-1);
                            FirebaseDatabase.getInstance().getReference()
                                    .child("public")
                                    .child(messages.getMessageId()).setValue(messages);

                            FirebaseDatabase.getInstance().getReference()
                                    .child("public")
                                    .child(messages.getMessageId()).setValue(messages);
                            dialog.dismiss();
                        }
                    });

                    binding.delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FirebaseDatabase.getInstance().getReference()
                                    .child("public")
                                    .child(messages.getMessageId()).setValue(null);
                            dialog.dismiss();
                        }
                    });

                    binding.cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();

                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class SentViewHolder extends RecyclerView.ViewHolder{
        ItemSentGroupBinding binding;
        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSentGroupBinding.bind(itemView);
        }
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder{
        ItemReceiveGroupBinding binding;
        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemReceiveGroupBinding.bind(itemView);
        }
    }
}
