package com.memorycards.ui.home;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.memorycards.R;
import com.memorycards.ui.dashboard.DatabaseHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private DatabaseHelper databaseHelper;
    private ListView listView;
    private CardCursorAdapter adapter;
    private AlertDialog countdownDialog;
    private CountDownTimer countDownTimer;
    private int cardIndex = 0;
    private int correctAnswers = 0;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        databaseHelper = new DatabaseHelper(requireContext());
        listView = root.findViewById(R.id.listView);


        Button buttonAnswering = root.findViewById(R.id.button_answering);
        buttonAnswering.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAnswerOptionsDialog();
            }
        });

        updateListView();

        return root;
    }

    private void updateListView() {
        String lastSelectedFolder = databaseHelper.getLastSelectedFolder();
        if (lastSelectedFolder != null) {
            Cursor cursor = databaseHelper.loadFrontCards(lastSelectedFolder);
            adapter = new CardCursorAdapter(requireContext(), cursor);
            listView.setAdapter(adapter);
        }
    }

    private void showAnswerOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Выберите вариант тестирования")
                .setPositiveButton("Зачет", (dialog, which) -> {
                    startTestWithoutTimer();
                })
                .setNegativeButton("На время", (dialog, which) -> {
                    dialog.dismiss();
                    startCountdownTimer();
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void startTestWithoutTimer() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.timer_dialog_layout, null);
        builder.setView(dialogView);

        TextView cardTextView = dialogView.findViewById(R.id.cardTextView);
        EditText inputEditText = dialogView.findViewById(R.id.inputEditText);
        Button acceptButton = dialogView.findViewById(R.id.acceptButton);
        Button finishButton = dialogView.findViewById(R.id.finishButton);

        cardTextView.setText(getCardTest(0));

        countdownDialog = builder.create();
        countdownDialog.show();

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userInput = inputEditText.getText().toString();
                List<Map<String, String>> cards = getAllCards();

                if (userInput.equals(cards.get(cardIndex).get("back"))) {
                    correctAnswers++;
                }

                cardIndex++;
                if (cardIndex >= cards.size()) {
                    countdownDialog.dismiss();

                } else {
                    cardTextView.setText(getCardTest(cardIndex));
                    inputEditText.setText("");
                }
            }
        });

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Map<String, String>> cards = getAllCards();
                showDetailedResultsDialog(correctAnswers, cards.size());
                cardIndex = 0;
                correctAnswers = 0;
                countdownDialog.dismiss();
            }
        });
    }

    private void startCountdownTimer() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.timer_dialog_layout, null);
        builder.setView(dialogView);

        TextView cardTextView = dialogView.findViewById(R.id.cardTextView);
        EditText inputEditText = dialogView.findViewById(R.id.inputEditText);
        Button acceptButton = dialogView.findViewById(R.id.acceptButton);
        Button finishButton = dialogView.findViewById(R.id.finishButton);
        TextView timerTextView = dialogView.findViewById(R.id.timerTextView);

        cardTextView.setText(getCardTest(0));

        countdownDialog = builder.create();
        countdownDialog.show();

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userInput = inputEditText.getText().toString();
                List<Map<String, String>> cards = getAllCards();

                if (userInput.equals(cards.get(cardIndex).get("back"))) {
                    correctAnswers++;
                }

                cardIndex++;
                if (cardIndex >= cards.size()) {
                    countdownDialog.dismiss();
                    showSimpleResultsDialog(correctAnswers);
                    cardIndex = 0;
                    correctAnswers = 0;
                } else {
                    cardTextView.setText(getCardTest(cardIndex));
                    inputEditText.setText("");
                }
            }
        });

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSimpleResultsDialog(correctAnswers);
                correctAnswers = 0;
                countdownDialog.dismiss();
            }
        });

        List<Map<String, String>> cards = getAllCards();
        int timerDuration = cards.size() * 20000;

        countDownTimer = new CountDownTimer(timerDuration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                timerTextView.setText("Таймер (" + secondsRemaining + " сек)");
            }

            @Override
            public void onFinish() {
                if (countdownDialog != null && countdownDialog.isShowing()) {
                    countdownDialog.dismiss();
                }

                cardIndex = 0;
                correctAnswers = 0;

                onDestroy();
            }
        };
        countDownTimer.start();
    }

    private String getCardTest(int elem) {
        List<Map<String, String>> cards = getAllCards();
        if (!cards.isEmpty()) {
            return cards.get(elem).get("front");
        }
        return "Текст лицевой стороны первой карточки";
    }

    private List<Map<String, String>> getAllCards() {
        List<Map<String, String>> cards = new ArrayList<>();
        String lastSelectedFolder = databaseHelper.getLastSelectedFolder();
        if (lastSelectedFolder != null) {
            Cursor cursor = databaseHelper.loadFrontCards(lastSelectedFolder);
            if (cursor.moveToFirst()) {
                do {
                    String front = cursor.getString(cursor.getColumnIndexOrThrow("front"));
                    String back = cursor.getString(cursor.getColumnIndexOrThrow("back"));
                    Map<String, String> card = new HashMap<>();
                    card.put("front", front);
                    card.put("back", back);
                    cards.add(card);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return cards;
    }

    private void showSimpleResultsDialog(int correctAnswers) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Результаты теста")
                .setMessage("Количество правильных ответов: " + correctAnswers)
                .setPositiveButton("OK", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDetailedResultsDialog(int correctAnswers, int totalQuestions) {
        int percentage = (int) (((double) correctAnswers / totalQuestions) * 100);
        String grade;
        if (percentage >= 90) {
            grade = "5";
        } else if (percentage >= 70) {
            grade = "4";
        } else if (percentage >= 50) {
            grade = "3";
        } else {
            grade = "2";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Результаты теста")
                .setMessage("Количество правильных ответов: " + correctAnswers +
                        "\nПроцент правильных ответов: " + percentage + "%" +
                        "\nОценка: " + grade)
                .setPositiveButton("OK", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
