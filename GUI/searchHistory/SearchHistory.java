package searchHistory;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

public class SearchHistory {
    private List<SearchRecord> records;
    private final File searchHistoryFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public SearchHistory() {
        searchHistoryFile = new File("search_history.json");
        records = new ArrayList<>();
        loadSearchHistory(); // 程式一啟動就讀取紀錄
    }

    // 新增搜尋紀錄
    public void addRecord(String keyword) {
        records.add(new SearchRecord(keyword, LocalDateTime.now().toString()));
        saveSearchHistory(); // 每次新增就自動儲存
    }

    // 取得所有紀錄
    public List<SearchRecord> getAllRecords() {
        return new ArrayList<>(records);
    }

    // 儲存到檔案
    private void saveSearchHistory() {
        try (Writer writer = new FileWriter(searchHistoryFile)) {
            gson.toJson(records, writer);
        } catch (IOException e) {
            System.err.println("儲存搜尋紀錄失敗: " + e.getMessage());
        }
    }

    // 從檔案讀取
    private void loadSearchHistory() {
        if (searchHistoryFile.exists()) {
            try (Reader reader = new FileReader(searchHistoryFile)) {
                records = gson.fromJson(reader, new TypeToken<List<SearchRecord>>() {
                }.getType());
                if (records == null) records = new ArrayList<>();
            } catch (IOException e) {
                System.err.println("讀取搜尋紀錄失敗: " + e.getMessage());
            }
        }
    }
}
