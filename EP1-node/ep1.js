import { readFileSync, readdir } from "fs";
import { randomUUID } from "node:crypto";

class Bcp {
  programCounter;
  programName;
  pCOM = [];
  processStatus;
  processQuantum;
  processCredits;
  regX;
  regY;
}

class ProcessTable {
  processId;
  process;

  constructor(processId, process) {
    this.processId = processId;
    this.process = process;
  }
}

/**
 * Le um arquivo em disco
 * @param {string} filePath - Caminho do arquivo
 * @returns {string[]} Retorna lista de linhas contidas no arquivo
 */
function readFile(filePath) {
  return readFileSync(filePath, "utf-8")
    .split("\n")
    .filter((l) => l !== "");
}

/**
 * Cria uma instancia de BCP
 * @param {string[]} program - Lista de comandos do programa
 * @param {number} initialQuantum - Valor inicial do quantum
 * @param {number} priority - Prioridade
 * @returns {Bcp} Retorna uma instancia de BCP
 */
function createBCP(program, initialQuantum, priority) {
  const bcp = new Bcp();

  for (let index = 0; index < program.length; index++) {
    if (index === 0) {
      bcp.programName = program[index];
      continue;
    }

    bcp.pCOM.push(program[index]);
  }

  bcp.processCredits = priority;
  bcp.processQuantum = initialQuantum;
  bcp.processStatus = "ready";

  return bcp;
}

function main() {
  const initialQuantum = readFile(`./programas/quantum.txt`)[0];
  const priorities = readFile(`./programas/prioridades.txt`);
  const notProgramFiles = ["quantum.txt", "prioridades.txt"];

  const bcpList = [];
  const processTableList = [];

  // Le o diretorio e percorre todos os arquivos dentro dele
  readdir("./programas/", (_err, files) => {
    for (let index = 0; index < files.length; index++) {
      if (notProgramFiles.includes(files[index])) {
        continue;
      }

      const program = readFile(`./programas/${files[index]}`);

      const bcp = createBCP(
        program,
        parseInt(initialQuantum),
        parseInt(priorities[index])
      );

      const processTable = new ProcessTable(randomUUID(), bcp);

      bcpList.push(bcp);
      processTableList.push(processTable);
    }

    console.log(bcpList);
  });
}

main();
