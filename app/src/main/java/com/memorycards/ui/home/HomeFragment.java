package com.memorycards.ui.home;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.memorycards.R;
import com.memorycards.ui.dashboard.DatabaseHelper;

public class HomeFragment extends Fragment {

    private DatabaseHelper databaseHelper;
    private ListView listView;
    private CardCursorAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        databaseHelper = new DatabaseHelper(requireContext());
        listView = root.findViewById(R.id.listView);

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
}
