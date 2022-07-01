package com.soohea.wxshop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.soohea.api.DataStatus;
import com.soohea.api.data.GoodsInfo;
import com.soohea.api.data.OrderInfo;
import com.soohea.api.generate.Order;
import com.soohea.wxshop.WxshopApplication;
import com.soohea.wxshop.entity.GoodsWithNumber;
import com.soohea.wxshop.entity.OrderResponse;
import com.soohea.wxshop.entity.Response;
import com.soohea.wxshop.generate.Goods;
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
import java.net.URISyntaxException;
import java.util.Arrays;

import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.any;
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

}
