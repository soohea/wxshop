package com.soohea.wxshop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.soohea.api.DataStatus;
import com.soohea.api.data.GoodsInfo;
import com.soohea.api.data.OrderInfo;
import com.soohea.api.data.PageResponse;
import com.soohea.api.data.RpcOrderGoods;
import com.soohea.api.generate.Order;
import com.soohea.wxshop.WxshopApplication;
import com.soohea.wxshop.entity.GoodsWithNumber;
import com.soohea.wxshop.entity.OrderResponse;
import com.soohea.wxshop.entity.Response;
import com.soohea.wxshop.generate.Goods;
import com.soohea.wxshop.generate.Shop;
import com.soohea.wxshop.mock.MockOrderRpcService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WxshopApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.config.location=classpath:test-application.yml"})
public class OrderIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    MockOrderRpcService mockOrderRpcService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(mockOrderRpcService);
        when(mockOrderRpcService.orderRpcService.createOrder(any(), any())).thenAnswer(invocationOnMock -> {
            Order order = invocationOnMock.getArgument(1);
            order.setId(1234L);
            return order;
        });
    }

    @Test
    public void canCreateOrder() throws IOException, URISyntaxException {
        UserLoginResponse loginResponse = loginAndGetCookie();
        OrderInfo orderInfo = new OrderInfo();
        GoodsInfo goodsInfo1 = new GoodsInfo();
        GoodsInfo goodsInfo2 = new GoodsInfo();

        goodsInfo1.setId(4);
        goodsInfo1.setNumber(3);
        goodsInfo2.setId(5);
        goodsInfo2.setNumber(5);

        orderInfo.setGoods(Arrays.asList(goodsInfo1, goodsInfo2));

        Response<OrderResponse> response = doHttpRequest("/api/v1/order", "POST", orderInfo, loginResponse.cookie)
                .asJsonObject(new TypeReference<Response<OrderResponse>>() {
                });
        Assertions.assertEquals(1234L, response.getData().getId());

        Assertions.assertEquals(2L, response.getData().getShop().getId());
        Assertions.assertEquals("shop2", response.getData().getShop().getName());
        Assertions.assertEquals("火星", response.getData().getAddress());
        Assertions.assertEquals(DataStatus.PENDING.getName(), response.getData().getStatus());
        Assertions.assertEquals(Arrays.asList(4L, 5L),
                response.getData().getGoods().stream().map(Goods::getId).collect(toList()));
        Assertions.assertEquals(Arrays.asList(3, 5),
                response.getData().getGoods().stream().map(GoodsWithNumber::getNumber).collect(toList()));


    }

    @Test
    public void canRollBackIfDeductStockFailed() throws IOException, URISyntaxException {
        UserLoginResponse loginResponse = loginAndGetCookie();
        OrderInfo orderInfo = new OrderInfo();
        GoodsInfo goodsInfo1 = new GoodsInfo();
        GoodsInfo goodsInfo2 = new GoodsInfo();

        goodsInfo1.setId(4);
        goodsInfo1.setNumber(3);
        goodsInfo2.setId(5);
        goodsInfo2.setNumber(6);

        orderInfo.setGoods(Arrays.asList(goodsInfo1, goodsInfo2));

        HttpResponse response = doHttpRequest("/api/v1/order", "POST", orderInfo, loginResponse.cookie);
        Assertions.assertEquals(HttpStatus.GONE.value(), response.code);

        canCreateOrder();

    }

    @Test
    public void canDeleteOrder() throws Exception {
        UserLoginResponse loginResponse = loginAndGetCookie();
        when(mockOrderRpcService.orderRpcService.getOrder(anyLong(), anyInt(), anyInt(), any()))
                .thenReturn(mockResponse());

        //获取当前订单
        PageResponse<OrderResponse> orders = doHttpRequest("/api/v1/order?pageSize=2&pageNum=3", "GET", null, loginResponse.cookie)
                .asJsonObject(new TypeReference<PageResponse<OrderResponse>>() {
                });
        Assertions.assertEquals(3, orders.getPageNum());
        Assertions.assertEquals(10, orders.getTotalPage());
        Assertions.assertEquals(2, orders.getPageSize());
        Assertions.assertEquals(Arrays.asList("shop2", "shop2"),
                orders.getData().stream().map(OrderResponse::getShop).map(Shop::getName).collect(toList()));
        Assertions.assertEquals(Arrays.asList("goods3", "goods4"),
                orders.getData().stream().map(OrderResponse::getGoods)
                        .flatMap(List::stream)
                        .map(Goods::getName)
                        .collect(toList()));
        Assertions.assertEquals(Arrays.asList(5, 3),
                orders.getData().stream()
                        .map(OrderResponse::getGoods)
                        .flatMap(List::stream)
                        .map(GoodsWithNumber::getNumber)
                        .collect(toList()));

        when(mockOrderRpcService.orderRpcService.deleteOrder(100L, 1L))
                .thenReturn(mockRpcOrderGoods(100, 1, 3, 2, 5, DataStatus.DELETED));

        //删除某个订单
        Response<OrderResponse> deleteOrder = doHttpRequest("/api/v1/order/100", "DELETE", null, loginResponse.cookie)
                .assertOkStatusCode()
                .asJsonObject(new TypeReference<Response<OrderResponse>>() {
                });
        Assertions.assertEquals(DataStatus.DELETED.getName(), deleteOrder.getData().getStatus());
        Assertions.assertEquals(100L, deleteOrder.getData().getId());
        Assertions.assertEquals(3, deleteOrder.getData().getGoods().get(0).getId());
        Assertions.assertEquals(5, deleteOrder.getData().getGoods().get(0).getNumber());

    }

    @Test
    public void return404IfOrderNotFound() throws Exception {
        UserLoginResponse loginResponse = loginAndGetCookie();
        Assertions.assertEquals(HttpURLConnection.HTTP_NOT_FOUND,
                doHttpRequest("/api/v1/order/123456", "PATCH", new HashMap<>(), loginResponse.cookie).code);
    }

    @Test
    public void canUpdateOrderExpressInformation() throws Exception {
        UserLoginResponse loginResponse = loginAndGetCookie();

        Order orderUpdateRequest = new Order();
        orderUpdateRequest.setId(12345L);
        orderUpdateRequest.setShopId(2L);
        orderUpdateRequest.setExpressCompany("顺丰");
        orderUpdateRequest.setExpressId("SF12345678");

        Order orderInDB = new Order();
        orderInDB.setId(12345L);
        orderInDB.setShopId(2L);

        when(mockOrderRpcService.orderRpcService.getOrderById(12345L)).thenReturn(orderInDB);
        when(mockOrderRpcService.orderRpcService.updateOrder(any())).thenReturn(
                mockRpcOrderGoods(12345L, 1L, 3L, 2L, 10, DataStatus.DELIVERED)
        );

        Response<OrderResponse> response = doHttpRequest("/api/v1/order/12345", "PATCH", orderUpdateRequest, loginResponse.cookie)
                .assertOkStatusCode()
                .asJsonObject(new TypeReference<Response<OrderResponse>>() {
                });
        Assertions.assertEquals(2L, response.getData().getShop().getId());
        Assertions.assertEquals("shop2", response.getData().getShop().getName());
        Assertions.assertEquals(DataStatus.DELIVERED.getName(), response.getData().getStatus());
        Assertions.assertEquals(1, response.getData().getGoods().size());
        Assertions.assertEquals(3, response.getData().getGoods().get(0).getId());
        Assertions.assertEquals(10, response.getData().getGoods().get(0).getNumber());
    }

    @Test
    public void canUpdateOrderStatus() throws Exception {
        UserLoginResponse loginResponse = loginAndGetCookie();

        Order orderUpdateRequest = new Order();
        orderUpdateRequest.setId(12345L);
        orderUpdateRequest.setStatus(DataStatus.RECEIVED.getName());

        Order orderInDB = new Order();
        orderInDB.setId(12345L);
        orderInDB.setUserId(1L);

        when(mockOrderRpcService.orderRpcService.getOrderById(12345L)).thenReturn(orderInDB);
        when(mockOrderRpcService.orderRpcService.updateOrder(any())).thenReturn(
                mockRpcOrderGoods(12345L, 1L, 3L, 2L, 10, DataStatus.RECEIVED)
        );

        Response<OrderResponse> response = doHttpRequest("/api/v1/order/12345", "PATCH", orderUpdateRequest, loginResponse.cookie)
                .assertOkStatusCode()
                .asJsonObject(new TypeReference<Response<OrderResponse>>() {
                });
        Assertions.assertEquals(2L, response.getData().getShop().getId());
        Assertions.assertEquals("shop2", response.getData().getShop().getName());
        Assertions.assertEquals(DataStatus.RECEIVED.getName(), response.getData().getStatus());
        Assertions.assertEquals(1, response.getData().getGoods().size());
        Assertions.assertEquals(3, response.getData().getGoods().get(0).getId());
        Assertions.assertEquals(10, response.getData().getGoods().get(0).getNumber());
    }

    private PageResponse<RpcOrderGoods> mockResponse() {
        RpcOrderGoods order1 = mockRpcOrderGoods(100, 1, 3, 2, 5, DataStatus.DELIVERED);
        RpcOrderGoods order2 = mockRpcOrderGoods(101, 1, 4, 2, 3, DataStatus.RECEIVED);
        return PageResponse.pagedData(3, 2, 10, Arrays.asList(order1, order2));
    }

    private RpcOrderGoods mockRpcOrderGoods(long orderId,
                                            long userId,
                                            long goodsId,
                                            long shopId,
                                            int number,
                                            DataStatus status) {
        RpcOrderGoods orderGoods = new RpcOrderGoods();
        Order order = new Order();
        GoodsInfo goodsInfo = new GoodsInfo();
        goodsInfo.setId(goodsId);
        goodsInfo.setNumber(number);
        order.setId(orderId);
        order.setUserId(userId);
        order.setShopId(shopId);
        order.setStatus(status.getName());
        orderGoods.setGoods(Arrays.asList(goodsInfo));
        orderGoods.setOrder(order);
        return orderGoods;
    }
}
