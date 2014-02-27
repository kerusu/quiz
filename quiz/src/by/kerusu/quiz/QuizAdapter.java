package by.kerusu.quiz;

import java.util.ArrayList;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class QuizAdapter extends BaseAdapter {

    public enum FilterMode {
        None, Answered, NotAnswered
    }

    private List<ParseObject> filteredPictures;
    private List<ParseObject> pictures;
    private List<ParseObject> answers;
    private List<ParseObject> actors;
    private Context context;
    private LayoutInflater layoutInflater;
    private int itemHeight;

    private Map<String, String> pictureUrlByPictureId = new HashMap<String, String>();
    private Map<String, String> actorNameByPictureId = new HashMap<String, String>();

    private FilterMode filterMode = FilterMode.None;

    public QuizAdapter(Activity activity, int screenWidth) {
        super();
        layoutInflater = LayoutInflater.from(activity);
        context = activity.getApplicationContext() == null ? activity : activity.getApplicationContext();
        itemHeight = (int) (1.33 * screenWidth);
    }

    @Override
    public int getCount() {
        return allDataSet() ? filteredPictures.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return filteredPictures.get(position);
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
            viewHolder.icon = (ImageView) convertView.findViewById(android.R.id.icon);
            viewHolder.name = (TextView) convertView.findViewById(R.id.username);
            viewHolder.pictureLayout = convertView.findViewById(R.id.pictureLayout);
            viewHolder.progressBar = convertView.findViewById(R.id.progressBar);

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
            viewHolder.progressBar.setVisibility(View.VISIBLE);
            viewHolder.iconUrl = iconUrl;
            Picasso.with(context).load(iconUrl).into(viewHolder.icon, new Callback() {
                @Override
                public void onSuccess() {
                    viewHolder.progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onError() {
                }
            });
        } else {
            viewHolder.progressBar.setVisibility(View.GONE);
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
            AlphaAnimation alpha = new AlphaAnimation(0.87F, 0.87F);
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
        applyFilterMode(filterMode);
        notifyDataSetChanged();
    }

    public void applyFilterMode(FilterMode filterMode) {
        if (!allDataSet()) {
            return;
        }
        filteredPictures = new ArrayList<ParseObject>();
        for (ParseObject picture : pictures) {
            switch (filterMode) {
            case None:
                filteredPictures.add(picture);
                break;
            case Answered:
                if (actorNameByPictureId.containsKey(picture.getObjectId())) {
                    filteredPictures.add(picture);
                }
                break;
            case NotAnswered:
                if (!actorNameByPictureId.containsKey(picture.getObjectId())) {
                    filteredPictures.add(picture);
                }
                break;
            default:
                break;
            }
        }

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
        View pictureLayout;
        View progressBar;
    }
}
