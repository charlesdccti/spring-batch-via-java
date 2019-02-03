package com.mb.teste.batch;

import org.springframework.batch.item.ItemProcessor;

public class PessoaItemProcessor implements ItemProcessor<Pessoa, Pessoa> {

	/** 
	 * Compõem o principal momento dentro do fluxo de um step, pois é aqui que todas as regras 
	 * de negócio, validações, verificações e outros tratamentos devem ser realizados para que a informação 
	 * coletada da fonte de entrada de dados esteja, enfim, absolutamente condizente com os objetos da aplicação. 
	 * Logo, os item processors são os agentes que efetivamente transformarão a informação original e darão, a 
	 * ela, uma roupagem mais completa, mais rica, adquirida a partir de todo um conjunto de regras e características 
	 * ditadas pela aplicação.
	 */
	@Override
	public Pessoa process(Pessoa pessoa) throws Exception {
		System.out.println("Entrou no processor");
		final String firstName = pessoa.getNome().toUpperCase();
		final String lastName = pessoa.getSobrenome().toUpperCase();

		final Pessoa transformed = new Pessoa(firstName, lastName);

		System.out.println("Converting (" + pessoa + ") into (" + transformed + ")");

		return transformed;
	}
}
