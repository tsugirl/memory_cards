package com.memorycards.ui.dashboard;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.memorycards.R;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private Button lastButton;
    private AlertDialog lastAlertDialog;
    private DatabaseHelper databaseHelper;
    private Button selectedButton;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        databaseHelper = new DatabaseHelper(requireContext());

        FloatingActionButton addButton = root.findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddFolderDialog();
            }
        });

        return root;
    }

    private void showAddFolderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Добавить папку");

        final EditText folderNameEditText = new EditText(requireContext());
        folderNameEditText.setHint("Название папки");
        builder.setView(folderNameEditText);

        builder.setPositiveButton("Создать", (dialog, which) -> {
            String folderName = folderNameEditText.getText().toString();
            addNewButton(folderName);
        });
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private Button addNewButton(String folderName) {

        GridLayout buttonsLayout = requireView().findViewById(R.id.folders_layout);


        Button newButton = new Button(requireContext());
        newButton.setText(folderName);


        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.width = GridLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.height = GridLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.setMargins(16, 16, 16, 16);
        layoutParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        newButton.setLayoutParams(layoutParams);


        newButton.setGravity(Gravity.CENTER);


        newButton.setOnClickListener(v -> showCreateCardDialog((Button) v));


        buttonsLayout.addView(newButton);

        return newButton;
    }


    private void showCreateCardDialog(Button button) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Создание карточки");


        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);


        EditText frontEditText = new EditText(requireContext());
        frontEditText.setHint("Лицевая сторона");
        layout.addView(frontEditText);


        EditText backEditText = new EditText(requireContext());
        backEditText.setHint("Обратная сторона");
        layout.addView(backEditText);


        builder.setView(layout)
                .setNegativeButton("Назад", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Создать", (dialog, which) -> {
                    String frontText = frontEditText.getText().toString();
                    String backText = backEditText.getText().toString();
                    String folderName = button.getText().toString();
                    databaseHelper.saveCard(folderName, frontText, backText);
                });


        Button deleteButton = new Button(requireContext());
        deleteButton.setText("Удалить папку");
        deleteButton.setTextColor(Color.WHITE);
        deleteButton.setBackgroundColor(Color.RED);
        deleteButton.setOnClickListener(v -> deleteFolder(button));
        layout.addView(deleteButton);


        lastAlertDialog = builder.create();
        lastAlertDialog.show();


        lastButton = button;

        Button favoriteButton = new Button(requireContext());
        favoriteButton.setText("Избранное");
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markAsFavorite(button);
            }
        });
        layout.addView(favoriteButton);
    }

    private void markAsFavorite(Button button) {

        if (selectedButton != null) {
            selectedButton.setBackgroundColor(Color.rgb(214, 215, 215));
        }


        selectedButton = button;
        selectedButton.setBackgroundColor(Color.GREEN);
    }

    private void deleteFolder(Button button) {
        if (button != null) {

            ViewGroup parentLayout = (ViewGroup) button.getParent();

            parentLayout.removeView(button);
            String folderName = button.getText().toString();
            databaseHelper.deleteCards(folderName);
            dismissAlertDialog();
        }
    }

    private void dismissAlertDialog() {
        if (lastAlertDialog != null && lastAlertDialog.isShowing()) {
            lastAlertDialog.dismiss();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        saveFoldersToFile(collectFolderNames());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFoldersFromFileAndUpdateUI();
        showSelectedFolderCardsAlert();
    }

    private void showSelectedFolderCardsAlert() {
        String selectedFolderName = databaseHelper.getLastSelectedFolder();

        if (selectedFolderName != null) {
            List<String[]> cards = databaseHelper.loadCards(selectedFolderName);

            StringBuilder message = new StringBuilder();
            for (String[] card : cards) {
                message.append("Лицо: ").append(card[0]).append("\n");
                message.append("Обратная сторона: ").append(card[1]).append("\n\n");
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Карточки для выбранной папки: " + selectedFolderName);
            builder.setMessage(message.toString());
            builder.setPositiveButton("OK", null);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    private List<String> collectFolderNames() {
        List<String> folderNames = new ArrayList<>();
        GridLayout buttonsLayout = requireView().findViewById(R.id.folders_layout);
        for (int i = 0; i < buttonsLayout.getChildCount(); i++) {
            View view = buttonsLayout.getChildAt(i);
            if (view instanceof Button) {
                Button button = (Button) view;
                folderNames.add(button.getText().toString());
            }
        }
        return folderNames;
    }

    private void loadFoldersFromFileAndUpdateUI() {
        List<String> loadedFolders = loadFoldersFromFile();
        String lastSelectedFolder = databaseHelper.getLastSelectedFolder();
        if (loadedFolders != null) {
            for (String folderName : loadedFolders) {
                Button button = addNewButton(folderName);

                if (folderName.equals(lastSelectedFolder)) {
                    button.setBackgroundColor(Color.GREEN);
                    selectedButton = button;
                }
            }
        }
    }

    private void saveFoldersToFile(List<String> folders) {
        String lastSelectedButton = selectedButton != null ? selectedButton.getText().toString() : null;
        databaseHelper.saveFolders(collectFolderNames(), lastSelectedButton);
    }

    private List<String> loadFoldersFromFile() {
        return databaseHelper.loadFolders();
    }
}
