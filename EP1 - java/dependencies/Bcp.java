package dependencies;

import java.util.ArrayList;
import java.util.List;

public class Bcp {
    public int programCounter;
    public String processName;
    public List<String> pCOM = new ArrayList<>();
    public String processStatus;
    public int processCredits;
    public int processPriority;
    int regX;
    int regY;
    int blockWait;

    public void decrementProcessCredits() {
        if (this.processCredits != 0) {
            this.processCredits--;
        }
    }
}
