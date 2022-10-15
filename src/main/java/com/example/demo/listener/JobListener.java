package com.example.demo.listener;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.configuration.xml.JobExecutionListenerParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class JobListener extends JobExecutionListenerParser{

	private static final Logger LOG = LoggerFactory.getLogger(JobListener.class);
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@AfterJob
	public void afterJob(JobExecution execution) {
	
		if(execution.getStatus() == BatchStatus.COMPLETED) {
			LOG.info("FINALIZO EL JOB, VERIFICA LOS RESULTADOS");
			
			Date start = execution.getStartTime();
			Date end   = execution.getEndTime();
			
			long diff = end.getTime() - start.getTime();
			long total = jdbcTemplate.queryForObject("SELECT  count(1) from persona_bach", Long.class);
			
			LOG.info("Tiempo de execucion : {}", TimeUnit.SECONDS.convert(diff,TimeUnit.MILLISECONDS));
			LOG.info("Registros {}",total);
		}
	}
	
}
