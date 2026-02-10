package com.plus.navanguilla;

import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class WebViewActivity extends AppCompatActivity {

    public static final String EXTRA_URL = "extra_url";
    public static final String EXTRA_TITLE = "extra_title";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        String url = getIntent().getStringExtra(EXTRA_URL);
        String title = getIntent().getStringExtra(EXTRA_TITLE);

        TextView titleView = findViewById(R.id.webview_title);
        TextView closeBtn = findViewById(R.id.webview_close);
        ProgressBar progressBar = findViewById(R.id.webview_progress);
        WebView webView = findViewById(R.id.webview);

        titleView.setText(title != null ? title : "");
        closeBtn.setOnClickListener(v -> finish());

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String pageTitle) {
                if (title == null || title.isEmpty()) {
                    titleView.setText(pageTitle);
                }
            }
        });

        if (url != null && !url.isEmpty()) {
            webView.loadUrl(url);
        }
    }

    @Override
    public void onBackPressed() {
        WebView webView = findViewById(R.id.webview);
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
