package artluix.rssreader;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;


public class FavoritePostsFragment extends Fragment {
    private ArrayList<PostData> postList = new ArrayList<PostData>();
    private PostAdapter postItemAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.saved_reader, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.post_list);
        postItemAdapter = new PostAdapter(getActivity(), R.layout.post, postList);
        listView.setAdapter(postItemAdapter);

        PostItemClicksListener postItemClicksListener = new PostItemClicksListener();
        listView.setOnItemClickListener(postItemClicksListener);
        listView.setOnTouchListener(postItemClicksListener);

        showSavedPosts();
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        showSavedPosts();
    }

    private void showSavedPosts() {
        postList.clear();
        PostsDBHelper postsDBHelper = new PostsDBHelper(getActivity());
        postList.addAll(postsDBHelper.getAllPosts());
        postItemAdapter.notifyDataSetChanged();
    }

    private void removeAllPosts() {
        PostsDBHelper postsDBHelper = new PostsDBHelper(getActivity());
        postsDBHelper.deleteAllPosts();
        Toast.makeText(getActivity(), "All posts were removed!", Toast.LENGTH_SHORT).show();
        postList.clear();
        postItemAdapter.notifyDataSetChanged();
    }

    private class PostItemClicksListener implements ListView.OnItemClickListener, ListView.OnTouchListener {
        private static final int MIN_DISTANCE_X = 100;
        private static final int MAX_DISTANCE_Y = 40;
        // for 1 finger
        private float downX, downY, upX, upY;
        // for 2 finger
        private float downX_1, downY_1, upX_1, upY_1,
                downX_2, downY_2, upX_2, upY_2;
        // for 1 finger //gives as chance to separate click from touch
        private boolean swipeDetected = false;
        // for 2 fingers
        private boolean swipeMode = false;

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (swipeDetected) {
                PostsDBHelper postsDBHelper = new PostsDBHelper(getActivity());
                postsDBHelper.deletePost(postList.get(position));
                Toast.makeText(getActivity(), "Post was removed!", Toast.LENGTH_SHORT).show();
                postList.remove(position);
                postItemAdapter.notifyDataSetChanged();
                swipeDetected = false;
            }
            else {
                Fragment webViewFragment = new PostWebFragment();
                Bundle args = new Bundle();
                args.putString(PostWebFragment.ARG_POST_URL, postList.get(position).getLink());
                webViewFragment.setArguments(args);

                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content, webViewFragment).addToBackStack(null).commit();
            }
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int pointerCount = event.getPointerCount();

            if (pointerCount == 1) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = event.getX();
                        downY = event.getY();
                        swipeDetected = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        upX = event.getX();
                        upY = event.getY();

                        float deltaX = downX - upX;
                        float deltaY = downY - upY;

                        if ((Math.abs(deltaX) > MIN_DISTANCE_X) && (Math.abs(deltaY) < MAX_DISTANCE_Y)) {
                            swipeDetected = true;
                            return true;
                        }
                        break;
                    default:
                        return false;
                }
            }
            else if (pointerCount == 2) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_POINTER_DOWN:
                        downX_1 = event.getX(0);
                        downY_1 = event.getY(0);
                        downX_2 = event.getX(1);
                        downY_2 = event.getY(1);
                        swipeMode = true;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (swipeMode) {
                            upX_1 = event.getX(0);
                            upY_1 = event.getY(0);
                            upX_2 = event.getX(1);
                            upY_2 = event.getY(1);
                        }
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        swipeMode = false;
                        float deltaX_1 = downX_1 - upX_1;
                        float deltaY_1 = downY_1 - upY_1;
                        float deltaX_2 = downX_2 - upX_2;
                        float deltaY_2 = downY_2 - upY_2;
                        if ((Math.abs(deltaX_1) > MIN_DISTANCE_X) && (Math.abs(deltaY_1) < MAX_DISTANCE_Y)
                                && (Math.abs(deltaX_2) > MIN_DISTANCE_X) && (Math.abs(deltaY_2) < MAX_DISTANCE_Y)) {
                            removeAllPosts();
                            return true;
                        }
                }
            }

            return false;
        }
    }
}
