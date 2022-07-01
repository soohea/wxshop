package com.soohea.wxshop.service;

import com.soohea.api.DataStatus;
import com.soohea.api.data.GoodsInfo;
import com.soohea.api.data.OrderInfo;
import com.soohea.api.generate.Order;
import com.soohea.api.rpc.OrderRpcService;
import com.soohea.wxshop.dao.GoodsStockMapper;
import com.soohea.wxshop.entity.GoodsWithNumber;
import com.soohea.wxshop.entity.HttpException;
import com.soohea.wxshop.entity.OrderResponse;
import com.soohea.wxshop.generate.Goods;
import com.soohea.wxshop.generate.ShopMapper;
import com.soohea.wxshop.generate.UserMapper;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Service
public class OrderService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);
    @Reference(version = "${wxshop.orderservice.version}")
    private OrderRpcService orderRpcService;

    private UserMapper userMapper;
    private GoodsStockMapper goodsStockMapper;
    private GoodsService goodsService;
    private ShopMapper shopMapper;
    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    public OrderService(UserMapper userMapper, GoodsStockMapper goodsStockMapper, GoodsService goodsService, ShopMapper shopMapper, SqlSessionFactory sqlSessionFactory) {
        this.userMapper = userMapper;
        this.goodsStockMapper = goodsStockMapper;
        this.goodsService = goodsService;
        this.shopMapper = shopMapper;
        this.sqlSessionFactory = sqlSessionFactory;
    }

    public OrderResponse createOrder(OrderInfo orderInfo, Long userId) {
        Map<Long, Goods> idToGoodsMap = getIdToGoodsMap(orderInfo);
        Order createdOrder = createOrderViaRpc(orderInfo, userId, idToGoodsMap);
        return generateResponse(createdOrder, idToGoodsMap, orderInfo);


    }

    private OrderResponse generateResponse(Order createdOrder, Map<Long, Goods> idToGoodsMap, OrderInfo orderInfo) {
        OrderResponse response = new OrderResponse(createdOrder);
        Long shopId = new ArrayList<>(idToGoodsMap.values()).get(0).getShopId();
        response.setShop(shopMapper.selectByPrimaryKey(shopId));
        response.setGoods(
                orderInfo.getGoods()
                        .stream()
                        .map(goods -> toGoodsWithNumber(goods, idToGoodsMap))
                        .collect(toList())
        );

        return response;

    }

    private Map<Long, Goods> getIdToGoodsMap(OrderInfo orderInfo) {
        List<Long> goodsId = orderInfo.getGoods()
                .stream()
                .map(GoodsInfo::getId)
                .collect(toList());
        return goodsService.getIdToGoodsMap(goodsId);

    }

    private Order createOrderViaRpc(OrderInfo orderInfo, Long userId, Map<Long, Goods> idToGoodsMap) {
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(DataStatus.PENDING.getName());
        order.setAddress(userMapper.selectByPrimaryKey(userId).getAddress());
        order.setTotalPrice(calculateTotalPrice(orderInfo, idToGoodsMap));

        return orderRpcService.createOrder(orderInfo, order);
    }



    @Transactional
    public void deductStock(OrderInfo orderInfo) {
        for (GoodsInfo goodsInfo : orderInfo.getGoods()) {
            if (goodsStockMapper.deductStock(goodsInfo) <= 0) {
                LOGGER.error("扣减库存失败，商品id：" + goodsInfo.getId() + "，数量" + goodsInfo.getNumber());
                throw HttpException.gone("扣减库存失败！");
            }
        }
    }


    private GoodsWithNumber toGoodsWithNumber(GoodsInfo goodsInfo, Map<Long, Goods> idToGoodsMap) {
        GoodsWithNumber result = new GoodsWithNumber(idToGoodsMap.get(goodsInfo.getId()));
        result.setNumber(goodsInfo.getNumber());
        return result;
    }

    private long calculateTotalPrice(OrderInfo orderInfo, Map<Long, Goods> idToGoodsMap) {
        long result = 0;

        for (GoodsInfo goodsInfo : orderInfo.getGoods()) {
            Goods goods = idToGoodsMap.get(goodsInfo.getId());
            if (goods == null) {
                throw HttpException.badRequest("goods id非法：" + goodsInfo.getId());
            }
            if (goodsInfo.getNumber() <= 0) {
                throw HttpException.badRequest("number非法：" + goodsInfo.getNumber());
            }

            result = result + goods.getPrice() * goodsInfo.getNumber();
        }

        return result;
    }
}