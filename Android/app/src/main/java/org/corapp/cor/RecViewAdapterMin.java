/*
 * Copyright (C) 2015 COR Church in Irvine, CA
 *
 * Licensed under the COR License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Filename: RecViewAdapterMin.java
 * Description: Create RecyclerView for cards to be placed in.
 *
 * Changes:             Author  |   Date    |   Description
 *                      J.Jones     3-Nov-15    Create file, added core functionality.
 *
 */

package org.corapp.cor;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class RecViewAdapterMin extends RecyclerView.Adapter<RecViewAdapterMin.MinistryViewHolder> {

    public static class MinistryViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView ministryName;
        TextView ministryLeader;
        TextView ministryContact;
        TextView ministryDesc;

        MinistryViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.min_card_view);
            ministryName = (TextView)itemView.findViewById(R.id.ministry_name);
            ministryLeader = (TextView)itemView.findViewById(R.id.ministry_leader);
            ministryContact = (TextView)itemView.findViewById(R.id.ministry_contact);
            ministryDesc = (TextView)itemView.findViewById(R.id.ministry_desc);
        }
    }

    List<Ministry> ministries;

    RecViewAdapterMin(List<Ministry> ministries){
        this.ministries = ministries;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
    // Update method desc. from webpage
    @Override
    public MinistryViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.min_card_view, viewGroup, false);
        MinistryViewHolder minViewHolder = new MinistryViewHolder(view);
        return minViewHolder;
    }

    @Override
    public void onBindViewHolder(MinistryViewHolder minViewHolder, int i) {
        minViewHolder.ministryName.setText(ministries.get(i).name);
        minViewHolder.ministryLeader.setText("Leader: " + ministries.get(i).leader);
        minViewHolder.ministryContact.setText("Contact: " + ministries.get(i).contact);
        minViewHolder.ministryDesc.setText("Description: " + ministries.get(i).description);
    }

    @Override
    public int getItemCount() {
        return ministries.size();
    }
}
