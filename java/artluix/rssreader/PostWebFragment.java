package artluix.rssreader;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class PostWebFragment extends Fragment {
    public static final String ARG_POST_URL = "post_url";
    private WebView webView;
    private ProgressDialog progressDialog;
    public PostWebFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.post_web, container, false);
        webView = (WebView) rootView.findViewById(R.id.post_web);
        String url = getArguments().getString(ARG_POST_URL);
        startWebView(url);
        return rootView;
    }

    private void startWebView(String url) {
        webView.setFocusable(false);
        webView.setFocusableInTouchMode(false);
        webView.getSettings().setUserAgentString("Android");

        progressDialog = new ProgressDialog(getActivity());

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                    }
            }
        });

        progressDialog.setMessage("Loading...\n" + url);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        webView.loadUrl(url);
    }
}
