package com.example.zhenghangxia.testdemo.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.zhenghangxia.testdemo.R;
import com.example.zhenghangxia.testdemo.bean.Msg;

import java.util.List;

/**
 * Created by zhenghangxia on 17-7-4.
 */

public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.ViewHolder> {

    private final List<Msg> mList;

    public MsgAdapter(List<Msg> msgList) {
        this.mList = msgList;
    }

    @Override
    public MsgAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_msg,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MsgAdapter.ViewHolder holder, int position) {
        Msg msg = mList.get(position);
        if (msg.getType() == Msg.TYPE_RECEIVED) {
            holder.leftLayout.setVisibility(View.VISIBLE);
            holder.rightLayout.setVisibility(View.GONE);
            holder.leftMsg.setText(msg.getContent());
        } else if (msg.getType() == Msg.TYPE_SENT){
            holder.leftLayout.setVisibility(View.GONE);
            holder.rightLayout.setVisibility(View.VISIBLE);
            holder.rightMsg.setText(msg.getContent());
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout leftLayout;
        LinearLayout rightLayout;
        TextView leftMsg;
        TextView rightMsg;

        public ViewHolder(View view) {
            super(view);
            leftLayout = (LinearLayout) view.findViewById(R.id.ll_left);
            rightLayout = (LinearLayout) view.findViewById(R.id.ll_right);
            leftMsg = (TextView) view.findViewById(R.id.tv_left_msg);
            rightMsg = (TextView) view.findViewById(R.id.tv_right_msg);
        }
    }
}
