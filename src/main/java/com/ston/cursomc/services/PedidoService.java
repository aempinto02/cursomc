package com.ston.cursomc.services;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ston.cursomc.domain.Cliente;
import com.ston.cursomc.domain.ItemPedido;
import com.ston.cursomc.domain.PagamentoComBoleto;
import com.ston.cursomc.domain.Pedido;
import com.ston.cursomc.domain.enums.EstadoPagamento;
import com.ston.cursomc.repositories.ItemPedidoRepository;
import com.ston.cursomc.repositories.PagamentoRepository;
import com.ston.cursomc.repositories.PedidoRepository;
import com.ston.cursomc.security.UserSS;
import com.ston.cursomc.services.exceptions.AuthorizationException;
import com.ston.cursomc.services.exceptions.ObjectNotFoundException;

@Service
public class PedidoService {

	@Autowired
	private PedidoRepository repository;
	
	@Autowired
	private BoletoService boletoService;
	
	@Autowired
	private PagamentoRepository pagRepository;
	
	@Autowired
	private ProdutoService prodService;
	
	@Autowired
	private ItemPedidoRepository iPRepository;

	@Autowired
	private ClienteService cliService;
	
	@Autowired
	private EmailService emailService;

	public Pedido find(Integer id) {
		Optional<Pedido> obj = repository.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException(
				 "Objeto não encontrado! Id: " + id + ", Tipo: " + Pedido.class.getName())); 
	}

	@Transactional
	public Pedido insert(Pedido pedido) {
		pedido.setId(null);
		pedido.setInstante(new Date());
		pedido.setCliente(cliService.find(pedido.getCliente().getId()));
		pedido.getPagamento().setEstado(EstadoPagamento.PENDENTE);
		pedido.getPagamento().setPedido(pedido);
		if(pedido.getPagamento() instanceof PagamentoComBoleto) {
			PagamentoComBoleto pagamento = (PagamentoComBoleto) pedido.getPagamento();
			boletoService.preencherPagamentoComBoleto(pagamento, pedido.getInstante());
		}
		pedido = repository.save(pedido);
		pagRepository.save(pedido.getPagamento());
		for(ItemPedido ip : pedido.getItens()) {
			ip.setDesconto(0.0);
			ip.setProduto(prodService.find(ip.getProduto().getId()));
			ip.setPreco(ip.getProduto().getPreco());
			ip.setPedido(pedido);
		}
		iPRepository.saveAll(pedido.getItens());
		emailService.sendOrderConfirmationHtmlEmail(pedido);
		return pedido;
	}
	
	public Page<Pedido> findPage(Integer page, Integer linesPerPage, String orderBy, String direction) {
		UserSS user = UserService.authenticated();
		if(user == null) {
			throw new AuthorizationException("Acesso negado!");
		}
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		Cliente cliente = cliService.find(user.getId());
		return repository.findByCliente(cliente, pageRequest);
	}
}
