package algoritmo;

public class Ladrao extends ProgramaLadrao {
	private static final int NAD = 0;
	private static final int NOR = 1;
	private static final int SUL = 2;
	private static final int LES = 3;
	private static final int OES = 4;
	
	private static final int[] MTZ_POSICIONAR_DR = 
		{
		NAD, LES, LES, LES, LES,
		SUL, LES, LES, LES, LES,
		SUL, LES, LES, LES, NAD,
		NAD, NAD, NAD, NAD, NAD,
		NAD, NAD, NAD, NAD, NAD,
		};
	
	private int ultimoMov = 0;
	private boolean dRelacional;
	
	public int acao() {
		int i = 0;
		int retorno = (int) (Math.random() * 5);
		int maiorOlfatoPoupador = 0;

		for (int olfato : sensor.getAmbienteOlfatoPoupador()){
			// se o olfato for o maior (mais recente)
			if (olfato > maiorOlfatoPoupador) {
				maiorOlfatoPoupador = olfato;
				// poupador passou pela esquerda
				if (i == 3) {
					retorno = OES;
					System.out.println("olfato - movendo pra esquerda");
				
				// poupador passou pela esquerda e por cima
				}else if (i == 0) {
					retorno = NOR;
					if (ultimoMov == NOR) {
						retorno = OES;
						System.out.println("olfato - movendo pra esquerda");
					} else
						System.out.println("olfato - movendo pra cima");
					
				// poupador passou por cima
				}else if (i == 1) {
					retorno = NOR;
					System.out.println("olfato - movendo pra cima");
					
				// poupador passou pela direita e por cima
				}else if (i == 0) {
					retorno = NOR;
					if (ultimoMov == NOR) {
						retorno = LES;
						System.out.println("olfato - movendo pra direita");
					} else
						System.out.println("olfato - movendo pra cima");
				
				// poupador a direita
				} else if (i == 4) {
					retorno = LES;
					System.out.println("olfato - movendo pra direita");
						
				// poupador passou pela esquerda e por baixo
				}else if (i == 0) {
					retorno = SUL;
					if (ultimoMov == SUL) {
						retorno = OES;
						System.out.println("olfato - movendo pra esquerda");
					} else
						System.out.println("olfato - movendo pra baixo");
					
				// poupador a baixo
				} else if (i == 6) {
					retorno = SUL;
					System.out.println("olfato - movendo pra baixo");
					
				// poupador passou pela direita e por baixo
				}else if (i == 0) {
					retorno = SUL;
					if (ultimoMov == SUL) {
						retorno = LES;
						System.out.println("olfato - movendo pra direita");
					} else
						System.out.println("olfato - movendo pra baixo");
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
					retorno = OES;
					System.out.println("movendo pra esquerda");
				}else if (i < 10) {
					// poupador a cima
					if (i == 2) {
						retorno = NOR;
						System.out.println("movendo pra cima");
						
					// poupador a esquerda e a cima
					} else if (i % 5 <= 1) {
						retorno = OES;
						System.out.println("movendo pra esquerda");
						
					// poupador a direita e a cima
					} else {
						retorno = LES;
						System.out.println("movendo pra direita");
					}
					
				// poupador a direita
				} else if (i == 13) {
					retorno = LES;
					System.out.println("movendo pra direita");
				
				// poupador a baixo
				} else if (i == 21) {
					retorno = SUL;
					System.out.println("movendo pra baixo");
					
				// poupador a esquerda e a baixo
				} else if (i % 5 <= 1) {
					retorno = OES;
					System.out.println("movendo pra esquerda");
			
				// poupador a direita e a baixo
				} else {
					retorno = LES;
					System.out.println("movendo pra direita");
				}
			}
			
			
			i++;
		}
		
		retorno = verificarDRelacional(retorno);
		
		ultimoMov = retorno;
		return retorno;
	}

	private int verificarDRelacional(int retornoAtual) {
		
		int retorno = retornoAtual;
		int i = 0;
		for (int visao : sensor.getVisaoIdentificacao()){
			// se houver outro ladrao
			if (visao >= 200 && visao <= 290) {
				if (i == 0 || i == 4 || i == 19 || i == 23) {
					dRelacional = true;
					System.out.println("dRELACIONAL");
				}
				// se nao estiver em dRelacional
//				if (!dRelacional) {
					retorno = MTZ_POSICIONAR_DR[i];
//				}
			}
			
			
			i++;
		}
		return retorno;
	}
	
	
}