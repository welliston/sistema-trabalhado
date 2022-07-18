package com.projetoTrabalhador.service;

import java.util.Calendar;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.projetoTrabalhador.entities.HourContract;
import com.projetoTrabalhador.entities.Worker;
import com.projetoTrabalhador.repository.DepartmentRepository;
import com.projetoTrabalhador.repository.WorkerRepository;
import com.projetoTrabalhador.requests.WorkerPostRequestBody;
import com.projetoTrabalhador.requests.WorkerPutRequestBody;
import com.projetoTrabalhador.service.exceptions.DataBaseError;
import com.projetoTrabalhador.service.exceptions.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkerService implements UserDetailsService{
	
	private final WorkerRepository repository;

	private final DepartmentRepository departmentRepository;

	public List<Worker> findAll() {
		return repository.findAll();
	}

	public Worker findById(long id) {
		Optional<Worker> obj = repository.findById(id);
		return obj.orElseThrow(() -> new ResourceNotFoundException(id));
	}

	public Worker insert(WorkerPostRequestBody workerPostRequestBody, long id) {
		PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
		
		Worker worker = Worker.builder()
		.name(workerPostRequestBody.getName())
		.userName(workerPostRequestBody.getUserName())
		.baseSalary(workerPostRequestBody.getBaseSalary())
		.password(passwordEncoder.encode(workerPostRequestBody.getPassword()))
		.authorities(workerPostRequestBody.getAuthorities())
		.department(departmentRepository.findById(id).get())
		.build();
										
		
		return repository.save(worker);
	}

	public void delete(long id) {

		try {
			repository.deleteById(id);
		} catch (EmptyResultDataAccessException e) {
			throw new DataBaseError(e.getMessage());
		}
	}

	public void update(WorkerPutRequestBody workerPutRequestBody) {
		PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
		try {
		Worker workerSaved = repository.findById(workerPutRequestBody.getId()).get();
		
		Worker worker = Worker.builder()
		.id(workerSaved.getId())
		.name(workerPutRequestBody.getName())
		.userName(workerPutRequestBody.getUserName())
		.baseSalary(workerPutRequestBody.getBaseSalary())
		.password(passwordEncoder.encode(workerPutRequestBody.getPassword()))
		.authorities(workerPutRequestBody.getAuthorities())
		.build();
		
		repository.save(worker);
		}catch (NoSuchElementException e) {
			throw new DataBaseError(e.getMessage());
		}
		
	}

	public double income(long id, int year, int month) {
		Worker worker = repository.findById(id).get();
		Double sum = worker.getBaseSalary();

		Set<HourContract> contracts = worker.getContracts();

		Calendar cal = Calendar.getInstance();
		for (HourContract c : contracts) {
			cal.setTime(c.getDate());
			int c_year = cal.get(Calendar.YEAR);
			int c_month = 1 + cal.get(Calendar.MONTH);

			if (year == c_year && month == c_month) {
				sum += c.getValuePerHour() * c.getHour();
			}

		}
		return sum;
	}

	@Override
	public UserDetails loadUserByUsername(String username){
		return Optional.ofNullable(repository.findByuserName(username))
				.orElseThrow(() -> new UsernameNotFoundException("worker user not found"));
	}
}
