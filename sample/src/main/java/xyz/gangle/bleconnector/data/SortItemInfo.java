package xyz.gangle.bleconnector.data;


public class SortItemInfo {

    public static final int ByName = 0;
    public static final int ByMacAddress = 1;
    public static final int ByRSSI = 2;

    public int type;
    public boolean isReverse = false;
    public boolean isEnable = false;

    public SortItemInfo(int type, boolean isReverse, boolean isEnable) {
        this.type = type;
        this.isReverse = isReverse;
        this.isEnable = isEnable;
    }

    public String getTitle() {
        String title;
        if (type == SortItemInfo.ByName) {
            title = "Name";
        } else if (type == SortItemInfo.ByMacAddress) {
            title = "Mac Address";
        } else if (type == SortItemInfo.ByRSSI) {
            title = "Rssi";
        } else {
            title = "Unknown";
        }

        return title + (isReverse ? "Reverse" : "");
    }
}
