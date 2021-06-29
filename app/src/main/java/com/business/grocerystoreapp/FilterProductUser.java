package com.business.grocerystoreapp;

import android.widget.Filter;

import com.business.grocerystoreapp.adapters.AdapterProductUser;
import com.business.grocerystoreapp.modal.ModalProduct;

import java.util.ArrayList;

public class FilterProductUser extends Filter {
    private AdapterProductUser adapter;
    private ArrayList<ModalProduct> filterList;

    public FilterProductUser(AdapterProductUser adapter, ArrayList<ModalProduct> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //validate data for search query
        if (constraint != null && constraint.length()>0){
            //search filed not empty, searching something, perform search

            //change to upper case, to make case insensitive
            constraint = constraint.toString().toUpperCase();
            //store our  filltered list
            ArrayList<ModalProduct> filteredModals = new ArrayList<>();
            for (int i =0;i<filterList.size();i++){
                //check, search by title and category
                if (filterList.get(i).getProductTitle().toUpperCase().contains(constraint) ||
                        filterList.get(i).getProductCategory().toUpperCase().contains(constraint) ){
                    //add Filtered data to list
                    filteredModals.add(filterList.get(i));
                }
            }
            results.count = filteredModals.size();
            results.values = filteredModals;

        }
        else {
            //search filed empty, not searching , return original/all/complete list

            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.productList = (ArrayList<ModalProduct>) results.values;
        //refresh adapter
        adapter.notifyDataSetChanged();
    }
}
