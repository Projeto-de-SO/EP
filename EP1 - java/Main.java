import java.util.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

class Bcp {
    int programCounter;
    String processName;
    List<String> pCOM = new ArrayList<>();
    String processStatus;
    int processCredits;
    int processPriority;
    int regX;
    int regY;
    int blockWait;

    public void decrementProcessCredits() {
        if (this.processCredits != 0) {
            this.processCredits--;
        }
    }
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
        System.out.println("Carregando " + bcp.processName);
    }

    public Bcp removeFromReadyList() {
        if (this.readyList.isEmpty()) {
            return null;
        }
        return this.readyList.remove(0);
    }

    public void addToBlockedList(Bcp bcp) {
        bcp.blockWait = 2;
        this.blockedList.add(bcp);
    }

    public void decrementBlockedList() {
        for (Bcp bcp : this.blockedList) {
            if (bcp.blockWait != 0) {
                bcp.blockWait--;
            }
        }
    }

    public void removeFromBlockedList(Bcp bcp) {
        this.blockedList.remove(bcp);
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
        while (hasProcess) {

            // Decrementa tempo de espera na lista de bloqueados
            processTable.decrementBlockedList();

            // Se ter processo na lista de prontos, executa
            if (!processTable.readyList.isEmpty()) {

                Bcp bcp = processTable.removeFromReadyList(); // Remove processo da lista de pronto
                bcp.decrementProcessCredits(); // Decrementa os créditos do processo

                System.out.println("Executando " + bcp.processName);

                int i;
                for (i = 1; i <= quantum; i++) {
                    if (bcp.pCOM.get(bcp.programCounter).equals("E/S")) { // Se for comando de E/S,
                        System.out.println("E/S iniciada em " + bcp.processName);
                        System.out.println("Interrompendo " + bcp.processName + " apos " + i + " instrucoes");
                        bcp.programCounter++;
                        processTable.addToBlockedList(bcp); // Interrompe o processo e coloca ele na lista de bloqueados
                        break;
                    } else if (bcp.pCOM.get(bcp.programCounter).equals("SAIDA")) { // Se for comando de SAIDA,
                        System.out.println(bcp.processName + " terminado. X=" + bcp.regX + " Y=" + bcp.regY);
                        processTable.removeFromBcpList(bcp); // Remove da tabela de processos
                        break;
                    } else if (bcp.pCOM.get(bcp.programCounter).startsWith("X=")) { // Atribui valor no registrador X
                        bcp.regX = Integer.parseInt(bcp.pCOM.get(bcp.programCounter).substring(2));
                        bcp.programCounter++;
                    } else if (bcp.pCOM.get(bcp.programCounter).startsWith("Y=")) { // Atribui valor no registrador Y
                        bcp.regY = Integer.parseInt(bcp.pCOM.get(bcp.programCounter).substring(2));
                        bcp.programCounter++;
                    } else {
                        bcp.programCounter++;
                    }
                    if (i == quantum) { // Se for o último quantum,
                        System.out.println("Interrompendo " + bcp.processName + " apos " + i + " instrucoes");
                        processTable.addToReadyList(bcp); // interrompe o processo e adiciona ele na lista de prontos
                    }
                }
            }

            // Checar se o tempo de espera do processo na lista de bloqueados é 0
            // Se for, então retorna para lista de prontos
            Iterator<Bcp> iterator = processTable.blockedList.iterator();
            while (iterator.hasNext()) {
                Bcp blockedBcp = iterator.next();
                if (blockedBcp.blockWait == 0) {
                    iterator.remove(); // Remove o processo da lista de bloqueados
                    processTable.addToReadyList(blockedBcp); // Adiciona de volta à lista de prontos
                }
            }

            // Verificação se todos os processos estão com os créditos zerados
            boolean allCreditsZero = true;
            for (Bcp bcp : processTable.bcpList) {
                if (bcp.processCredits > 0) {
                    allCreditsZero = false;
                    break; // Se encontrar qualquer processo com crédito maior que 0, encerra verificação
                }
            }
            if (allCreditsZero) { // Se todos os processor estão com os créditos zerados,
                for (Bcp bcp : processTable.bcpList) {
                    bcp.processCredits = bcp.processPriority; // Redistribui os créditos
                }
                System.out.println("Creditos redistribuidos");
            }

            // Encerra escalonador se a tabela de processos estiver vazia
            if (processTable.bcpList.isEmpty()) {
                System.out.println("Quantum: " + quantum);
                hasProcess = false;
            }
        }
    }
}