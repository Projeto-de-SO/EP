import java.util.*;
import java.io.PrintWriter;
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

class Scheduler {
    boolean hasProcess;
    List<Bcp> readyList;
    List<Bcp> blockedList;
    int quantum; // Número do quantum
    double totalInterruption; // Número total de interrupções
    double totalProcess; // Número total de processos
    double totalInstruction; // Número total de instruções realizadas

    public Scheduler(int quantum) {
        this.hasProcess = true;
        this.blockedList = new ArrayList<>();
        this.readyList = new ArrayList<>();
        this.quantum = quantum;
        this.totalInstruction = 0;
        this.totalInterruption = 0;
        this.totalProcess = 0;
    }

    void processLoad(ProcessTable processTable, PrintWriter writer) {
        List<Bcp> bcpListTemp = new ArrayList<>(processTable.bcpList);

        this.totalProcess = bcpListTemp.size();

        Collections.sort(bcpListTemp, new Comparator<Bcp>() {
            @Override
            public int compare(Bcp bcp1, Bcp bcp2) {
                return Integer.compare(bcp2.processPriority, bcp1.processPriority);
            }
        });
        for (Bcp bcp : bcpListTemp) {
            processTable.addToReadyList(this.readyList, bcp);
            writer.println("Carregando " + bcp.processName);
            this.totalInstruction = this.totalInstruction + bcp.pCOM.size();
        }
        bcpListTemp.clear();
    }

    void run(ProcessTable processTable, PrintWriter writer) {

        processLoad(processTable, writer);

        while (hasProcess) {

            // Decrementa tempo de espera na lista de bloqueados
            processTable.decrementBlockedList(this.blockedList);

            // Se ter processo na lista de prontos, executa
            if (!this.readyList.isEmpty()) {

                Bcp bcp = processTable.removeFromReadyList(this.readyList); // Remove processo da lista de pronto
                bcp.decrementProcessCredits(); // Decrementa os créditos do processo

                writer.println("Executando " + bcp.processName);

                int i;
                for (i = 1; i <= this.quantum; i++) {
                    if (bcp.pCOM.get(bcp.programCounter).equals("E/S")) { // Se for comando de E/S,
                        writer.println("E/S iniciada em " + bcp.processName);
                        writer.println("Interrompendo " + bcp.processName + " apos " + i + " instrucoes");
                        bcp.programCounter++;
                        this.totalInterruption++;
                        processTable.addToBlockedList(this.blockedList, bcp);
                        break; // Coloca ele na lista de bloqueados e interrompe o processo
                    } else if (bcp.pCOM.get(bcp.programCounter).equals("SAIDA")) { // Se for comando de SAIDA,
                        writer.println(bcp.processName + " terminado. X=" + bcp.regX + " Y=" + bcp.regY);
                        this.totalInterruption++;
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
                    if (i == this.quantum) { // Se for o último quantum,
                        writer.println("Interrompendo " + bcp.processName + " apos " + i + " instrucoes");
                        this.totalInterruption++;
                        processTable.addToReadyList(this.readyList, bcp);
                        // Interrompe o processo e adiciona ele na lista de prontos
                    }
                }
            }

            // Checar se o tempo de espera do processo na lista de bloqueados é 0
            // Se for, então retorna para lista de prontos
            Iterator<Bcp> iterator = this.blockedList.iterator();
            while (iterator.hasNext()) {
                Bcp blockedBcp = iterator.next();
                if (blockedBcp.blockWait == 0) {
                    iterator.remove(); // Remove o processo da lista de bloqueados
                    processTable.addToReadyList(this.readyList, blockedBcp); // Adiciona de volta à lista de prontos
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
                writer.println("Creditos redistribuidos");
            }

            // Encerra escalonador se a tabela de processos estiver vazia
            if (processTable.bcpList.isEmpty()) {
                this.hasProcess = false;
            }
        }

        writer.println("Quantum: " + quantum);
        writer.println("Media de trocas: " + String.format("%.2f", this.totalInterruption / this.totalProcess));
        writer.println("Media de instrucoes: " + String.format("%.2f", this.totalInstruction / this.totalInterruption));
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

        try (PrintWriter writer = new PrintWriter("logs/log" + quantum + ".txt")) {
            Scheduler scheduler = new Scheduler(quantum);
            scheduler.run(processTable, writer); // Executa o escalonador e escreve no arquivo
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}