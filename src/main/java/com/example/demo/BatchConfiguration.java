package com.example.demo;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.example.demo.listener.JobListener;
import com.example.demo.model.Persona;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

	@Autowired
	public JobBuilderFactory builderFactory;
	
	@Autowired
	public StepBuilderFactory stepBuilderFactory;
	
	@Bean
	public FlatFileItemReader<Persona> reader(){
		return new FlatFileItemReaderBuilder<Persona>()
				.name("personaItemReader")
				.resource(new ClassPathResource("sample 10k.csv"))
				.delimited()
				.names(new String[] {"nombre","apellido","telefono"})
				.fieldSetMapper(new BeanWrapperFieldSetMapper<Persona>() {{
					setTargetType(Persona.class);
				}})
				.linesToSkip(1)
				.build();
	}
	
	@Bean
	public JdbcBatchItemWriter<Persona> writer(DataSource dataSource){
		return new JdbcBatchItemWriterBuilder<Persona>()
				.itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Persona>())
				.sql("insert into persona_bach (nombre,apellido,telefono) values(:nombre,:apellido,:telefono)")
				.dataSource(dataSource)
				.build();
	}
	
	@Bean
	public Job importPersonaJob(JobListener jobListener, Step step) {
		return builderFactory.get("importPersonaJob")
				.incrementer(new RunIdIncrementer())
				.listener(jobListener)
				.flow(step)
				.end()
				.build();
	}
	
	@Bean
	public Step step1(JdbcBatchItemWriter<Persona> writer) {
		return stepBuilderFactory.get("step1")
				.<Persona,Persona>chunk(1000)
				.reader(reader())
				.writer(writer)
				.build();
	}
}
