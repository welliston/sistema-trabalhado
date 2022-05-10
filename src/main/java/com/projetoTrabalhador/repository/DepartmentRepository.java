package com.projetoTrabalhador.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.projetoTrabalhador.entities.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long>{
	
	Department findById(long id);
}
