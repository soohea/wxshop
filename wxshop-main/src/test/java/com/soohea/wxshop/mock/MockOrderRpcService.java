package com.soohea.wxshop.mock;

import com.soohea.api.DataStatus;
import com.soohea.api.data.OrderInfo;
import com.soohea.api.data.PageResponse;
import com.soohea.api.data.RpcOrderGoods;
import com.soohea.api.generate.Order;
import com.soohea.api.rpc.OrderRpcService;
import org.apache.dubbo.config.annotation.Service;
import org.mockito.Mock;


@Service(version = "${wxshop.orderservice.version}")
public class MockOrderRpcService implements OrderRpcService {
    @Mock
    public OrderRpcService orderRpcService;

    @Override
    public Order createOrder(OrderInfo orderInfo, Order order) {
        return orderRpcService.createOrder(orderInfo, order);
    }

    @Override
    public Order getOrderById(long orderId) {
        return orderRpcService.getOrderById(orderId);
    }

    @Override
    public RpcOrderGoods deleteOrder(long orderId, Long userId) {
        return orderRpcService.deleteOrder(orderId, userId);
    }

    @Override
    public PageResponse<RpcOrderGoods> getOrder(long userId, Integer pageNum, Integer pageSize, DataStatus status) {
        return orderRpcService.getOrder(userId, pageNum, pageSize, status);
    }

    @Override
    public RpcOrderGoods updateOrder(Order order) {
        return orderRpcService.updateOrder(order);
    }


}
