package com.soohea.api.rpc;

import com.soohea.api.data.OrderInfo;
import com.soohea.api.generate.Order;

public interface OrderRpcService {
    Order createOrder(OrderInfo orderInfo, Order order);
}
