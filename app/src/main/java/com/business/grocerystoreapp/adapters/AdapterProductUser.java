package com.business.grocerystoreapp.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.business.grocerystoreapp.FilterProductUser;
import com.business.grocerystoreapp.R;
import com.business.grocerystoreapp.modal.ModalProduct;

import java.util.ArrayList;

public class AdapterProductUser extends RecyclerView.Adapter<AdapterProductUser.HolderProductUser> implements Filterable {
    private Context context;
    public ArrayList<ModalProduct> productList,filterList;
    private FilterProductUser filter;


    public AdapterProductUser(Context context, ArrayList<ModalProduct> productList) {
        this.context = context;
        this.productList = productList;
        this.filterList = productList;
    }

    @NonNull
    @Override
    public HolderProductUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_product_user,parent,false);
        return new HolderProductUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductUser holder, int position) {
        //get Data
        ModalProduct productModel = productList.get(position);
        String id = productModel.getProductId();
        String uid = productModel.getUid();
        String discountAvailable = productModel.getDiscountAvailable();
        String discountNote = productModel.getDiscountNote();
        String discountPrice = productModel.getDiscountPrice();
        String productCategory = productModel.getProductCategory();
        String productIcon = productModel.getProductIcon();
        String title = productModel.getProductTitle();
        String description = productModel.getProductDescription();
        String productQuantity = productModel.getProductQuantity();
        String timestamp = productModel.getTimestamp();
        String actualPrice = productModel.getProductPrice();

        //set data
        //
        holder.discountNote_Tv.setText(discountNote);
        holder.title_Tv.setText(title);
        holder.description_tv.setText(description);
        holder.priceTv_Item.setText("₹"+actualPrice);
        holder.discountPrice_Tv.setText("₹"+discountPrice);

        if (discountAvailable.equals("true")){
            //product is on discount
            holder.priceTv_Item.setText(actualPrice);
            holder.discountPrice_Tv.setVisibility(View.VISIBLE);
            holder.discountNote_Tv.setVisibility(View.VISIBLE);
            holder.priceTv_Item.setPaintFlags(holder.priceTv_Item.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else {
            //product is not on discount
            holder.discountPrice_Tv.setVisibility(View.GONE);
            holder.discountNote_Tv.setVisibility(View.GONE);
            holder.priceTv_Item.setText(actualPrice);
            holder.priceTv_Item.setPaintFlags(0);

        }
        try {
            Glide.with(context).load(productIcon).into(holder.product_IconIv);

        }
        catch (Exception e){
            holder.product_IconIv.setImageResource(R.drawable.ic_add_shopping_primary);
        }
        holder.addToCartTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // add product into cart

            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // show product details

            }
        });

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterProductUser(this,filterList);
        }
        return filter;
    }

    class HolderProductUser extends RecyclerView.ViewHolder{
        //uid views
         private TextView discountNote_Tv,title_Tv, description_tv,addToCartTv, discountPrice_Tv, priceTv_Item;
         private ImageView product_IconIv;
        public HolderProductUser(@NonNull View itemView) {
            super(itemView);
            discountNote_Tv = itemView.findViewById(R.id.discountNote_Tv);
            title_Tv = itemView.findViewById(R.id.title_Tv);
            description_tv = itemView.findViewById(R.id.description_tv);
            addToCartTv = itemView.findViewById(R.id.addToCartTv);
            discountPrice_Tv = itemView.findViewById(R.id.discountPrice_Tv);
            priceTv_Item = itemView.findViewById(R.id.priceTv_Item);
            product_IconIv = itemView.findViewById(R.id.product_IconIv);

        }


    }
}
