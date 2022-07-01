package com.soohea.order.mapper;

import com.soohea.api.data.OrderInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MyOrderMapper {
    void insertOrders(OrderInfo orderInfo);
}
