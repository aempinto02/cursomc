package com.ston.cursomc;

import java.text.SimpleDateFormat;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.ston.cursomc.domain.Categoria;
import com.ston.cursomc.domain.Cidade;
import com.ston.cursomc.domain.Cliente;
import com.ston.cursomc.domain.Endereco;
import com.ston.cursomc.domain.Estado;
import com.ston.cursomc.domain.Pagamento;
import com.ston.cursomc.domain.PagamentoComBoleto;
import com.ston.cursomc.domain.PagamentoComCartao;
import com.ston.cursomc.domain.Pedido;
import com.ston.cursomc.domain.Produto;
import com.ston.cursomc.domain.enums.EstadoPagamento;
import com.ston.cursomc.domain.enums.TipoCliente;
import com.ston.cursomc.repositories.CategoriaRepository;
import com.ston.cursomc.repositories.CidadeRepository;
import com.ston.cursomc.repositories.ClienteRepository;
import com.ston.cursomc.repositories.EnderecoRepository;
import com.ston.cursomc.repositories.EstadoRepository;
import com.ston.cursomc.repositories.PagamentoRepository;
import com.ston.cursomc.repositories.PedidoRepository;
import com.ston.cursomc.repositories.ProdutoRepository;

@SpringBootApplication
public class CursomcApplication implements CommandLineRunner {
	
	@Autowired
	private CategoriaRepository repository;

	@Autowired
	private ProdutoRepository prodRepository;

	@Autowired
	private EstadoRepository estRepository;
	
	@Autowired
	private CidadeRepository cidRepository;

	@Autowired
	private ClienteRepository cliRepository;
	
	@Autowired
	private EnderecoRepository endRepository;

	@Autowired
	private PedidoRepository pedRepository;
	
	@Autowired
	private PagamentoRepository pagRepository;

	public static void main(String[] args) {
		SpringApplication.run(CursomcApplication.class, args);
	}
	
	@Override
	public void run(String... args) throws Exception {
		Categoria cat1 = new Categoria(null, "Informática");
		Categoria cat2 = new Categoria(null, "Escritório");
		
		Produto p1 = new Produto(null, "Computador", 2000.0);
		Produto p2 = new Produto(null, "Impressora", 800.0);
		Produto p3 = new Produto(null, "Mouse", 100.0);
		
		cat1.getProdutos().addAll(Arrays.asList(p1, p2, p3));
		cat2.getProdutos().addAll(Arrays.asList(p2));
		
		p1.getCategorias().addAll(Arrays.asList(cat1));
		p2.getCategorias().addAll(Arrays.asList(cat1, cat2));
		p3.getCategorias().addAll(Arrays.asList(cat1));
		
		Estado est1 = new Estado(null, "Minas Gerais");
		Estado est2 = new Estado(null, "São Paulo");
		
		Cidade c1 = new Cidade(null, "Uberlândia", est1);
		Cidade c2 = new Cidade(null, "São Paulo", est2);
		Cidade c3 = new Cidade(null, "Campinas", est2);
		
		est1.getCidades().addAll(Arrays.asList(c1));
		est2.getCidades().addAll(Arrays.asList(c2, c3));
		
		Cliente cli1 = new Cliente(null, "Maria Silva", "maria@gmail.com", "93281654782", TipoCliente.PESSOAFISICA);
		
		cli1.getTelefones().addAll(Arrays.asList("24158275", "981754162"));
		
		Endereco e1 = new Endereco(null, "Rua Flores", "300", "Apto303", "Jardim Mole", "30128586", cli1, c1);
		Endereco e2 = new Endereco(null, "Avenida Matos", "105", "Sala 800", "Centro", "38421736", cli1, c2);
		
		cli1.getEnderecos().addAll(Arrays.asList(e1, e2));
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		
		Pedido ped1 = new Pedido(null, sdf.parse("30/09/2017 10:32"), cli1, e1);
		Pedido ped2 = new Pedido(null, sdf.parse("10/10/2017 19:35"), cli1, e2);
		
		Pagamento pag1 = new PagamentoComCartao(null, EstadoPagamento.QUITADO, ped1, 6);
		ped1.setPagamento(pag1);

		Pagamento pag2 = new PagamentoComBoleto(null, EstadoPagamento.PENDENTE, ped2, sdf.parse("20/10/2017 00:00"), null);
		ped2.setPagamento(pag2);
		
		cli1.getPedidos().addAll(Arrays.asList(ped1, ped2));
		
		repository.saveAll(Arrays.asList(cat1, cat2));
		prodRepository.saveAll(Arrays.asList(p1, p2, p3));
		estRepository.saveAll(Arrays.asList(est1, est2));
		cidRepository.saveAll(Arrays.asList(c1, c2, c3));
		cliRepository.saveAll(Arrays.asList(cli1));
		endRepository.saveAll(Arrays.asList(e1, e2));
		pedRepository.saveAll(Arrays.asList(ped1, ped2));
		pagRepository.saveAll(Arrays.asList(pag1, pag2));
	}

}
