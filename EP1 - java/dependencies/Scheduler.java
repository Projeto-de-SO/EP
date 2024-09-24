package dependencies;

import java.io.PrintWriter;
import java.util.*;

public class Scheduler {
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

    public void run(ProcessTable processTable, PrintWriter writer) {

        processLoad(processTable, writer);

        while (hasProcess) {

            // Decrementa tempo de espera na lista de bloqueados
            processTable.decrementBlockedList(this.blockedList);

            // Se ter processo na lista de prontos, executa
            if (!this.readyList.isEmpty()) {

                Bcp bcp = processTable.removeFromReadyList(this.readyList); // Remove processo da lista de pronto
                bcp.decrementProcessCredits(); // Decrementa os créditos do processo

                bcp.processStatus = "executando";
                writer.println("Executando " + bcp.processName);

                int i;
                for (i = 1; i <= this.quantum; i++) {
                    if (bcp.pCOM.get(bcp.programCounter).equals("E/S")) { // Se for comando de E/S,
                        writer.println("E/S iniciada em " + bcp.processName);
                        writer.println("Interrompendo " + bcp.processName + " apos " + i + " instrucoes");
                        bcp.programCounter++;
                        this.totalInterruption++;
                        processTable.addToBlockedList(this.blockedList, bcp);
                        bcp.processStatus = "bloqueado";
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
                        bcp.processStatus = "pronto";
                        // Interrompe o processo e adiciona ele na lista de prontos
                    }
                }
            }

            // Checar se o tempo de espera do processo na lista de bloqueados é 0
            // Se for, então retorna para lista de prontos
            Iterator<Bcp> iterator = this.blockedList.iterator();
            while (iterator.hasNext()) {
                Bcp bcp = iterator.next();
                if (bcp.blockWait == 0) {
                    iterator.remove(); // Remove o processo da lista de bloqueados
                    processTable.addToReadyList(this.readyList, bcp); // Adiciona de volta à lista de prontos
                    bcp.processStatus = "pronto";
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
                // writer.println("Creditos redistribuidos");
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