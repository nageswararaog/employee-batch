package com.imaginnovate.demo.batch;

import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.imaginnovate.demo.entity.BatchProcess;
import com.imaginnovate.demo.repository.BatchProcessRepository;

@Component
public class BatchJobCompletionListener extends JobExecutionListenerSupport {
	
	@Autowired
	private BatchProcessRepository batchProcessRepository;
	
	@Value("${input.file.path}")
	private String inputFilePath;

		@Override
		public void beforeJob(JobExecution jobExecution) {
			BatchProcess batchProcess = new BatchProcess();
			batchProcess.setProcessName("Employee Upsert Batch Process");
			batchProcess.setStartTimestamp(new Timestamp(System.currentTimeMillis()));
			batchProcess.setProcessedFileName(inputFilePath);
			batchProcessRepository.save(batchProcess);
		}

		@Override
		public void afterJob(JobExecution jobExecution) {
			Optional<BatchProcess> optionalBatchProcess = batchProcessRepository.findById(jobExecution.getJobId().intValue());
			if (optionalBatchProcess.isPresent()) {
				BatchProcess batchProcess = optionalBatchProcess.get();
				batchProcess.setEndTimestamp(new Timestamp(System.currentTimeMillis()));

				// Calculate counts
				long insertedCount = jobExecution.getStepExecutions().stream().mapToLong(StepExecution::getWriteCount)
						.sum();

				int errorCount = jobExecution.getAllFailureExceptions().size();

				// Update batch process record
				batchProcess.setInsertedRecordCount((int) insertedCount);
				batchProcess.setErroredRecordCount(errorCount);
				batchProcess.setProcessedFileName(renameProcessedFile());
				batchProcessRepository.save(batchProcess);
			}
		}

		private String renameProcessedFile() {
			File oldFile = new File(inputFilePath);
			String newFileName = "processed-employees-" + new SimpleDateFormat("MM-dd-yy-HH-mm-ss").format(new Date())
					+ ".csv";
			File newFile = new File(oldFile.getParent() + "/" + newFileName);
			if (oldFile.renameTo(newFile)) {
				return newFile.getName();
			} else {
				return oldFile.getName();
			}
		}
	}