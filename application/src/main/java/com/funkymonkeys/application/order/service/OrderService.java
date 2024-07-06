package com.funkymonkeys.application.order.service;


import com.funkymonkeys.application.order.dto.OrderDTO;
import com.funkymonkeys.application.order.model.Order;
import com.funkymonkeys.application.order.model.OrderStatus;
import com.funkymonkeys.application.order.model.ShipStatus;
import com.funkymonkeys.application.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public Order save(OrderDTO orderDTO) {
        Order order = Order.builder()
                .tiendaNubeId(orderDTO.tiendaNubeId())
                .number(orderDTO.number())
                .orderStatus(orderDTO.orderStatus())
                .shipStatus(orderDTO.shipStatus())
                .customer(orderDTO.customer())
                .build();

        return orderRepository.save(order);
    }

    public boolean delete(Long orderId) {
        Optional<Order> orderToDelete= orderRepository.findById(orderId);
        if(orderToDelete.isPresent()) {
            orderRepository.delete(orderToDelete.get());
            return true;
        }

        return false;
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    public List<Order> findByPackaged() {
        return orderRepository.findByOrderStatus(OrderStatus.PACKAGED);
    }

    public Order packaged(Long id) {
        Optional<Order> orderUpdate = orderRepository.findById(id);
        if(orderUpdate.isPresent()) {
            Order order = orderUpdate.get();
            order.setOrderStatus(OrderStatus.PACKAGED);
            orderRepository.save(order);
            return order;
        }
        throw new NoSuchElementException(String.format("The id:%s does not exist",id));
    }
}
