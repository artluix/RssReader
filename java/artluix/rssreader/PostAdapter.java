package artluix.rssreader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


public class PostAdapter extends ArrayAdapter<PostData> {
    private LayoutInflater postInflater;
    private ArrayList<PostData> postList;

    public PostAdapter(Context context, int resource, ArrayList<PostData> objects) {
        super(context, resource, objects);

        postInflater = LayoutInflater.from(context);
        postList = objects;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = postInflater.inflate(R.layout.post, null);

            viewHolder = new ViewHolder();
            viewHolder.postThumbView = (ImageView) convertView.findViewById(R.id.post_thumb);
            viewHolder.postTitleView = (TextView) convertView.findViewById(R.id.post_title);
            viewHolder.postDateView = (TextView) convertView.findViewById(R.id.post_date);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (postList.get(position).getLink() == null) {
            viewHolder.postThumbView.setImageResource(R.drawable.ic_rss_news);
        }

        viewHolder.postTitleView.setText(postList.get(position).getTitle());
        viewHolder.postDateView.setText(postList.get(position).getDate());

        return convertView;
    }

    private static class ViewHolder {
        TextView postTitleView;
        TextView postDateView;
        ImageView postThumbView;
    }
}
