package com.plus.navanguilla;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.provider.Settings;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoadExitInterview extends AppCompatActivity {

    private static final String TAG = "ExitInterview";
    private Handler handler;
    private View loadingContainer;
    private View emptyContainer;
    private View thankyouContainer;
    private View alreadySubmittedContainer;
    private ScrollView questionnaireScroll;
    private TextView interviewTitle;
    private TextView interviewDescription;
    private LinearLayout questionsContainer;
    private Button btnSubmit;

    private int interviewId = -1;
    private List<QuestionData> questions = new ArrayList<>();
    private String thisdevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        justhelper.setBrightness(this, 75);
        setContentView(R.layout.activity_load_exit_interview);
        handler = new Handler(Looper.getMainLooper());

        loadingContainer = findViewById(R.id.loading_container);
        emptyContainer = findViewById(R.id.empty_container);
        thankyouContainer = findViewById(R.id.thankyou_container);
        alreadySubmittedContainer = findViewById(R.id.already_submitted_container);
        questionnaireScroll = findViewById(R.id.questionnaire_scroll);
        interviewTitle = findViewById(R.id.interview_title);
        interviewDescription = findViewById(R.id.interview_description);
        questionsContainer = findViewById(R.id.questions_container);
        btnSubmit = findViewById(R.id.btn_submit);

        Button goback = findViewById(R.id.backmain);
        goback.setOnClickListener(v -> {
            Intent intent = new Intent(LoadExitInterview.this, Myactivity.class);
            startActivity(intent);
        });

        btnSubmit.setOnClickListener(v -> submitAnswers());

        thisdevice = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        loadInterview();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(LoadExitInterview.this, Myactivity.class);
        startActivity(intent);
    }

    private void loadInterview() {
        String url = justhelper.BASE_URL + "/navigation/load_exit_interview.php?device_id=" + thisdevice;
        Request request = new Request.Builder().url(url).build();
        Log.i(TAG, "Loading: " + url);

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to load", e);
                handler.post(() -> {
                    loadingContainer.setVisibility(View.GONE);
                    Toast.makeText(LoadExitInterview.this, "Failed to load questionnaire", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String json = response.body().string();
                Log.i(TAG, json);
                handler.post(() -> parseAndDisplay(json));
            }
        });
    }

    private void parseAndDisplay(String json) {
        loadingContainer.setVisibility(View.GONE);

        try {
            JSONObject obj = new JSONObject(json);

            if (obj.isNull("id")) {
                emptyContainer.setVisibility(View.VISIBLE);
                return;
            }

            // Check if already submitted within cooldown period
            if (obj.optBoolean("already_submitted", false)) {
                alreadySubmittedContainer.setVisibility(View.VISIBLE);
                return;
            }

            interviewId = obj.getInt("id");
            String title = obj.optString("title", "");
            String description = obj.optString("description", "");

            interviewTitle.setText(title);
            if (description != null && !description.isEmpty()) {
                interviewDescription.setText(description);
                interviewDescription.setVisibility(View.VISIBLE);
            } else {
                interviewDescription.setVisibility(View.GONE);
            }

            JSONArray questionsArray = obj.getJSONArray("questions");
            for (int i = 0; i < questionsArray.length(); i++) {
                JSONObject qObj = questionsArray.getJSONObject(i);
                QuestionData qd = new QuestionData();
                qd.id = qObj.getInt("id");
                qd.question = qObj.getString("question");
                qd.type = qObj.getString("type");
                qd.required = qObj.optBoolean("required", true);

                if (qd.type.equals("multiple_choice")) {
                    JSONArray opts = qObj.optJSONArray("options");
                    if (opts != null) {
                        qd.options = new ArrayList<>();
                        for (int j = 0; j < opts.length(); j++) {
                            qd.options.add(opts.getString(j));
                        }
                    }
                } else if (qd.type.equals("rating")) {
                    qd.ratingMax = qObj.optInt("rating_max", 5);
                }

                questions.add(qd);
                addQuestionView(qd, i);
            }

            questionnaireScroll.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            Log.e(TAG, "Parse error", e);
            emptyContainer.setVisibility(View.VISIBLE);
        }
    }

    private void addQuestionView(QuestionData qd, int index) {
        float density = getResources().getDisplayMetrics().density;

        // Card container
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.WHITE);
        cardBg.setCornerRadius(12 * density);
        card.setBackground(cardBg);
        card.setElevation(2 * density);
        int cardPad = (int) (16 * density);
        card.setPadding(cardPad, cardPad, cardPad, cardPad);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = (int) (12 * density);
        card.setLayoutParams(cardParams);

        // Question number + text
        TextView questionLabel = new TextView(this);
        String reqMark = qd.required ? " *" : "";
        questionLabel.setText((index + 1) + ". " + qd.question + reqMark);
        questionLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        questionLabel.setTextColor(Color.parseColor("#1C1C1E"));
        questionLabel.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        labelParams.bottomMargin = (int) (12 * density);
        questionLabel.setLayoutParams(labelParams);
        card.addView(questionLabel);

        // Render based on type
        switch (qd.type) {
            case "multiple_choice":
                addMultipleChoiceView(card, qd);
                break;
            case "rating":
                addRatingView(card, qd);
                break;
            case "text":
                addTextView(card, qd);
                break;
        }

        questionsContainer.addView(card);
    }

    private void addMultipleChoiceView(LinearLayout card, QuestionData qd) {
        RadioGroup radioGroup = new RadioGroup(this);
        radioGroup.setOrientation(RadioGroup.VERTICAL);
        radioGroup.setTag("answer_" + qd.id);

        if (qd.options != null) {
            for (int i = 0; i < qd.options.size(); i++) {
                RadioButton rb = new RadioButton(this);
                rb.setText(qd.options.get(i));
                rb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                rb.setTextColor(Color.parseColor("#1C1C1E"));
                rb.setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4));
                rb.setId(View.generateViewId());
                radioGroup.addView(rb);
            }
        }

        qd.answerView = radioGroup;
        card.addView(radioGroup);
    }

    private void addRatingView(LinearLayout card, QuestionData qd) {
        LinearLayout ratingRow = new LinearLayout(this);
        ratingRow.setOrientation(LinearLayout.HORIZONTAL);
        ratingRow.setGravity(Gravity.CENTER_VERTICAL);
        ratingRow.setTag("answer_" + qd.id);

        final int[] selectedRating = {0};
        List<TextView> ratingButtons = new ArrayList<>();

        for (int i = 1; i <= qd.ratingMax; i++) {
            final int rating = i;
            TextView btn = new TextView(this);
            btn.setText(String.valueOf(i));
            btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            btn.setGravity(Gravity.CENTER);
            btn.setTypeface(null, Typeface.BOLD);

            int size = qd.ratingMax <= 5 ? dpToPx(44) : dpToPx(34);
            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(size, size);
            btnParams.rightMargin = dpToPx(6);
            btn.setLayoutParams(btnParams);

            setRatingStyle(btn, false);
            ratingButtons.add(btn);

            btn.setOnClickListener(v -> {
                selectedRating[0] = rating;
                for (int j = 0; j < ratingButtons.size(); j++) {
                    setRatingStyle(ratingButtons.get(j), j < rating);
                }
            });

            ratingRow.addView(btn);
        }

        qd.answerView = ratingRow;
        qd.ratingButtons = ratingButtons;
        qd.selectedRating = selectedRating;
        card.addView(ratingRow);
    }

    private void setRatingStyle(TextView btn, boolean selected) {
        float density = getResources().getDisplayMetrics().density;
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(8 * density);
        if (selected) {
            bg.setColor(Color.parseColor("#8B5CF6"));
            btn.setTextColor(Color.WHITE);
        } else {
            bg.setColor(Color.parseColor("#F3F4F6"));
            bg.setStroke((int)(1 * density), Color.parseColor("#D1D5DB"));
            btn.setTextColor(Color.parseColor("#374151"));
        }
        btn.setBackground(bg);
    }

    private void addTextView(LinearLayout card, QuestionData qd) {
        EditText editText = new EditText(this);
        editText.setHint("Type your answer here...");
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        editText.setTextColor(Color.parseColor("#1C1C1E"));
        editText.setHintTextColor(Color.parseColor("#8E8E93"));
        editText.setMinLines(3);
        editText.setGravity(Gravity.TOP | Gravity.START);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#F9FAFB"));
        bg.setCornerRadius(dpToPx(8));
        bg.setStroke(dpToPx(1), Color.parseColor("#D1D5DB"));
        editText.setBackground(bg);
        editText.setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10));

        qd.answerView = editText;
        card.addView(editText);
    }

    private void submitAnswers() {
        // Validate required fields
        for (QuestionData qd : questions) {
            if (!qd.required) continue;

            String answer = getAnswer(qd);
            if (answer == null || answer.trim().isEmpty()) {
                Toast.makeText(this, "Please answer all required questions", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Submitting...");

        // Build the POST body
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("interview_id", String.valueOf(interviewId));
        formBuilder.add("device_id", thisdevice);

        for (int i = 0; i < questions.size(); i++) {
            QuestionData qd = questions.get(i);
            String answer = getAnswer(qd);
            formBuilder.add("question_id[" + i + "]", String.valueOf(qd.id));
            formBuilder.add("answer[" + i + "]", answer != null ? answer : "");
        }

        RequestBody body = formBuilder.build();
        String url = justhelper.BASE_URL + "/navigation/submit_exit_interview.php";
        Request request = new Request.Builder().url(url).post(body).build();
        Log.i(TAG, "Submitting to: " + url);

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Submit failed", e);
                handler.post(() -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Submit Feedback");
                    Toast.makeText(LoadExitInterview.this, "Failed to submit. Please try again.", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String resp = response.body().string();
                Log.i(TAG, "Submit response: " + resp);
                handler.post(() -> {
                    try {
                        JSONObject json = new JSONObject(resp);
                        if ("success".equals(json.optString("status"))) {
                            questionnaireScroll.setVisibility(View.GONE);
                            thankyouContainer.setVisibility(View.VISIBLE);
                        } else {
                            btnSubmit.setEnabled(true);
                            btnSubmit.setText("Submit Feedback");
                            Toast.makeText(LoadExitInterview.this, "Failed to submit. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        btnSubmit.setEnabled(true);
                        btnSubmit.setText("Submit Feedback");
                        Toast.makeText(LoadExitInterview.this, "Failed to submit. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private String getAnswer(QuestionData qd) {
        if (qd.answerView == null) return null;

        switch (qd.type) {
            case "multiple_choice":
                if (qd.answerView instanceof RadioGroup) {
                    RadioGroup rg = (RadioGroup) qd.answerView;
                    int checkedId = rg.getCheckedRadioButtonId();
                    if (checkedId == -1) return null;
                    RadioButton rb = rg.findViewById(checkedId);
                    return rb != null ? rb.getText().toString() : null;
                }
                break;
            case "rating":
                if (qd.selectedRating != null && qd.selectedRating[0] > 0) {
                    return String.valueOf(qd.selectedRating[0]);
                }
                return null;
            case "text":
                if (qd.answerView instanceof EditText) {
                    return ((EditText) qd.answerView).getText().toString().trim();
                }
                break;
        }
        return null;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    // Inner class to hold question data
    static class QuestionData {
        int id;
        String question;
        String type;
        boolean required;
        List<String> options;
        int ratingMax;
        View answerView;
        List<TextView> ratingButtons;
        int[] selectedRating;
    }
}
