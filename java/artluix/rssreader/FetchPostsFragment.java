package artluix.rssreader;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class FetchPostsFragment extends Fragment implements RefreshableInterface, PostDataDelegate {
    private ArrayList<PostData> postList = new ArrayList<PostData>();
    private ArrayList<String> guidList = new ArrayList<String>();
    private PostAdapter postItemAdapter;
    private RefreshableListView postListView;
    private boolean isLoading;
    private boolean isRefreshLoading;
    private String url;

    public static final String ARG_POSTS_SRC = "posts_src";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.rss_reader, container, false);
        postListView = (RefreshableListView) rootView.findViewById(R.id.refreshable_post_list);
        postItemAdapter = new PostAdapter(getActivity(), R.layout.post, postList);
        postListView.setAdapter(postItemAdapter);
        postListView.setOnItemClickListener(new PostItemClicksListener());
        postListView.setOnItemLongClickListener(new PostItemClicksListener());
        url = getArguments().getString(ARG_POSTS_SRC);

        postListView.setOnRefresh(this);
        postListView.onRefreshStart();
        return rootView;
    }

    @Override
    public void onDataLoadComplete(boolean b) {
        if (b) {
            postItemAdapter.notifyDataSetChanged();
        }
        if (isRefreshLoading) {
            postListView.onRefreshComplete();
        }
        else {
            postListView.onLoadingMoreComplete();
        }
        isLoading = false;
    }

    @Override
    public void startFresh() {
        if (!isLoading) {
            isRefreshLoading = true;
            isLoading = true;
            new RssDataLoader().execute(url);
            return;
        }
        postListView.onRefreshComplete();
    }


    private class PostItemClicksListener implements ListView.OnItemClickListener, ListView.OnItemLongClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Fragment webViewFragment = new PostWebFragment();
            Bundle args = new Bundle();
            args.putString(PostWebFragment.ARG_POST_URL, postList.get(position).getLink());
            webViewFragment.setArguments(args);

            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content, webViewFragment).addToBackStack(null).commit();
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            PostsDBHelper postsDBHelper = new PostsDBHelper(getActivity());
            postsDBHelper.addPost(postList.get(position));
            Toast.makeText(getActivity(), "Post was added!", Toast.LENGTH_SHORT).show();
            postItemAdapter.notifyDataSetChanged();
            return true;
        }
    }

    private enum RSSXMLTag {
        TITLE, DATE, LINK, CONTENT, GUID, IGNORETAG
    }

    private class RssDataLoader extends AsyncTask<String, Integer, ArrayList<PostData>> {
        private RSSXMLTag currentTag;

        @Override
        protected ArrayList doInBackground(String... params) {
            String urlStr = params[0];
            InputStream is;
            ArrayList<PostData> resultDataList = new ArrayList<>();

            try {
                URL url = new URL(urlStr);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(10000);
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.connect();
                is = connection.getInputStream();

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(is, null);

                int eventType = xpp.getEventType();
                PostData newPostData = null;
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.ENGLISH);
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("item")) {
                            newPostData = new PostData();
                            currentTag = RSSXMLTag.IGNORETAG;
                        }
                        else if (xpp.getName().equals("title")) {
                            currentTag = RSSXMLTag.TITLE;
                        }
                        else if (xpp.getName().equals("link")) {
                            currentTag = RSSXMLTag.LINK;
                        }
                        else if (xpp.getName().equals("pubDate")) {
                            currentTag = RSSXMLTag.DATE;
                        }
                    }
                    else if (eventType == XmlPullParser.END_TAG) {
                        if (xpp.getName().equals("item")) {
                            Date newPostDate = dateFormat.parse(newPostData.getDate());
                            newPostData.setDate(dateFormat.format(newPostDate));
                            resultDataList.add(newPostData);
                        }
                        else {
                            currentTag = RSSXMLTag.IGNORETAG;
                        }
                    }
                    else if (eventType == XmlPullParser.TEXT) {
                        String content = xpp.getText();
                        content = content.trim();
                        if (newPostData != null) {
                            switch (currentTag) {
                                case TITLE:
                                    if (content.length() != 0)
                                        newPostData.setTitle(content);
                                    break;
                                case LINK:
                                    if (content.length() != 0)
                                        newPostData.setLink(content);
                                    break;
                                case DATE:
                                    if (content.length() != 0)
                                        newPostData.setDate(content);
                                    break;
                                default:
                                    break;
                            }
                        }
                    }

                    eventType = xpp.next();
                }

            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (XmlPullParserException e) {
                e.printStackTrace();
            }
            catch (ParseException e) {
                e.printStackTrace();
            }

            return resultDataList;
        }

        @Override
        protected void onPostExecute(ArrayList<PostData> resultList) {
            boolean b = false;
            int index = 0;
            for (int i = 0; i < resultList.size(); i++) {
                if (!guidList.contains(resultList.get(i).getLink())) {
                    b = true;
                    guidList.add(resultList.get(i).getLink());
                    if (isRefreshLoading) {
                        postList.add(index, resultList.get(i));
                        ++index;
                    }
                    else {
                        postList.add(resultList.get(i));
                    }
                }
            }
            onDataLoadComplete(b);
            super.onPostExecute(resultList);
        }
    }
}
