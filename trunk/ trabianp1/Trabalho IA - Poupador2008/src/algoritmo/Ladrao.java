package algoritmo;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Ladrao extends ProgramaLadrao {
	// valores da ação
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
	private static final int VISAO_FORA = -5;
	private static final int VISAO_VAZIA = 20;
	private static final int VISAO_PAREDE = -5;
	private static final int VISAO_POUP = 200;
	private static final int VISAO_LADRAO = -1;
	private static final int VISAO_MOEDA = -5;
	private static final int VISAO_BANCO = -5;
	
	private int esquerda = 0, direita = 0, cima = 0, baixo = 0;
	private int ultimaAcao = 0;
	private Point ultimoPonto;
	private int[] mtzDecisao;
	private boolean perseguindo;
	private int poupPerseguido;
	private int pondUltimaPos;
	private int numMoedas;
	
	
	private static final List<Integer> areaEsquerda = Arrays.asList(new Integer[] {0, 1, 5, 6, 10, 11, 14, 15, 19, 20});
	private static final List<Integer> areaDireita = Arrays.asList(new Integer[] {3, 4, 8, 9, 12, 13, 17, 18, 22, 23});
	private static final List<Integer> areaCima = Arrays.asList(new Integer[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
	private static final List<Integer> areaBaixo = Arrays.asList(new Integer[] {14, 15, 16, 17, 18, 19, 20, 21, 22, 23});
	
	// representa as posicoes da matriz de olfato(3x3) na matriz de decisao(5x5)
	private static final int[] olfatoDecisao = {6, 7, 8, 11, 12, 15, 16, 17};
	private static final int[] pontoMatrizX = {-2, -1, 0, 1, 2,
											   -2, -1, 0, 1, 2,
											   -2, -1, 0, 1, 2,
											   -2, -1, 0, 1, 2
											   -2, -1, 0, 1, 2};
	private static final int[] pontoMatrizY = {-2, -2, -2, -2, -2,
											   -1, -1, -1, -1, -1,
												0, 0, 0, 0, 0,
											    1, 1, 1, 1, 1,
											    2, 2, 2, 2, 2};
	
	private HashMap<Ponto, Integer> pontosPercorridos = new HashMap<Ponto, Integer>();
	
	public int acao() {
		
		mtzDecisao = new int[] {
				0, 0, 0, 0, 0,
				0, 0, 0, 0, 0,
				0, 0, 0, 0, 0,
				0, 0, 0, 0, 0,
				0, 0, 0, 0, 0
		};
		
		int acao = 0;
		
		montarMatrizDecisao(acao);
		acao = realizarAcao(acao);
		
		ultimaAcao = acao;
		ultimoPonto = sensor.getPosicao();
		
		adicionarUltimoPontoPercorrido();
		
		numMoedas = sensor.getNumeroDeMoedas();
			
		return acao;
	}

	private void montarMatrizDecisao(int retornoAtual) {
		
		// Utilizando a visão
		int i = 0;
		boolean viuPoup = false;
		for (int visao : sensor.getVisaoIdentificacao()){
			// se houver outro ladrao
			if (visao >= LADRAO) {
				mtzDecisao[i] = VISAO_LADRAO; 
			} else if (visao >= POUPADOR && visao < LADRAO) {
				// adiciona ponderacao para ir na direcao do poupador caso ja
				// nao tenha roubado moedas e caso nao tenham ja 2 ladroes nele
				if (!roubouMoedas() && !temDoisLadroes()) {
					mtzDecisao[i] = VISAO_POUP;
					viuPoup = true;
					poupPerseguido = visao;
				}
			} else if (visao == PAREDE) {
				mtzDecisao[i] = VISAO_PAREDE;
			} else if (visao == MOEDA || visao == PASTILHA) {
				mtzDecisao[i] = VISAO_MOEDA;
			} else if (visao == BANCO) {
				mtzDecisao[i] = VISAO_BANCO;
			} else if (visao == CELULA_VAZIA) {
				mtzDecisao[i] = VISAO_VAZIA;
				if (!perseguindo) {
					ponderarPontosPercorridos(i);
				}
			} else if (visao == FORA_AMBIENTE || visao == SEM_VISAO) {
				mtzDecisao[i] = VISAO_FORA;
			}
			
			ponderacaoMovimentoImediato(visao, i);
			
			i++;
		}
		
		perseguindo = viuPoup;
		
		// Utilizando olfato
		int[] mtzOlfatoPonderado = ponderarOlfato(sensor.getAmbienteOlfatoPoupador());
		
		// posicao da matriz olfato relativa na matriz decisao
		int posicaoRelativa = 0;
		
		i = 0;
		for (int olfato : mtzOlfatoPonderado){
			posicaoRelativa = olfatoDecisao[i];
			mtzDecisao[posicaoRelativa] = mtzDecisao[posicaoRelativa] * olfato;
			i++;
		}
		
	}
	
	private boolean temDoisLadroes() {
		int i = 0;
		int ladroes = 0;
		for (int visao : sensor.getVisaoIdentificacao()){
			// se ver outro ladrao
			if (visao >= LADRAO) {
				 ladroes++;
			}
			
			i++;
		}
		// retorna true se ver 2 ou mais ladroes
		return (ladroes >= 2);
	}

	private boolean roubouMoedas() {
		boolean roubou = false;
		// se tiver roubado moedas
		if (sensor.getNumeroDeMoedas() > numMoedas) {
			roubou = true;
		}
		return roubou;
	}

	private void adicionarUltimoPontoPercorrido() {
		// se nao estiver em perseguicao
		if (!perseguindo) {
			int valorUltimoPonto = 0;
			if (pontosPercorridos.get(ultimoPonto) != null) {
				// busca o valor do ultimo ponto corrido
				valorUltimoPonto = pontosPercorridos.get(ultimoPonto);
			}
			
			if (valorUltimoPonto > -20) {
				// decrementa o valor do ultimo ponto para diminuir a chance de passar pelo mesmo ponto
				pontosPercorridos.put(new Ponto(ultimoPonto), valorUltimoPonto - 5);
			} else {
				pontosPercorridos.put(new Ponto(ultimoPonto), 0);
			}
		}
	}
	
	private void ponderarPontosPercorridos(int i) {
		Ponto ponto = encontrarPonto(i);
		if (pontosPercorridos.containsKey(ponto)) {
			mtzDecisao[i] += pontosPercorridos.get(ponto);
			System.out.println(pontosPercorridos.get(ponto));
		}
	}

	private void ponderacaoMovimentoImediato(int visao, int i) {
		// adiciona maior valor ponderado para os movimentos imediatos
		if (i == 7 || i == 11 || i == 12 || i == 16) {
			// valor entre 5 e 10 para multiplicar a acao de movimento imediato
			mtzDecisao[i] = mtzDecisao[i] * ((int) (Math.random() * 5) + 5);
		}
	}
	
	private int[] ponderarOlfato(int[] ambienteOlfatoPoupador) {
		int[] mtzPonderada = new int[8];
		
		// percorre a matriz de olfato
		for (int i = 0; i < 8; i++) {
			if (ambienteOlfatoPoupador[i] == 0) {
				// se o olfato for 0, pondera como 1 (para ser multiplicado por 1 e não ser alterado o valor)
				mtzPonderada[i] = 1;
				
			} else if (ambienteOlfatoPoupador[i] >= 1) {
				// se o olfato for maior que 1, pondera com o modulo da
				// diferença do valor por 6, ex. para valor = 1, o valor
				// ponderado = 5
				mtzPonderada[i] = Math.abs(ambienteOlfatoPoupador[i] - 6);
			}
		}
		
		return mtzPonderada;
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
		// se a ultima acao nao tiver mudad
		pondUltimaPos = -1000;
		// verifica se a ultima acao houve mudanca de posicao
		if (verificarMudancaPosicao()) {
			pondUltimaPos = 20;
		}

		// adiciona um valor a proxima acao caso ela seja igual a ultimaAcao de
		// acordo com a mudanca de posicao
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
	
	private Ponto encontrarPonto(int i) {
		int xAtual = (int) sensor.getPosicao().getX();
		int yAtual = (int) sensor.getPosicao().getY();
		Ponto ponto = new Ponto(xAtual + pontoMatrizX[i], yAtual + pontoMatrizY[i]);
		return ponto;
	}

}

class Ponto extends Point {
	
	Ponto(int x, int y) {
		super(x, y);
	}
	
	Ponto(Point point) {
		super(point);
	}
	
	@Override
	public boolean equals(Object objeto) {
		boolean retorno = super.equals(objeto);
		if (objeto instanceof Ponto) {
			Ponto ponto = (Ponto) objeto;
			retorno = (this.getX() == ponto.getX() && this.getY() == ponto.getY());
		}
		return retorno;
	}
}