#Escalonador de Processos

##Tabela de Conteudo
-Funcoes criadas no codigo e seu proprosito para resolver o problema

###Funcoes

1.int countLines(FILE* file)
-Funcao usada para contar as linhas dentro de um arquivo, ou seja, dentro do processo a ser carregado
2.int loadQuantum()
-Funcao que carrega o inteiro dentro do arquivo quantum.txt e retorna o valor
3.int* loadPriorities(int _size)
-Funcao que abre o arquivo de prioridades e retorna um array de inteiros com as prioridades contidas no arquivo em ordem
4.char\*\* loadCommandsFromFile(FILE_ file)
-Funcao que carrega os comandos de um arquivo e retorna um array de strings, cada posicao do array eh uma comando do processo
5.initializeProcess()
-Funcao para inicializar o processo (praticamente um construtor)

###Funcoes que ainda faltam ser implementadas

1.initializeProcess()
-Funcao para inicializar o processo (praticamente um construtor)
2.ProcessTable\* initializeTable()
-Funcao que inicializa a tabela de processos. Nessa funcao, cada processo individual sera inicializado, ou seja, a funcao de inicializar processo sera chamada dentro dessa funcao, entao a tabela de processos vai criar um array de processos contendo cada uma das informacoes necessarias para poder gerar a tabela de processos.
