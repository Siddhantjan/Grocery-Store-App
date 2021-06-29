package com.business.grocerystoreapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.business.grocerystoreapp.R;
import com.business.grocerystoreapp.activities.ShopDetailsActivity;
import com.business.grocerystoreapp.modal.ModalShop;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterShop extends RecyclerView.Adapter<AdapterShop.HolderShop> {

    private Context context;
    public ArrayList<ModalShop> shopsList;

    public AdapterShop(Context context, ArrayList<ModalShop> shopsList) {
        this.context = context;
        this.shopsList = shopsList;
    }

    @NonNull
    @Override
    public HolderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ///inflate layout row_shop.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_shop,parent,false);
        return new HolderShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderShop holder, int position) {
        //getData
        ModalShop modalShop = shopsList.get(position);
        String a = modalShop.getAccountType();
        String address = modalShop.getAddress();
        String city = modalShop.getCity();
        String country = modalShop.getCountry();
        String deliveryFees = modalShop.getDeliveryFee();
        String email = modalShop.getEmail();
        String latitude = modalShop.getLatitude();
        String longitude = modalShop.getLongitude();
        String online = modalShop.getOnline();
        String phoneNumber = modalShop.getPhoneNumber();
        String shopOpen = modalShop.getShopOpen();
       final String  shopUid = modalShop.getUid();
        String  timestamp = modalShop.getTimestamp();
        String  state = modalShop.getState();
        String  shopProfileImage = modalShop.getProfileImage();
        String  shopName = modalShop.getShopName();

        //setData
        holder.shopNameTv.setText(shopName);
        holder.shop_phoneTv.setText(phoneNumber);
        holder.shop_address.setText(address);
        //check if online
        if (online.equals("true")){
            //shop owner is online
            holder.onlineIv.setVisibility(View.VISIBLE);
        }
        else {
            holder.onlineIv.setVisibility(View.GONE);
        }
        //  check shop is open or  not
        if (shopOpen.equals("true")){
            //shop open
            holder.shopClosedTv.setVisibility(View.GONE);
        }
        else {
            //shop is closed
            holder.shopClosedTv.setVisibility(View.VISIBLE);
        }

        try {
            Glide.with(context).load(shopProfileImage).placeholder(R.drawable.ic_store_gray).into(holder.shopIv);
        }
        catch (Exception e){
            holder.shopIv.setImageResource(R.drawable.ic_store_gray);
            Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // shop details
                Intent intent = new Intent(context, ShopDetailsActivity.class);
                intent.putExtra("shopUid",shopUid);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return shopsList.size();
    }


    //View Holder
    class HolderShop extends RecyclerView.ViewHolder{
        //ui views  of row shop.xml
        private CircleImageView shopIv;
        private ImageView onlineIv;
        private TextView shopClosedTv;
        private TextView shopNameTv,shop_phoneTv,shop_address;
        private RatingBar shop_rating_bar;

        public HolderShop(@NonNull View itemView) {
            super(itemView);

            //init uid Views
            shopIv = itemView.findViewById(R.id.shopIv);
            onlineIv = itemView.findViewById(R.id.onlineIv);
            shopClosedTv = itemView.findViewById(R.id.shopClosedTv);
            shopNameTv = itemView.findViewById(R.id.shopNameTv);
            shop_phoneTv = itemView.findViewById(R.id.shop_phoneTv);
            shop_address = itemView.findViewById(R.id.shop_address);
            shop_rating_bar = itemView.findViewById(R.id.shop_rating_bar);


        }

    }
}
