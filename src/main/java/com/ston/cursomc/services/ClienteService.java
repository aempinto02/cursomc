package com.ston.cursomc.services;

import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ston.cursomc.domain.Cidade;
import com.ston.cursomc.domain.Cliente;
import com.ston.cursomc.domain.Endereco;
import com.ston.cursomc.domain.enums.Perfil;
import com.ston.cursomc.domain.enums.TipoCliente;
import com.ston.cursomc.dto.ClienteDTO;
import com.ston.cursomc.dto.ClienteNewDTO;
import com.ston.cursomc.repositories.ClienteRepository;
import com.ston.cursomc.repositories.EnderecoRepository;
import com.ston.cursomc.security.UserSS;
import com.ston.cursomc.services.exceptions.AuthorizationException;
import com.ston.cursomc.services.exceptions.DataIntegrityException;
import com.ston.cursomc.services.exceptions.ObjectNotFoundException;

@Service
public class ClienteService {
	
	@Autowired
	private BCryptPasswordEncoder pe;

	@Autowired
	private ClienteRepository repository;

	@Autowired
	private EnderecoRepository endRepository;
	
	@Autowired
	private S3Service s3Service;
	
	@Autowired
	private ImageService imageService;
	
	@Value("${img.prefix.client.profile}")
	private String prefix;
	
	@Value("${img.profile.size}")
	private Integer size;

	public Cliente find(Integer id) {
		
		UserSS user = UserService.authenticated();
		if(user == null || !user.hasRole(Perfil.ADMIN) && !id.equals(user.getId())) {
			throw new AuthorizationException("Acesso negado!");
		}
		
		Optional<Cliente> obj = repository.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException(
				 "Objeto n??o encontrado! Id: " + id + ", Tipo: " + Cliente.class.getName())); 
	}
	
	@Transactional
	public Cliente insert(Cliente cliente) {
		cliente.setId(null);
		cliente = repository.save(cliente);
		endRepository.saveAll(cliente.getEnderecos());
		return cliente;
	}

	public Cliente update(Cliente cliente) {
		Cliente newCliente = find(cliente.getId());
		updateData(newCliente, cliente);
		return repository.save(newCliente);
	}
	
	public void deleteById(Integer id) {
		find(id);
		try {
			repository.deleteById(id);
		} catch(DataIntegrityViolationException e) {
			throw new DataIntegrityException("N??o ?? poss??vel excluir porque h?? pedidos relacionados");
		}
	}

	public List<Cliente> findAll() {
		return repository.findAll();
	}
	
	public Cliente findByEmail(String email) {
		UserSS user = UserService.authenticated();
		if(user == null || !user.hasRole(Perfil.ADMIN) && !email.equals(user.getUsername())) {
			throw new AuthorizationException("Acesso negado");
		}
		
		Cliente cliente = repository.findByEmail(email);
		if(cliente == null) {
			throw new ObjectNotFoundException("Objeto n??o encontrado! Id: " + user.getId()
			+ ", Tipo: " + Cliente.class.getName());
		}
		return cliente;
	}
	
	public Page<Cliente> findPage(Integer page, Integer linesPerPage, String orderBy, String direction) {
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		return repository.findAll(pageRequest);
	}
	
	public Cliente fromDTO(ClienteDTO clienteDto) {
		return new Cliente(clienteDto.getId(), clienteDto.getNome(), clienteDto.getEmail(), null, null, null);
	}
	
	public Cliente fromDTO(ClienteNewDTO clienteDto) {
		String senha0 = pe.encode(clienteDto.getSenha());
		Cliente cliente = new Cliente(null, clienteDto.getNome(), clienteDto.getEmail(), clienteDto.getCpfOuCnpj(), TipoCliente.toEnum(clienteDto.getTipo()), senha0);
		Cidade cidade = new Cidade(clienteDto.getCidadeId(), null, null);
		Endereco end = new Endereco(null, clienteDto.getLogradouro(), clienteDto.getNumero(), clienteDto.getComplemento(), clienteDto.getBairro(), clienteDto.getCep(), cliente, cidade);
		cliente.getEnderecos().add(end);
		cliente.getTelefones().add(clienteDto.getTelefone1());
		if(clienteDto.getTelefone2() != null) cliente.getTelefones().add(clienteDto.getTelefone2());
		if(clienteDto.getTelefone3() != null) cliente.getTelefones().add(clienteDto.getTelefone3());
		
		return cliente;
	}
	
	private void updateData(Cliente newCliente, Cliente cliente) {
		newCliente.setNome(cliente.getNome());
		newCliente.setEmail(cliente.getEmail());
	}
	
	public URI uploadProfilePicture(MultipartFile multipartFile) {
		UserSS user = UserService.authenticated();
		if(user == null) {
			throw new AuthorizationException("Acesso Negado");
		}
		
		BufferedImage jpgImage = imageService.getJpgImageFromFile(multipartFile);
		jpgImage = imageService.cropSquare(jpgImage);
		jpgImage = imageService.resize(jpgImage, size);
		String fileName = prefix + user.getId() + ".jpg";
		
		return s3Service.uploadFile(imageService.getInputStream(jpgImage, "jpg"), fileName, "image");
	}
}
