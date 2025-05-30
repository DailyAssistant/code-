package imagehistoryAndStore;

import java.time.LocalDateTime;

public class ImageRecord {
    String filename;
    String path;
    String addedTime;
    String lastUsed;
    int useCount;
    boolean favorite;

    public ImageRecord(String filename, String path) {
        this.filename = filename;
        this.path = path;
        this.addedTime = LocalDateTime.now().toString();
        this.lastUsed = "";
        this.useCount = 0;
        this.favorite = false;
    }

    public String getFilename() {
        return filename;
    }

    public String getPath() {
        return path;
    }

    public String getAddedTime() {
        return addedTime;
    }

    public String getLastUsed() {
        return lastUsed;
    }

    public int getUseCount() {
        return useCount;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setLastUsed(String lastUsed) {
        this.lastUsed = lastUsed;
    }

    public void incrementUseCount() {
        this.useCount++;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}
