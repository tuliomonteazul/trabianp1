package algoritmo;

public class Ladrao extends ProgramaLadrao {
	// valores da ação
	private static final int NAD = 0;
	private static final int CIM = 1;
	private static final int BAI = 2;
	private static final int DIR = 3;
	private static final int ESQ = 4;
	
	// valores da visão
	private static final int SEM_VISAO = -2;
	private static final int FORA_AMBIENTE = -1;
	private static final int CELULA_VAZIA = 0;
	private static final int PAREDE = 1;
	private static final int BANCO = 3;
	private static final int MOEDA = 4;
	private static final int PASTILHA = 5;
	private static final int POUPADOR = 100;
	private static final int LADRAO = 200;
	
	
	// valores ponderados
	private static final int OLFATO_POUP = 10;
	private static final int OLFATO_LADRAO = -10;
	private static final int VISAO_PAREDE = -20;
	private static final int VISAO_POUP = 20;
	private static final int VISAO_LADRAO = -10;
	private static final int VISAO_MOEDA = -10;
	private static final int VISAO_BANCO = -10;
	
	private int ultimoMov = 0;
	
	public int acao() {
		
		int[] mtzDecisao = {
				0, 0, 0, 0, 0,
				0, 0, 0, 0, 0,
				0, 0, 0, 0, 0,
				0, 0, 0, 0, 0,
				0, 0, 0, 0, 0
		};
		
		int i = 0;
		int retorno = (int) (Math.random() * 5);
		int maiorOlfatoPoupador = 0;

		for (int olfato : sensor.getAmbienteOlfatoPoupador()){
			// se o olfato for o maior (mais recente)
			if (olfato < maiorOlfatoPoupador && olfato != 0) {
				maiorOlfatoPoupador = olfato;
				// poupador passou pela esquerda
				if (i == 3) {
					retorno = ESQ;
				
				// poupador passou pela esquerda e por cima
				}else if (i == 0) {
					retorno = CIM;
					if (ultimoMov == CIM) {
						retorno = ESQ;
					}
					
				// poupador passou por cima
				}else if (i == 1) {
					retorno = CIM;
					
				// poupador passou pela direita e por cima
				}else if (i == 0) {
					retorno = CIM;
					if (ultimoMov == CIM) {
						retorno = DIR;
					}
				
				// poupador a direita
				} else if (i == 4) {
					retorno = DIR;
						
				// poupador passou pela esquerda e por baixo
				}else if (i == 0) {
					retorno = BAI;
					if (ultimoMov == BAI) {
						retorno = ESQ;
					}
				// poupador a baixo
				} else if (i == 6) {
					retorno = BAI;
					
				// poupador passou pela direita e por baixo
				}else if (i == 0) {
					retorno = BAI;
					if (ultimoMov == BAI) {
						retorno = DIR;
					}
				}
			}
			
			
			i++;
		}
		
		i = 0;
		for (int visao : sensor.getVisaoIdentificacao()){
			// se houver poupador
			if (visao >= 100 && visao <= 190) {
				// poupador a esquerda
				if (i == 10) {
					retorno = ESQ;
				}else if (i < 10) {
					// poupador a cima
					if (i == 2) {
						retorno = CIM;
						
					// poupador a esquerda e a cima
					} else if (i % 5 <= 1) {
						retorno = ESQ;
						
					// poupador a direita e a cima
					} else {
						retorno = DIR;
					}
					
				// poupador a direita
				} else if (i == 13) {
					retorno = DIR;
				
				// poupador a baixo
				} else if (i == 21) {
					retorno = BAI;
					
				// poupador a esquerda e a baixo
				} else if (i % 5 <= 1) {
					retorno = ESQ;
			
				// poupador a direita e a baixo
				} else {
					retorno = DIR;
				}
			}
			
			
			i++;
		}
		
		ultimoMov = retorno;
		return retorno;
	}

	private int verificarVisao(int retornoAtual, int[] mtzDecisao) {
		
		int retorno = retornoAtual;
		int i = 0;
		for (int visao : sensor.getVisaoIdentificacao()){
			// se houver outro ladrao
			if (visao >= LADRAO) {
				mtzDecisao[i] = VISAO_LADRAO; 
			} else if (visao >= POUPADOR && visao < LADRAO) {
				mtzDecisao[i] = VISAO_POUP;
			} else if (visao == PAREDE) {
				mtzDecisao[i] = VISAO_PAREDE;
			} else if (visao == MOEDA || visao == PASTILHA) {
				mtzDecisao[i] = VISAO_MOEDA;
			} else if (visao == BANCO) {
				mtzDecisao[i] = VISAO_MOEDA;
			}
			
			
			i++;
		}
		return retorno;
	}
	

}