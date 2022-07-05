package com.soohea.order.mapper;

import com.soohea.api.data.GoodsInfo;
import com.soohea.api.data.OrderInfo;
import com.soohea.api.generate.Order;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MyOrderMapper {
    void insertOrders(OrderInfo orderInfo);

    List<GoodsInfo> getGoodsInfoOfOrder(long orderId);
}
