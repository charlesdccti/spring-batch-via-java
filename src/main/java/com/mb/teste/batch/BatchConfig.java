package com.mb.teste.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

	private final JobBuilderFactory jobBuilderFactory;

	private final StepBuilderFactory stepBuilderFactory;

	@Autowired
	PessoaRepository pessoaRepository;

	//private final DataSource dataSource;

	public BatchConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
		// DataSource dataSource) {
		this.jobBuilderFactory = jobBuilderFactory;
		this.stepBuilderFactory = stepBuilderFactory;
		//  this.dataSource = dataSource;
	}

	/*
	 * Todo passo conterá, conforme veremos mais adiante, um componente responsável pela extração de dados. Sua função 
	 * será identificar a origem dos dados a serem tratados, extraí-los e mapeá-los na forma de objetos que serão, 
	 * posteriormente, processados pelos componentes seguintes desta cadeia.
	 * 
	 * Há, na API do Spring Batch, alguns componentes que já apresentam comportamento padrão para os tipos mais comuns 
	 * de fontes de dados. Todos esses componentes derivam da interface org.springframework.batch.item.ItemReader
	 */
	@Bean
	public FlatFileItemReader<Pessoa> reader(){
		System.out.println("Entrou no reader");
		/* 
		   Usado para consumir registros gravados em arquivos de texto (o formato mais comum 
		   empregado na representação de dados em arquivos deste tipo é o CSV
		   
		   Mapeia o CSV para as classe que deseja manipular e depois perssistir na base de dados
		*/
		FlatFileItemReader<Pessoa> reader = new FlatFileItemReader<>();
		reader.setResource(new ClassPathResource("pessoa.csv"));
		reader.setLineMapper(new DefaultLineMapper<Pessoa>() {
			{
				setLineTokenizer(new DelimitedLineTokenizer() {
					{
						setNames(new String[]{"nome", "sobrenome"});
					}
				});
				setFieldSetMapper(new BeanWrapperFieldSetMapper<Pessoa>() {
					{
						setTargetType(Pessoa.class);
					}
				});
			}
		});
		return reader;
	}


	/**
	 *  Retorna um PessoaItemProcessor 
	 *  
	 *  Item processors são componentes empregados por um Step em atividades como transformação, validações e 
	 * 	verificações.
	 */
	@Bean
	public PessoaItemProcessor processor() {
		return new PessoaItemProcessor();
	}

	/*
	 * Item writers são os últimos componentes no fluxo de execução de um step. É aqui que, finalmente, os 
	 * objetos já transformados pelos item processors serão disponibilizados em algum canal de comunicação para consumo 
	 * (que pode ser uma base de dados relacional, um sistema de mensageria, arquivos de texto ou outro).
	 * 
	 * Assim como para item readers, há alguns tipos de objetos com lógica pré-definida para a escrita de itens. 
	 * 
	 */
	@Bean
	public RepositoryItemWriter<Pessoa> writer() {
		System.out.println("Entrou no writer");
		RepositoryItemWriter<Pessoa> writer = new RepositoryItemWriter<>();
		writer.setRepository(pessoaRepository);
		writer.setMethodName("save");
		System.out.println("Salvou pessoa");
		return writer;
	}

	@Bean
	public Job importPessoaJob(JobCompletionNotificationListener listener) {
		return jobBuilderFactory.get("importPessoaJob")
				.incrementer(new RunIdIncrementer())
				.listener(listener)
				.flow(step1())
				.end()
				.build();
	}

	@Bean
	public Step step1() {
		return stepBuilderFactory.get("step1")
				.<Pessoa, Pessoa>chunk(10)
				.reader(reader())
				.processor(processor())
				.writer(writer())
				.build();
	}

}
