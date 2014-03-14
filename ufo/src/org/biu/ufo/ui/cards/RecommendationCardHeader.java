/*
 * ******************************************************************************
 *   Copyright (c) 2013-2014 Gabriele Mariotti.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  *****************************************************************************
 */

package org.biu.ufo.ui.cards;

import it.gmariotti.cardslib.library.internal.CardHeader;

import org.biu.ufo.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class RecommendationCardHeader extends CardHeader {

    protected String mPrice;
    protected int mPriceCurrencyResId;

    public RecommendationCardHeader(Context context) {
        this(context,R.layout.recommendation_header_layout);
    }


    public void setPrice(String fuelCost){
        this.mPrice = fuelCost;
    }

    public void setPriceCurrencyResId(int currencyResId){
        this.mPriceCurrencyResId = currencyResId;
    }

    public RecommendationCardHeader(Context context, int innerLayout) {
        super(context);
        mInnerLayout= innerLayout;
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent,View view){

        TextView station_name = (TextView)view.findViewById(R.id.rec_station_name);
        station_name.setText(mTitle);

        TextView fuel_cost = (TextView)view.findViewById(R.id.rec_station_price);
        fuel_cost.setText(String.valueOf(mPrice));

        TextView cost_currency = (TextView)view.findViewById(R.id.rec_station_price_currency);
        cost_currency.setText(mPriceCurrencyResId);
    }

 }
