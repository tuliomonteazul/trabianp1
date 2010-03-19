package algoritmo;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Ladrao extends ProgramaLadrao {
	// valores da ação
	private static final int NAD = 0;
	private static final int CIM = 1;
	private static final int BAI = 2;
	private static final int DIR = 3;
	private static final int ESQ = 4;
	
	// valores da visão (NAO MEXER)
	private static final int SEM_VISAO = -2;
	private static final int FORA_AMBIENTE = -1;
	private static final int CELULA_VAZIA = 0;
	private static final int PAREDE = 1;
	private static final int BANCO = 3;
	private static final int MOEDA = 4;
	private static final int PASTILHA = 5;
	private static final int POUPADOR = 100;
	private static final int LADRAO = 200;
	
	
	// valores ponderados (HEURISTICA)
	private static final int OLFATO_POUP = 10;
	private static final int OLFATO_LADRAO = -10;
	private static final int VISAO_VAZIA = 10;
	private static final int VISAO_PAREDE = -20;
	private static final int VISAO_POUP = 20;
	private static final int VISAO_LADRAO = 5;
	private static final int VISAO_MOEDA = -20;
	private static final int VISAO_BANCO = -20;
	
	private int esquerda = 0, direita = 0, cima = 0, baixo = 0;
	private int ultimaAcao = 0;
	private Point ultimoPonto;
	private int[] mtzDecisao;
	
	
	private static final List<Integer> areaEsquerda = Arrays.asList(new Integer[] {0, 1, 5, 6, 10, 11, 14, 15, 19, 20});
	private static final List<Integer> areaDireita = Arrays.asList(new Integer[] {3, 4, 8, 9, 12, 13, 17, 18, 22, 23});
	private static final List<Integer> areaCima = Arrays.asList(new Integer[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
	private static final List<Integer> areaBaixo = Arrays.asList(new Integer[] {14, 15, 16, 17, 18, 19, 20, 21, 22, 23});
	
	
	public int acao() {
		
		mtzDecisao = new int[] {
				0, 0, 0, 0, 0,
				0, 0, 0, 0, 0,
				0, 0, 0, 0, 0,
				0, 0, 0, 0, 0,
				0, 0, 0, 0, 0
		};
		
		int acao = (int) (Math.random() * 5);
//		int maiorOlfatoPoupador = 0;
//
//		for (int olfato : sensor.getAmbienteOlfatoPoupador()){
//			// se o olfato for o maior (mais recente)
//			if (olfato < maiorOlfatoPoupador && olfato != 0) {
//				maiorOlfatoPoupador = olfato;
//				// poupador passou pela esquerda
//				if (i == 3) {
//					retorno = ESQ;
//				
//				// poupador passou pela esquerda e por cima
//				}else if (i == 0) {
//					retorno = CIM;
//					if (ultimaAcao == CIM) {
//						retorno = ESQ;
//					}
//					
//				// poupador passou por cima
//				}else if (i == 1) {
//					retorno = CIM;
//					
//				// poupador passou pela direita e por cima
//				}else if (i == 0) {
//					retorno = CIM;
//					if (ultimaAcao == CIM) {
//						retorno = DIR;
//					}
//				
//				// poupador a direita
//				} else if (i == 4) {
//					retorno = DIR;
//						
//				// poupador passou pela esquerda e por baixo
//				}else if (i == 0) {
//					retorno = BAI;
//					if (ultimaAcao == BAI) {
//						retorno = ESQ;
//					}
//				// poupador a baixo
//				} else if (i == 6) {
//					retorno = BAI;
//					
//				// poupador passou pela direita e por baixo
//				}else if (i == 0) {
//					retorno = BAI;
//					if (ultimaAcao == BAI) {
//						retorno = DIR;
//					}
//				}
//			}
//			
//			
//			i++;
//		}
		
		montarMatrizDecisao(acao);
		acao = realizarAcao(acao);
		
		ultimaAcao = acao;
		ultimoPonto = sensor.getPosicao();
		return acao;
	}

	private void montarMatrizDecisao(int retornoAtual) {
		
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
			} else if (visao == CELULA_VAZIA) {
				mtzDecisao[i] = VISAO_VAZIA;
			}
			
			ponderacaoMovimentoImediato(i);
			
			
			i++;
		}
		
	}
	

	private void ponderacaoMovimentoImediato(int i) {
		if (i == 7 || i == 11 || i == 12 || i == 16) {
			mtzDecisao[i] = mtzDecisao[i] * 10;
		}
	}

	private int realizarAcao(int acaoAtual) {
		
		int acao = acaoAtual;
		esquerda = 0;
		direita = 0;
		cima = 0;
		baixo = 0;
		final List<Integer> acoes = new ArrayList<Integer>();
		
		ponderarUltimaAcao();
		
		for (int i = 0; i < mtzDecisao.length; i++){
			// se o ponto estiver a esquerda
			if (areaEsquerda.contains(i)) {
				// acao de ir pra esquerda recebe o valor ponderado na posicao i da matriz
				esquerda += mtzDecisao[i]; 
			}
			// se o ponto estiver a direita
			if (areaDireita.contains(i)) {
				// acao de ir pra direita recebe o valor ponderado na posicao i da matriz
				direita += mtzDecisao[i]; 
			}
			// se o ponto estiver a cima
			if (areaCima.contains(i)) {
				// acao de ir pra cima recebe o valor ponderado na posicao i da matriz
				cima += mtzDecisao[i]; 
			}
			// se o ponto estiver a baixo
			if (areaBaixo.contains(i)) {
				// acao de ir pra baixo recebe o valor ponderado na posicao i da matriz
				baixo += mtzDecisao[i]; 
			}
		}
		
		acoes.add(esquerda);
		acoes.add(direita);
		acoes.add(cima);
		acoes.add(baixo);
		Collections.sort(acoes);
		
		// valor maximo é o ultimo valor da lista
		final int max = acoes.get(acoes.size() - 1);
		
		if (max == esquerda) {
			acao = ESQ;
		} else if (max == direita) {
			acao = DIR;
		} else if (max == cima) {
			acao = CIM;
		} else if (max == baixo) {
			acao = BAI;
		}
		
		return acao;
	}
	
	private void ponderarUltimaAcao() {
		int pondUltimaPos = -100;
		if (verificarMudancaPosicao()) {
			pondUltimaPos = 100;
		}
		
		if (ultimaAcao == ESQ) {
			esquerda += pondUltimaPos;
		} else if (ultimaAcao == DIR) {
			direita += pondUltimaPos;
		} else if (ultimaAcao == CIM) {
			cima += pondUltimaPos;
		} else if (ultimaAcao == BAI) {
			baixo += pondUltimaPos;
		}
	}

	private boolean verificarMudancaPosicao(){
		Point point = sensor.getPosicao();
		boolean mudou = false;
		if (point != null && ultimoPonto != null) {
			if (point.getX() != ultimoPonto.getX()) {
				mudou = true;
			} else if (point.getY() != ultimoPonto.getY()) {
				mudou = true;
			}
		}
		return mudou;
	}
	

}