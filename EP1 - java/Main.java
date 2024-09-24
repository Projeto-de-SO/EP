import dependencies.Bcp;
import dependencies.ProcessTable;
import dependencies.Scheduler;

import java.util.*;
import java.io.PrintWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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
        bcp.processStatus = "pronto";

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