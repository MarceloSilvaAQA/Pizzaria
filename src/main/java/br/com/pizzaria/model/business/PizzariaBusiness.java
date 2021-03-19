package br.com.pizzaria.model.business;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import br.com.pizzaria.model.entidades.cardapio.Adicional;
import br.com.pizzaria.model.entidades.cardapio.Bebida;
import br.com.pizzaria.model.entidades.cardapio.Categoria;
import br.com.pizzaria.model.entidades.cardapio.Pizza;
import br.com.pizzaria.model.entidades.cardapio.Tamanho;
import br.com.pizzaria.model.entidades.cliente.Cliente;
import br.com.pizzaria.model.entidades.pedido.Pedido;
import br.com.pizzaria.model.entidades.pedido.PedidoBebida;
import br.com.pizzaria.model.entidades.pedido.PedidoCliente;
import br.com.pizzaria.model.entidades.pedido.PedidoPizza;
import br.com.pizzaria.model.entidades.pedido.PedidoTelaPrincipal;

public class PizzariaBusiness {

	private static final double DESCONTO_COMBO = 0.25;
	private static final double CUPOM_DESCONTO_PRIMEIRA_COMPRA = 0.10;
	private List<PedidoCliente> clientes = new ArrayList<>();
	private List<PedidoTelaPrincipal> pedidosTela = new ArrayList<>();

	public void adicionarPedido(Cliente cliente, Pedido pedido) {

		boolean achou = false;
		for (PedidoCliente pedidoCliente : clientes) {
			if (cliente.getNome().equalsIgnoreCase(pedidoCliente.getCliente().getNome())) {
				achou = true;
				aplicaValorTotalPedido(pedido);
				aplicaComboPromocao(pedido);
				aplicaPromocaoBebidas(pedido);
				removeSaldoBebidas(pedido);

				pedidoCliente.adicionarPedido(pedido);
			}
		}

		if (!achou) {
			aplicaValorTotalPedido(pedido);
			aplicaDescontoPrimeiraCompra(pedido);
			aplicaComboPromocao(pedido);
			aplicaPromocaoBebidas(pedido);
			removeSaldoBebidas(pedido);

			PedidoCliente pedidoCliente = new PedidoCliente();
			pedidoCliente.setCliente(cliente);
			pedidoCliente.adicionarPedido(pedido);

			clientes.add(pedidoCliente);
		}
	}

	public void removePedidoPizza(Pedido pedido, PedidoPizza pedidoPizza) {
		pedido.removePizzaPedido(pedidoPizza);
	}

	public void removePedidoBebida(Pedido pedido, PedidoBebida pedidoBebida) {
		pedido.removeBebida(pedidoBebida);

	}

	private void aplicaPromocaoBebidas(Pedido pedido) {

	}

	private void removeSaldoBebidas(Pedido pedido) {

	}

	private void aplicaComboPromocao(Pedido pedido) {

		if (verificaComboPromocao(pedido)) {
			double valorTotal = pedido.getValorTotal();
			double valorTotalComDesconto = valorTotal - (valorTotal * DESCONTO_COMBO);

			pedido.setValorTotal(valorTotalComDesconto);
		}
		;

	}

	private boolean verificaComboPromocao(Pedido pedido) {

		List<PedidoPizza> pizzas = pedido.getPedidoPizzas();
		List<PedidoBebida> bebidas = pedido.getPedidoBebidas();

		// Itens da regra do combo
		Optional<PedidoPizza> pizzaGrandeClassica = Optional.empty();
		Optional<PedidoPizza> pizzaGrandeVegetariana = Optional.empty();
		Optional<PedidoPizza> pizzaBrotoDoce = Optional.empty();
		Optional<PedidoBebida> bebidaLata = Optional.empty();

		boolean pizzasSaborUnico = pizzas.stream().allMatch(p -> p.getPizzaUnica() != null);

		if (pizzasSaborUnico) {
			// Verifica se tem uma pizza grande de sabor clássica
			pizzaGrandeClassica = pizzas.stream().filter(pizzaGrande -> pizzaGrande.getTamanho().equals(Tamanho.GRANDE))
					.filter(pizzaClassica -> pizzaClassica.getPizzaUnica().getCategoria().equals(Categoria.CLASSICA))
					.findAny();

			// Verifica se tem uma pizza grande de sabor vegetariano
			pizzaGrandeVegetariana = pizzas.stream()
					.filter(pizzaGrande -> pizzaGrande.getTamanho().equals(Tamanho.GRANDE))
					.filter(pizzaClassica -> pizzaClassica.getPizzaUnica().getCategoria().equals(Categoria.VEGETARIANA))
					.findAny();

			// Verifica se tem uma pizza broto de sabor doce
			pizzaBrotoDoce = pizzas.stream().filter(pizzaBroto -> pizzaBroto.getTamanho().equals(Tamanho.BROTO))
					.filter(pizzaDoce -> pizzaDoce.getPizzaUnica().getCategoria().equals(Categoria.DOCE)).findAny();
		}

		// Verifica se tem um refrigerante de lata
		bebidaLata = bebidas.stream().filter(bebida -> bebida.getBebida().equals(Bebida.REFRIGERANTE_LATA)).findAny();

		if ((pizzaGrandeClassica.isPresent() || pizzaGrandeVegetariana.isPresent()) && pizzaBrotoDoce.isPresent()
				&& bebidaLata.isPresent()) {
			return true;
		}
		;
		return false;

	}

	private void aplicaDescontoPrimeiraCompra(Pedido pedido) {

		double valorTotal = pedido.getValorTotal();
		double valorComCupomDesconto = valorTotal - (valorTotal * CUPOM_DESCONTO_PRIMEIRA_COMPRA);

		pedido.setValorTotal(valorComCupomDesconto);

	}

	public double getValorTotalPedido(Pedido pedido) {

		double valorTotalBebibas = calculaValorTotalBebidas(pedido);
		double valorTotalPizzas = calculaValorTotalPizzas(pedido);

		double valorTotal = valorTotalBebibas + valorTotalPizzas;
		return valorTotal;
	}

	private double calculaValorTotalPizzas(Pedido pedido) {
		List<PedidoPizza> pedidoPizzas = pedido.getPedidoPizzas();

		double valorTotalPizzas = 0.0;
		double valorTotalPizza = 0.0;

		for (PedidoPizza pizza : pedidoPizzas) {

			switch (pizza.getTamanho()) {
			case BROTO:
				valorTotalPizza = calculaValorPizzaBroto(pizza);
				break;
			case MEDIA:
				valorTotalPizza = calculaValorPizzaMedia(pizza);
				break;
			case GRANDE:
				valorTotalPizza = calculaValorPizzaGrande(pizza);
				break;
			case BIG:
				valorTotalPizza = calculaValorPizzaBig(pizza);
				break;
			}

			valorTotalPizzas += valorTotalPizza;

		}

		return valorTotalPizzas;
	}

	private double calculaValorPizzaBroto(PedidoPizza pizza) {

		double valorTotalPizzaDoisSabores = 0.0;
		double valorTotalPizzaUnica = 0.0;
		double valorTotalBorda = pizza.getBorda().getValorBroto();
		double valorTotalMassa = pizza.getMassa().getValorBroto();
		double valorTotalAdicionais = 0.0;

		if (pizza.getPizzasSabores().size() > 0) {
			List<Pizza> pizzasSabores = pizza.getPizzasSabores();
			Optional<Pizza> pizzaMaiorValor = pizzasSabores.stream().max(Comparator.comparing(Pizza::getValorBroto));
			valorTotalPizzaDoisSabores = pizzaMaiorValor.get().getValorBroto();
		} else {
			valorTotalPizzaUnica = pizza.getPizzaUnica().getValorBroto();
		}

		if (pizza.getAdicionais().size() > 0) {
			List<Adicional> adicional = pizza.getAdicionais();
			valorTotalAdicionais = adicional.stream().map(x -> x.getValorBroto()).reduce(0.0, (a, x) -> a + x);
		}

		double valorTotalPizza = valorTotalPizzaUnica + valorTotalPizzaDoisSabores + valorTotalBorda + valorTotalMassa
				+ valorTotalAdicionais;
		return valorTotalPizza;

	}

	private double calculaValorPizzaMedia(PedidoPizza pizza) {

		double valorTotalPizzaDoisSabores = 0.0;
		double valorTotalPizzaUnica = 0.0;
		double valorTotalBorda = pizza.getBorda().getValorMedia();
		double valorTotalMassa = pizza.getMassa().getValorMedia();
		double valorTotalAdicionais = 0.0;

		if (pizza.getPizzasSabores().size() > 0) {
			List<Pizza> pizzasSabores = pizza.getPizzasSabores();
			Optional<Pizza> pizzaMaiorValor = pizzasSabores.stream().max(Comparator.comparing(Pizza::getValorMedia));
			valorTotalPizzaDoisSabores = pizzaMaiorValor.get().getValorMedia();
		} else {
			valorTotalPizzaUnica = pizza.getPizzaUnica().getValorMedia();
		}

		if (pizza.getAdicionais().size() > 0) {
			List<Adicional> adicional = pizza.getAdicionais();
			valorTotalAdicionais = adicional.stream().map(x -> x.getValorMedia()).reduce(0.0, (a, x) -> a + x);
		}

		double valorTotalPizza = valorTotalPizzaUnica + valorTotalPizzaDoisSabores + valorTotalBorda + valorTotalMassa
				+ valorTotalAdicionais;
		return valorTotalPizza;

	}

	private double calculaValorPizzaGrande(PedidoPizza pizza) {

		double valorTotalPizzaDoisSabores = 0.0;
		double valorTotalPizzaUnica = 0.0;
		double valorTotalBorda = pizza.getBorda().getValorGrande();
		double valorTotalMassa = pizza.getMassa().getValorGrande();
		double valorTotalAdicionais = 0.0;

		if (pizza.getPizzasSabores().size() > 0) {
			List<Pizza> pizzasSabores = pizza.getPizzasSabores();
			Optional<Pizza> pizzaMaiorValor = pizzasSabores.stream().max(Comparator.comparing(Pizza::getValorGrande));
			valorTotalPizzaDoisSabores = pizzaMaiorValor.get().getValorGrande();
		} else {
			valorTotalPizzaUnica = pizza.getPizzaUnica().getValorGrande();
		}

		if (pizza.getAdicionais().size() > 0) {
			List<Adicional> adicional = pizza.getAdicionais();
			valorTotalAdicionais = adicional.stream().map(x -> x.getValorGrande()).reduce(0.0, (a, x) -> a + x);
		}

		double valorTotalPizza = valorTotalPizzaUnica + valorTotalPizzaDoisSabores + valorTotalBorda + valorTotalMassa
				+ valorTotalAdicionais;
		return valorTotalPizza;

	}

	private double calculaValorPizzaBig(PedidoPizza pizza) {

		double valorTotalPizzaDoisSabores = 0.0;
		double valorTotalPizzaUnica = 0.0;
		double valorTotalBorda = pizza.getBorda().getValorBig();
		double valorTotalMassa = pizza.getMassa().getValorBig();
		double valorTotalAdicionais = 0.0;

		if (pizza.getPizzasSabores().size() > 0) {
			List<Pizza> pizzasSabores = pizza.getPizzasSabores();
			Optional<Pizza> pizzaMaiorValor = pizzasSabores.stream().max(Comparator.comparing(Pizza::getValorBig));
			valorTotalPizzaDoisSabores = pizzaMaiorValor.get().getValorBig();
		} else {
			valorTotalPizzaUnica = pizza.getPizzaUnica().getValorBig();
		}

		if (pizza.getAdicionais().size() > 0) {
			List<Adicional> adicional = pizza.getAdicionais();
			valorTotalAdicionais = adicional.stream().map(x -> x.getValorBig()).reduce(0.0, (a, x) -> a + x);
		}

		double valorTotalPizza = valorTotalPizzaUnica + valorTotalPizzaDoisSabores + valorTotalBorda + valorTotalMassa
				+ valorTotalAdicionais;
		return valorTotalPizza;

	}

	private double calculaValorTotalBebidas(Pedido pedido) {
		List<PedidoBebida> pedidoBebidas = pedido.getPedidoBebidas();
		double valorTotalBebidas = 0.0;

		for (PedidoBebida bebida : pedidoBebidas) {
			double valorBebida = bebida.getBebida().getPreco() * bebida.getQuantidade();
			valorTotalBebidas += valorBebida;
		}
		;

		return valorTotalBebidas;
	}

	private void aplicaValorTotalPedido(Pedido pedido) {
		pedido.setValorTotal(getValorTotalPedido(pedido));
	}

	private void aplicaValorTotalPedidoTelaPrincipal(PedidoTelaPrincipal pedidoTelaPrincipal, double valor) {
		pedidoTelaPrincipal.setValorTotal(pedidoTelaPrincipal.getValorTotal() + valor);

	}

	public List<PedidoTelaPrincipal> getPedidosTelaPrincipal() {
		pedidosTela.clear();

		for (PedidoCliente cliente : clientes) {
			List<Pedido> pedidosCliente = cliente.getPedidos();

			for (Pedido pedidoCliente : pedidosCliente) {

				boolean achouData = false;
				PedidoTelaPrincipal pedidoTelaData = null;

				for (PedidoTelaPrincipal pedidoTela : pedidosTela) {
					if (pedidoCliente.getData().equals(pedidoTela.getData())) {
						achouData = true;
						pedidoTelaData = pedidoTela;
					}
				}

				PedidoTelaPrincipal pedidoTelaPrincipal = null;

				if (achouData) {
					pedidoTelaPrincipal = pedidoTelaData;
				} else {
					pedidoTelaPrincipal = new PedidoTelaPrincipal();
					pedidosTela.add(pedidoTelaPrincipal);
					pedidoTelaPrincipal.setData(pedidoCliente.getData());
				}

				pedidoTelaPrincipal.setQuantidadePedidos(
						pedidoTelaPrincipal.getQuantidadePedidos() + pedidoCliente.getQuantidadePedidos());
				aplicaValorTotalPedidoTelaPrincipal(pedidoTelaPrincipal, pedidoCliente.getValorTotal());
			}
		}

		return new ArrayList<>(pedidosTela);
	}
}
