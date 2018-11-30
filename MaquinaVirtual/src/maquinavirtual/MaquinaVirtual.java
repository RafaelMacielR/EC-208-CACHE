package maquinavirtual;

/*
    Máquina Virtual - EC208

    - Equipe:
        César Augusto Ramos -       1199
        João Ferreira Neto -        692
        Rafael Maciel dos Reis -    977


    - Especificações:

        Tamanho das instruções: 32 bits
	
	Código das operações:
            ADD: 	000001
            SUB: 	000011
            LOAD: 	100001
            STORE:	100010

        Código das instruções:
            Tipo 1:     000001
            Tipo 2:     000010

        Instruções Tipo 1: 
	
            Utilizado para operaçções aritméticas (soma, subtração, ...)
	     
            MSB                                      				      LSB
            (Tipo instr.) (End. Reg 1) (End. Reg 2) (End Reg Dest.) (Deslocamento) (Operação)
                6 bits        5 bits      5 bits       5 bits   	5 bits	     6 bits

 	 	 
        Instruções Tipo 2:
    
            Uitlizado para operações de LOAD e STORE
     	 
            MSB                                             LSB 	 
            (Tipo instr.) (End Reg 1) (Operação) (End Memória de dados)
                6 bits       5 bits      6 bits             15 bits

        
        Memória Cache:

            (Valido)    (TAG)       (Data)
              1 bit     15 bits      8 bits
 */

public class MaquinaVirtual {
    
    /*
        -- Instruções --
    
        LOAD(REG0, END3)      = 000010|00000|100001|000000000000011
        LOAD(REG1, END4)      = 000010|00001|100001|000000000000100
        ADD(REG0, REG1, REG3) = 000001|00000|00001|00010|00000|000001
        SUB(REG0, REG1, REG4) = 000001|00000|00001|00011|00000|000011
        STORE(REG3, END7)     = 000010|00010|100010|000000000000111
    */
    
    // Memoria de programa            
    static int ProgMemory[] = {0b00001000000100001000000000000011,
                               0b00001000001100001000000000000100,
                               0b00000100000000010001000000000001,
                               0b00000100000000010001100000000011,
                               0b00001000010100010000000000000111};
    
    //Memoria de dados
    static int DataMemory[] = { 3, 2, 0, 9, 8, 0, 0, 0, 0, 0, 0, 0};
    
    //Memória Cache
    static int CacheMemory[] = {0b000000000000001100001001, // 0|000000000000011|00001001
                                0b000000000000000100000010};// 0|000000000000001|00000010
    
    // Registradores
    static int PC;
    static int Instr; // Instrução
    static int InstrType; //Tipo da instrução
    static int OpType; //Tipo da operação
    static int RegSource1; //Registrador 1
    static int RegSource2; //Registrador 2
    static int RegDest; //Registrador destino
    static int RegAddrMemory; //Endereço da memória
    static int Reg[] = new int[10]; //Registradores
    static boolean RUN = true; //Controle da execução
    
    static final int CACHE_MISS = -1;
    static final int CACHE_HIT = -2;
    
    public static void main(String[] args) {
        
        while (RUN) {
            Instr = ProgMemory[PC]; //Get instruction
            
            get_instr_type(Instr); //Decode type of instruction
            
            find_data(Instr); //Find datas
            
            execute(Instr); // Execute instruction
            
            PC++;
            
            if(PC == 5) {
                RUN = false;
            }
        }    
    }
    
    
    public static void get_instr_type(int Addr) {
        InstrType = Addr>>26;
    }
    
    public static void find_data(int Addr) {
        
    
        if(InstrType==1) {
            RegSource1 = Addr>>21;
            RegSource1 = RegSource1 & 0b00000000000000000000000000011111;
        
            RegSource2 = Addr>>16;
            RegSource2 = RegSource2 & 0b00000000000000000000000000011111;
            
            RegDest = Addr>>11;
            RegDest = RegDest & 0b00000000000000000000000000011111;
        }
        
        if(InstrType==2) {
            RegSource1 = Addr>>21;
            RegSource1 = RegSource1 & 0b00000000000000000000000000011111;
            
            RegAddrMemory = Addr & 0b00000000000000000111111111111111;
        }
    }
    
    public static void execute(int Addr) {
        switch(InstrType) {
            case 1:
                execute_instr_type1(Addr);
                
                break;
            case 2:
                execute_instr_type2(Addr);
                break;
        }
    }
    
    public static void execute_instr_type1(int Addr) {
        OpType = Addr & 0b00000000000000000000000000111111;
        
        switch(OpType) {
            case 1: //ADD
               Reg[RegDest] = Reg[RegSource1] + Reg[RegSource2];
               break;
            case 3: //SUB
                Reg[RegDest] = Reg[RegSource1] - Reg[RegSource2];
                break;
        }
    }
    
    public static void execute_instr_type2(int Addr) {
        OpType = Addr >> 15;
        OpType = OpType & 0b00000000000000000000000000111111;
        
        switch(OpType) {
            case 33: //LOAD
                Reg[RegSource1] = find_on_cache_memory(RegAddrMemory);
               
                if(Reg[RegSource1] == CACHE_MISS) {
                    Reg[RegSource1] = DataMemory[RegAddrMemory];
                } 
               break;
            case 34: //STORE
                DataMemory[RegAddrMemory] = Reg[RegSource1];
                break;
        }
    }
    
    public static int find_on_cache_memory(int Addr) {        
        for(int i=0; i < CacheMemory.length; i++) {
            int mAddr = CacheMemory[i] >> 8;
            mAddr = mAddr & 0b000000000111111111111111;
            
            int valid = CacheMemory[i] >> 23;
            valid = valid & 0b000000000000000000000001;
            
            if(Addr == mAddr && valid == 0) {
                System.out.println("Endereço: " + Addr + " - CACHE HIT");
                
                return CacheMemory[i] & 0b000000000000000011111111;
            }
            
            System.out.println("Endereço: " + Addr + " - CACHE MISS");
        }
        
        return CACHE_MISS;       
    }
}

