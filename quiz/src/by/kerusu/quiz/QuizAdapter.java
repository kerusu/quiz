package by.kerusu.quiz;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseFile;
import com.parse.ParseObject;
import com.squareup.picasso.Picasso;

public class QuizAdapter extends BaseAdapter {

    private List<ParseObject> pictures;
    private List<ParseObject> answers;
    private List<ParseObject> actors;
    private Context context;
    private LayoutInflater layoutInflater;
    private int itemHeight;

    private Map<String, String> pictureUrlByPictureId;
    private Map<String, String> actorNameByPictureId;

    public QuizAdapter(Activity activity, int screenWidth) {
        super();
        layoutInflater = LayoutInflater.from(activity);
        context = activity.getApplicationContext() == null ? activity : activity.getApplicationContext();
        itemHeight = (int) (1.33 * screenWidth);
    }

    @Override
    public int getCount() {
        return allDataSet() ? pictures.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return pictures.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.quiz_listview_item, parent, false);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.view = convertView;
            viewHolder.icon = (ImageView) convertView.findViewById(android.R.id.icon);
            viewHolder.name = (TextView) convertView.findViewById(R.id.username);
            viewHolder.pictureLayout = convertView.findViewById(R.id.pictureLayout);

            convertView.setTag(viewHolder);
        }

        final ViewHolder viewHolder = (ViewHolder) convertView.getTag();

        ViewGroup.LayoutParams lp = convertView.getLayoutParams();
        if (lp.height != itemHeight) {
            lp.height = itemHeight;
            convertView.setLayoutParams(lp);
        }

        ParseObject item = (ParseObject) getItem(position);
        String pictureId = item.getObjectId();
        String iconUrl = pictureUrlByPictureId.get(pictureId);

        if (iconUrl != null && !iconUrl.equals(viewHolder.iconUrl)) {
            viewHolder.iconUrl = iconUrl;
            Picasso.with(context).load(iconUrl).into(viewHolder.icon);
        }

        String username = actorNameByPictureId.get(pictureId);
        if (username != null) {
            AlphaAnimation alpha = new AlphaAnimation(0.3F, 0.3F);
            alpha.setDuration(0); // Make animation instant
            alpha.setFillAfter(true); // Tell it to persist after the
                                      // animation ends
            // And then on your layout
            viewHolder.pictureLayout.startAnimation(alpha);
            viewHolder.name.setText(username);
            viewHolder.name.setTextColor(context.getResources().getColor(R.color.username_color));
        } else {
            AlphaAnimation alpha = new AlphaAnimation(1F, 1F);
            alpha.setDuration(0); // Make animation instant
            alpha.setFillAfter(true); // Tell it to persist after the
                                      // animation ends
            // And then on your layout
            viewHolder.pictureLayout.startAnimation(alpha);
            viewHolder.name.setText(R.string.default_username);
            viewHolder.name.setTextColor(context.getResources().getColor(R.color.default_username_color));
        }

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public void setPictures(List<ParseObject> pictures) {
        this.pictures = pictures;
        updateAdapter();
    }

    public void setAnswers(List<ParseObject> answers) {
        this.answers = answers;
        updateAdapter();
    }

    public void setActors(List<ParseObject> actors) {
        this.actors = actors;
        updateAdapter();
    }

    private void updateAdapter() {
        if (!allDataSet()) {
            return;
        }
        rebuildMappings();
        notifyDataSetChanged();
    }

    private boolean allDataSet() {
        return actors != null && pictures != null && answers != null;
    }

    private void rebuildMappings() {
        if (!allDataSet()) {
            return;
        }

        pictureUrlByPictureId = new HashMap<String, String>();
        actorNameByPictureId = new HashMap<String, String>();

        for (ParseObject picture : pictures) {
            ParseFile file = (ParseFile) (picture.get("rawData"));
            pictureUrlByPictureId.put(picture.getObjectId(), file.getUrl());
        }

        for (ParseObject answer : answers) {
            String pictureId = answer.getString("pictureId");
            if (TextUtils.isEmpty(pictureId)) {
                continue;
            }
            String actorId = answer.getString("actorId");
            if (TextUtils.isEmpty(actorId)) {
                continue;
            }
            for (ParseObject actor : actors) {
                if (actor.getObjectId().equals(actorId)) {
                    String name = actor.getString("name");
                    actorNameByPictureId.put(pictureId, name);
                }
            }
        }
    }

    private static final class ViewHolder {
        String iconUrl;
        ImageView icon;
        TextView name;
        View view;
        View pictureLayout;
    }
}
