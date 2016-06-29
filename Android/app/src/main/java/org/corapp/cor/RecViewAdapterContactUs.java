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
 * Filename: RecViewAdapterContactUs.java
 * Description: Create RecyclerView for cards to be placed in.
 *
 * Changes:             Author  |   Date    |   Description
 *                      J.Jones     7-Nov-15    Create file, added core functionality.
 *                      J.Jones     9-Nov-15    Added information for Contact Info card.
 *                      J.Jones     10-Nov-15   Added information for Email Contact card.
 *                                              Added Map card.
 *
 */

package org.corapp.cor;

import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class RecViewAdapterContactUs extends RecyclerView.Adapter<RecViewAdapterContactUs.ContactUsViewHolder> {

    public static class ContactUsViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback {
        TextView heading;
        TextView contactPhone;
        TextView contactFax;
        TextView contactEmail;
        TextView name;
        TextView addr;
        Context con, mContext;
        LinearLayout lL;
        MapView mapView;
        GoogleMap gMap;
        final LatLng COR = new LatLng(33.633356, -117.737975);

        ContactUsViewHolder(Context context, View itemView) {
            super(itemView);
            mContext = context;
            con = context;
            heading = (TextView)itemView.findViewById(R.id.heading_text);
            contactPhone = (TextView)itemView.findViewById(R.id.phone_number);
            contactFax = (TextView)itemView.findViewById(R.id.fax_number);
            contactEmail = (TextView)itemView.findViewById(R.id.church_email);
            mapView = (MapView)itemView.findViewById(R.id.mapView);
            name = (TextView)itemView.findViewById(R.id.place_name);
            addr = (TextView)itemView.findViewById(R.id.address);
            lL = (LinearLayout)itemView.findViewById(R.id.linear_layout);

            if(mapView != null) {
                mapView.onCreate(null);
                mapView.onResume();
                mapView.getMapAsync(this);
            }
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            gMap = googleMap;
            gMap.addMarker(new MarkerOptions().position(COR));
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(COR, 16f);
            gMap.moveCamera(cameraUpdate);
        }
    }

    List<ContactInfo> contact;
    List<EmailContact> email;
    List<Address> address;

    RecViewAdapterContactUs(List<ContactInfo> contact, List<EmailContact> email, List<Address> address){
        this.contact = contact;
        this.email = email;
        this.address = address;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public ContactUsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.contact_card_view, viewGroup, false);
        ContactUsViewHolder contactViewHolder = new ContactUsViewHolder(viewGroup.getContext(), view);
        return contactViewHolder;
    }

    @Override
    public void onBindViewHolder(ContactUsViewHolder conViewHolder, int i) {
        conViewHolder.heading.setText("Contact Information");
        conViewHolder.contactPhone.setText("Church Office: " + contact.get(0).phoneNum);
        conViewHolder.contactFax.setText("Fax: " + contact.get(0).faxNum);
        conViewHolder.contactEmail.setText("Email: " + contact.get(0).email);

        conViewHolder.name.setText(address.get(0).name);
        conViewHolder.addr.setText(address.get(0).street + '\n' + address.get(0).cityZip);
        //new mapReadyTask().execute(conViewHolder);
        //GoogleMap gMap = conViewHolder.gMap;
        if(conViewHolder.gMap != null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(conViewHolder.COR, 16f);
            conViewHolder.gMap.moveCamera(cameraUpdate);
        }
        //GoogleMap gMap = MainActivity.gMap;


        /* Instead of creating multiple cards like in the ministries tab,
         * iterate through the list, setting each view and adding it to the layout
         */
        for(int j = 0; j < email.size(); j++) {
            TextView name = new TextView(conViewHolder.con);
            name.setText(email.get(j).name);
            name.setTypeface(null, Typeface.BOLD);
            conViewHolder.lL.addView(name);
            TextView title = new TextView(conViewHolder.con);
            title.setText("Title: " + email.get(j).title);
            title.setTypeface(null, Typeface.BOLD_ITALIC);
            conViewHolder.lL.addView(title);
            TextView emailAddr = new TextView(conViewHolder.con);
            emailAddr.setAutoLinkMask(Linkify.EMAIL_ADDRESSES);
            emailAddr.setText("Email: " + email.get(j).email);
            emailAddr.setTypeface(null, Typeface.BOLD);
            conViewHolder.lL.addView(emailAddr);
            TextView space = new TextView(conViewHolder.con);
            conViewHolder.lL.addView(space);
        }
    }

    @Override
    public void onViewRecycled(ContactUsViewHolder holder) {
        // Cleanup MapView here
        if (holder.gMap != null) {
            holder.gMap.clear();
            holder.gMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        }
    }

    @Override
    public int getItemCount() {
        return contact.size();
    }

    private class mapReadyTask extends AsyncTask<Object, Void, Void> {
        ContactUsViewHolder conViewHolder;
        @Override
        protected Void doInBackground(Object... params) {

            conViewHolder = (ContactUsViewHolder)params[0];
            GoogleMap gMap = conViewHolder.gMap;
            if(gMap != null) {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(conViewHolder.COR, 16f);
                gMap.moveCamera(cameraUpdate);
            }
            return null;
        }
    }
}
