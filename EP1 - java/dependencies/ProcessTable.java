package dependencies;

import java.util.ArrayList;
import java.util.List;

public class ProcessTable {
    List<Bcp> bcpList;

    public ProcessTable() {
        this.bcpList = new ArrayList<>();
    }

    public void addToBcpList(Bcp bcp) {
        this.bcpList.add(bcp);
    }

    public void removeFromBcpList(Bcp bcp) {
        this.bcpList.remove(bcp);
    }

    public void addToReadyList(List<Bcp> readyList, Bcp bcp) {
        if (readyList.isEmpty()) {
            readyList.add(bcp);
        } else {
            int position = 0;
            for (int i = 0; i < readyList.size(); i++) {
                Bcp existingBcp = readyList.get(i);
                if (bcp.processCredits > existingBcp.processCredits) {
                    position = i;
                    break;
                }
                position = i + 1;
            }
            readyList.add(position, bcp);
        }
    }

    public Bcp removeFromReadyList(List<Bcp> readyList) {
        if (readyList.isEmpty()) {
            return null;
        }
        return readyList.remove(0);
    }

    public void addToBlockedList(List<Bcp> blockedList, Bcp bcp) {
        bcp.blockWait = 2;
        blockedList.add(bcp);
    }

    public void decrementBlockedList(List<Bcp> blockedList) {
        for (Bcp bcp : blockedList) {
            if (bcp.blockWait != 0) {
                bcp.blockWait--;
            }
        }
    }

    public void removeFromBlockedList(List<Bcp> blockedList, Bcp bcp) {
        blockedList.remove(bcp);
    }

}
