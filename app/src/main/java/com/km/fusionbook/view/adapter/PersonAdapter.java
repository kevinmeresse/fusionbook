package com.km.fusionbook.view.adapter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.km.fusionbook.R;
import com.km.fusionbook.interfaces.IDClickListener;
import com.km.fusionbook.model.Person;
import com.km.fusionbook.view.customviews.GlideCircleTransform;

/**
 * An adapter to show a person's details in a recyclerview
 */
public class PersonAdapter extends RealmRecyclerViewAdapter<Person> {

    private Context context;
    private IDClickListener itemClickListener;

    public PersonAdapter(Context context, IDClickListener itemClickListener) {
        this.context = context;
        this.itemClickListener = itemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_item_person, parent, false);
        return new PersonViewHolder(view);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        PersonViewHolder eventHolder = (PersonViewHolder) holder;
        final Person person = getItem(position);

        SpannableStringBuilder fullname = new SpannableStringBuilder();
        fullname.append(person.getFirstname()).append(" ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fullname.append(person.getLastname(), new StyleSpan(android.graphics.Typeface.BOLD), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            int start = fullname.length();
            fullname.append(person.getLastname());
            fullname.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, fullname.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        eventHolder.fullname.setText(fullname);

        if (!TextUtils.isEmpty(person.getPictureUrl())) {
            Glide.with(context)
                    .load(person.getPictureUrl())
                    .transform(new GlideCircleTransform(context))
                    .into(eventHolder.avatar);
            eventHolder.avatar.setVisibility(View.VISIBLE);
        } else {
            eventHolder.avatar.setVisibility(View.GONE);
        }
        eventHolder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null) {
                    itemClickListener.onClick(v, person.getId(), position);
                }
            }
        });
    }

    /* The inner RealmBaseAdapter
     * view count is applied here.
     *
     * getRealmAdapter is defined in RealmRecyclerViewAdapter.
     */
    @Override
    public int getItemCount() {
        if (getRealmAdapter() != null) {
            return getRealmAdapter().getCount();
        }
        return 0;
    }

    private class PersonViewHolder extends RecyclerView.ViewHolder {

        public View container;
        public TextView fullname;
        public ImageView avatar;

        public PersonViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.container);
            fullname = (TextView) view.findViewById(R.id.fullname);
            avatar = (ImageView) view.findViewById(R.id.avatar);
        }
    }

    public static class DividerItemDecoration extends RecyclerView.ItemDecoration {
        private Paint mPaint;

        public DividerItemDecoration(int color) {
            mPaint = new Paint(0);
            mPaint.setColor(color);
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();

            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);

                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + 1;

                RectF mBounds = new RectF(left, top, right, bottom);

                c.drawRect(mBounds, mPaint);
            }
        }
    }
}
