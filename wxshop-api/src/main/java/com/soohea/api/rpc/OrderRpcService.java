package com.soohea.api.rpc;

import com.soohea.api.DataStatus;
import com.soohea.api.data.OrderInfo;
import com.soohea.api.data.PageResponse;
import com.soohea.api.data.RpcOrderGoods;
import com.soohea.api.generate.Order;

public interface OrderRpcService {
    Order createOrder(OrderInfo orderInfo, Order order);

    Order getOrderById(long orderId);

    RpcOrderGoods deleteOrder(long orderId, Long userId);

    PageResponse<RpcOrderGoods> getOrder(long userId, Integer pageNum, Integer pageSize, DataStatus status);

    RpcOrderGoods updateOrder(Order order);
}
