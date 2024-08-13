package com.imaginnovate.demo.batch;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import com.imaginnovate.demo.dto.EmployeeDTO;
import com.imaginnovate.demo.entity.Employee;
import com.imaginnovate.demo.entity.ErrorLog;
import com.imaginnovate.demo.entity.PhoneNumber;
import com.imaginnovate.demo.repository.BatchProcessRepository;
import com.imaginnovate.demo.repository.EmployeeRepository;
import com.imaginnovate.demo.repository.ErrorLogRepository;
import com.imaginnovate.demo.repository.PhoneNumberRepository;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

	@Autowired
	private JobRepository jobRepository;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private PhoneNumberRepository phoneNumberRepository;

	@Autowired
	private BatchProcessRepository batchProcessRepository;

	@Autowired
	private ErrorLogRepository errorLogRepository;

	@Value("${input.file.path}")
	private String inputFilePath;

	@Bean
	public Job employeeJob() {
		return new JobBuilder("employeeJob", jobRepository).incrementer(new RunIdIncrementer()).start(employeeStep())
				.build();
	}

	@Bean
	public Step employeeStep() {
		return new StepBuilder("employeeStep", jobRepository).<EmployeeDTO, Employee>chunk(100, transactionManager)
				.reader(csvReader()).processor(processor()).writer(writer()).listener(new BatchJobCompletionListener())
				.build();
	}

	@Bean
	public FlatFileItemReader<EmployeeDTO> csvReader() {
		return new FlatFileItemReaderBuilder<EmployeeDTO>().name("employeeItemReader")
				.resource(new FileSystemResource(inputFilePath)).delimited()
				.names(new String[] { "employeeId", "firstName", "lastName", "email", "phoneNumber", "doj", "salary" })
				.fieldSetMapper(new EmployeeFieldSetMapper()).build();
	}

	@Bean
	public ItemProcessor<EmployeeDTO, Employee> processor() {
		return employeeDTO -> {
			Optional<Employee> existingEmployee = employeeRepository.findById(employeeDTO.getEmployeeId());
			Employee employee = existingEmployee.orElse(new Employee());

			employee.setFirstName(employeeDTO.getFirstName());
			employee.setLastName(employeeDTO.getLastName());
			employee.setEmail(employeeDTO.getEmail());
			employee.setDoj(employeeDTO.getDoj());
			employee.setSalary(employeeDTO.getSalary());

			phoneNumberRepository.deleteAll(phoneNumberRepository.findByEmployee_EmployeeId(employee.getEmployeeId()));
			Set<PhoneNumber> phoneNumbers = new HashSet<>();
			for (String phone : employeeDTO.getPhoneNumber()) {
				PhoneNumber phoneNumber = new PhoneNumber();
				phoneNumber.setPhoneNumber(phone);
				phoneNumber.setEmployee(employee);
				phoneNumbers.add(phoneNumber);
			}
			employee.setPhoneNumbers(phoneNumbers);

			return employee;
		};
	}

	@Bean
	public ItemWriter<Employee> writer() {

		return employees -> {
			for (Employee employee : employees) {
				try {
					employeeRepository.save(employee);
					employee.getPhoneNumbers().forEach(phoneNumber -> phoneNumberRepository.save(phoneNumber));

				} catch (Exception e) {
					ErrorLog errorLog = new ErrorLog();
					errorLog.setTimestamp(new Timestamp(System.currentTimeMillis()));
					errorLog.setFilename("employees.csv");
					errorLog.setMessage("Error processing Employee ID: " + employee.getEmployeeId());
					errorLog.setActualError(e.getMessage());
					errorLogRepository.save(errorLog);
				}
			}
		};
	}

}
