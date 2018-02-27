package com.example.pccs_0007.rxjavapagination;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by PCCS-0007 on 27-Feb-18.
 */

public class DisplayPhotosAdapter extends RecyclerView.Adapter<DisplayPhotosAdapter.MyViewHolder> {

    Context context;
    ArrayList<PhotoInsideModel> productModelArrayList;
    Gson gson;

    public DisplayPhotosAdapter(Context context, ArrayList<PhotoInsideModel> productModelArrayList) {

        this.context = context;
        this.productModelArrayList = productModelArrayList;
        gson = new Gson();
    }

    @Override
    public DisplayPhotosAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.photos_list, parent, false);

        return new DisplayPhotosAdapter.MyViewHolder(itemView);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView title,qty;

        public MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
            qty = view.findViewById(R.id.qty);
        }
    }


    @Override
    public void onBindViewHolder(final DisplayPhotosAdapter.MyViewHolder holder, final int position) {
        PhotoInsideModel photoInsideModel = productModelArrayList.get(position);
        holder.title.setText(photoInsideModel.original_title);
        holder.qty.setText(photoInsideModel.release_date);
    }

    @Override
    public int getItemCount() {
        return (productModelArrayList != null) ? productModelArrayList.size() : 0;
    }

    public void addItems(List<PhotoInsideModel> prod) {
        //productModelArrayList.clear();
        productModelArrayList.addAll(prod);
        notifyDataSetChanged();
    }
}