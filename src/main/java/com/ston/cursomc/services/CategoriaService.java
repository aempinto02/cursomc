package com.ston.cursomc.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ston.cursomc.domain.Categoria;
import com.ston.cursomc.repositories.CategoriaRepository;
import com.ston.cursomc.services.exceptions.ObjectNotFoundException;

@Service
public class CategoriaService {

	@Autowired
	private CategoriaRepository repository;

	public Categoria find(Integer id) {
		Optional<Categoria> obj = repository.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException(
				 "Objeto não encontrado! Id: " + id + ", Tipo: " + Categoria.class.getName())); 
	}
	
	public Categoria insert(Categoria categoria) {
		categoria.setId(null);
		return repository.save(categoria);
	}

	public Categoria update(Categoria categoria) {
		find(categoria.getId());
		return repository.save(categoria);
	}
}
