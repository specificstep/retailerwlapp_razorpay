package specificstep.com.Models;

public class NavigationModels {

    private String title;
    private int icon;
    private int not_cnt;

    public NavigationModels() {
    }

    public NavigationModels(String title, int icon, int not_cnt) {
        this.title = title;
        this.icon = icon;
        this.not_cnt = not_cnt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getNot_cnt() {
        return not_cnt;
    }

    public void setNot_cnt(int not_cnt) {
        this.not_cnt = not_cnt;
    }
}
