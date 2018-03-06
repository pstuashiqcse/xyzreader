package com.mcc.xyzreader.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mcc.xyzreader.R;
import com.mcc.xyzreader.data.ArticleLoader;
import com.mcc.xyzreader.model.ArticleModel;
import com.mcc.xyzreader.ui.ArticleListActivity;
import com.mcc.xyzreader.ui.DynamicHeightNetworkImageView;
import com.mcc.xyzreader.ui.ImageLoaderHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {

    private final ArrayList<ArticleModel> arrayList;
    private ItemClickListener itemClickListener;
    private Context context;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2,1,1);


    public ArticleAdapter(Context context, ArrayList<ArticleModel> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private DynamicHeightNetworkImageView thumbnailView;
        private TextView titleView;
        private TextView subtitleView;

        public ViewHolder(View view) {
            super(view);

            thumbnailView = (DynamicHeightNetworkImageView) view.findViewById(R.id.thumbnail);
            titleView = (TextView) view.findViewById(R.id.article_title);
            subtitleView = (TextView) view.findViewById(R.id.article_subtitle);

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(getAdapterPosition());
            }
        }
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_article, parent, false);
        return new ViewHolder(view);

    }

    private Date parsePublishedDate(int position) {
        try {
            String date = arrayList.get(position).getDate();
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            ex.printStackTrace();
            return new Date();
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.titleView.setText(arrayList.get(position).getTitle());
        Date publishedDate = parsePublishedDate(position);
        if (!publishedDate.before(START_OF_EPOCH.getTime())) {

            holder.subtitleView.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            publishedDate.getTime(),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + "<br/>" + " by "
                            + arrayList.get(position).getAuthor()));
        } else {
            holder.subtitleView.setText(Html.fromHtml(
                    outputFormat.format(publishedDate)
                            + "<br/>" + " by "
                            + arrayList.get(position).getAuthor()));
        }
        holder.thumbnailView.setImageUrl(
                arrayList.get(position).getThumbUrl(),
                ImageLoaderHelper.getInstance(context).getImageLoader());
        holder.thumbnailView.setAspectRatio(arrayList.get(position).getThumbAspectRatio());
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public interface ItemClickListener {
        void onItemClick(int position);
    }


}
