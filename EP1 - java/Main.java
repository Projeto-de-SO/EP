import java.util.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

class Bcp {
    int programCounter;
    String processName;
    List<String> pCOM = new ArrayList<>();
    String processStatus;
    int processQuantum;
    int processCredits;
    int processPriority;
    int regX;
    int regY;
    int blockWait;
}

class ProcessTable {
    List<Bcp> readyList;
    List<Bcp> blockedList;
    List<Bcp> bcpList;

    public ProcessTable() {
        this.bcpList = new ArrayList<>();
        this.readyList = new ArrayList<>();
        this.blockedList = new ArrayList<>();
    }

    public void addToBcpList(Bcp bcp) {
        this.bcpList.add(bcp);
    }

    public void removeFromBcpList(Bcp bcp) {
        this.bcpList.remove(bcp);
    }

    public void addToReadyList(Bcp bcp) {
        if (this.readyList.isEmpty()) {
            this.readyList.add(bcp);
        } else {
            int position = 0;
            for (int i = 0; i < this.readyList.size(); i++) {
                Bcp existingBcp = this.readyList.get(i);
                if (bcp.processCredits > existingBcp.processCredits) {
                    position = i;
                    break;
                }
                position = i + 1;
            }
            this.readyList.add(position, bcp);
        }
        System.out.println("CARREGANDO " + bcp.processName);
    }

    public Bcp removeFromReadyList() {
        if (this.readyList.isEmpty()) {
            return null;
        }
        return this.readyList.remove(0);
    }

    public void decrementBlockedList() {
        for (Bcp bcp : this.blockedList) {
            if (bcp.blockWait != 0) {
                bcp.blockWait--;
            }
        }
    }

    public void addToBlockedList(Bcp bcp) {
        bcp.blockWait = 2;
        this.blockedList.add(bcp);
    }
}

public class Main {
    public static List<String> readFile(String filePath) throws IOException {
        return Files.readAllLines(Paths.get(filePath));
    }

    public static Bcp createBCP(List<String> program, int priority) {
        Bcp bcp = new Bcp();

        for (int index = 0; index < program.size(); index++) {
            if (index == 0) {
                bcp.processName = program.get(index);
            } else {
                bcp.pCOM.add(program.get(index));
            }
        }

        bcp.programCounter = 0;
        bcp.processPriority = priority;
        bcp.processCredits = priority;
        bcp.processStatus = "ready";

        return bcp;
    }

    public static void main(String[] args) throws IOException {
        int quantum = Integer.parseInt(readFile("./programas/quantum.txt").get(0));

        List<String> priorities = readFile("./programas/prioridades.txt");
        List<String> notProgramFiles = List.of("quantum.txt", "prioridades.txt");

        ProcessTable processTable = new ProcessTable();

        int[] index = { 0 };

        Files.list(Paths.get("./programas/")).forEach(filePath -> {

            try {
                String fileName = filePath.getFileName().toString();
                if (!notProgramFiles.contains(fileName)) {
                    List<String> program = readFile(filePath.toString());

                    Bcp bcp = createBCP(program, Integer.parseInt(priorities.get(index[0])));

                    processTable.addToBcpList(bcp);
                    index[0]++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        List<Bcp> bcpListTemp = new ArrayList<>(processTable.bcpList);

        Collections.sort(bcpListTemp, new Comparator<Bcp>() {
            @Override
            public int compare(Bcp bcp1, Bcp bcp2) {
                return Integer.compare(bcp2.processPriority, bcp1.processPriority);
            }
        });

        for (Bcp bcp : bcpListTemp) {
            processTable.addToReadyList(bcp);
        }
        boolean hasProcess = true;

        //while (hasProcess) {
            if (!processTable.readyList.isEmpty()) {

                Bcp bcp = processTable.removeFromReadyList();
                System.out.println("EXECUTANDO " + bcp.processName);
                int i;
                for (i = 1; i > 3; i++) {
                    if (bcp.pCOM.get(bcp.programCounter) == "E/S") {
                        System.out.println("E/S iniciada em " + bcp.processName);
                        System.out.println("Interrompendo " + bcp.processName + "após " + i + "instruções");
                        bcp.programCounter++;
                        processTable.decrementBlockedList();
                        processTable.addToBlockedList(bcp);
                        break;
                    } else if (bcp.pCOM.get(bcp.programCounter) == "SAIDA") {
                        System.out.println(bcp.processName + "terminado. X=" + bcp.regX + " Y=" + bcp.regY);
                        processTable.decrementBlockedList();
                        processTable.removeFromBcpList(bcp);
                        break;
                    } else {
                        bcp.programCounter++;
                        if (i == 3) {
                            System.out.println("Interrompendo " + bcp.processName + "após " + i + "instruções");
                        }
                    }
                }

            }
        //}
    }
}