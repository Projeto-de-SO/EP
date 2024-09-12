#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define PROCESS_BLOCKED 0
#define PROCESS_READY 1
#define PROCESS_RUNNING 2

#define INITIAL_CAPACITY 10
#define MAX_PROCESSESS 10
#define MAX_COMMANDS 21
#define MAX_CHARS 8

typedef struct {
    char programName[7];
    char programCounter[3];
    int processStatus;
    int reg_X;
    int reg_Y;
    char** p_COM;
    int processQuantum;
    int processCredits;
} BCP;

typedef struct {
    int process_id;
    BCP* process;
    BCP* next_process;
} ProcessTable;

/*Funcao para contar as linhas do documento. Será muito util na hora
de atribuir os comandos de cada documento ao vetor de strings p_COM*/

int countLines(FILE* file) {
    int lines = 0;
    char ch;

    // Move o ponteiro de leitura para o início do arquivo
    long originalPos = ftell(file);
    if (originalPos == -1) {
        return -1; // Retorna um valor de erro em caso de falha na chamada a ftell
    }

    // Conta as linhas no arquivo
    while((ch = fgetc(file)) != EOF) {
        if (ch == '\n') {
            lines++;
        }
    }

    // Retorna o ponteiro de leitura para o início do arquivo
    fseek(file, originalPos, SEEK_SET);

    return lines;
}

int loadQuantum(){
    FILE *file;
    int quantum;

    // Abre o arquivo para leitura
    file = fopen("programas\\quantum.txt", "r");

    // Verifica se o arquivo foi aberto corretamente
    if (file == NULL) {
        perror("Failed on opening quantum file");
        exit(EXIT_FAILURE); // Retorna um código de erro
    }

    // Lê um inteiro do arquivo
    if (fscanf(file, "%d", &quantum) != 1) {
        // Verifica se a leitura foi bem-sucedida
        perror("Failed on reading quantum integer");
        fclose(file);
        return 1; 
    }

    fclose(file);

    return quantum; // Sucesso
}

int* loadPriorities() {
    FILE *file = fopen("programas\\prioridades.txt", "r");
    if (file == NULL) {
        printf("Failed on opening priority file\n");
        exit(EXIT_FAILURE);
    }

    // Aloca memória para o vetor com tamanho fixo
    int *priorities = (int *)malloc(sizeof(int) * MAX_PROCESSESS);
    if (priorities == NULL) {
        printf("Failed on allocating memory\n");
        fclose(file);
        exit(EXIT_FAILURE);
    }

    // Lê os inteiros do arquivo e armazena no vetor
    int i = 0;
    while (fscanf(file, "%d", &priorities[i]) == 1 && i < MAX_PROCESSESS) {
        i++;
    }

    // Fecha o arquivo
    fclose(file);

    // Se o número de inteiros lidos for menor que MAX_PROCESSESS, realoca a memória
    if (i < MAX_PROCESSESS) {
        priorities = (int *)realloc(priorities, sizeof(int) * i);
        if (priorities == NULL) {
            printf("Failed on reallocating memory\n");
            exit(EXIT_FAILURE);
        }
    }

    return priorities;
}

/*Funcao para abrir o arquivo*/

FILE* openFile(char* fileName){
    FILE* file = fopen(fileName, "r");
    if(file == NULL){
        printf("Failed on opening file %s\n", fileName);
        exit(EXIT_FAILURE);
    }
    
    return file;
}

/*Funcao que retorna um vetor de strings, cada posicao do vetor contem uma do arquivo*/
char** loadCommandsFromFile(FILE* file){
    int length = countLines(file);
    char** loadedCommands = malloc(sizeof(char*)*length);//alocando o espaco necessario para o vetor de strings que armazenara os comandos
    char buffer[MAX_CHARS];
    int i = 0;

    while(fgets(buffer, MAX_CHARS, file)){
       if(buffer[0]=='\n') continue;

       buffer[strcspn(buffer, "\n")] = 0;

       loadedCommands[i] = malloc(strlen(buffer)+1);
       strcpy(loadedCommands[i], buffer);
    //    printf("loadedProcessCommands[%d] = %s\n", i, loadedCommands[i]);
        i++;
    }
    loadedCommands[i] = NULL;
    return loadedCommands;
}

BCP* initializeProcess(FILE* file, int quantum, int priority){
    BCP* process;
}

ProcessTable* initializeTable(){
    
}

void main() {
    int quantum = loadQuantum();
    int* priorities = loadPriorities();
    ProcessTable *currentJobs;
    char fileName[20];
    

    FILE* file = openFile("programas\\01.txt");
    char** commands = loadCommandsFromFile(file);

    int i = 0;
    while(commands[i]){
        printf("Command %d: %s\n", i+1, commands[i]);
        i++;
    }

    fclose(file);
}
