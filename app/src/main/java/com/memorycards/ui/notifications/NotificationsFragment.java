package com.memorycards.ui.notifications;

import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.memorycards.R;
import com.memorycards.ui.dashboard.DatabaseHelper;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class NotificationsFragment extends Fragment {

    private DatabaseHelper databaseHelper;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);

        databaseHelper = new DatabaseHelper(requireContext());

        Button buttonImportAll = root.findViewById(R.id.button_import_all);

        buttonImportAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        exportData(databaseHelper.getAllCards(), "all_cards");
                    }
                }).start();
            }
        });

        return root;
    }

    private void exportData(List<Map<String, String>> cards, String fileName) {
        exportToTxt(cards, fileName);
        exportToExcel(cards, fileName);
    }

    private void exportToTxt(List<Map<String, String>> cards, String fileName) {
        File txtFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName + ".txt");
        try (FileOutputStream fos = new FileOutputStream(txtFile)) {
            for (Map<String, String> card : cards) {
                String line = card.get("front") + " -:- " + card.get("back") + "\n";
                fos.write(line.getBytes());
            }
            showToast("TXT файл успешно сохранен: " + txtFile.getPath());
        } catch (IOException e) {
            showToast("Ошибка при сохранении TXT файла: " + e.getMessage());
        }
    }

    private void exportToExcel(List<Map<String, String>> cards, String fileName) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Cards");

        int rowNum = 0;
        for (Map<String, String> card : cards) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(card.get("front"));
            row.createCell(1).setCellValue(card.get("back"));
        }

        File excelFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName + ".xlsx");
        try (FileOutputStream fos = new FileOutputStream(excelFile)) {
            workbook.write(fos);
            workbook.close();
            showToast("Excel файл успешно сохранен: " + excelFile.getPath());
        } catch (IOException e) {
            showToast("Ошибка при сохранении Excel файла: " + e.getMessage());
        }
    }

    private void showToast(final String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
